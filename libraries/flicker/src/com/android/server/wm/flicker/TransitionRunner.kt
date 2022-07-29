/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.wm.flicker

import android.util.Log
import com.android.server.wm.flicker.FlickerRunResult.Companion.RunStatus
import com.android.server.wm.flicker.monitor.IFileGeneratingMonitor
import com.android.server.wm.flicker.monitor.ITransitionMonitor
import com.android.server.wm.flicker.monitor.NoTraceMonitor
import com.android.server.wm.traces.common.ConditionList
import com.android.server.wm.traces.common.WindowManagerConditionsFactory
import com.android.server.wm.traces.parser.getCurrentState
import java.io.IOException
import java.nio.file.Files
import org.junit.runner.Description

/**
 * Runner to execute the transitions of a flicker test
 *
 * The commands are executed in the following order:
 * 1) [Flicker.testSetup]
 * 2) [Flicker.runSetup
 * 3) Start monitors
 * 4) [Flicker.transitions]
 * 5) Stop monitors
 * 6) [Flicker.runTeardown]
 * 7) [Flicker.testTeardown]
 *
 * If the tests were already executed, reuse the previous results
 *
 */
open class TransitionRunner {
    /**
     * Iteration identifier during test run
     */
    internal var iteration = -1
        private set
    private val tags = mutableSetOf<String>()

    // Iteration to resultBuilder
    private var results = mutableMapOf<Int, FlickerRunResult>()

    /**
     * Executes the setup, transitions and teardown defined in [flicker]
     *
     * @param flicker test specification
     * @throws IllegalArgumentException If the transitions are empty or repetitions is set to 0
     */
    open fun execute(flicker: Flicker, useCacheIfAvailable: Boolean = true): FlickerResult {
        check(flicker)
        return run(flicker)
    }

    /**
     * Validate the [flicker] test specification before executing the transitions
     *
     * @param flicker test specification
     * @throws IllegalArgumentException If the transitions are empty or repetitions is set to 0
     */
    protected fun check(flicker: Flicker) {
        require(flicker.transitions.isNotEmpty() || onlyHasNoTraceMonitors(flicker)) {
            "A flicker test must include transitions to run"
        }
        require(flicker.repetitions > 0) {
            "Number of repetitions must be greater than 0"
        }
    }

    private fun onlyHasNoTraceMonitors(flicker: Flicker) =
            flicker.traceMonitors.all { it is NoTraceMonitor }

    open fun cleanUp() {
        tags.clear()
        results.clear()
    }

    /**
     * Runs the actual setup, transitions and teardown defined in [flicker]
     *
     * @param flicker test specification
     */
    internal open fun run(flicker: Flicker): FlickerResult {
        val executionErrors = mutableListOf<ExecutionError>()
        safeExecution(flicker, executionErrors) {
            runTestSetup(flicker)
            for (x in 0 until flicker.repetitions) {
                Log.d(FLICKER_TAG, "${flicker.testName} - " +
                        "Running iteration $x/${flicker.repetitions}")
                iteration = x
                results[iteration] = FlickerRunResult(flicker.testName, iteration)
                val description = Description.createSuiteDescription(flicker.testName)
                if (flicker.faasEnabled) {
                    Log.d(FLICKER_TAG, "${flicker.testName} - " +
                            "Setting up FaaS for iteration $x/${flicker.repetitions}")
                    flicker.faas.testStarted(description)
                }
                Log.d(FLICKER_TAG, "${flicker.testName} - " +
                        "Running transition setup $x/${flicker.repetitions}")
                runTransitionSetup(flicker)
                Log.d(FLICKER_TAG, "${flicker.testName} - " +
                        "Running transition $x/${flicker.repetitions}")
                runTransition(flicker)
                Log.d(FLICKER_TAG, "${flicker.testName} - " +
                        "Running transition teardown $x/${flicker.repetitions}")
                runTransitionTeardown(flicker)
                Log.d(FLICKER_TAG, "${flicker.testName} - " +
                        "Processing transition traces $x/${flicker.repetitions}")
                processRunTraces(flicker, RunStatus.ASSERTION_SUCCESS)
                if (flicker.faasEnabled) {
                    Log.d(FLICKER_TAG, "${flicker.testName} - " +
                            "Notifying FaaS of finished transition $x/${flicker.repetitions}")
                    flicker.faas.testFinished(description)
                    if (flicker.faas.executionErrors.isNotEmpty()) {
                        executionErrors.addAll(flicker.faas.executionErrors)
                    }
                }
                Log.d(FLICKER_TAG, "${flicker.testName} - " +
                        "Completed iteration $x/${flicker.repetitions}")
            }

            runTestTeardown(flicker)
        }

        val result = FlickerResult(
            results.values.toList(), // toList ensures we clone the list before cleanUp
            tags.toSet(),
            executionErrors
        )
        cleanUp()
        return result
    }

    private fun safeExecution(
        flicker: Flicker,
        executionErrors: MutableList<ExecutionError>,
        execution: () -> Unit
    ) {
        try {
            execution()
        } catch (e: TestSetupFailure) {
            // If we fail on the test setup we can't run any of the transitions
            executionErrors.add(e)
        } catch (e: TransitionSetupFailure) {
            // If we fail on the transition run setup then we don't want to run any further
            // transitions nor save any results for this run. We simply want to run the test
            // teardown.
            executionErrors.add(e)
            getCurrentRunResult().setStatus(RunStatus.RUN_FAILED)
            safeExecution(flicker, executionErrors) {
                runTestTeardown(flicker)
            }
        } catch (e: TransitionExecutionFailure) {
            // If a transition fails to run we don't want to run the following iterations as the
            // device is likely in an unexpected state which would lead to further errors. We simply
            // want to run the test teardown
            executionErrors.add(e)
            flicker.traceMonitors.forEach { it.tryStop() }
            safeExecution(flicker, executionErrors) {
                processRunTraces(flicker, RunStatus.RUN_FAILED)
                runTestTeardown(flicker)
            }
        } catch (e: TransitionTeardownFailure) {
            // If a transition teardown fails to run we don't want to run the following iterations
            // as the device is likely in an unexpected state which would lead to further errors.
            // But, we do want to run the test teardown.
            executionErrors.add(e)
            flicker.traceMonitors.forEach { it.tryStop() }
            safeExecution(flicker, executionErrors) {
                processRunTraces(flicker, RunStatus.RUN_FAILED)
                runTestTeardown(flicker)
            }
        } catch (e: TraceProcessingFailure) {
            // If we fail to process the run traces we still want to run the teardowns and report
            // the execution error.
            executionErrors.add(e)
            safeExecution(flicker, executionErrors) {
                runTransitionTeardown(flicker)
                runTestTeardown(flicker)
            }
        } catch (e: TestTeardownFailure) {
            // If we fail in the execution of the test teardown there is nothing else to do apart
            // from reporting the execution error.
            executionErrors.add(e)
            getCurrentRunResult().setStatus(RunStatus.RUN_FAILED)
        }
    }

    /**
     * Parses the traces collected by the monitors to generate FlickerRunResults containing the
     * parsed trace and information about the status of the run.
     * The run results are added to the resultBuilders list which is then used to run Flicker
     * assertions on.
     */
    @Throws(TraceProcessingFailure::class)
    private fun processRunTraces(
        flicker: Flicker,
        status: RunStatus
    ) {
        try {
            val result = getCurrentRunResult()
            result.setStatus(status)
            setMonitorResults(flicker, result)
            result.lock()

            if (flicker.faasEnabled && !status.isFailure) {
                // Don't run FaaS on failed transitions
                val wmTrace = result.buildWmTrace()
                val layersTrace = result.buildLayersTrace()
                val transitionsTrace = result.buildTransitionsTrace()

                flicker.faasTracesCollector.wmTrace = wmTrace
                flicker.faasTracesCollector.layersTrace = layersTrace
                flicker.faasTracesCollector.transitionsTrace = transitionsTrace
            }
        } catch (e: Throwable) {
            // We have failed to add the results to the runs, so we can effectively consider these
            // results as "lost" as they won't be used from now forth. So we can safely rename
            // to file to indicate the failure and make it easier to find in the archives.
            flicker.traceMonitors.forEach {
                // All monitors that generate files we want to keep in the archives should implement
                // IFileGeneratingMonitor
                if (it is IFileGeneratingMonitor) {
                    Utils.addStatusToFileName(it.outputFile, RunStatus.PARSING_FAILURE)
                }
            }
            throw TraceProcessingFailure(e)
        }
    }

    @Throws(TestSetupFailure::class)
    private fun runTestSetup(flicker: Flicker) {
        try {
            flicker.testSetup.forEach { it.invoke(flicker) }
        } catch (e: Throwable) {
            throw TestSetupFailure(e)
        }
    }

    @Throws(TestTeardownFailure::class)
    private fun runTestTeardown(flicker: Flicker) {
        try {
            flicker.testTeardown.forEach { it.invoke(flicker) }
        } catch (e: Throwable) {
            throw TestTeardownFailure(e)
        }
    }

    @Throws(TransitionSetupFailure::class)
    private fun runTransitionSetup(flicker: Flicker) {
        try {
            flicker.runSetup.forEach { it.invoke(flicker) }
            flicker.wmHelper.StateSyncBuilder()
                .add(UI_STABLE_CONDITIONS)
                .waitFor()
        } catch (e: Throwable) {
            throw TransitionSetupFailure(e)
        }
    }

    @Throws(TransitionExecutionFailure::class)
    private fun runTransition(flicker: Flicker) {
        try {
            flicker.traceMonitors.forEach { it.start() }
            flicker.transitions.forEach { it.invoke(flicker) }
        } catch (e: Throwable) {
            throw TransitionExecutionFailure(e)
        }
    }

    @Throws(TransitionTeardownFailure::class)
    private fun runTransitionTeardown(flicker: Flicker) {
        try {
            flicker.wmHelper.StateSyncBuilder()
                .add(UI_STABLE_CONDITIONS)
                .waitFor()
            flicker.traceMonitors.forEach { it.tryStop() }
            flicker.runTeardown.forEach { it.invoke(flicker) }
        } catch (e: Throwable) {
            throw TransitionTeardownFailure(e)
        }
    }

    private fun setMonitorResults(
        flicker: Flicker,
        result: FlickerRunResult,
    ): FlickerRunResult {

        flicker.traceMonitors.forEach {
            result.setResultsFromMonitor(it)
        }

        return result
    }

    private fun ITransitionMonitor.tryStop() {
        this.run {
            try {
                stop()
            } catch (e: Exception) {
                Log.e(FLICKER_TAG, "Unable to stop $this", e)
            }
        }
    }

    private fun getTaggedFilePath(flicker: Flicker, tag: String, file: String) =
        "${flicker.testName}_${iteration}_${tag}_$file"

    /**
     * Captures a snapshot of the device state and associates it with a new tag.
     *
     * This tag can be used to make assertions about the state of the device when the
     * snapshot is collected.
     *
     * [tag] is used as part of the trace file name, thus, only valid letters and digits
     * can be used
     *
     * @param flicker test specification
     * @throws IllegalArgumentException If [tag] contains invalid characters
     */
    open fun createTag(flicker: Flicker, tag: String) {
        require(!tag.contains(" ")) {
            "The test tag $tag can not contain spaces since it is a part of the file name"
        }
        tags.add(tag)

        val deviceStateBytes = getCurrentState(flicker.instrumentation.uiAutomation)
        try {
            val wmDumpFile = flicker.outputDir.resolve(
                getTaggedFilePath(flicker, tag, "wm_dump")
            )
            Files.write(wmDumpFile, deviceStateBytes.first)

            val layersDumpFile = flicker.outputDir.resolve(
                getTaggedFilePath(flicker, tag, "layers_dump")
            )
            Files.write(layersDumpFile, deviceStateBytes.second)

            getCurrentRunResult().addTaggedState(
                tag,
                wmDumpFile.toFile(),
                layersDumpFile.toFile(),
            )
        } catch (e: IOException) {
            throw RuntimeException("Unable to create trace file: ${e.message}", e)
        }
    }

    private fun getCurrentRunResult(): FlickerRunResult {
        return results[iteration]!!
    }

    companion object {
        /**
         * Conditions that determine when the UI is in a stable stable and no windows or layers are
         * animating or changing state.
         */
        private val UI_STABLE_CONDITIONS = ConditionList(
            listOf(
                WindowManagerConditionsFactory.isWMStateComplete(),
                WindowManagerConditionsFactory.hasLayersAnimating().negate()
            )
        )

        open class ExecutionError(private val inner: Throwable) : Throwable(inner) {
            init {
                super.setStackTrace(inner.stackTrace)
            }

            override val message: String?
                get() = inner.toString()
        }

        class TestSetupFailure(val e: Throwable) : ExecutionError(e)
        class TransitionSetupFailure(val e: Throwable) : ExecutionError(e)
        class TransitionExecutionFailure(val e: Throwable) : ExecutionError(e)
        class TraceProcessingFailure(val e: Throwable) : ExecutionError(e)
        class TransitionTeardownFailure(val e: Throwable) : ExecutionError(e)
        class TestTeardownFailure(val e: Throwable) : ExecutionError(e)
    }
}

/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.server.wm.flicker.integration

import android.app.Instrumentation
import android.platform.test.annotations.Presubmit
import androidx.test.platform.app.InstrumentationRegistry
import com.android.launcher3.tapl.LauncherInstrumentation
import com.android.server.wm.flicker.FlickerBuilder
import com.android.server.wm.flicker.FlickerTest
import com.android.server.wm.flicker.FlickerTestFactory
import com.android.server.wm.flicker.assertions.FlickerSubject
import com.android.server.wm.flicker.helpers.BrowserAppHelper
import com.android.server.wm.flicker.junit.FlickerBuilderProvider
import com.android.server.wm.flicker.junit.FlickerParametersRunnerFactory
import com.android.server.wm.flicker.traces.layers.LayersTraceSubject
import com.android.server.wm.flicker.traces.region.RegionSubject
import com.android.server.wm.flicker.traces.windowmanager.WindowManagerTraceSubject
import com.android.server.wm.traces.common.region.Region
import com.android.server.wm.traces.parser.windowmanager.WindowManagerStateHelper
import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
@Parameterized.UseParametersRunnerFactory(FlickerParametersRunnerFactory::class)
class FullTestRun(private val flicker: FlickerTest) {
    private val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    private val testApp: BrowserAppHelper = BrowserAppHelper(instrumentation)
    private val tapl: LauncherInstrumentation = LauncherInstrumentation()

    init {
        flicker.scenario.setIsTablet(
            WindowManagerStateHelper(instrumentation, clearCacheAfterParsing = false)
                .currentState
                .wmState
                .isTablet
        )
        tapl.setExpectedRotationCheckEnabled(true)
    }

    /**
     * Entry point for the test runner. It will use this method to initialize and cache flicker
     * executions
     */
    @FlickerBuilderProvider
    fun buildFlicker(): FlickerBuilder {
        return FlickerBuilder(instrumentation).apply {
            setup { flicker.scenario.setIsTablet(wmHelper.currentState.wmState.isTablet) }
            teardown { testApp.exit(wmHelper) }
            transitions { testApp.launchViaIntent(wmHelper) }
        }
    }

    /**
     * This is a shel test from the flicker infra to ensure the WM tracing pipeline executed
     * entirely executed correctly
     */
    @Presubmit
    @Test
    fun internalWmCheck() {
        var trace: WindowManagerTraceSubject? = null
        var executionCount = 0
        flicker.assertWm {
            executionCount++
            trace = this
            this.isNotEmpty()
        }
        flicker.assertWm {
            executionCount++
            val failure: Result<Any> = runCatching { this.isEmpty() }
            if (failure.isSuccess) {
                error("Should have thrown failure")
            }
        }
        flicker.assertWmStart {
            executionCount++
            validateState(this, trace?.first())
            validateVisibleRegion(this.visibleRegion(), trace?.first()?.visibleRegion())
        }
        flicker.assertWmEnd {
            executionCount++
            validateState(this, trace?.last())
            validateVisibleRegion(this.visibleRegion(), trace?.last()?.visibleRegion())
        }
        Truth.assertWithMessage("Execution count").that(executionCount).isEqualTo(4)
    }

    /**
     * This is a shel test from the flicker infra to ensure the Layers tracing pipeline executed
     * entirely executed correctly
     */
    @Presubmit
    @Test
    fun internalLayersCheck() {
        var trace: LayersTraceSubject? = null
        var executionCount = 0
        flicker.assertLayers {
            executionCount++
            trace = this
            this.isNotEmpty()
        }
        flicker.assertLayers {
            executionCount++
            val failure: Result<Any> = runCatching { this.isEmpty() }
            if (failure.isSuccess) {
                error("Should have thrown failure")
            }
        }
        flicker.assertLayersStart {
            executionCount++
            validateState(this, trace?.first())
            validateVisibleRegion(this.visibleRegion(), trace?.first()?.visibleRegion())
        }
        flicker.assertLayersEnd {
            executionCount++
            validateState(this, trace?.last())
            validateVisibleRegion(this.visibleRegion(), trace?.last()?.visibleRegion())
        }
        Truth.assertWithMessage("Execution count").that(executionCount).isEqualTo(4)
    }

    private fun validateState(actual: FlickerSubject?, expected: FlickerSubject?) {
        Truth.assertWithMessage("Actual state").that(actual).isNotNull()
        Truth.assertWithMessage("Expected state").that(expected).isNotNull()
        Truth.assertWithMessage("Incorrect state")
            .that(actual?.completeFacts?.joinToString { it.toString() })
            .isEqualTo(expected?.completeFacts?.joinToString { it.toString() })
    }

    private fun validateVisibleRegion(
        actual: RegionSubject?,
        expected: RegionSubject?,
    ) {
        Truth.assertWithMessage("Actual visible region").that(actual).isNotNull()
        Truth.assertWithMessage("Expected visible region").that(expected).isNotNull()
        actual?.coversExactly(expected?.region ?: Region.EMPTY)

        val failure: Result<Any?> = runCatching {
            actual?.isHigher(expected?.region ?: Region.EMPTY)
        }
        if (failure.isSuccess) {
            error("Should have thrown failure")
        }
    }

    companion object {
        /**
         * Creates the test configurations.
         *
         * See [FlickerTestFactory.nonRotationTests] for configuring screen orientation and
         * navigation modes.
         */
        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun getParams(): List<FlickerTest> {
            return FlickerTestFactory.nonRotationTests()
        }
    }
}

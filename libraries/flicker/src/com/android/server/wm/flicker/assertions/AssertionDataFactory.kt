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

package com.android.server.wm.flicker.assertions

import com.android.server.wm.flicker.AssertionTag
import com.android.server.wm.flicker.traces.FlickerTraceSubject
import kotlin.reflect.KClass

/**
 * Helper class to create assertions to execute on a trace or state
 *
 * @param stateSubject Type of subject used for state assertions
 * @param traceSubject Type of subject used for trace assertions
 */
class AssertionDataFactory(
    stateSubject: KClass<out FlickerSubject>,
    private val traceSubject: KClass<out FlickerTraceSubject<*>>
) : AssertionStateDataFactory(stateSubject) {

    /**
     * Creates an [assertion] to be executed on trace
     *
     * @param assertion Assertion predicate
     */
    fun createTraceAssertion(
        assertion: (FlickerTraceSubject<FlickerSubject>) -> Unit
    ): AssertionData {
        val closedAssertion: FlickerTraceSubject<FlickerSubject>.() -> Unit = {
            require(isAssertionsEmpty()) { "Subject was already used to execute assertions" }
            assertion(this)
            forAllEntries()
        }
        return AssertionData(
            tag = AssertionTag.ALL,
            expectedSubjectClass = traceSubject,
            assertion = closedAssertion as FlickerSubject.() -> Unit
        )
    }
}

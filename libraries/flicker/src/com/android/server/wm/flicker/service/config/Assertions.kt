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

package com.android.server.wm.flicker.service.config

import com.android.server.wm.flicker.service.assertors.AssertionData
import com.android.server.wm.flicker.service.assertors.BaseAssertionBuilder
import com.android.server.wm.flicker.service.assertors.Components
import com.android.server.wm.flicker.service.assertors.assertions.AppLayerIsInvisibleAtEnd
import com.android.server.wm.flicker.service.assertors.assertions.AppLayerIsInvisibleAtStart
import com.android.server.wm.flicker.service.assertors.assertions.AppLayerIsVisibleAtEnd
import com.android.server.wm.flicker.service.assertors.assertions.AppLayerIsVisibleAtStart
import com.android.server.wm.flicker.service.assertors.assertions.EntireScreenCoveredAlways
import com.android.server.wm.flicker.service.assertors.assertions.EntireScreenCoveredAtEnd
import com.android.server.wm.flicker.service.assertors.assertions.EntireScreenCoveredAtStart
import com.android.server.wm.flicker.service.assertors.assertions.LayerIsVisibleAlways
import com.android.server.wm.flicker.service.assertors.assertions.LayerIsVisibleAtEnd
import com.android.server.wm.flicker.service.assertors.assertions.LayerIsVisibleAtStart
import com.android.server.wm.flicker.service.assertors.assertions.NonAppWindowIsVisibleAlways
import com.android.server.wm.flicker.service.assertors.assertions.StatusBarLayerPositionAtEnd
import com.android.server.wm.flicker.service.assertors.assertions.StatusBarLayerPositionAtStart
import com.android.server.wm.flicker.service.assertors.assertions.VisibleLayersShownMoreThanOneConsecutiveEntry
import com.android.server.wm.flicker.service.assertors.assertions.VisibleWindowsShownMoreThanOneConsecutiveEntry
import com.android.server.wm.flicker.service.assertors.common.AppLayerBecomesVisible
import com.android.server.wm.flicker.service.assertors.common.AppWindowBecomesTopWindow
import com.android.server.wm.flicker.service.assertors.common.AppWindowBecomesVisible
import com.android.server.wm.flicker.service.config.common.AssertionInvocationGroup.BLOCKING
import com.android.server.wm.flicker.service.config.common.AssertionInvocationGroup.NON_BLOCKING
import com.android.server.wm.flicker.service.config.common.Scenario
import com.android.server.wm.flicker.service.config.common.ScenarioInstance
import com.android.server.wm.traces.common.transition.Transition

object Assertions {
    fun assertionsForScenarioInstance(scenarioInstance: ScenarioInstance): List<AssertionData> {
        return assertionsForScenario(scenarioInstance.scenario).map {
            AssertionData(scenarioInstance.scenario, it, it.invocationGroup)
        }
    }

    fun assertionsForTransition(transition: Transition): List<AssertionData> {
        val assertions: MutableList<AssertionData> = mutableListOf()
        for (scenario in Scenario.values()) {
            scenario.description
            if (scenario.executionCondition.shouldExecute(transition)) {
                for (assertion in assertionsForScenario(scenario)) {
                    assertions.add(
                        AssertionData(scenario, assertion, assertion.invocationGroup)
                    )
                }
            }
        }

        return assertions
    }

    private val COMMON_ASSERTIONS = listOf(
        LayerIsVisibleAtStart(Components.NAV_BAR) runAs BLOCKING,
        LayerIsVisibleAtEnd(Components.NAV_BAR) runAs BLOCKING,
        NonAppWindowIsVisibleAlways(Components.NAV_BAR) runAs NON_BLOCKING,
        NonAppWindowIsVisibleAlways(Components.STATUS_BAR) runAs NON_BLOCKING,
        LayerIsVisibleAlways(Components.STATUS_BAR) runAs BLOCKING,
        EntireScreenCoveredAtStart() runAs BLOCKING,
        EntireScreenCoveredAtEnd() runAs BLOCKING,
        EntireScreenCoveredAlways() runAs BLOCKING,
        VisibleWindowsShownMoreThanOneConsecutiveEntry() runAs BLOCKING,
        VisibleLayersShownMoreThanOneConsecutiveEntry() runAs BLOCKING,
        StatusBarLayerPositionAtStart() runAs BLOCKING,
        StatusBarLayerPositionAtEnd() runAs BLOCKING,
    )

    private val APP_LAUNCH_ASSERTIONS = COMMON_ASSERTIONS + listOf(
        AppLayerIsVisibleAtStart(Components.LAUNCHER) runAs NON_BLOCKING,
        AppLayerIsInvisibleAtStart(Components.OPENING_APP) runAs BLOCKING,

        AppLayerIsInvisibleAtEnd(Components.LAUNCHER) runAs NON_BLOCKING,
        AppLayerIsVisibleAtEnd(Components.OPENING_APP) runAs NON_BLOCKING,
        AppLayerBecomesVisible(Components.OPENING_APP) runAs NON_BLOCKING,
        AppWindowBecomesVisible(Components.OPENING_APP) runAs NON_BLOCKING,
        AppWindowBecomesTopWindow(Components.OPENING_APP) runAs NON_BLOCKING,
    )

    private val APP_CLOSE_ASSERTIONS = COMMON_ASSERTIONS + listOf(
        AppLayerIsVisibleAtStart(Components.CLOSING_APP) runAs BLOCKING,
        AppLayerIsInvisibleAtStart(Components.LAUNCHER) runAs NON_BLOCKING,

        AppLayerIsInvisibleAtEnd(Components.CLOSING_APP) runAs BLOCKING,
        AppLayerIsVisibleAtEnd(Components.LAUNCHER) runAs NON_BLOCKING,
    )

    private fun assertionsForScenario(scenario: Scenario): List<BaseAssertionBuilder> {
        return when (scenario) {
            Scenario.COMMON -> COMMON_ASSERTIONS
            Scenario.APP_LAUNCH -> APP_LAUNCH_ASSERTIONS
            Scenario.APP_CLOSE -> APP_CLOSE_ASSERTIONS
            Scenario.ROTATION -> TODO()
            Scenario.IME_APPEAR -> TODO()
            Scenario.IME_DISAPPEAR -> TODO()
            Scenario.PIP_ENTER -> TODO()
            Scenario.PIP_EXIT -> TODO()
        }
    }
}
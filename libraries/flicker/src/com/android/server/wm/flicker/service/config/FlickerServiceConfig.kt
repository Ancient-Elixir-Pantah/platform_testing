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
import com.android.server.wm.flicker.service.assertors.common.AppLayerIsInvisibleAtEnd
import com.android.server.wm.flicker.service.assertors.common.AppLayerIsInvisibleAtStart
import com.android.server.wm.flicker.service.assertors.common.AppLayerIsVisibleAtEnd
import com.android.server.wm.flicker.service.assertors.common.AppLayerIsVisibleAtStart
import com.android.server.wm.flicker.service.assertors.common.EntireScreenCoveredAlways
import com.android.server.wm.flicker.service.assertors.common.EntireScreenCoveredAtEnd
import com.android.server.wm.flicker.service.assertors.common.EntireScreenCoveredAtStart
import com.android.server.wm.flicker.service.assertors.common.LayerIsVisibleAlways
import com.android.server.wm.flicker.service.assertors.common.LayerIsVisibleAtEnd
import com.android.server.wm.flicker.service.assertors.common.LayerIsVisibleAtStart
import com.android.server.wm.flicker.service.assertors.common.NonAppWindowIsVisibleAlways
import com.android.server.wm.flicker.service.assertors.common.StatusBarLayerPositionAtEnd
import com.android.server.wm.flicker.service.assertors.common.StatusBarLayerPositionAtStart
import com.android.server.wm.flicker.service.assertors.common.VisibleLayersShownMoreThanOneConsecutiveEntry
import com.android.server.wm.flicker.service.assertors.common.VisibleWindowsShownMoreThanOneConsecutiveEntry
import com.android.server.wm.flicker.service.config.AssertionInvocationGroup.BLOCKING
import com.android.server.wm.flicker.service.config.AssertionInvocationGroup.NON_BLOCKING
import com.android.server.wm.traces.common.transition.Transition
import com.android.server.wm.traces.common.transition.Transition.Companion.Type

class FlickerServiceConfig {

    fun assertionsForTransition(transition: Transition): List<AssertionData> {
        val assertions: MutableList<AssertionData> = mutableListOf()
        for (assertionGroup in Scenario.values()) {
            assertionGroup.description
            if (assertionGroup.executionCondition.shouldExecute(transition)) {
                for (assertion in assertionGroup.assertions) {
                    assertions.add(
                        AssertionData(assertionGroup, assertion, assertion.invocationGroup)
                    )
                }
            }
        }

        return assertions
    }

    companion object {
        enum class Scenario(
            val description: String,
            val executionCondition: AssertionExecutionCondition,
            val assertions: List<BaseAssertionBuilder>
        ) {
            COMMON(
                "Common", AssertionExecutionCondition.ALWAYS, COMMON_ASSERTIONS
            ),
            APP_LAUNCH(
                "AppLaunch", AssertionExecutionCondition.APP_LAUNCH, APP_LAUNCH_ASSERTIONS
            ),
            APP_CLOSE(
                "AppClose", AssertionExecutionCondition.APP_CLOSE, APP_CLOSE_ASSERTIONS
            )
        }

        enum class AssertionExecutionCondition(
            val shouldExecute: (transition: Transition) -> Boolean
        ) {
            ALWAYS({ true }),
            NEVER({ false }),
            APP_LAUNCH({ t ->
                t.type == Type.OPEN &&
                    t.changes.any { it.transitMode == Type.OPEN }
            }),
            APP_CLOSE({ t ->
                t.type == Type.CLOSE &&
                    t.changes.any { it.transitMode == Type.CLOSE }
            })
        }

        val COMMON_ASSERTIONS = listOf(
            LayerIsVisibleAtStart(Components.NAV_BAR) runAs NON_BLOCKING,
            LayerIsVisibleAtEnd(Components.NAV_BAR) runAs NON_BLOCKING,
            NonAppWindowIsVisibleAlways(Components.NAV_BAR) runAs NON_BLOCKING,
            NonAppWindowIsVisibleAlways(Components.STATUS_BAR) runAs NON_BLOCKING,
            LayerIsVisibleAlways(Components.STATUS_BAR) runAs NON_BLOCKING,
            EntireScreenCoveredAtStart() runAs NON_BLOCKING,
            EntireScreenCoveredAtEnd() runAs NON_BLOCKING,
            EntireScreenCoveredAlways() runAs NON_BLOCKING,
            VisibleWindowsShownMoreThanOneConsecutiveEntry() runAs NON_BLOCKING,
            VisibleLayersShownMoreThanOneConsecutiveEntry() runAs NON_BLOCKING,
            StatusBarLayerPositionAtStart() runAs NON_BLOCKING,
            StatusBarLayerPositionAtEnd() runAs NON_BLOCKING
        )

        val APP_LAUNCH_ASSERTIONS = COMMON_ASSERTIONS + listOf(
            AppLayerIsVisibleAtStart(Components.LAUNCHER) runAs NON_BLOCKING,
            AppLayerIsInvisibleAtStart(Components.OPENING_APP) runAs NON_BLOCKING,

            AppLayerIsInvisibleAtEnd(Components.LAUNCHER) runAs NON_BLOCKING,
            AppLayerIsVisibleAtEnd(Components.OPENING_APP) runAs NON_BLOCKING
        )

        val APP_CLOSE_ASSERTIONS = COMMON_ASSERTIONS + listOf(
            AppLayerIsVisibleAtStart(Components.CLOSING_APP) runAs NON_BLOCKING,
            AppLayerIsInvisibleAtStart(Components.LAUNCHER) runAs NON_BLOCKING,

            AppLayerIsInvisibleAtEnd(Components.CLOSING_APP) runAs NON_BLOCKING,
            AppLayerIsVisibleAtEnd(Components.LAUNCHER) runAs NON_BLOCKING
        )
    }
}

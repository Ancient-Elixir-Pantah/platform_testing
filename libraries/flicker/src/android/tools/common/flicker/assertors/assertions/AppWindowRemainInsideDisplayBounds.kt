/*
 * Copyright (C) 2023 The Android Open Source Project
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

package android.tools.common.flicker.assertors.assertions

import android.tools.common.flicker.IScenarioInstance
import android.tools.common.flicker.assertors.ComponentTemplate
import android.tools.common.flicker.subject.wm.WindowManagerTraceSubject

/**
 * Checks that [component] window remains inside the display bounds throughout the whole animation
 */
class AppWindowRemainInsideDisplayBounds(private val component: ComponentTemplate) :
    AssertionTemplateWithComponent(component) {
    /** {@inheritDoc} */
    override fun doEvaluate(
        scenarioInstance: IScenarioInstance,
        wmSubject: WindowManagerTraceSubject
    ) {
        wmSubject
            .invoke("appWindowRemainInsideDisplayBounds") { entry ->
                val displays = entry.wmState.displays
                if (displays.isEmpty()) {
                    entry.fail("No displays found")
                }
                val display = entry.wmState.displays.sortedBy { it.id }.first()
                entry
                    .visibleRegion(component.build(scenarioInstance))
                    .coversAtMost(display.displayRect)
            }
            .forAllEntries()
    }
}
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

package com.android.server.wm.flicker.service.assertors.common

import com.android.server.wm.flicker.service.assertors.ComponentBuilder
import com.android.server.wm.flicker.traces.windowmanager.WindowManagerTraceSubject
import com.android.server.wm.traces.common.transition.Transition

/**
 * Checks that [getWindowState] window becomes pinned
 */
class AppWindowBecomesPinned(component: ComponentBuilder) :
    BaseAssertionBuilderWithComponent(component) {
    /** {@inheritDoc} */
    override fun doEvaluate(
        transition: Transition,
        wmSubject: WindowManagerTraceSubject
    ) {
        wmSubject.invoke("appWindowIsNotPinned") { it.isNotPinned(component.build(transition)) }
            .then()
            .invoke("appWindowIsPinned") { it.isPinned(component.build(transition)) }
            .forAllEntries()
    }
}

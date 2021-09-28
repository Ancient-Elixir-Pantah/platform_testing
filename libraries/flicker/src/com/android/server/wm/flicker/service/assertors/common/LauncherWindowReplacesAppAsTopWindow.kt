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

import com.android.server.wm.flicker.service.assertors.BaseAssertion
import com.android.server.wm.flicker.service.assertors.Components
import com.android.server.wm.flicker.traces.layers.LayersTraceSubject
import com.android.server.wm.flicker.traces.windowmanager.WindowManagerTraceSubject
import com.android.server.wm.traces.common.FlickerComponentName
import com.android.server.wm.traces.common.tags.Tag

/**
 * Checks that [getWindowState] is the top visible app window at the start of the transition and
 * that it is replaced by [Components.LAUNCHER] during the transition
 */
class LauncherWindowReplacesAppAsTopWindow : BaseAssertion() {
    private fun getWindowState(tag: Tag, wmSubject: WindowManagerTraceSubject) =
        wmSubject.subjects.first().subjects
            .firstOrNull { it.windowState?.token == tag.windowToken }
            ?: wmSubject.subjects.first().subjects
                .firstOrNull { it.windowState?.layer == tag.layerId }
            ?: error("Unable to identify app from tag")

    override fun doEvaluate(
        tag: Tag,
        wmSubject: WindowManagerTraceSubject,
        layerSubject: LayersTraceSubject
    ) {
        val window = getWindowState(tag, wmSubject)
        val appComponent = FlickerComponentName.unflattenFromString(window.name)
        wmSubject.isAppWindowOnTop(appComponent)
            .then()
            .isAppWindowOnTop(Components.LAUNCHER)
    }
}

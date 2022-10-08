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

package com.android.server.wm.traces.common.service.processors

import com.android.server.wm.traces.common.DeviceStateDump
import com.android.server.wm.traces.common.service.ScenarioType
import com.android.server.wm.traces.common.service.TagIdGenerator
import com.android.server.wm.traces.common.tags.Tag
import kotlin.math.max
import kotlin.math.min

/**
 * Represents the Finite State Machine used by tagging processors and implements adding start and
 * end tags.
 * @param tags represents the map of timestamps associated with tag(s).
 */
abstract class FSMState(protected val tags: MutableMap<Long, MutableList<Tag>>) {
    abstract fun process(
        previous: DeviceStateDump?,
        current: DeviceStateDump,
        next: DeviceStateDump?
    ): FSMState?

    protected fun addStartTransitionTag(
        state: DeviceStateDump,
        scenarioType: ScenarioType,
        layerId: Int = 0,
        windowToken: String = "",
        taskId: Int = 0,
        timestamp: Long = min(state.wmState.timestamp, state.layerState.timestamp)
    ) {
        lastTagId = TagIdGenerator.getNext()
        val startTag =
            Tag(
                id = lastTagId,
                scenarioType,
                isStartTag = true,
                layerId = layerId,
                windowToken = windowToken,
                taskId = taskId
            )
        if (!tags.containsKey(timestamp)) {
            tags[timestamp] = mutableListOf()
        }
        tags.getValue(timestamp).add(startTag)
    }

    protected fun addEndTransitionTag(
        state: DeviceStateDump,
        transition: ScenarioType,
        layerId: Int = 0,
        windowToken: String = "",
        taskId: Int = 0,
        timestamp: Long = max(state.wmState.timestamp, state.layerState.timestamp)
    ) {
        val endTag =
            Tag(
                id = lastTagId,
                transition,
                isStartTag = false,
                layerId = layerId,
                windowToken = windowToken,
                taskId = taskId
            )
        if (!tags.containsKey(timestamp)) {
            tags[timestamp] = mutableListOf()
        }
        tags.getValue(timestamp).add(endTag)
    }

    protected fun hasOpenTag() = tags.values.flatten().size % 2 != 0

    companion object {
        private var lastTagId = -1
    }
}

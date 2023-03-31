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

package android.tools.device.flicker.datastore

import android.tools.TEST_SCENARIO
import android.tools.common.CrossPlatform
import android.tools.common.io.ResultArtifactDescriptor
import android.tools.common.io.RunStatus
import android.tools.common.io.TransitionTimeRange
import android.tools.device.traces.getDefaultFlickerOutputDir
import android.tools.device.traces.io.Artifact
import android.tools.device.traces.io.ResultData
import java.io.File

object Consts {
    internal const val FAILURE = "Expected failure"

    private val artifact =
        Artifact(
            RunStatus.RUN_EXECUTED,
            TEST_SCENARIO,
            getDefaultFlickerOutputDir(),
            emptyMap<ResultArtifactDescriptor, File>()
        )

    internal val TEST_RESULT =
        ResultData(
            _artifact = artifact,
            _transitionTimeRange =
                TransitionTimeRange(
                    CrossPlatform.timestamp.empty(),
                    CrossPlatform.timestamp.empty()
                ),
            _executionError = null
        )

    internal val RESULT_FAILURE =
        ResultData(
            _artifact = artifact,
            _transitionTimeRange =
                TransitionTimeRange(
                    CrossPlatform.timestamp.empty(),
                    CrossPlatform.timestamp.empty()
                ),
            _executionError = null
        )
}
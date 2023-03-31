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

package android.tools.common

import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
object PlatformConsts {
    /**
     * The default Display id, which is the id of the primary display assuming there is one.
     *
     * Duplicated from [Display.DEFAULT_DISPLAY] because this class is used by JVM and KotlinJS
     */
    @JsName("DEFAULT_DISPLAY") const val DEFAULT_DISPLAY = 0

    /**
     * Window type: an application window that serves as the "base" window of the overall
     * application
     *
     * Duplicated from [WindowManager.LayoutParams.TYPE_BASE_APPLICATION] because this class is used
     * by JVM and KotlinJS
     */
    @JsName("TYPE_BASE_APPLICATION") const val TYPE_BASE_APPLICATION = 1

    /**
     * Window type: special application window that is displayed while the application is starting
     *
     * Duplicated from [WindowManager.LayoutParams.TYPE_APPLICATION_STARTING] because this class is
     * used by JVM and KotlinJS
     */
    @JsName("TYPE_APPLICATION_STARTING") const val TYPE_APPLICATION_STARTING = 3

    /**
     * Rotation constant: 0 degrees rotation (natural orientation)
     *
     * Duplicated from [Surface.ROTATION_0] because this class is used by JVM and KotlinJS
     */
    @JsName("ROTATION_0") const val ROTATION_0 = 0

    /**
     * Rotation constant: 90 degrees rotation.
     *
     * Duplicated from [Surface.ROTATION_90] because this class is used by JVM and KotlinJS
     */
    @JsName("ROTATION_90") const val ROTATION_90 = 1

    /**
     * Rotation constant: 180 degrees rotation.
     *
     * Duplicated from [Surface.ROTATION_180] because this class is used by JVM and KotlinJS
     */
    @JsName("ROTATION_180") const val ROTATION_180 = 2

    /**
     * Rotation constant: 270 degrees rotation.
     *
     * Duplicated from [Surface.ROTATION_270] because this class is used by JVM and KotlinJS
     */
    @JsName("ROTATION_270") const val ROTATION_270 = 3

    /**
     * Navigation bar mode constant: 3 button navigation.
     *
     * Duplicated from [WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL_OVERLAY] because this
     * class is used by JVM and KotlinJS
     */
    @JsName("MODE_GESTURAL")
    const val MODE_GESTURAL = "com.android.internal.systemui.navbar.gestural"

    /**
     * Navigation bar mode : gestural navigation.
     *
     * Duplicated from [WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY] because this
     * class is used by JVM and KotlinJS
     */
    @JsName("MODE_3BUTTON")
    const val MODE_3BUTTON = "com.android.internal.systemui.navbar.threebutton"
}
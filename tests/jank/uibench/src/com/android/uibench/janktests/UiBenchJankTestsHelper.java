/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.uibench.janktests;

import android.content.Context;
import android.content.Intent;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;

/**
 * Jank benchmark tests helper for UiBench app
 */

public class UiBenchJankTestsHelper {
    public static final int LONG_TIMEOUT = 5000;
    public static final int TIMEOUT = 250;
    public static final int INNER_LOOP = 3;
    public static final int EXPECTED_FRAMES = 100;

    public static final String RES_PACKAGE_NAME = "android";
    public static final String PACKAGE_NAME = "com.android.test.uibench";

    private static UiBenchJankTestsHelper mInstance;
    private UiDevice mDevice;
    private Context mContext;

    private UiBenchJankTestsHelper(UiDevice device, Context context) {
        mDevice = device;
        mContext = context;
    }

    public static UiBenchJankTestsHelper getInstance(UiDevice device) {
        return new UiBenchJankTestsHelper(device, null);
    }

    public static UiBenchJankTestsHelper getInstance(UiDevice device, Context context) {
        if (mInstance == null) {
            mInstance = new UiBenchJankTestsHelper(device, context);
        }
        return mInstance;
    }

    // Launch UiBench app
    public void launchUiBench() {
        Intent intent = mContext.getPackageManager()
                .getLaunchIntentForPackage(PACKAGE_NAME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        mDevice.waitForIdle();
    }

    // Helper method to go back to home screen
    public void goBackHome() throws UiObjectNotFoundException {
        String launcherPackage = mDevice.getLauncherPackageName();
        UiObject2 homeScreen = mDevice.findObject(By.res(launcherPackage,"workspace"));
        while (homeScreen == null) {
            mDevice.pressBack();
            homeScreen = mDevice.findObject(By.res(launcherPackage,"workspace"));
        }
    }
}

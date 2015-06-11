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

package com.android.sysapp.janktests;

import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.jank.GfxMonitor;
import android.support.test.jank.JankTest;
import android.support.test.jank.JankTestBase;
import android.support.test.launcherhelper.ILauncherStrategy;
import android.support.test.launcherhelper.LauncherStrategyFactory;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;
import junit.framework.Assert;

/**
 * Jank test for YouTube recommendation window fling 3 times.
 */

public class YouTubeJankTests extends JankTestBase {
    private static final int TIMEOUT = 5000;
    private static final int INNER_LOOP = 5;
    private static final int EXPECTED_FRAMES = 100;
    private static final String PACKAGE_NAME = "com.google.android.youtube";
    private static final String APP_NAME = "YouTube";

    private UiDevice mDevice;
    private ILauncherStrategy mLauncherStrategy = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
        try {
            mDevice.setOrientationNatural();
        } catch (RemoteException e) {
            throw new RuntimeException("failed to freeze device orientaion", e);
        }
        mLauncherStrategy = LauncherStrategyFactory.getInstance(mDevice).getLauncherStrategy();
    }

    @Override
    protected void tearDown() throws Exception {
        mDevice.unfreezeRotation();
        super.tearDown();
    }

    public void launchYouTube () throws UiObjectNotFoundException {
        mLauncherStrategy.launch(APP_NAME, PACKAGE_NAME);
        dismissCling();
    }

    // Measures jank while fling YouTube recommendation
    @JankTest(beforeTest="launchYouTube", expectedFrames=EXPECTED_FRAMES)
    @GfxMonitor(processName=PACKAGE_NAME)
    public void testYouTubeRecomendationWindowFling() {
        UiObject2 uiObject = mDevice.wait(
                Until.findObject(By.res(PACKAGE_NAME, "pane_fragment_container")), TIMEOUT);
        Assert.assertNotNull("Recommendation container is null", uiObject);
        for (int i = 0; i < INNER_LOOP; i++) {
            uiObject.scroll(Direction.DOWN, 1.0f);
            SystemClock.sleep(100);
            uiObject.scroll(Direction.UP, 1.0f);
        }
    }

    private void dismissCling() {
        // Dismiss the dogfood splash screen that might appear on first start
        UiObject2 dialog_dismiss_btn = mDevice.wait(Until.findObject(
                By.res(PACKAGE_NAME, "dogfood_warning_dialog_dismiss_button").text("OK")), TIMEOUT);
        if (dialog_dismiss_btn != null) {
            dialog_dismiss_btn.click();
        }
        UiObject2 welcomeSkip = mDevice.wait(
            Until.findObject(By.res(PACKAGE_NAME, "skip_button").text("Skip")), TIMEOUT);
        if (welcomeSkip != null) {
            welcomeSkip.click();
        }
        UiObject2 musicFaster = mDevice.wait(
            Until.findObject(By.res(PACKAGE_NAME, "text").text("Find music faster")), TIMEOUT);
        if (musicFaster != null) {
            UiObject2 ok = mDevice.wait(
                    Until.findObject(By.res(PACKAGE_NAME, "ok").text("OK")), TIMEOUT);
            Assert.assertNotNull("No 'ok' button to bypass music", ok);
            ok.click();
      }
    }
}

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

package com.android.myroboapplication;

import static org.junit.Assert.assertFalse;

import androidx.test.runner.AndroidJUnit4;

import com.android.robotestutil.RobolectricTestUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WelcomeActivityDeviceTest {

    /** Running test type check in a device test should return false */
    @Test
    public void testTypeCheck() {
        boolean isRoboTest = RobolectricTestUtil.isRobolectricTest();
        assertFalse(isRoboTest);
    }
}

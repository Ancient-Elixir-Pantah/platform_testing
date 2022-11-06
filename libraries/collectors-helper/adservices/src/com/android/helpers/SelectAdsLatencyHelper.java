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

package com.android.helpers;

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SelectAdsLatencyHelper {

    public static LatencyHelper getLogcatCollector() {
        return LatencyHelper.getLogcatLatencyHelper(new SelectAdsProcessInputForLatencyMetrics());
    }

    @VisibleForTesting
    public static LatencyHelper getCollector(
            LatencyHelper.InputStreamFilter inputStreamFilter, Clock clock) {
        return new LatencyHelper(
                new SelectAdsProcessInputForLatencyMetrics(), inputStreamFilter, clock);
    }

    private static class SelectAdsProcessInputForLatencyMetrics
            implements LatencyHelper.ProcessInputForLatencyMetrics {
        private static final String LOG_LABEL_P50_5G = "SELECT_ADS_LATENCY_P50_5G";
        private static final String LOG_LABEL_P50_4GPLUS = "SELECT_ADS_LATENCY_P50_4GPLUS";
        private static final String LOG_LABEL_P50_4G = "SELECT_ADS_LATENCY_P50_4G";
        private static final String LOG_LABEL_P90_5G = "SELECT_ADS_LATENCY_P90_5G";
        private static final String LOG_LABEL_P90_4GPLUS = "SELECT_ADS_LATENCY_P90_4GPLUS";
        private static final String LOG_LABEL_P90_4G = "SELECT_ADS_LATENCY_P90_4G";

        @Override
        public String getTestLabel() {
            return "SelectAds";
        }

        @Override
        public Map<String, Long> processInput(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            Pattern latencyMetricPattern =
                    Pattern.compile(getTestLabel() + ": \\((.*): (\\d+) ms\\)");

            String line = "";
            Map<String, Long> output = new HashMap<String, Long>();
            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = latencyMetricPattern.matcher(line);
                while (matcher.find()) {
                    /**
                     * The lines from Logcat will look like: 06-13 18:09:24.058 20765 20781 D
                     * SelectAds: (SELECT_ADS_LATENCY_P50_5G: 1900 ms)
                     */
                    String metric = matcher.group(1);
                    long latency = Long.parseLong(matcher.group(2));
                    switch (metric) {
                        case LOG_LABEL_P50_5G:
                            output.put(LOG_LABEL_P50_5G, latency);
                            break;
                        case LOG_LABEL_P50_4GPLUS:
                            output.put(LOG_LABEL_P50_4GPLUS, latency);
                            break;
                        case LOG_LABEL_P50_4G:
                            output.put(LOG_LABEL_P50_4G, latency);
                            break;
                        case LOG_LABEL_P90_5G:
                            output.put(LOG_LABEL_P90_5G, latency);
                            break;
                        case LOG_LABEL_P90_4GPLUS:
                            output.put(LOG_LABEL_P90_4GPLUS, latency);
                            break;
                        case LOG_LABEL_P90_4G:
                            output.put(LOG_LABEL_P90_4G, latency);
                            break;
                    }
                }
            }
            return output;
        }
    }
}

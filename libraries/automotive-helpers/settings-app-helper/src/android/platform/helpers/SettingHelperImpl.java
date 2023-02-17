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

package android.platform.helpers;

import android.app.Instrumentation;
import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.platform.helpers.ScrollUtility.ScrollActions;
import android.platform.helpers.ScrollUtility.ScrollDirection;
import android.platform.helpers.exceptions.UnknownUiException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;

import androidx.test.InstrumentationRegistry;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Setting Helper class for Android Auto platform functional tests */
public class SettingHelperImpl extends AbstractStandardAppHelper implements IAutoSettingHelper {

    private static final String LOG_TAG = SettingHelperImpl.class.getSimpleName();

    private ScrollUtility mScrollUtility;

    private UiModeManager mUiModeManager;
    private Context mContext;
    private boolean mUseCommandToOpenSettings = true;

    public SettingHelperImpl(Instrumentation instr) {
        super(instr);
        mUiModeManager =
                InstrumentationRegistry.getInstrumentation()
                        .getContext()
                        .getSystemService(UiModeManager.class);
        mContext = InstrumentationRegistry.getContext();
        mUseCommandToOpenSettings =
                Boolean.valueOf(
                        InstrumentationRegistry.getArguments()
                                .getString("use_command_to_open_settings", "true"));
        mScrollUtility = ScrollUtility.getInstance(getSpectatioUiUtil());
    }

    /** {@inheritDoc} */
    @Override
    public void open() {
        if (mUseCommandToOpenSettings) {
            Log.i(LOG_TAG, "Using Command to open Settings.");
            openFullSettings();
        } else {
            Log.i(LOG_TAG, "Using Intent to open Settings.");
            super.open();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getPackage() {
        return getPackageFromConfig(AutomotiveConfigConstants.SETTINGS_PACKAGE);
    }

    @Override
    public void dismissInitialDialogs() {
        // Nothing to dismiss
    }

    /** {@inheritDoc} */
    @Override
    public void stopSettingsApplication() {
        getSpectatioUiUtil()
                .executeShellCommand(
                        getCommandFromConfig(AutomotiveConfigConstants.STOP_SETTING_APP_COMMAND));
    }

    /** {@inheritDoc} */
    @Override
    public String getLauncherName() {
        return "Settings";
    }

    /** {@inheritDoc} */
    @Override
    public void exit() {
        getSpectatioUiUtil().pressHome();
        getSpectatioUiUtil().wait1Second();
    }

    /** {@inheritDoc} */
    @Override
    public void openSetting(String setting) {
        executeWorkflow(setting);
    }

    @Override
    public String getPageTitleText() {
        UiObject2 pageToolbarTitle = getPageTitle();
        return pageToolbarTitle.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void openFullSettings() {
        getSpectatioUiUtil()
                .executeShellCommand(
                        getCommandFromConfig(AutomotiveConfigConstants.OPEN_SETTINGS_COMMAND));
    }

    /** {@inheritDoc} */

    /** {@inheritDoc} */
    @Override
    public void turnOnOffWifi(boolean onOff) {
        boolean isOn = isWifiOn();
        if (isOn != onOff) {
            BySelector enableOptionSelector =
                    getUiElementFromConfig(AutomotiveConfigConstants.TOGGLE_WIFI);
            UiObject2 enableOption = getSpectatioUiUtil().findUiObject(enableOptionSelector);
            validateUiObject(enableOption, AutomotiveConfigConstants.TOGGLE_WIFI);
            getSpectatioUiUtil().clickAndWait(enableOption);
        } else {
            throw new RuntimeException("Wi-Fi enabled state is already " + (onOff ? "on" : "off"));
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWifiOn() {
        WifiManager wifi = (WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE);
        return wifi.isWifiEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public void turnOnOffHotspot(boolean onOff) {
        boolean isOn = isHotspotOn();
        if (isOn != onOff) {
            BySelector enableOptionSelector =
                    getUiElementFromConfig(AutomotiveConfigConstants.TOGGLE_HOTSPOT);
            UiObject2 enableOption = getSpectatioUiUtil().findUiObject(enableOptionSelector);
            validateUiObject(enableOption, AutomotiveConfigConstants.TOGGLE_HOTSPOT);
            getSpectatioUiUtil().clickAndWait(enableOption);
        } else {
            throw new RuntimeException(
                    "Hotspot enabled state is already " + (onOff ? "on" : "off"));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void toggleHotspot() {
        BySelector enableOptionSelector =
                getUiElementFromConfig(AutomotiveConfigConstants.TOGGLE_HOTSPOT);
        UiObject2 enableOption = getSpectatioUiUtil().findUiObject(enableOptionSelector);
        validateUiObject(enableOption, AutomotiveConfigConstants.TOGGLE_HOTSPOT);
        getSpectatioUiUtil().clickAndWait(enableOption);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHotspotOn() {
        BySelector enableOptionSelector =
                getUiElementFromConfig(AutomotiveConfigConstants.TOGGLE_HOTSPOT);
        UiObject2 enableOption = getSpectatioUiUtil().findUiObject(enableOptionSelector);
        validateUiObject(enableOption, AutomotiveConfigConstants.TOGGLE_HOTSPOT);
        return enableOption.isChecked();
    }

    /** {@inheritDoc} */
    @Override
    public void turnOnOffBluetooth(boolean onOff) {
        boolean isOn = isBluetoothOn();
        if (isOn != onOff) {
            BySelector enableOptionSelector =
                    getUiElementFromConfig(AutomotiveConfigConstants.TOGGLE_BLUETOOTH);
            UiObject2 enableOption = getSpectatioUiUtil().findUiObject(enableOptionSelector);
            validateUiObject(enableOption, AutomotiveConfigConstants.TOGGLE_BLUETOOTH);
            getSpectatioUiUtil().clickAndWait(enableOption);
        } else {
            throw new RuntimeException(
                    "Bluetooth enabled state is already " + (onOff ? "on" : "off"));
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBluetoothOn() {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        return ba.isEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public void searchAndSelect(String item) {
        searchAndSelect(item, 0);
    }

    /** {@inheritDoc} */
    @Override
    public void searchAndSelect(String item, int selectedIndex) {
        BySelector searchButtonSelector = getUiElementFromConfig(AutomotiveConfigConstants.SEARCH);
        UiObject2 searchButton = getSpectatioUiUtil().findUiObject(searchButtonSelector);
        validateUiObject(searchButton, AutomotiveConfigConstants.SEARCH);
        getSpectatioUiUtil().clickAndWait(searchButton);
        getSpectatioUiUtil().waitForIdle();

        BySelector searchBoxSelector = getUiElementFromConfig(AutomotiveConfigConstants.SEARCH_BOX);
        UiObject2 searchBox = getSpectatioUiUtil().findUiObject(searchBoxSelector);
        validateUiObject(searchBox, AutomotiveConfigConstants.SEARCH_BOX);
        searchBox.setText(item);
        getSpectatioUiUtil().wait5Seconds();

        // close the keyboard to reveal all search results.
        getSpectatioUiUtil().pressBack();

        BySelector searchResultsSelector =
                getUiElementFromConfig(AutomotiveConfigConstants.SEARCH_RESULTS);
        UiObject2 searchResults = getSpectatioUiUtil().findUiObject(searchResultsSelector);
        validateUiObject(searchResults, AutomotiveConfigConstants.SEARCH_RESULTS);
        int numberOfResults = searchResults.getChildren().get(0).getChildren().size();
        if (numberOfResults == 0) {
            throw new RuntimeException("No results found");
        }
        getSpectatioUiUtil()
                .clickAndWait(searchResults.getChildren().get(0).getChildren().get(selectedIndex));
        getSpectatioUiUtil().waitForIdle();
        getSpectatioUiUtil().wait5Seconds();

        BySelector objectSelector = By.textContains(item);
        UiObject2 object = getSpectatioUiUtil().findUiObject(objectSelector);
        validateUiObject(object, AutomotiveConfigConstants.SEARCH_RESULTS);
        validateUiObject(object, String.format("Opened page does not contain searched item"));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isValidPageTitle(String item) {
        UiObject2 pageTitle = getPageTitle();
        return pageTitle.getText().contains(item);
    }

    private UiObject2 getPageTitle() {
        getSpectatioUiUtil().wait5Seconds();
        BySelector[] selectors =
                new BySelector[] {
                    getUiElementFromConfig(AutomotiveConfigConstants.PAGE_TITLE),
                    getUiElementFromConfig(AutomotiveConfigConstants.PERMISSIONS_PAGE_TITLE)
                };

        for (BySelector selector : selectors) {
            List<UiObject2> pageTitles = getSpectatioUiUtil().findUiObjects(selector);
            validateUiObject(pageTitles, String.format("Page title"));
            if (pageTitles != null && pageTitles.size() > 0) {
                return pageTitles.get(pageTitles.size() - 1);
            }
        }
        throw new RuntimeException("Unable to find page title");
    }

    /** {@inheritDoc} */
    @Override
    public void goBackToSettingsScreen() {
        // count is used to avoid infinite loop in case someone invokes
        // after exiting settings application
        int count = 5;
        BySelector titleText =
                getUiElementFromConfig(AutomotiveConfigConstants.SETTINGS_TITLE_TEXT);
        while (count > 0
                && isAppInForeground()
                && getSpectatioUiUtil().findUiObjects(titleText) == null) {
            getSpectatioUiUtil().pressBack();
            getSpectatioUiUtil().wait5Seconds(); // to avoid stale object error
            count--;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void openMenuWith(String... menuOptions) {
        // Scroll and Find Subsettings
        for (String menu : menuOptions) {
            Pattern menuPattern = Pattern.compile(menu, Pattern.CASE_INSENSITIVE);
            BySelector selector = By.text(menuPattern);

            ScrollActions scrollAction =
                    ScrollActions.valueOf(
                            getActionFromConfig(
                                    AutomotiveConfigConstants.SETTINGS_SUB_SETTING_SCROLL_ACTION));

            BySelector forwardButtonSelector =
                    getUiElementFromConfig(
                            AutomotiveConfigConstants.SETTINGS_SUB_SETTING_SCROLL_FORWARD_BUTTON);
            BySelector backwardButtonSelector =
                    getUiElementFromConfig(
                            AutomotiveConfigConstants.SETTINGS_SUB_SETTING_SCROLL_BACKWARD_BUTTON);

            BySelector scrollableElementSelector =
                    getUiElementFromConfig(
                            AutomotiveConfigConstants.SETTINGS_SUB_SETTING_SCROLL_ELEMENT);
            ScrollDirection scrollDirection =
                    ScrollDirection.valueOf(
                            getActionFromConfig(
                                    AutomotiveConfigConstants
                                            .SETTINGS_SUB_SETTING_SCROLL_DIRECTION));

            UiObject2 object =
                    mScrollUtility.scrollAndFindUiObject(
                            scrollAction,
                            scrollDirection,
                            forwardButtonSelector,
                            backwardButtonSelector,
                            scrollableElementSelector,
                            selector,
                            String.format("Scroll on setting to find subssetting %s", selector));

            validateUiObject(
                    object, String.format("Unable to find UI Element %s.", selector.toString()));
            getSpectatioUiUtil().clickAndWait(object);
            getSpectatioUiUtil().waitForIdle();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getValue(String setting) {
        String cmd = String.format("settings get system %s", setting);
        String value = getSpectatioUiUtil().executeShellCommand(cmd);
        return Integer.parseInt(value.replaceAll("\\s", ""));
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(String setting, int value) {
        String cmd = String.format(Locale.US, "settings put system %s %d", setting, value);
        getSpectatioUiUtil().executeShellCommand(cmd);
    }

    /** {@inheritDoc} */
    @Override
    public boolean checkMenuExists(String setting) {
        return getSpectatioUiUtil().hasUiElement(setting);
    }

    private void validateUiObject(UiObject2 uiObject, String action) {
        if (uiObject == null) {
            throw new UnknownUiException(
                    String.format("Unable to find UI Element for %s.", action));
        }
    }

    private void validateUiObject(List<UiObject2> uiObjects, String action) {
        if (uiObjects == null) {
            throw new UnknownUiException(
                    String.format("Unable to find UI Element for %s.", action));
        }
    }

    /**
     * TODO - Keeping the below empty functions for now, to avoid the compilation error in Vendor it
     * will be removed after vendor clean up (b/266450258)
     */

    /** {@inheritDoc} */
    @Override
    public UiObject2 findSettingMenu(String setting) {
        UiObject2 menuObject = null;
        return menuObject;
    }

    @Override
    public void findSettingMenuAndClick(String setting) {}

    /**
     * Checks whether a setting menu is enabled or not. When not enabled, the menu item cannot be
     * clicked.
     */
    @Override
    public boolean isSettingMenuEnabled(String menu) {
        boolean isSettingMenuEnabled = false;
        return isSettingMenuEnabled;
    }

    private UiObject2 getMenu(String menu, int index) {
        UiObject2 menuButton = null;
        return menuButton;
    }

    /** {@inheritDoc} */
    @Override
    public void changeSeekbarLevel(int index, ChangeType changeType) {}
}

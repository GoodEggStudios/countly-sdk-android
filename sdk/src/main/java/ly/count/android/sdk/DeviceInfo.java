/*
Copyright (c) 2012, 2013, 2014 Countly

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package ly.count.android.sdk;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class provides several static methods to retrieve information about
 * the current device and operating environment.
 *
 * It is important to call setDeviceID early, before logging any session or custom
 * event data.
 */
class DeviceInfo {

    private static String appVersion = null;
    private static String storeInstall = null;

    /**
     * Returns the display name of the current operating system.
     */
    @SuppressWarnings("SameReturnValue")
    static String getOS() {
        return "Android";
    }

    /**
     * Returns the current operating system version as a displayable string.
     */
    @SuppressWarnings("SameReturnValue")
    static String getOSVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * Returns the current device model.
     */
    @SuppressWarnings("SameReturnValue")
    static String getDevice() {
        return "UNTRACKED";
    }

    static String deepLink;

    /**
     * Returns the non-scaled pixel resolution of the current default display being used by the
     * WindowManager in the specified context.
     * @param context context to use to retrieve the current WindowManager
     * @return a string in the format "WxH", or the empty string "" if resolution cannot be determined
     */
    static String getResolution(final Context context) {
        return "UNTRACKED";
    }

    /**
     * Maps the current display density to a string constant.
     * @param context context to use to retrieve the current display metrics
     * @return a string constant representing the current display density, or the
     *         empty string if the density is unknown
     */
    static String getDensity(final Context context) {
        return "UNTRACKED";
    }

    /**
     * Returns the display name of the current network operator from the
     * TelephonyManager from the specified context.
     * @param context context to use to retrieve the TelephonyManager from
     * @return the display name of the current network operator, or the empty
     *         string if it cannot be accessed or determined
     */
    static String getCarrier(final Context context) {
        return "UNTRACKED";
    }

    static int getTimezoneOffset() {
        return TimeZone.getDefault().getOffset(new Date().getTime()) / 60000;
    }

    /**
     * Returns the current locale (ex. "en_US").
     */
    static String getLocale() {
        return "UNTRACKED";
    }

    /**
     * Returns the application version string stored in the specified
     * context's package info versionName field, or "1.0" if versionName
     * is not present.
     */
    static String getAppVersion(final Context context) {
        if (appVersion != null) return appVersion;

        appVersion = Countly.DEFAULT_APP_VERSION;
        try {
            appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            if (Countly.sharedInstance().isLoggingEnabled()) {
                Log.i(Countly.TAG, "No app version found");
            }
        }
        return appVersion;
    }

    /**
     * Returns the package name of the app that installed this app
     */
    static String getStore(final Context context) {
        if (storeInstall != null) return storeInstall;
        storeInstall = "";
        try {
            storeInstall = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        } catch (Exception e) {
            if (Countly.sharedInstance().isLoggingEnabled()) {
                Log.i(Countly.TAG, "Can't get Installer package [getStore]");
            }
        }
        if (storeInstall == null || storeInstall.length() == 0) {
            storeInstall = "";
            if (Countly.sharedInstance().isLoggingEnabled()) {
                Log.i(Countly.TAG, "No store found [getStore]");
            }
        }
        return storeInstall;
    }

    /**
     * Returns a URL-encoded JSON string containing the device metrics
     * to be associated with a begin session event.
     * See the following link for more info:
     * https://count.ly/resources/reference/server-api
     */
    static String getMetrics(final Context context) {
        final JSONObject json = new JSONObject();

        fillJSONIfValuesNotEmpty(json,
                "_device", getDevice(),
                "_os", getOS(),
                "_os_version", getOSVersion(),
                "_carrier", getCarrier(context),
                "_resolution", getResolution(context),
                "_density", getDensity(context),
                "_locale", getLocale(),
                "_app_version", getAppVersion(context),
                "_store", getStore(context),
                "_deep_link", "UNTRACKED");

        String result = json.toString();

        try {
            result = java.net.URLEncoder.encode(result, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            // should never happen because Android guarantees UTF-8 support
        }

        return result;
    }

    /**
     * Utility method to fill JSONObject with supplied objects for supplied keys.
     * Fills json only with non-null and non-empty key/value pairs.
     * @param json JSONObject to fill
     * @param objects varargs of this kind: key1, value1, key2, value2, ...
     */
    static void fillJSONIfValuesNotEmpty(final JSONObject json, final String ... objects) {
        try {
            if (objects.length > 0 && objects.length % 2 == 0) {
                for (int i = 0; i < objects.length; i += 2) {
                    final String key = objects[i];
                    final String value = objects[i + 1];
                    if (value != null && value.length() > 0) {
                        json.put(key, value);
                    }
                }
            }
        } catch (JSONException ignored) {
            // shouldn't ever happen when putting String objects into a JSONObject,
            // it can only happen when putting NaN or INFINITE doubles or floats into it
        }
    }
}

package com.github.skjolber.nfc.external;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class ActivityAliasTools {

    private static final String TAG = ActivityAliasTools.class.getName();

    public static boolean isBootFilter(Context context) {
        return getFilter(".BootCompleted", context);
    }

    public static void setUsbDeviceFilter(Context context, boolean enabled) {
        setFilter(".UsbDevice", context, enabled);
    }

    public static boolean isUsbDeviceFilter(Context context) {
        return getFilter(".UsbDevice", context);
    }

    public static void setBootFilter(Context context, boolean enabled) {
        setFilter(".BootCompleted", context, enabled);
    }

    public static void setFilter(String component, Context context, boolean enabled) {

        String playIdentifier = getPlayIdentifier(context);

        ComponentName componentName = new ComponentName(playIdentifier, playIdentifier + component);

        // disable alias so that we do not receive our own intents
        PackageManager pm = context.getPackageManager();

        int code;
        if (enabled) {
            Log.d(TAG, "Enable feature " + componentName.getClassName());

            code = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        } else {
            code = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

            Log.d(TAG, "Disable feature " + componentName.getClassName());
        }

        if (pm.getComponentEnabledSetting(componentName) != code) {
            pm.setComponentEnabledSetting(componentName, code, PackageManager.DONT_KILL_APP);
        }
    }

    public static boolean getFilter(String component, Context context) {
        PackageManager pm = context.getPackageManager();

        String playIdentifier = getPlayIdentifier(context);

        ComponentName componentName = new ComponentName(playIdentifier, playIdentifier + component);

        // disable alias so that we do not receive our own intents

        return pm.getComponentEnabledSetting(componentName) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    private static String getPlayIdentifier(Context context) {
        PackageInfo pi;
        try {
            pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            return pi.applicationInfo.packageName;
        } catch (final NameNotFoundException e) {
            return PreferencesActivity.class.getPackage().getName();
        }
    }
}

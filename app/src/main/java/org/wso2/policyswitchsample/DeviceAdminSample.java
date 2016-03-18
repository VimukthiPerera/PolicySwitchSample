package org.wso2.policyswitchsample;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Created by root on 1/12/16.
 */
public class DeviceAdminSample extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context context, Intent intent) {
        // EnableProfileActivity is launched with the newly set up profile.
        Intent launch = new Intent(context, MapsActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launch.putExtra("adminEnabled", true);
        Toast.makeText(context, "Device admin enabled", Toast.LENGTH_SHORT).show();
        context.startActivity(launch);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, "Device admin disabled", Toast.LENGTH_SHORT).show();
    }

    /**
     * Generates a {@link ComponentName} that is used throughout the app.
     * @return a {@link ComponentName}
     */
    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), DeviceAdminSample.class);
    }
}

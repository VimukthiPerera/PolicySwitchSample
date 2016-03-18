package org.wso2.policyswitchsample;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.wso2.edgeanalyticsservice.EdgeAnalyticsCallback;

public class BuildingExitListener extends EdgeAnalyticsCallback {
    DevicePolicyManager myDevicePolicyMgr;
    ComponentName componentName;
    boolean cameraDisabledState = false;

    public BuildingExitListener() {
    }

    @Override
    public void callback(Context context, String s) {
        cameraDisabledState = false;
        disableCamera(context);
        Toast.makeText(context, "Outside building", Toast.LENGTH_SHORT).show();
    }

    public void disableCamera(Context context) {

        if(myDevicePolicyMgr == null || componentName == null) {
            myDevicePolicyMgr = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            componentName = DeviceAdminSample.getComponentName(context);
        }

        if(myDevicePolicyMgr.getCameraDisabled(componentName) != cameraDisabledState) {
            myDevicePolicyMgr.setCameraDisabled(componentName, cameraDisabledState);
            Toast.makeText(context, cameraDisabledState ? "Disabled camera!" : "Enabled camera!", Toast.LENGTH_SHORT).show();
        }
    }
}

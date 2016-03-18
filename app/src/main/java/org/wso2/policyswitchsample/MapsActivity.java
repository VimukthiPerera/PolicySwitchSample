package org.wso2.policyswitchsample;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.wso2.edgeanalyticsservice.EdgeAnalyticsCallback;
import org.wso2.edgeanalyticsservice.NoDataAgentManager;
import org.wso2.edgeanalyticsservice.Stream;
import org.wso2.edgeanalyticsservice.WSO2Application;
import org.wso2.edgeanalyticsservice.WSO2Context;
import org.wso2.edgeanalyticsservice.WSO2DataTypes;
import org.wso2.edgeanalyticsservice.WSO2SystemStreams;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ComponentName componentName;
    private DeviceAdminSample deviceAdminSample;
    private WSO2Application wso2Application;
    private NoDataAgentManager noDataAgentManager;
    private GoogleMap mMap;
    private PolygonOptions rectOptions;
    private Polygon polygon;
    private DevicePolicyManager myDevicePolicyMgr;
    private Button activateButton;

    private boolean cameraDisabledState = true;
    //Change these values to the required area
    private LatLng top_right = new LatLng(6.909988, 79.852605);
    private LatLng bottom_left = new LatLng(6.909739, 79.852404);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        activateButton = (Button) findViewById(R.id.button_change_policy);
        myDevicePolicyMgr = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        componentName = DeviceAdminSample.getComponentName(this);
        if(myDevicePolicyMgr.isAdminActive(componentName)){
            activateButton.setVisibility(View.GONE);
        } else {
            Intent deviceAdminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            deviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DeviceAdminSample.getComponentName(this));
            deviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "None cause: "+DeviceAdminSample.getComponentName(this));
            Toast.makeText(this, DeviceAdminSample.getComponentName(this).toString(), Toast.LENGTH_LONG).show();
            startActivityForResult(deviceAdminIntent, 1);
            this.finish();
            activateButton.setVisibility(View.VISIBLE);
        }
        wso2Application = WSO2Application.getInstance();
        noDataAgentManager = (NoDataAgentManager) wso2Application.getWSO2Service(WSO2Context.EDGE_ANALYTICS_SERVICE_NO_DATA);
        try {
            if (savedInstanceState == null) {
                noDataAgentManager.subscribeStreamToData(new Stream("LocationStream")
                        .addData("latitude", WSO2DataTypes.DOUBLE)
                        .addData("longitude", WSO2DataTypes.DOUBLE), WSO2SystemStreams.LATITUDE, WSO2SystemStreams.LONGITUDE);
                // This query will trigger the callbacks defined below if the device is outside the specified area
                noDataAgentManager.addQuery("from LocationStream[latitude > "+top_right.latitude+" " +
                        "or longitude > "+top_right.longitude+" " +
                        "or latitude < "+bottom_left.latitude+" " +
                        "or longitude < "+bottom_left.longitude+"] " +
                        "select latitude,longitude " +
                        "insert into OutputStream0 ;");
                noDataAgentManager.addCallback("OutputStream0", new EdgeAnalyticsCallback() {
                    @Override
                    public void callback(Context context, String s) {
                        setPolygonColor(100, 255, 0, 0);
                    }
                });
                noDataAgentManager.addCallback("OutputStream0", "org.wso2.LOCATION_EXIT");
                // This query will trigger the callbacks defined below if the device is inside the specified area
                noDataAgentManager.addQuery("from LocationStream[latitude < "+top_right.latitude+" " +
                        "and longitude < "+top_right.longitude+" " +
                        "and latitude > "+bottom_left.latitude+" " +
                        "and longitude > "+bottom_left.longitude+"] " +
                        "select latitude,longitude " +
                        "insert into OutputStream1 ;");
                noDataAgentManager.addCallback("OutputStream1", new EdgeAnalyticsCallback() {
                    @Override
                    public void callback(Context context, String s) {
                        setPolygonColor(100, 0, 255, 128);
                    }
                });
                noDataAgentManager.addCallback("OutputStream1", "org.wso2.LOCATION_ENTER");
            } else {
                Button btn = (Button) findViewById(R.id.button_change_policy);
                btn.setVisibility(View.GONE);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* Former inside:
                "from LocationStream[latitude < 6.979819 " +
                        "and longitude < 79.888228 " +
                        "and latitude > 6.859168 " +
                        "and longitude > 79.815787] " +
                        "select latitude,longitude " +
                        "insert into OutputStream1 ;"*/
//                RT- 6.909988, 79.852605
//                LB- 6.909739, 79.852404

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker at center and move the camera
        LatLng wso2LatLng = new LatLng((top_right.latitude+bottom_left.latitude)/2, (top_right.longitude+bottom_left.longitude)/2);
        mMap.addMarker(new MarkerOptions().position(wso2LatLng).title("Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(wso2LatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20), 2000, null);
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);


//                RT- 6.909988, 79.852605
//                LB- 6.909739, 79.852404

        // Instantiates a new Polygon object and adds points to define a rectangle
        rectOptions = new PolygonOptions()
                .add(new LatLng(top_right.latitude, bottom_left.longitude),
                        new LatLng(top_right.latitude, top_right.longitude),
                        new LatLng(bottom_left.latitude, top_right.longitude),
                        new LatLng(bottom_left.latitude, bottom_left.longitude));

        rectOptions.fillColor(Color.argb(100, 0, 255, 0));

// Get back the mutable Polygon
        polygon = mMap.addPolygon(rectOptions);
    }


    public void setPolygonColor(int a, int r, int g, int b) {
        polygon.setFillColor(Color.argb(a, r, g, b));
    }

    public void policyButton(View view) {
        changePolicy();
    }

    private void changePolicy() {
        Intent deviceAdminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        deviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DeviceAdminSample.getComponentName(this));
        deviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "None cause: "+DeviceAdminSample.getComponentName(this));
        Toast.makeText(this, DeviceAdminSample.getComponentName(this).toString(), Toast.LENGTH_LONG).show();
        startActivityForResult(deviceAdminIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check if this is the result of the provisioning activity
        if (requestCode == 1) {
            myDevicePolicyMgr = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
            componentName = DeviceAdminSample.getComponentName(this);

            //Disable camera until cleared by callback
            myDevicePolicyMgr.setCameraDisabled(componentName, true);
            Toast.makeText(this, "Disabled camera!", Toast.LENGTH_SHORT).show();

        } else {
            // This is the result of some other activity, call the superclass
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void disableCamera(View view) {

        if(myDevicePolicyMgr == null || componentName == null) {
            myDevicePolicyMgr = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
            componentName = DeviceAdminSample.getComponentName(this);
        }

        if(myDevicePolicyMgr.getCameraDisabled(componentName) != cameraDisabledState) {
            myDevicePolicyMgr.setCameraDisabled(componentName, cameraDisabledState);
            Toast.makeText(this, cameraDisabledState ? "Disabled camera!" : "Enabled camera!", Toast.LENGTH_SHORT).show();
        }
    }

}

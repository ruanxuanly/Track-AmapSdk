package com.example.ruanxuan.mytrack;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.PersistableBundle;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    MapView mMapView = null;
    private AMapLocationClient aMapLocationClient = null;
    private AMapLocationListener aMapLocationListener = null;
    private AMapLocationClientOption aMapLocationClientOption = null;
    private AMap maMap = null;
    private Marker mMark = null;
    private UiSettings mUIsetting = null;
    private Polyline mPolyline;
    private PolylineOptions mLineOption;
    private Button mStartBtn = null;
    private boolean isLocationStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int checkCallPhonePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},3);
        }

        mMapView = (MapView)findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        maMap = mMapView.getMap();
        maMap.setMyLocationEnabled(true);
        mUIsetting = maMap.getUiSettings();
        mUIsetting.setScaleControlsEnabled(true);
        mUIsetting.setMyLocationButtonEnabled(true);
        mLineOption = new PolylineOptions();
        mLineOption.width(10);
        mLineOption.color(Color.BLACK);
        mStartBtn = (Button)findViewById(R.id.btnLocation);
        maMap.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                Log.i("rx","setLocationSource -> activate");
                if(aMapLocationClientOption != null){
                    Log.i("rx","start once location");
                    aMapLocationClientOption.setOnceLocation(true);
                    aMapLocationClient.startLocation();
                }

            }

            @Override
            public void deactivate() {
                Log.i("rx","setLocationSource -> deactivate");
            }
        });

        Log.i("rx","start location");
        aMapLocationClient = new AMapLocationClient(getApplicationContext());
        aMapLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if(aMapLocationClient == null)
                    return;
                if(aMapLocation.getErrorCode() == 0){
                    LatLng latLng = new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                    //第一个参数是经纬度  第二个参数的是zoom
                    maMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));
                    mLineOption.add(latLng);
                    mPolyline = maMap.addPolyline(mLineOption);

                    if(mMark == null){
                        mMark = maMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.point)));
                    }else{
                        mMark.setPosition(latLng);
                    }

                }
            }
        };
        aMapLocationClient.setLocationListener(aMapLocationListener);

        aMapLocationClientOption = new AMapLocationClientOption();
        aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //aMapLocationClientOption.setGpsFirst(true);
        aMapLocationClient.setLocationOption(aMapLocationClientOption);
        aMapLocationClientOption.setInterval(2000);
//        aMapLocationClient.startLocation();

        mPolyline = maMap.addPolyline(mLineOption);
        mPolyline.setDottedLine(true);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLocationStarted){
                    isLocationStarted = false;
                    Log.i("rx","stop Location");
                    mStartBtn.setText("开始");
                    aMapLocationClient.stopLocation();
                }else{
                    isLocationStarted = true;
                    Log.i("rx","start Location");
                    aMapLocationClientOption.setOnceLocation(true);
                    aMapLocationClient.startLocation();
                    mStartBtn.setText("结束");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 3){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                finish();
            }
        }else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        aMapLocationClient.stopLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}

package com.LingLing.SapientialTravel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.autonavi.tbt.TrafficFacilityInfo;

import java.util.List;

public class FindActivity extends Activity implements LocationSource, AMapLocationListener, View.OnClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter, AMap.OnMarkerClickListener, AMap.OnMapClickListener, AMapNaviListener {
    private EditText edtSearch;
    private Button mBtnSearch;
    private Button back;
    private MapView mapView;
    private AMap aMap;
    private boolean isFirstLoc = true;
    private boolean bolIsPoi = false;
    private PoiSearch.Query query;
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private LocationSource.OnLocationChangedListener mListener = null;//定位监听器
    protected double Latitude;//定位的纬度
    protected double Longitude;//定位的精度
    private RouteOverLay mRouteOverLay;//规划路线

    private MyPoiOverlay poiOverlay;//poi图层
    private List<PoiItem> poiItems;//poi数据
    protected double lastLatitude;//poi点的纬度
    protected double lastLongitude;//poi点的经度
    private String poiName;//poi点的名字
    private String poiAndress;//poi点的地址
    private int poiDistance;//poi点的距离
    private double testLatitude;
    private double testLongitude;
    private String testTitle;

    private TextView poi_name;
    private TextView poi_distance;
    private Button btgothere;
    private Button btmore;

    private Marker detailMarker;
    private Marker mlastMarker;
    protected AMapNavi aMapNavi;
    protected TTSController mTtsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aMapNavi = AMapNavi.getInstance(getApplicationContext());
        setContentView(R.layout.activity_find);
        mapView = (MapView) findViewById(R.id.findMapView);
        edtSearch = (EditText) findViewById(R.id.edt_input);
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        back = (Button) findViewById(R.id.back);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
            Log.e("TAG", "aMap==" + aMap.toString());
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.position));
            myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER);//地图依照设备方向旋转，随设备移动
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setLocationSource(this);//定位监听事件
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.setMyLocationEnabled(true);
            aMap.setOnMapClickListener(this);
            aMap.setOnMarkerClickListener(this);
            aMap.setOnInfoWindowClickListener(this);
            aMap.setInfoWindowAdapter(this);
        }
        mBtnSearch.setOnClickListener(this);
        back.setOnClickListener(this);

        mRouteOverLay = new RouteOverLay(aMap, null, getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_search) {
            String pois = edtSearch.getText().toString().trim();
            bolIsPoi = true;
            search(pois, "", 1000);

        } else if (i == R.id.back) {
            Intent service = new Intent(FindActivity.this, MusicServer.class);
            stopService(service);
            finish();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                Latitude = aMapLocation.getLatitude();
                Longitude = aMapLocation.getLongitude();
                mListener.onLocationChanged(aMapLocation);//显示系统小蓝点
                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), 18));
                    isFirstLoc = false;
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //开启定位
        mLocationClient.startLocation();

    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        View popuView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.popup, null);
        poi_name = (TextView) popuView.findViewById(R.id.poi_name);
        poi_distance = (TextView) popuView.findViewById(R.id.poi_distance);
        btgothere = (Button) popuView.findViewById(R.id.bt_tohere);
        btmore = (Button) popuView.findViewById(R.id.bt_more);
        poi_name.setText(poiName);
        poi_distance.setText(poiDistance + "米");
        btgothere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击气泡时，停止语音介绍
                Intent service = new Intent(FindActivity.this, MusicServer.class);
                stopService(service);
                marker.remove();
                //点击气泡时，在地图上进行路径规划（不需要调用外部地图界面）显示起点终点路径--进行导航时最好手机图标根据人运动而运动
                aMapNavi.addAMapNaviListener(FindActivity.this);
                aMapNavi.addAMapNaviListener(mTtsManager);
                Intent intent = new Intent(FindActivity.this, WalkRouteCalculateActivity.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", Latitude);
                bundle.putDouble("longitude", Longitude);
                bundle.putDouble("lastLatitude", lastLatitude);
                bundle.putDouble("lastLongitude", lastLongitude);
                intent.putExtras(bundle);
                startActivity(intent);
                boolean isSuccess = aMapNavi.calculateWalkRoute(new NaviLatLng(Latitude, Longitude), new NaviLatLng(lastLatitude, lastLongitude));
                if (!isSuccess) {
                    Toast.makeText(FindActivity.this, "路线计算失败，检查参数情况", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindActivity.this, DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("poiName", poiName);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        return popuView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        aMap.clear();
        poiOverlay.removeFromMap();
        mRouteOverLay.removeFromMap();
        detailMarker.hideInfoWindow();
        Intent service = new Intent(FindActivity.this, MusicServer.class);
        stopService(service);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getObject() != null) {
            try {
                PoiItem mCurrentPoi = (PoiItem) marker.getObject();
                if (mlastMarker == null) {
                    mlastMarker = marker;
                } else {
                    mlastMarker = marker;
                }
                Intent service = new Intent(FindActivity.this, MusicServer.class);
                startService(service);
                detailMarker = marker;
                int poiIndex = poiOverlay.getPoiIndex(marker);
                lastLatitude = poiItems.get(poiIndex).getLatLonPoint().getLatitude();
                lastLongitude = poiItems.get(poiIndex).getLatLonPoint().getLongitude();
                Log.e("TAG", "定位维度：" + Latitude + "\n" + "定位经度" + Longitude);
                Log.e("TAG", "目的地纬度：" + lastLatitude + "\n" + "目的地经度：" + lastLongitude);
                poiName = poiItems.get(poiIndex).getTitle();
                marker.showInfoWindow();

                poiAndress = poiItems.get(poiIndex).getSnippet();
                Toast.makeText(this, "poi点的名字：" + poiName + "poi点的地址：" + poiAndress, Toast.LENGTH_SHORT).show();
                Log.e("TAG", "点击了第" + poiIndex + "个图标" + "---" + "名字：" + mCurrentPoi.getTitle() + "---" + "地址：" + mCurrentPoi.getSnippet());

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
        return false;
    }

    private void search(final String content, String val, final int distance) {
        Log.e("开始", "1");
        if (this.bolIsPoi) {
            if (content == null) {
                Toast.makeText(this, "输入为空", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("开始", "3");
                query = new PoiSearch.Query(content, val, "西安市");
                query.setPageSize(30);
                query.setPageNum(0);
                PoiSearch poiSearch = new PoiSearch(this, query);
                if ((Latitude != 0) && (Longitude != 0)) {
                    poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(Latitude, Longitude), distance));
                    poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
                        @Override
                        public void onPoiSearched(PoiResult poiResult, int i) {
                            bolIsPoi = false;
                            Log.e("开始", "4");
                            poiItems = poiResult.getPois();
                            Log.e("子条目搜索结果", "poiItems==" + poiItems.size());
                            Toast.makeText(FindActivity.this, "搜索到了："+poiItems.size()+"个", Toast.LENGTH_SHORT).show();
                            Log.e("TAG", "id==" + poiItems.get(0).getPoiId());
                            for (int m = 0; m < poiItems.size(); m++) {
                                poiName = poiItems.get(m).getTitle();
                                poiDistance = poiItems.get(m).getDistance();
                                String snippet = poiItems.get(m).getSnippet();
                                testLatitude = poiResult.getPois().get(0).getLatLonPoint().getLatitude() + 0.000004;//自定义的poi建筑物的纬度
                                testLongitude = poiResult.getPois().get(0).getLatLonPoint().getLongitude() + 0.000005;//自定义的poi建筑物的经度
                                Log.e("TAG", "第一个点名字：" + poiResult.getPois().get(0).getTitle() + "\n" + "第一个点地址：" + poiResult.getPois().get(0).getSnippet());
                                testTitle = poiResult.getPois().get(0).getSnippet();
                                Log.e("TAG", "名字:" + poiName + "\n" + "地址:" + snippet);
                            }
                            Log.e("TAG", "地址为：" + poiItems.get(0).getSnippet());
                            //获取poiItem数据
                            if (poiItems != null && poiItems.size() > 0) {
                                //新的marker
                                poiOverlay = new MyPoiOverlay(aMap, poiItems, content);
                                poiOverlay.addToMap();
                                if (content.equals("景点")) {
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(new LatLng(testLatitude, testLongitude));
                                    markerOptions.title(testTitle);
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.xiqipao));
                                    aMap.addMarker(markerOptions);
                                }
                                poiOverlay.zoomToSpan();
                            } else {
                                Toast.makeText(FindActivity.this, "没有搜索到相关数据", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPoiItemSearched(PoiItem poiItem, int i) {

                        }
                    });
                    poiSearch.searchPOIAsyn();
                } else {
                    Toast.makeText(this, "搜索失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteSuccess() {
        AMapNaviPath naviPath = aMapNavi.getNaviPath();
        if (naviPath == null) return;
        // 获取路径规划线路，显示到地图上
        mRouteOverLay.setAMapNaviPath(naviPath);
        mRouteOverLay.addToMap();
        mRouteOverLay.zoomToSpan();
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        Toast.makeText(this, "路线计算失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        aMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}

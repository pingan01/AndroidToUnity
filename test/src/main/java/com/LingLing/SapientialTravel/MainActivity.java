package com.LingLing.SapientialTravel;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.unity3d.player.UnityPlayerActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements LocationSource, AMapLocationListener, View.OnClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter, AMap.OnMarkerClickListener, AMap.OnMapClickListener, AMapNaviListener {
    private AMap aMap;//地图对象
    private MapView mapView;
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private LocationSource.OnLocationChangedListener mListener = null;//定位监听器
    private boolean isFirstLoc = true;//标识符--判断是否只显示一次定位信息和用户重新定位
    private Button mBtnLoc;
    private Button mBtnSwitch;
    private Button mBtnback;
    private Button mBtnmenu;
    private Button mBtnArrow;
    private Button mBtnArrowLeft;

    private Button sciencPoints;
    private Button play;
    private Button show;
    private Button hotel;
    private Button food;
    private Button relaxation;
    private Button shopping;
    private Button bus;
    private Button stop;

    private LinearLayout menu;//一级菜单列表
    private RelativeLayout show_lv;//二级菜单列表

    private ListView lv;
    private ArrayList<String> list = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    boolean isMenuShow = false;//目录是否展示
    private boolean bolIsPoi = false;
    private PoiSearch.Query query;
    protected double Latitude;//定位的纬度
    protected double Longitude;//定位的精度
    private Marker detailMarker;
    private Marker mlastMarker;


    private MyPoiOverlay poiOverlay;//poi图层
    private List<PoiItem> poiItems;//poi数据
    protected double lastLatitude;//poi点的纬度
    protected double lastLongitude;//poi点的经度
    private String poiName;//poi点的名字
    private String poiAndress;//poi点的地址
    private int poiDistance;//poi点的距离
    private TextView poi_name;
    private TextView poi_distance;
    private Button btgothere;
    private Button btmore;
    private MyLocationStyle myLocationStyle;

    private RouteOverLay mRouteOverLay;//规划路线
    protected AMapNavi aMapNavi;
    private String naviInfo;//文字转语音
    private float totalDistance;
    private float totalTime;

    private boolean isExit = false;//是否退出
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isExit = false;
        }
    };
    protected TTSController mTtsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        aMapNavi = AMapNavi.getInstance(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
            Log.e("TAG", "aMap==" + aMap.toString());
            myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.position));
            myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER);//地图依照设备方向旋转，随设备移动
            //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//定位点依照设备方向旋转，随设备移动
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setLocationSource(this);//定位监听事件
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.setMyLocationEnabled(true);
            aMap.setOnMapClickListener(this);
            aMap.setOnMarkerClickListener(this);
            aMap.setOnInfoWindowClickListener(this);
            aMap.setInfoWindowAdapter(this);
        }
        mBtnLoc.setOnClickListener(this);
        mBtnSwitch.setOnClickListener(this);
        mBtnmenu.setOnClickListener(this);
        mBtnArrowLeft.setOnClickListener(this);
        mBtnback.setOnClickListener(this);
        sciencPoints.setOnClickListener(this);
        play.setOnClickListener(this);
        show.setOnClickListener(this);
        hotel.setOnClickListener(this);
        food.setOnClickListener(this);
        relaxation.setOnClickListener(this);
        shopping.setOnClickListener(this);
        bus.setOnClickListener(this);
        stop.setOnClickListener(this);


        mRouteOverLay = new RouteOverLay(aMap, null, getApplicationContext());
    }

    private void initView() {
        mapView = (MapView) findViewById(R.id.map);
        mBtnLoc = (Button) findViewById(R.id.bt_location);
        mBtnback = (Button) findViewById(R.id.btn_back);
        mBtnSwitch= (Button) findViewById(R.id.switch_map);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "jianti.ttf");
        mBtnSwitch.setTypeface(typeface);

        mBtnmenu = (Button) findViewById(R.id.bt_menu);
        mBtnArrow = (Button) findViewById(R.id.btn_arrow);
        mBtnArrowLeft = (Button) findViewById(R.id.btn_arrow_left);
        menu = (LinearLayout) findViewById(R.id.menu);

        sciencPoints = (Button) findViewById(R.id.bt_scenic_spots);
        play = (Button) findViewById(R.id.bt_play);
        show = (Button) findViewById(R.id.bt_show);
        hotel = (Button) findViewById(R.id.bt_hotel);
        food = (Button) findViewById(R.id.bt_food);
        relaxation = (Button) findViewById(R.id.bt_relaxation);
        shopping = (Button) findViewById(R.id.bt_shopping);
        bus = (Button) findViewById(R.id.bt_bus);
        stop = (Button) findViewById(R.id.stop);
    }

    private void initLocation() {
        isFirstLoc = true;
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
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                Latitude = aMapLocation.getLatitude();
                Longitude = aMapLocation.getLongitude();
                mListener.onLocationChanged(aMapLocation);//显示系统小蓝点
                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), 18));
                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(aMapLocation.getCountry() + ""
                            + aMapLocation.getProvince() + ""
                            + aMapLocation.getCity() + ""
                            + aMapLocation.getProvince() + ""
                            + aMapLocation.getDistrict() + ""
                            + aMapLocation.getStreet() + ""
                            + aMapLocation.getStreetNum());
                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
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
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bt_location) {
            initLocation();

        }else if (i==R.id.switch_map){
            if (mBtnSwitch.getText().equals("2D")) {
                mBtnSwitch.setText("3D");
                mBtnSwitch.setBackgroundResource(R.drawable.bt_style_3d);
                myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
                aMap.setMyLocationStyle(myLocationStyle);
            } else if (mBtnSwitch.getText().equals("3D")) {
                mBtnSwitch.setText("2D");
                mBtnSwitch.setBackgroundResource(R.drawable.bt_style_2d);
                myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER);
                aMap.setMyLocationStyle(myLocationStyle);
            }
        }

        else if (i == R.id.btn_back) {
            Intent service = new Intent(MainActivity.this, MusicServer.class);
            stopService(service);
            finish();
        } else if (i == R.id.bt_menu) {
            if (!isMenuShow) {
                menu.setVisibility(View.VISIBLE);
                mBtnmenu.setVisibility(View.GONE);
                mBtnArrowLeft.setVisibility(View.VISIBLE);
                mBtnArrow.setVisibility(View.GONE);
                isMenuShow = true;
            } else {
                menu.setVisibility(View.GONE);
                isMenuShow = false;
            }
        } else if (i == R.id.btn_arrow_left) {
            menu.setVisibility(View.GONE);
            mBtnmenu.setVisibility(View.VISIBLE);
            mBtnArrowLeft.setVisibility(View.GONE);
            mBtnArrow.setVisibility(View.VISIBLE);
        }
        //搜索景点：0
        else if (i == R.id.bt_scenic_spots) {
            aMap.clear();
            resetBtIcon(v);
            sciencPoints.setBackgroundResource(R.drawable.scenic2);
            bolIsPoi = true;
            search("景点", "", 1000);
            //搜索游乐：1
        } else if (i == R.id.bt_play) {
            aMap.clear();
            resetBtIcon(v);
            play.setBackgroundResource(R.drawable.play2);
            bolIsPoi = true;
            search("木塔寺公园", "", 1000);
            //搜索表演：2
        } else if (i == R.id.bt_show) {
            aMap.clear();
            resetBtIcon(v);
            show.setBackgroundResource(R.drawable.show2);
            bolIsPoi = true;
            search("舞蹈", "", 1000);
            //搜索酒店：3
        } else if (i == R.id.bt_hotel) {
            aMap.clear();
            resetBtIcon(v);
            hotel.setBackgroundResource(R.drawable.hotel2);
            bolIsPoi = true;
            search("酒店", "", 1000);
            //搜索餐饮：4
        } else if (i == R.id.bt_food) {
            aMap.clear();
            resetBtIcon(v);
            food.setBackgroundResource(R.drawable.food2);
            bolIsPoi = true;
            search("餐饮", "", 1000);
            //搜索休闲：5
        } else if (i == R.id.bt_relaxation) {
            aMap.clear();
            resetBtIcon(v);
            relaxation.setBackgroundResource(R.drawable.relaxation2);
            bolIsPoi = true;
            search("休闲", "", 1000);
            //搜索购物：6
        } else if (i == R.id.bt_shopping) {
            aMap.clear();
            resetBtIcon(v);
            shopping.setBackgroundResource(R.drawable.shopping2);
            bolIsPoi = true;
            search("超市", "", 1000);
            //搜索公交：7
        } else if (i == R.id.bt_bus) {
            aMap.clear();
            resetBtIcon(v);
            bus.setBackgroundResource(R.drawable.bus2);
            bolIsPoi = true;
            search("公交车站", "", 1000);
            //搜索停车场：
        } else if (i == R.id.stop) {
            aMap.clear();
            resetBtIcon(v);
            stop.setBackgroundResource(R.drawable.stop2);
            bolIsPoi = true;
            search("停车场", "", 1000);
        }
    }

    /**
     * 重置菜单各个背景图片
     */
    protected void resetBtIcon(View v) {
        mBtnSwitch.setVisibility(View.GONE);
        int i = v.getId();
        if (i == R.id.bt_scenic_spots) {

            play.setBackgroundResource(R.drawable.play1);
            show.setBackgroundResource(R.drawable.show1);
            hotel.setBackgroundResource(R.drawable.hotel1);
            relaxation.setBackgroundResource(R.drawable.relaxation1);
            bus.setBackgroundResource(R.drawable.bus1);
            food.setBackgroundResource(R.drawable.food1);
            shopping.setBackgroundResource(R.drawable.shopping1);
            stop.setBackgroundResource(R.drawable.stop1);

        } else if (i == R.id.bt_play) {

            sciencPoints.setBackgroundResource(R.drawable.scenic1);
            show.setBackgroundResource(R.drawable.show1);
            hotel.setBackgroundResource(R.drawable.hotel1);
            relaxation.setBackgroundResource(R.drawable.relaxation1);
            bus.setBackgroundResource(R.drawable.bus1);
            food.setBackgroundResource(R.drawable.food1);
            shopping.setBackgroundResource(R.drawable.shopping1);
            stop.setBackgroundResource(R.drawable.stop1);

        } else if (i == R.id.bt_food) {

            sciencPoints.setBackgroundResource(R.drawable.scenic1);
            play.setBackgroundResource(R.drawable.play1);
            show.setBackgroundResource(R.drawable.show1);
            hotel.setBackgroundResource(R.drawable.hotel1);
            relaxation.setBackgroundResource(R.drawable.relaxation1);
            bus.setBackgroundResource(R.drawable.bus1);
            shopping.setBackgroundResource(R.drawable.shopping1);
            stop.setBackgroundResource(R.drawable.stop1);

        } else if (i == R.id.bt_hotel) {

            sciencPoints.setBackgroundResource(R.drawable.scenic1);
            play.setBackgroundResource(R.drawable.play1);
            show.setBackgroundResource(R.drawable.show1);
            relaxation.setBackgroundResource(R.drawable.relaxation1);
            bus.setBackgroundResource(R.drawable.bus1);
            food.setBackgroundResource(R.drawable.food1);
            shopping.setBackgroundResource(R.drawable.shopping1);
            stop.setBackgroundResource(R.drawable.stop1);

        } else if (i == R.id.bt_relaxation) {

            sciencPoints.setBackgroundResource(R.drawable.scenic1);
            play.setBackgroundResource(R.drawable.play1);
            show.setBackgroundResource(R.drawable.show1);
            hotel.setBackgroundResource(R.drawable.hotel1);
            bus.setBackgroundResource(R.drawable.bus1);
            food.setBackgroundResource(R.drawable.food1);
            shopping.setBackgroundResource(R.drawable.shopping1);
            stop.setBackgroundResource(R.drawable.stop1);

        } else if (i == R.id.bt_bus) {

            sciencPoints.setBackgroundResource(R.drawable.scenic1);
            play.setBackgroundResource(R.drawable.play1);
            show.setBackgroundResource(R.drawable.show1);
            hotel.setBackgroundResource(R.drawable.hotel1);
            relaxation.setBackgroundResource(R.drawable.relaxation1);
            food.setBackgroundResource(R.drawable.food1);
            shopping.setBackgroundResource(R.drawable.shopping1);
            stop.setBackgroundResource(R.drawable.stop1);

        } else if (i == R.id.bt_show) {

            sciencPoints.setBackgroundResource(R.drawable.scenic1);
            play.setBackgroundResource(R.drawable.play1);
            hotel.setBackgroundResource(R.drawable.hotel1);
            relaxation.setBackgroundResource(R.drawable.relaxation1);
            bus.setBackgroundResource(R.drawable.bus1);
            food.setBackgroundResource(R.drawable.food1);
            shopping.setBackgroundResource(R.drawable.shopping1);
            stop.setBackgroundResource(R.drawable.stop1);

        } else if (i == R.id.bt_shopping) {

            sciencPoints.setBackgroundResource(R.drawable.scenic1);
            play.setBackgroundResource(R.drawable.play1);
            show.setBackgroundResource(R.drawable.show1);
            hotel.setBackgroundResource(R.drawable.hotel1);
            relaxation.setBackgroundResource(R.drawable.relaxation1);
            bus.setBackgroundResource(R.drawable.bus1);
            food.setBackgroundResource(R.drawable.food1);
            stop.setBackgroundResource(R.drawable.stop1);
        } else if (i == R.id.stop) {

            sciencPoints.setBackgroundResource(R.drawable.scenic1);
            play.setBackgroundResource(R.drawable.play1);
            show.setBackgroundResource(R.drawable.show1);
            hotel.setBackgroundResource(R.drawable.hotel1);
            relaxation.setBackgroundResource(R.drawable.relaxation1);
            bus.setBackgroundResource(R.drawable.bus1);
            food.setBackgroundResource(R.drawable.food1);
            shopping.setBackgroundResource(R.drawable.shopping1);
        }
    }

    //参数val：查询类型：酒店/餐馆...等
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

                            for (int m = 0; m < poiItems.size(); m++) {
                                poiName = poiItems.get(m).getTitle();
                                poiDistance = poiItems.get(m).getDistance();
                                String snippet = poiItems.get(m).getSnippet();
                                Log.e("TAG", "名字:" + poiName + "\n" + "地址:" + snippet);
                            }
                            Log.e("TAG", "地址为：" + poiItems.get(0).getSnippet());

                            //获取poiItem数据
                            if (poiItems != null && poiItems.size() > 0) {
                                //新的marker
                                poiOverlay = new MyPoiOverlay(aMap, poiItems, content);
                                poiOverlay.addToMap();
                                poiOverlay.zoomToSpan();
                            } else {
                                Toast.makeText(MainActivity.this, "没有搜索到相关数据", Toast.LENGTH_SHORT).show();
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
    public void onInfoWindowClick(Marker marker) {

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
                Intent service = new Intent(MainActivity.this, MusicServer.class);
                stopService(service);
                marker.remove();
                //点击气泡时，在地图上进行路径规划（不需要调用外部地图界面）显示起点终点路径--进行导航时最好手机图标根据人运动而运动
                aMapNavi.addAMapNaviListener(MainActivity.this);
                aMapNavi.addAMapNaviListener(mTtsManager);
                Intent intent = new Intent(MainActivity.this, WalkRouteCalculateActivity.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", Latitude);
                bundle.putDouble("longitude", Longitude);
                bundle.putDouble("lastLatitude", lastLatitude);
                bundle.putDouble("lastLongitude", lastLongitude);
                intent.putExtras(bundle);
                startActivity(intent);
                boolean isSuccess = aMapNavi.calculateWalkRoute(new NaviLatLng(Latitude, Longitude), new NaviLatLng(lastLatitude, lastLongitude));
                if (!isSuccess) {
                    Toast.makeText(MainActivity.this, "路线计算失败，检查参数情况", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
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
    public boolean onMarkerClick(Marker marker) {
        if (marker.getObject() != null) {
            try {
                PoiItem mCurrentPoi = (PoiItem) marker.getObject();
                if (mlastMarker == null) {
                    mlastMarker = marker;
                } else {
                    mlastMarker = marker;
                }
                detailMarker = marker;
                int poiIndex = poiOverlay.getPoiIndex(marker);
                lastLatitude = poiItems.get(poiIndex).getLatLonPoint().getLatitude();
                lastLongitude = poiItems.get(poiIndex).getLatLonPoint().getLongitude();
                Log.e("TAG", "定位维度：" + Latitude + "\n" + "定位经度" + Longitude);
                Log.e("TAG", "目的地纬度：" + lastLatitude + "\n" + "目的地经度：" + lastLongitude);
                poiName = poiItems.get(poiIndex).getTitle();
                Intent service = new Intent(MainActivity.this, MusicServer.class);
                startService(service);
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

    @Override
    public void onMapClick(LatLng latLng) {
        aMap.clear();
        mBtnSwitch.setVisibility(View.VISIBLE);
        poiOverlay.removeFromMap();
        mRouteOverLay.removeFromMap();
        detailMarker.hideInfoWindow();
        Intent service = new Intent(MainActivity.this, MusicServer.class);
        stopService(service);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isExit) {
                Toast.makeText(this, "再按一次退出智能导游", Toast.LENGTH_SHORT).show();
                isExit = true;
                Intent service = new Intent(MainActivity.this, MusicServer.class);
                stopService(service);
                handler.sendEmptyMessageDelayed(0, 2000);
            } else {
                Log.e("LOG", "退出程序");
                this.finish();
            }
        }
        return true;
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
}

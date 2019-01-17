package com.LingLing.SapientialTravel;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.DirectionView;
import com.amap.api.navi.view.NextTurnTipView;
import com.amap.api.navi.view.OverviewButtonView;

public class WalkRouteCalculateActivity extends BaseActivity {
    protected double Latitude;
    protected double Longitude;
    protected double lastLatitude;//poi点的纬度
    protected double lastLongitude;//poi点的经度
    private OverviewButtonView overviewButtonView;//全览按钮
    private DirectionView compass;//指南针
    private NextTurnTipView myDirection;//转向图标
    private int[] customIcons = {R.drawable.sou2, R.drawable.sou3, R.drawable.sou4,
            R.drawable.sou5, R.drawable.sou6, R.drawable.sou7,
            R.drawable.sou8, R.drawable.sou9, R.drawable.sou10,
            R.drawable.sou11, R.drawable.sou12, R.drawable.sou13,
            R.drawable.sou14, R.drawable.sou15, R.drawable.sou16,
            R.drawable.sou17, R.drawable.sou18, R.drawable.sou19};
    private TextView currentRoad;
    private TextView distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_navi);
        initView();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Latitude = extras.getDouble("latitude");
        Longitude = extras.getDouble("longitude");
        lastLatitude = extras.getDouble("lastLatitude");
        lastLongitude = extras.getDouble("lastLongitude");
        mAMapNaviView.setAMapNaviViewListener(this);
        mAMapNaviView.onCreate(savedInstanceState);

        AMapNaviViewOptions mapOptions = mAMapNaviView.getViewOptions();
        mapOptions.setLayoutVisible(false);
        //mapOptions.setCarBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.position));
        mAMapNaviView.setViewOptions(mapOptions);

        mAMapNaviView.setNaviMode(AMapNaviView.NORTH_UP_MODE);
    }

    private void initView() {
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        myDirection = (NextTurnTipView) findViewById(R.id.myDirection);
        myDirection.setCustomIconTypes(getResources(), customIcons);
        mAMapNaviView.setLazyNextTurnTipView(myDirection);
        overviewButtonView = (OverviewButtonView) findViewById(R.id.myButton);
        mAMapNaviView.setLazyOverviewButtonView(overviewButtonView);//全览
        compass = (DirectionView) findViewById(R.id.myCompass);
        mAMapNaviView.setLazyDirectionView(compass);//指南针
        currentRoad = (TextView) findViewById(R.id.roadName);
        distance = (TextView) findViewById(R.id.goToDistance);
    }

    //导航引导信息回掉
    @Override
    public void onNaviInfoUpdate(NaviInfo naviinfo) {
        super.onNaviInfoUpdate(naviinfo);
        currentRoad.setText(naviinfo.getCurrentRoadName());
        distance.setText(naviinfo.getCurStepRetainDistance() + "米");
        Log.e("TAG", "当前路段剩余距离:" + naviinfo.getCurStepRetainDistance() + "\n" + "导航转向图标" + naviinfo.getIconType() +
                "\n" + "自车所在小路段:" + naviinfo.getCurLink() + "\n" + "当前位置前一个形状点索引:" + naviinfo.getCurPoint() + "\n" +
                "当前大路段索引:" + naviinfo.getCurStep() + "当前路段剩余时间:" + naviinfo.getCurStepRetainTime());
        //time.setText(naviinfo.getCurStepRetainTime());
    }

    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {

    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }


    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {
        Log.e("导航界面", "导航页面加载成功");
        Log.e("导航界面", "请不要使用AMapNaviView.getMap().setOnMapLoadedListener();会overwrite导航SDK内部画线逻辑");
    }

    @Override
    public void onCalculateRouteSuccess() {
        super.onCalculateRouteSuccess();
        aMapNavi.startNavi(NaviType.EMULATOR);
    }

    @Override
    public void onInitNaviSuccess() {
        super.onInitNaviSuccess();
        aMapNavi.calculateWalkRoute(new NaviLatLng(Latitude, Longitude), new NaviLatLng(lastLatitude, lastLongitude));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAMapNaviView.onPause();
        mTtsManager.stopSpeaking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAMapNaviView.onDestroy();
        aMapNavi.stopNavi();
        aMapNavi.destroy();
        mTtsManager.destroy();
    }

}

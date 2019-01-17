package com.LingLing.SapientialTravel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
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
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.unity3d.player.UnityPlayerActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HPA on 2017/7/31.
 */

public class UnityActivity extends UnityPlayerActivity implements PoiSearch.OnPoiSearchListener {
    public AMapLocationClient mLocationClient = null;
    public AMapLocationClientOption mLocationOption = null;
    private String LocationInfo;
    private String strRerurnInfo;
    private String NavInfo0;
    private String NavInfo1;
    private String NavInfo2;
    private String NavInfo3;
    private String NavInfo4;
    private String NavInfo5;
    private String NavInfo6;
    private String NavInfo7;
    private String NavInfo8;
    private String orientation;

    private PoiSearch.Query query;
    private PoiResult poir;
    private double Latitude;
    private double Longitude;
    private boolean bolIsPoi = false;
    private double testLatitude;
    private double testLongitude;
    private String testPlace = "西安灵境科技有限公司技术部";
    private float testDistance;
    private String key;
    private String testValue;
    private String info;
    private List<Infomation> infomations = new ArrayList<>();
    public static final String BUY = "http://keji.lingjing.com/zhl/api/app/GetBuyTicketCata";//获取线上可购买门票列表

    public static String number;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestByHttpGet();
    }

    private void requestByHttpGet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpGet get = new HttpGet(BUY);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = null;
                try {
                    response = client.execute(get);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (response != null) {
                    //判断请求是否成功
                    if (response.getStatusLine().getStatusCode() == 200) {
                        //返回数据
                        String result = null;
                        try {
                            result = EntityUtils.toString(response.getEntity(), "UTF-8");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.e("TAG", "返回结果==" + result);
                        try {
                            JSONObject json = new JSONObject(result);
                            JSONArray array = json.getJSONArray("detail");
                            Log.e("TAG", "数组长度：" + array.length());
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                Infomation infos = new Infomation();
                                infos.id = object.getInt("id");
                                infos.name = object.getString("name");
                                infos.useTimeType = object.getString("usetimetype");
                                infos.useStartTime = object.getString("usestarttime");
                                infos.useEndTime = object.getString("useendtime");
                                infos.useTimeHour = object.getInt("usetimehour");
                                infos.conveStartTime = object.getString("convestarttime");
                                infos.conveEndTime = object.getString("conveendtime");
                                infos.money = object.getInt("money");
                                infos.surplusCount = object.getInt("surpluscount");
                                infos.info = object.getString("info");
                                infos.remark = object.getString("remark");
                                infos.type = object.getString("type");
                                infos.projName = object.getString("projname");
                                infos.isImg = object.getBoolean("isimg");
                                Log.e("TAG", "门票ID=" + infos.id + "\n" + "门票名称=" + infos.name + "\n" + "门票使用时间模式：" + infos.useTimeType + "\n" + "门票使用起始时间：" + infos.useStartTime + "\n"
                                        + "门票使用结束时间:" + infos.useEndTime + "\n" + "门票购买后的有效时间:" + infos.useTimeHour + "\n" + "门票兑换起始时间：" + infos.conveStartTime + "\n"
                                        + "门票兑换结束时间:" + infos.conveEndTime + "\n" + "门票价格：" + infos.money + "\n" + "门票剩余可兑换数量：" + infos.surplusCount + "\n"
                                        + "门票信息:" + infos.info + "\n" + "门票备注:" + infos.remark + "\n" + "门票类型:" + infos.type + "\n" + "门票使用的景点名称:" + infos.projName + "\n" + "是否上传展示图:" + infos.isImg
                                );
                                infomations.add(infos);
                            }
                        } catch (JSONException e) {
                            Log.e("TAG", "错误:" + e.toString());
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(UnityActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).start();
    }

    //吐司显示地位信息
    public void ShowToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startLocation();
                bolIsPoi = true;
                Toast.makeText(getApplicationContext(), LocationInfo, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //保存图片
    public void Save(byte[] bytes, String path, String name) {
        Toast.makeText(this, "字节数组为空", Toast.LENGTH_SHORT).show();
        if (bytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            File file = new File(path, name);
            if (file.exists()) {
                file.delete();
            }
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
                outputStream.flush();
                outputStream.close();
                Toast.makeText(this, "已经保存了", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //判断是否联网
    public boolean ConnectNet() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                //网络连接
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        final String str = "网络不可用";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            }
        });
        return false;
    }

    //跳转到2D地图界面
    public void OpenMap() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //跳转到查询界面
    public void FindMap() {
        Intent intent = new Intent(this, FindActivity.class);
        startActivity(intent);
    }

    //调到移动支付界面
    public void Pay(String name, String pwd, String token) {

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("infomations", (ArrayList<? extends Parcelable>) infomations);
        bundle.putString("name", name);
        bundle.putString("pwd", pwd);
        bundle.putString("token", token);
        Log.e("TAG", "infomations长度：" + infomations.size());
        Intent intent = new Intent(this, BuyActivity.class);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }

    //微信登陆
    public void Login(String num) {
        number = num;
        IWXAPI api = WXAPIFactory.createWXAPI(this, "wx5ed5b0f7415d95b7",true);
        api.registerApp("wx5ed5b0f7415d95b7");//注册api
        if (!api.isWXAppInstalled()) {
            Toast.makeText(this, "未安装微信", Toast.LENGTH_SHORT).show();
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        api.sendReq(req);
        Log.e("TAG", "授权微信登陆");
    }

    //获取定位信息
    public String GetInfo() {

        startLocation();
        this.bolIsPoi = true;
        return this.LocationInfo;
    }

    //获取定位纬度
    public double GetLocLat() {
        startLocation();
        this.bolIsPoi = true;
        return this.Latitude;
    }

    //获取定位经度
    public double GetLocLon() {
        startLocation();
        this.bolIsPoi = true;
        return this.Longitude;
    }

    //获取周边POI信息
    public String GetPoi(String content, String val, int index) {
        key = content;
        startLocation();
        search(content, val, index);
        return this.strRerurnInfo;
    }

    //获取导航信息
    public String GetNav(double latitude, double longitude) {
        startLocation();
        navi(latitude, longitude);
        return info;
    }

    //获取起始导航初始方向
    public String GetOrien() {
        return orientation;
    }

    //获取两点距离
    public double GetDistance(double latitude, double longitude, double testLatitude, double testLongitude) {
        return AMapUtils.calculateLineDistance(new LatLng(latitude, longitude), new LatLng(testLatitude, testLongitude));
    }

    //搜索周边建筑物信息
    public void search(String content, String val, int index) {
        if (this.bolIsPoi) {
            if (content == null) {
                Toast.makeText(this, "输入为空", Toast.LENGTH_SHORT).show();
            } else {
                this.query = new PoiSearch.Query(content, val, "");
                this.query.setPageSize(30);
                this.query.setPageNum(index);
                PoiSearch poiSearch = new PoiSearch(this, this.query);
                if ((this.Latitude != 0.0D) && (this.Longitude != 0.0D)) {
                    poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(this.Latitude, this.Longitude), 1000));
                    poiSearch.setOnPoiSearchListener(this);
                    poiSearch.searchPOIAsyn();
                } else {
                    Toast.makeText(this, "定位失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //开启定位
    private void startLocation() {
        this.mLocationClient = new AMapLocationClient(getApplicationContext());
        this.mLocationClient.setLocationListener(this.mLocationListener);
        this.mLocationOption = new AMapLocationClientOption();
        this.mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        this.mLocationOption.setInterval(2000L);
        this.mLocationClient.setLocationOption(this.mLocationOption);
        this.mLocationClient.startLocation();
    }

    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (location != null) {
                if (location.getErrorCode() == 0) {
                    //获取坐标信息
                    Latitude = location.getLatitude();
                    Longitude = location.getLongitude();

                    StringBuffer sb = new StringBuffer(256);
                    sb.append("时间:" + location.getTime());
                    sb.append("\n纬度:" + location.getLatitude());
                    sb.append("\n经度:" + location.getLongitude());
                    sb.append("\n精度:" + location.getAccuracy());
                    sb.append("\n地址:" + location.getAddress());
                    sb.append("\n国家信息:" + location.getCountry());
                    sb.append("\n省信息:" + location.getProvince());
                    sb.append("\n城市信息:" + location.getCity());
                    sb.append("\n城区信息:" + location.getDistrict());
                    sb.append("\n街道信息:" + location.getStreet());
                    sb.append("\n定位点AOI信息:" + location.getAoiName());
                    LocationInfo = sb.toString();
                } else {
                    Log.e("AmapError", "location Error, ErrCode:"
                            + location.getErrorCode() + ", errInfo:"
                            + location.getErrorInfo());
                }
            }
        }
    };

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        this.bolIsPoi = false;
        System.out.println("Result" + (poiResult.getPois().get(0)).getLatLonPoint());
        System.out.println("Code" + i);
        this.poir = poiResult;
        StringBuffer sb = new StringBuffer(256);
        if (this.poir.getPois().size() < 11) {
            for (int j = 0; j < this.poir.getPois().size(); j++) {
                sb.append("\n名字:");
                sb.append((this.poir.getPois().get(j)).getTitle());
                sb.append("\n>地址:");
                sb.append((this.poir.getPois().get(j)).getSnippet());
                sb.append("\n>距离:");
                sb.append((this.poir.getPois().get(j)).getDistance());
                sb.append("\n>经度:");
                sb.append(this.poir.getPois().get(j).getLatLonPoint().getLongitude());
                sb.append("\n>纬度:");
                sb.append(this.poir.getPois().get(j).getLatLonPoint().getLatitude());
                sb.append("\n>评分:");
                sb.append(this.poir.getPois().get(j).getPoiExtension().getmRating());
                testLatitude = this.poir.getPois().get(0).getLatLonPoint().getLatitude() + 0.0000004;//自定义的poi建筑物的纬度
                testLongitude = this.poir.getPois().get(0).getLatLonPoint().getLongitude() + 0.000005;//自定义的poi建筑物的经度
                testValue = this.poir.getPois().get(0).getPoiExtension().getmRating();
                testDistance = AMapUtils.calculateLineDistance(new LatLng(Latitude, Longitude), new LatLng(testLatitude, testLongitude));
            }
        } else {
            for (int j = 0; j < 11; j++) {
                sb.append("\n名字:");
                sb.append((this.poir.getPois().get(j)).getTitle());
                sb.append("\n>地址:");
                sb.append((this.poir.getPois().get(j)).getSnippet());
                sb.append("\n>距离:");
                sb.append((this.poir.getPois().get(j)).getDistance());
                sb.append("\n>经度:");
                sb.append(this.poir.getPois().get(j).getLatLonPoint().getLongitude());
                sb.append("\n>纬度:");
                sb.append(this.poir.getPois().get(j).getLatLonPoint().getLatitude());
                sb.append("\n>评分:");
                sb.append(this.poir.getPois().get(j).getPoiExtension().getmRating());
                testLatitude = this.poir.getPois().get(0).getLatLonPoint().getLatitude() + 0.0000004;//自定义的poi建筑物的纬度
                testLongitude = this.poir.getPois().get(0).getLatLonPoint().getLongitude() + 0.000005;//自定义的poi建筑物的经度
                testValue = this.poir.getPois().get(0).getPoiExtension().getmRating();
                testDistance = AMapUtils.calculateLineDistance(new LatLng(Latitude, Longitude), new LatLng(testLatitude, testLongitude));
            }
        }
        if (key.equals("旺座现代城G座15层")) {
            this.strRerurnInfo = ("\n名字:" + testPlace + "\n>地址:" + "科技五路橡树星座B座11层" + "\n>距离:" + testDistance + "\n>经度:" + testLongitude + "\n>纬度:" + testLatitude + "\n>评分:" + testValue) + sb.toString();
        } else {
            this.strRerurnInfo = sb.toString();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    //获取导航数据信息
    private void navi(double lastLatitude, double lastLongitude) {
        RouteSearch routeSearch = new RouteSearch(this);
        Log.e("TAG", "2");
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(new LatLonPoint(Latitude, Longitude), new LatLonPoint(lastLatitude, lastLongitude));
        Log.e("TAG", "Latitude==" + Latitude + "----" + "Longitude" + Longitude + "\n" + "lastLatitude" + lastLatitude + "----" + "lastLongitude" + lastLongitude);
        //构造路径规划的起点和终点坐标
        RouteSearch.WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(fromAndTo);
        Log.e("TAG", "walkRouteQuery==" + walkRouteQuery);
        routeSearch.calculateWalkRouteAsyn(walkRouteQuery);
        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
                List<WalkPath> paths = walkRouteResult.getPaths();
                WalkPath walkPath = paths.get(0);
                List<WalkStep> steps = walkPath.getSteps();
                orientation = steps.get(0).getOrientation();
                NavInfo0 = steps.get(0).getInstruction();
                NavInfo1 = steps.get(Math.min(1, steps.size() - 1)).getInstruction();
                if (NavInfo1.contains("到达目的地")) {
                    info = NavInfo0 + "," + NavInfo1;
                    return;
                }
                NavInfo2 = steps.get(Math.min(2, steps.size() - 1)).getInstruction();
                if (NavInfo2.contains("到达目的地")) {
                    info = NavInfo0 + "," + NavInfo1 + "," + NavInfo2;
                    return;
                }
                NavInfo3 = steps.get(Math.min(3, steps.size() - 1)).getInstruction();
                if (NavInfo3.contains("到达目的地")) {
                    info = NavInfo0 + "," + NavInfo1 + "," + NavInfo2 + "," + NavInfo3;
                    info = NavInfo0 + "," + NavInfo1 + "," + NavInfo2 + "," + NavInfo3;
                    return;
                }
                NavInfo4 = steps.get(Math.min(4, steps.size() - 1)).getInstruction();
                if (NavInfo4.contains("到达目的地")) {
                    info = NavInfo0 + "," + NavInfo1 + "," + NavInfo2 + "," + NavInfo3 + "," + NavInfo4;
                    return;
                }
                NavInfo5 = steps.get(Math.min(5, steps.size() - 1)).getInstruction();
                if (NavInfo5.contains("到达目的地")) {
                    info = NavInfo0 + "," + NavInfo1 + "," + NavInfo2 + "," + NavInfo3 + "," + NavInfo4 + "," + NavInfo5;
                    return;
                }
                NavInfo6 = steps.get(Math.min(6, steps.size() - 1)).getInstruction();
                if (NavInfo6.contains("到达目的地")) {
                    info = NavInfo0 + "," + NavInfo1 + "," + NavInfo2 + "," + NavInfo3 + "," + NavInfo4 + "," + NavInfo5 + "," + NavInfo6;
                    return;
                }
                NavInfo7 = steps.get(Math.min(7, steps.size() - 1)).getInstruction();
                if (NavInfo7.contains("到达目的地")) {
                    info = NavInfo0 + "," + NavInfo1 + "," + NavInfo2 + "," + NavInfo3 + "," + NavInfo4 + "," + NavInfo5 + "," + NavInfo6 + "," + NavInfo7;
                    return;
                }
                NavInfo8 = steps.get(Math.min(8, steps.size() - 1)).getInstruction();
                if (NavInfo2.contains("到达目的地")) {
                    info = NavInfo0 + "," + NavInfo1 + "," + NavInfo2 + "," + NavInfo3 + "," + NavInfo4 + "," + NavInfo5 + "," + NavInfo6 + "," + NavInfo7 + "," + NavInfo8;
                    return;
                }
            }

            @Override
            public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

            }
        });
    }

    @Override
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == KeyEvent.KEYCODE_HOME) {
            Log.e("TAG", "按下了home键");
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
            Log.e("TAG", "杀死进程");
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override
    protected void onStart() {

        super.onStart();
    }
}

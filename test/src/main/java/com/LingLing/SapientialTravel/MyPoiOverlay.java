package com.LingLing.SapientialTravel;

import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HPA on 2017/7/17.
 */

public class MyPoiOverlay {
    private String keyword;
    private AMap mAMap;
    private List<PoiItem> mPois;
    private ArrayList<Marker> mPoiMarks = new ArrayList<>();

    //构造函数，传进来的是amap对象和查询到的结果itmes  mPois
    public MyPoiOverlay(AMap aMap, List<PoiItem> pois, String key) {
        mAMap = aMap;
        mPois = pois;
        keyword = key;
    }

    //增加Marker到地图
    public void addToMap() {
        for (int i = 0; i < mPois.size(); i++) {
            Marker marker = mAMap.addMarker(getMarkerOptions(i));
            PoiItem item = mPois.get(i);
            marker.setObject(item);
            mPoiMarks.add(marker);
        }
    }

    //保留指定的marker
    public void save(String name) {
        mPoiMarks.clear();
        removeFromMap();
        Log.e("所有的poi", "mPoiMarks=" + mPoiMarks + "\n" + "mPois=" + mPois);
        for (int i = 0; i < mPois.size(); i++) {
            Marker marker = mAMap.addMarker(getMarkerOptions(i));
            PoiItem item = mPois.get(i);
            if (name.equals(item.getTitle())) {
                marker.setObject(item);
                mPoiMarks.add(marker);
                Log.e("现在的poi点", "mPoiMarks=" + mPoiMarks);
            }

        }
    }

    //移除所有的marker
    public void removeFromMap() {
        for (Marker marker : mPoiMarks) {
            marker.remove();
        }
    }

    //移动镜头到当前的视角
    public void zoomToSpan() {
        if (mPois != null && mPois.size() > 0) {
            if (mAMap == null) return;
            LatLngBounds bounds = getLatLngBounds();
            //瞬间移动到目标位置
            mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        }
    }

    private MarkerOptions getMarkerOptions(int index) {
        return new MarkerOptions().position(new LatLng(mPois.get(index).getLatLonPoint().getLatitude(), mPois.get(index).
                getLatLonPoint().getLongitude())).title(mPois.get(index).getTitle()).snippet(mPois.get(index).getSnippet()).icon(getBitmapDescriptor());
    }

    private LatLngBounds getLatLngBounds() {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (int i = 0; i < mPois.size(); i++) {
            builder.include(new LatLng(mPois.get(i).getLatLonPoint().getLatitude(), mPois.get(i).getLatLonPoint().getLongitude()));
        }
        return builder.build();
    }

    //获取位置--第几个角标就是第几个poi
    public int getPoiIndex(Marker marker) {
        for (int i = 0; i < mPoiMarks.size(); i++) {
            if (mPoiMarks.get(i).equals(marker)) {
                return i;
            }
        }
        return -1;
    }

    public PoiItem getPoiItem(int index) {
        if (index < 0 || index >= mPois.size()) {
            return null;
        }
        return mPois.get(index);
    }

    protected BitmapDescriptor getBitmapDescriptor() {
        if (keyword.equals("景点")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.marker_scenic_spots);
        }
        if (keyword.equals("木塔寺公园")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.marker_play);
        }
        if (keyword.equals("舞蹈")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.marker_show);
        }
        if (keyword.equals("酒店")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.marker_hotel);
        }
        if (keyword.equals("餐饮")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.marker_food);
        }
        if (keyword.equals("休闲")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.marker_relaxation);
        }
        if (keyword.equals("超市")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.marker_shopping);
        }
        if (keyword.equals("公交车站")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.marker_electrocar);
        }
        if (keyword.equals("停车场")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.marker_stop);
        }
        return BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher);
    }
}

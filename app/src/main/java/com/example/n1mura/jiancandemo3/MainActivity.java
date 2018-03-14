package com.example.n1mura.jiancandemo3;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AMapLocationListener, View.OnClickListener, AMap.OnMapClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter, AMap.OnMarkerClickListener, PoiSearch.OnPoiSearchListener {
//public class MainActivity extends Activity {

    private MapView mMapView = null;
    private AMap aMap = null;
    private MyLocationStyle myLocationStyle = null;
    private UiSettings uiSettings = null;
    private AMapLocationClient mapLocationClient = null;
    private AMapLocationClientOption mapLocationClientOption = null;

    private PoiResult poiResult;//poi结果
    private int currentPage = 0; //当前页面
    private PoiSearch.Query query;//poi查询条件类
//    private LatLonPoint lp = new LatLonPoint(31.22944, 121.402913);//121.409513 31.223544
    private LatLonPoint lp = null;
    private Marker locationMarker;//选择的点
    private Marker detailMarker;
    private Marker mlastMarker;
    private PoiSearch poiSearch;
    private myPoiOverlay poiOverlay;//poi图层
    private List<PoiItem> poiItems;//poi数据

    private RelativeLayout mPoiDetail;
    private TextView mPoiName, mPoiAddress;
    private String keyWord = "";
    private EditText mSearchText;
    private int turn = 0;

    private int[] markers = {
            R.drawable.poi_marker_1,
            R.drawable.poi_marker_2,
            R.drawable.poi_marker_3,
            R.drawable.poi_marker_4,
            R.drawable.poi_marker_5,
            R.drawable.poi_marker_6,
            R.drawable.poi_marker_7,
            R.drawable.poi_marker_8,
            R.drawable.poi_marker_9,
            R.drawable.poi_marker_10,
    };

    private LatLonPoint[] latLonPoints = {
            new LatLonPoint(31.22944, 121.402913),
            new LatLonPoint(30.22944, 121.402913),
            new LatLonPoint(29.22944, 121.402913),
            new LatLonPoint(28.22944, 121.402913),
            new LatLonPoint(27.22944, 121.402913),
    };

    private class myPoiOverlay {
        private AMap mamap;
        private List<PoiItem> mPois;
        private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();

        public myPoiOverlay(AMap aMap, List<PoiItem> pois) {
            mamap = aMap;
            mPois = pois;
        }

        public void addToMap() {
            for (int i = 0; i < mPois.size(); i++) {
                Marker marker = mamap.addMarker(getMarkerOptions(i));
                PoiItem item = mPois.get(i);
                marker.setObject(item);
                mPoiMarks.add(marker);
            }
        }

        public void removeFromMap() {
            for (Marker marker : mPoiMarks) {
                marker.remove();
            }
        }

        public void zoomToSpan() {
            if (mPois != null && mPois.size() > 0) {
                if (mamap == null) {
                    return;
                }
                LatLngBounds bounds = getLatLngBounds();
                mamap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                mamap.moveCamera(CameraUpdateFactory.changeTilt(45));
                mamap.moveCamera(CameraUpdateFactory.zoomIn());
            }
        }

        private LatLngBounds getLatLngBounds() {
            LatLngBounds.Builder builder = LatLngBounds.builder();
            for (int i = 0; i < mPois.size(); i++) {
                builder.include(new LatLng(mPois.get(i).getLatLonPoint().getLatitude(), mPois.get(i).getLatLonPoint().getLongitude()));
            }
            return builder.build();
        }

        private MarkerOptions getMarkerOptions(int index) {
            return new MarkerOptions()
                    .position(
                            new LatLng(mPois.get(index).getLatLonPoint().getLatitude(), mPois.get(index).getLatLonPoint().getLongitude()))
                    .title(getTitle(index)).snippet(getSnippet(index))
                    .icon(getBitmapDescriptor(index));
        }

        protected String getTitle(int index) {
            return mPois.get(index).getTitle();
        }

        protected String getSnippet(int index) {
            return mPois.get(index).getSnippet();
        }

        public int getPoiIndex(Marker marker) {
            for (int i = 0; i < mPoiMarks.size(); i++){
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

        protected BitmapDescriptor getBitmapDescriptor(int arg0) {
            if (arg0 < 10) {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(getResources(), markers[arg0])
                );
                return icon;
            } else {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(getResources(), R.drawable.marker_other_highlight)
                );
                return  icon;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_main);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        init();
    }

    /*
    初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
//            aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
            aMap.setOnMapClickListener(this);
            aMap.setOnMarkerClickListener(this);
            aMap.setOnInfoWindowClickListener(this);
            aMap.setInfoWindowAdapter(this);
            aMap.showIndoorMap(true);

            TextView searchButton = (TextView) findViewById(R.id.btn_search);
            searchButton.setOnClickListener(this);
//            doSearchQuery();
//            locationMarker = aMap.addMarker(new MarkerOptions()
//                    .anchor(0.5f, 0.5f)
//                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.point4)))
//                    .position(new LatLng(lp.getLatitude(), lp.getLongitude())));
//            locationMarker.showInfoWindow();
        }

        setLocation();
        setup();
//        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lp.getLatitude(), lp.getLongitude()), 18));

//        aMap.showIndoorMap(true);//true：显示室内地图；false：不显示；
//        setLocation();
//        setUI();
//        setGestures();
    }

    private void setup() {
        mPoiDetail = (RelativeLayout) findViewById(R.id.poi_detail);
        mPoiDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ShopActivity.class));
            }
        });
        mPoiName = (TextView) findViewById(R.id.poi_name);
        mPoiAddress = (TextView) findViewById(R.id.poi_address);
//        mSearchText = (EditText) findViewById(R.id.input_edittext);

//        doSearchQuery();
    }

    /*
    开始poi搜索
     */
    protected void doSearchQuery() {
//        keyWord = mSearchText.getText().toString().trim();
        keyWord = "餐饮";
        currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", "");
        query.setPageSize(50);
        query.setPageNum(currentPage);

        if (lp != null) {
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude()), 1000, true));
//            poiSearch.setBound(new PoiSearch.SearchBound(lp, 1000, true));
            poiSearch.searchPOIAsyn();
        }
    }

    protected void setGestures() {
        uiSettings.setZoomGesturesEnabled(true);//缩放手势
        uiSettings.setScrollGesturesEnabled(true);//滑动手势
        uiSettings.setRotateGesturesEnabled(true);//旋转手势
        uiSettings.setTiltGesturesEnabled(true);//倾斜手势
    }

    protected void setUI() {
        uiSettings = aMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);//控制缩放按钮是否显示
        uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);//设置缩放按钮位置
        uiSettings.getZoomPosition();//获取缩放按钮位置
        uiSettings.setCompassEnabled(true);//控制指南针是否显示
        uiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示
        uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_CENTER);//设置logo位置
    }

    protected void setLocation() {
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        myLocationStyle.interval(500); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        mapLocationClient = new AMapLocationClient(getApplicationContext());
        mapLocationClient.setLocationListener(aMapLocationListener);

//        Log.e("aaa", "shabi");
//        Toast.makeText(getApplicationContext(), "shabi", Toast.LENGTH_LONG);

        mapLocationClientOption = new AMapLocationClientOption();
        mapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mapLocationClientOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        mapLocationClientOption.setMockEnable(true);

        if (mapLocationClient != null) {
            mapLocationClient.setLocationOption(mapLocationClientOption);
            mapLocationClient.stopLocation();
            mapLocationClient.startLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapLocationClient.stopLocation();
        mapLocationClient.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        whetherToshowDetailInfo(false);
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    public AMapLocationListener aMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    lp = new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                    Log.e("lp纬度", "latitude:" + lp.getLatitude());
                    Log.e("纬度", "latitude:" + aMapLocation.getLatitude());
                    Log.e("定位来源", "locationType:" + aMapLocation.getLocationType());
//                    Toast.makeText(getApplicationContext(), "lp" + lp.getLatitude() + " weidu " + aMapLocation.getLatitude(), Toast.LENGTH_LONG);
//                    lp = latLonPoints[turn%5];
//                    Log.e("lp", lp.getLatitude()+" "+lp.getLongitude());
//                    turn++;
//                    doSearchQuery();
//
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(new LatLng(lp.getLatitude(), lp.getLongitude()));
//                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_janne)));
//                    markerOptions.setFlat(true);
                } else {
                    Log.e("AmapError", "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo" + aMapLocation.getErrorInfo());
                }
            }
        }
    };

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onPoiSearched(PoiResult result, int rcode) {
        if (rcode == 1000) {
            if (result != null && result.getQuery() != null) {
                if (result.getQuery().equals(query)) {
                    poiResult = result;
                    poiItems = poiResult.getPois();
                    List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();
                    if (poiItems != null && poiItems.size() > 0) {
                        whetherToshowDetailInfo(false);
                        if (mlastMarker != null) {
                            resetlastmarker();
                        }
                        if (poiOverlay != null) {
                            poiOverlay.removeFromMap();
                        }
                        aMap.clear();
                        poiOverlay = new myPoiOverlay(aMap, poiItems);
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();

//                        aMap.addMarker(new MarkerOptions()
//                                .anchor(0.5f, 0.5f)
//                                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.point4)))
//                                .position(new LatLng(lp.getLatitude(), lp.getLongitude())));
                        aMap.addCircle(new CircleOptions()
//                                .center(new LatLng(lp.getLatitude(), lp.getLongitude())).radius(10)
                                .center(new LatLng(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude())).radius(1000)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.argb(50, 1, 1, 1))
                                .strokeWidth(2));
                    } else if (suggestionCities != null && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        Toast.makeText(getApplicationContext(), "对不起，没有搜索到相关数据", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "对不起，没有搜索到相关数据", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), rcode+"", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getObject() != null) {
            whetherToshowDetailInfo(true);
            try {
                PoiItem mCurrentPoi = (PoiItem) marker.getObject();
                if (mlastMarker == null) {
                    mlastMarker = marker;
                } else {
                    resetlastmarker();
                    mlastMarker = marker;
                }
                detailMarker = marker;
                detailMarker.setIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.poi_marker_pressed)));
                setPoiItemDisplayContent(mCurrentPoi);
            } catch (Exception e) {
                //TODO: handle exception
            }
        } else {
            whetherToshowDetailInfo(false);
            resetlastmarker();
        }
        return true;
    }

    private void setPoiItemDisplayContent(final PoiItem item) {
        mPoiName.setText(item.getTitle());
        mPoiAddress.setText(item.getSnippet() + item.getDistance());
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_search:
                doSearchQuery();
                break;
            default:
                break;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        whetherToshowDetailInfo(false);
        if (mlastMarker != null) {
            resetlastmarker();
        }
    }

    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName()
                    + "城市区号:" + cities.get(i).getCityCode()
                    + "城市编码:" + cities.get(i).getAdCode() + "\n";
        }
        Toast.makeText(getApplicationContext(), infomation, Toast.LENGTH_LONG).show();
    }

    private void resetlastmarker() {
        int index = poiOverlay.getPoiIndex(mlastMarker);
        if (index < 10) {
            mlastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), markers[index])));
        } else {
            mlastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marker_other_highlight)));
        }
        mlastMarker = null;
    }

    private void whetherToshowDetailInfo(boolean isToShow) {
        if (isToShow) {
            mPoiDetail.setVisibility(View.VISIBLE);
        } else {
            mPoiDetail.setVisibility(View.GONE);
        }
    }
}

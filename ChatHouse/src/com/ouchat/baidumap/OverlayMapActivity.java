package com.ouchat.baidumap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.ouchat.R;

/**
 * 演示覆盖物的用法
 */
public class OverlayMapActivity extends Activity {
	/**
	 * 构造广播监听类，监听 SDK key 验证以及网络异常广播
	 */
	public class SDKReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			Log.d("ouou", "action: " + s);
			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				Log.d("ouou","key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置");
			} else if (s
					.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				Log.d("ouou","网络出错");
			}
		}
	}

	private SDKReceiver mReceiver;
	/**
	 * MapView 是地图主控件
	 */
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private Marker mMarkerA;
	private Marker mMarkerB;
	private Marker mMarkerC;
	private Marker mMarkerD;
	private InfoWindow mInfoWindow;
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	boolean isFirstLoc = true;// 是否首次定位
	// 初始化全局 bitmap 信息，不用时及时 recycle
	BitmapDescriptor bdA;
//	BitmapDescriptor bdB = BitmapDescriptorFactory
//			.fromResource(R.drawable.icon_markb);
//	BitmapDescriptor bdC = BitmapDescriptorFactory
//			.fromResource(R.drawable.icon_markc);
//	BitmapDescriptor bdD = BitmapDescriptorFactory
//			.fromResource(R.drawable.icon_markd);
//	BitmapDescriptor bd = BitmapDescriptorFactory
//			.fromResource(R.drawable.icon_markd);
//	BitmapDescriptor bdGround = BitmapDescriptorFactory
//			.fromResource(R.drawable.icon_markd);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overlay);
		mCurrentMode = LocationMode.NORMAL;
		// 注册 SDK 广播监听者
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mReceiver = new SDKReceiver();
		registerReceiver(mReceiver, iFilter);
		
		LinearLayout testLayout = (LinearLayout) LayoutInflater.from(this)
				.inflate(R.layout.testlayout, null);

		Bitmap bitmap = getViewBitmap(testLayout);
		bdA = BitmapDescriptorFactory.fromBitmap(bitmap);

		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
		mBaiduMap.setMapStatus(msu);
		initOverlay();
		
		// 开启定位图层
				mBaiduMap.setMyLocationEnabled(true);
				// 定位初始化
				mLocClient = new LocationClient(this);
				mLocClient.registerLocationListener(myListener);
				LocationClientOption option = new LocationClientOption();
				option.setOpenGps(true);// 打开gps
				option.setCoorType("bd09ll"); // 设置坐标类型
				option.setScanSpan(1000);
				mLocClient.setLocOption(option);
				mLocClient.start();
		
//		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
//			public boolean onMarkerClick(final Marker marker) {
//				Button button = new Button(getApplicationContext());
//				button.setBackgroundResource(R.drawable.popup);
//				OnInfoWindowClickListener listener = null;
//				if (marker == mMarkerA || marker == mMarkerD) {
//					button.setText("更改位置");
//					listener = new OnInfoWindowClickListener() {
//						public void onInfoWindowClick() {
//							LatLng ll = marker.getPosition();
//							LatLng llNew = new LatLng(ll.latitude + 0.005,
//									ll.longitude + 0.005);
//							marker.setPosition(llNew);
//							mBaiduMap.hideInfoWindow();
//						}
//					};
//					LatLng ll = marker.getPosition();
//					mInfoWindow = new InfoWindow(BitmapDescriptorFactory
//							.fromView(button), ll, -47, listener);
//					mBaiduMap.showInfoWindow(mInfoWindow);
//				} else if (marker == mMarkerB) {
//					button.setText("更改图标");
//					button.setOnClickListener(new OnClickListener() {
//						public void onClick(View v) {
//							marker.setIcon(bd);
//							mBaiduMap.hideInfoWindow();
//						}
//					});
//					LatLng ll = marker.getPosition();
//					mInfoWindow = new InfoWindow(button, ll, -47);
//					mBaiduMap.showInfoWindow(mInfoWindow);
//				} else if (marker == mMarkerC) {
//					button.setText("删除");
//					button.setOnClickListener(new OnClickListener() {
//						public void onClick(View v) {
//							marker.remove();
//							mBaiduMap.hideInfoWindow();
//						}
//					});
//					LatLng ll = marker.getPosition();
//					mInfoWindow = new InfoWindow(button, ll, -47);
//					mBaiduMap.showInfoWindow(mInfoWindow);
//				}
//				return true;
//			}
//		});
	}
	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
		
			if (location == null || mMapView == null)
				return;
			
			Log.d("ouou", "location:"+location);
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
	private Bitmap getViewBitmap(View addViewContent) {
		addViewContent.setDrawingCacheEnabled(true);
		addViewContent.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		addViewContent.layout(0, 0, addViewContent.getMeasuredWidth(),
				addViewContent.getMeasuredHeight());

		addViewContent.buildDrawingCache();
		Bitmap cacheBitmap = addViewContent.getDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
		return bitmap;
	}

	public void initOverlay() {
		// add marker overlay
		LatLng llA = new LatLng(39.963175, 116.400244);
		LatLng llB = new LatLng(39.942821, 116.369199);
		LatLng llC = new LatLng(39.939723, 116.425541);
		LatLng llD = new LatLng(39.906965, 116.401394);

		OverlayOptions ooA = new MarkerOptions().position(llA).icon(bdA)
				.zIndex(9).draggable(true);
		mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
//		OverlayOptions ooB = new MarkerOptions().position(llB).icon(bdB)
//				.zIndex(5);
//		mMarkerB = (Marker) (mBaiduMap.addOverlay(ooB));
//		OverlayOptions ooC = new MarkerOptions().position(llC).icon(bdC)
//				.perspective(false).anchor(0.5f, 0.5f).rotate(30).zIndex(7);
//		mMarkerC = (Marker) (mBaiduMap.addOverlay(ooC));
//		OverlayOptions ooD = new MarkerOptions().position(llD).icon(bdD)
//				.perspective(false).zIndex(7);
//		mMarkerD = (Marker) (mBaiduMap.addOverlay(ooD));
//
//		// add ground overlay
//		LatLng southwest = new LatLng(39.92235, 116.380338);
//		LatLng northeast = new LatLng(39.947246, 116.414977);
//		LatLngBounds bounds = new LatLngBounds.Builder().include(northeast)
//				.include(southwest).build();
//
//		OverlayOptions ooGround = new GroundOverlayOptions()
//				.positionFromBounds(bounds).image(bdGround).transparency(0.8f);
//		mBaiduMap.addOverlay(ooGround);
//
//		MapStatusUpdate u = MapStatusUpdateFactory
//				.newLatLng(bounds.getCenter());
//		mBaiduMap.setMapStatus(u);
//
//		mBaiduMap.setOnMarkerDragListener(new OnMarkerDragListener() {
//			public void onMarkerDrag(Marker marker) {
//			}
//
//			public void onMarkerDragEnd(Marker marker) {
//				Toast.makeText(
//						OverlayMapActivity.this,
//						"拖拽结束，新位置：" + marker.getPosition().latitude + ", "
//								+ marker.getPosition().longitude,
//						Toast.LENGTH_LONG).show();
//			}
//
//			public void onMarkerDragStart(Marker marker) {
//			}
//		});
	}

	/**
	 * 清除所有Overlay
	 * 
	 * @param view
	 */
	public void clearOverlay(View view) {
		mBaiduMap.clear();
	}

	/**
	 * 重新添加Overlay
	 * 
	 * @param view
	 */
	public void resetOverlay(View view) {
		clearOverlay(null);
		initOverlay();
	}

	@Override
	protected void onPause() {
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		mMapView.onResume();
		
		
		
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		
		// MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		mMapView.onDestroy();
		super.onDestroy();
		// 回收 bitmap 资源
		bdA.recycle();
//		bdB.recycle();
//		bdC.recycle();
//		bdD.recycle();
//		bd.recycle();
//		bdGround.recycle();
		// 取消监听 SDK 广播
		unregisterReceiver(mReceiver);
		
	}

}

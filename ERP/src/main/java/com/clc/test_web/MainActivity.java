package com.clc.test_web;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.clc.test_web.entiry.BaseJPushEntity;
import com.clc.test_web.util.DeviceUuidFactory;
import com.clc.test_web.util.SoapCallback;
import com.clc.test_web.util.SoapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.jpush.android.api.JPushInterface;

public class MainActivity extends Activity {

    public static boolean firstOpening = true;
    private static String[] titles = null;

    private BaseJPushEntity mJPushEntiry;

    public static final int MSG_WEBVIEW_CONSTRUCTOR = 1;
    public static final int MSG_WEBVIEW_POLLING = 2;

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // add constant here
    private static final int TBS_WEB = 0;
    private static final int FULL_SCREEN_VIDEO = 1;
    private static final int FILE_CHOOSER = 2;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // for view init
    private Context mContext = null;
    private SimpleAdapter gridAdapter;
    private GridView gridView;
    private ArrayList<HashMap<String, Object>> items;

    private static boolean main_initialized = false;

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    //声明LocationClient类
    public LocationClient mLocationClient = null;
    //注册监听函数
    private MyLocationListener myListener = new MyLocationListener();
    private String TAG = "MainActivity";

    // Activity OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_advanced);

        initData();
        getMyLocation();

        mContext = this;
        if (!main_initialized) {
            this.new_init();
        }

//        mTestHandler.sendEmptyMessageDelayed(MSG_OPEN_HOME, 500);

    }

    private void initData() {
        String registrationID = JPushInterface.getRegistrationID(this);
        String uuid = getDeviceUUID(null);
        Log.i("Tobin", "registrationID：" + registrationID);
        Log.i("Tobin", "获取UUID：" + DeviceUuidFactory.getInstance(this).getDeviceUuid());

        mJPushEntiry = new BaseJPushEntity();
        mJPushEntiry.id = "";
        mJPushEntiry.UserId = "";
        mJPushEntiry.CreateTime = "";
        mJPushEntiry.RegistrationId = registrationID;
        mJPushEntiry.uuid = uuid;

        JSONArray data = new JSONArray();
        JSONObject subitem = new JSONObject();
        //生成发送的源数据
        try {
            subitem.put("RegistrationId", registrationID);
            subitem.put("uuid", uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        data.put(subitem);
        String jsonString = subitem.toString();


        Map<String, String> map = new HashMap<>();
//        map.put("UserId", SpUtils.getUserData(mActivity).getId());
        map.put("baseJPushEntityJson", jsonString);

        new SoapUtils().Post(this,"InsertJpush",map, new SoapCallback() {
            @Override
            public void onError(String error) {

            }

            @Override
            public void onSuccess(String data) {

            }
        });
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    // Activity OnResume
    @Override
    protected void onResume() {
        this.new_init();

        // this.gridView.setAdapter(gridAdapter);
        super.onResume();
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // initiate new UI content
    private void new_init() {
        items = new ArrayList<HashMap<String, Object>>();
        this.gridView = (GridView) this.findViewById(R.id.item_grid);

        if (gridView == null)
            throw new IllegalArgumentException("the gridView is null");

        titles = getResources().getStringArray(R.array.index_titles);
        int[] iconResourse = {R.drawable.tbsweb, R.drawable.fullscreen,
                R.drawable.filechooser};

        HashMap<String, Object> item = null;
        // HashMap<String, ImageView> block = null;
        for (int i = 0; i < titles.length; i++) {
            item = new HashMap<String, Object>();
            item.put("title", titles[i]);
            item.put("icon", iconResourse[i]);

            items.add(item);
        }
        this.gridAdapter = new SimpleAdapter(this, items,
                R.layout.function_block, new String[]{"title", "icon"},
                new int[]{R.id.Item_text, R.id.Item_bt});
        if (null != this.gridView) {
            this.gridView.setAdapter(gridAdapter);
            this.gridAdapter.notifyDataSetChanged();
            this.gridView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> gridView, View view,
                                        int position, long id) {
                    Intent intent = null;
                    switch (position) {
                        case FILE_CHOOSER: {

                        }
                        break;
                        case FULL_SCREEN_VIDEO: {
                        }
                        break;

                        case TBS_WEB: {
                            intent = new Intent(MainActivity.this,
                                    BrowserActivity.class);
                            MainActivity.this.startActivity(intent);

                        }
                        break;

                    }

                }
            });

        }
        main_initialized = true;

    }

    // ///////////////////////////////////////////////////////////////////////////////////////////
    // Activity menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
//                this.tbsSuiteExit();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void tbsSuiteExit() {
        // exit TbsSuite?
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle("X5功能演示");
        dialog.setPositiveButton("OK", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Process.killProcess(Process.myPid());
            }
        });
        dialog.setMessage("quit now?");
        dialog.create().show();
    }

    public static final int MSG_OPEN_HOME = 2;
    private Handler mTestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_OPEN_HOME:
                    Intent intent = new Intent();
                    intent.putExtra("latitude",mJPushEntiry.latitude);
                    intent.putExtra("longitude",mJPushEntiry.longitude);
                    intent.setClass(MainActivity.this, BrowserActivity.class);
                    MainActivity.this.startActivity(intent);
                    ((Activity) MainActivity.this).overridePendingTransition(0, 0);
                    ((Activity) MainActivity.this).overridePendingTransition(0, 0);
                    finish();
                    break;
            }
            super.handleMessage(msg);
        }
    };
//160a3797c86071ef3dd
    //609179e0-5335-4573-a169-39b4aa87f496
//	535c5718-31dc-4b8d-afdb-2c9c28482104
//	275927b3-dcb0-4e4d-90c7-61d4633f6b57

    /**
     * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
     * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * targetSdkVersion 22 如果大于22增加动态权限管理
     * 获取设备唯一UUID
     */
    private String getDeviceUUID(Context mContext) {
        UUID uuid = DeviceUuidFactory.getInstance(this).getDeviceUuid();
        return "" + uuid;
    }


    /**
     * 用于发送UUID的线程
     */
    @SuppressLint("StaticFieldLeak")
    public class SendUUIDAsyncTask extends AsyncTask {
        private BaseJPushEntity baseJPushEntity;

        SendUUIDAsyncTask(BaseJPushEntity entiry) {
            baseJPushEntity = entiry;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
//			URL url = null;
//			try {
//				url = new URL(GlobalConstants.SEND_UUID_URL);
//				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//				conn.setConnectTimeout(5000);//
//				conn.setReadTimeout(10000);//
//				conn.setRequestMethod("POST");
//				conn.setDoInput(true);
//				conn.setDoOutput(true);
//				conn.connect();
//
//				JSONObject subitem = new JSONObject();
//				//生成发送的源数据
//				try {
//					subitem.put("RegistrationId", mRegistrationID);
//					subitem.put("uuid", mUUID);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//
//				String jsonString=subitem.toString();
//				conn.getOutputStream().write(jsonString.getBytes());
//
//				InputStream is = conn.getInputStream();
//				String result = readText(is);
//
//				JSONTokener jsonParser = new JSONTokener(result);
//				JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
//				String token = (String) jsonObject.get("data");
//				String data = (String) jsonObject.get("msg");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

            JSONArray data = new JSONArray();
            JSONObject subitem = new JSONObject();
            //生成发送的源数据
            try {
//                subitem.put("id", baseJPushEntity.id);
//                subitem.put("UserId", baseJPushEntity.UserId);
                subitem.put("RegistrationId", baseJPushEntity.RegistrationId);
                subitem.put("uuid", baseJPushEntity.uuid);
//                subitem.put("CreateTime", baseJPushEntity.CreateTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            data.put(subitem);
            String jsonString = subitem.toString();

//            String SOAP_ACTION="http://WebXml.com.cn/getRegionProvince";
//            String NAMESPACE="http://WebXml.com.cn/";
//            String METHOD_NAME="getRegionProvince";
//            String URL="http://ws.webxml.com.cn/WebServices/WeatherWS.asmx?WSDL";

            String SOAP_ACTION = "http://tempuri.org/InsertJpush";
            String NAMESPACE = "http://tempuri.org/";
            String METHOD_NAME = "InsertJpush";
            String URL = "http://172.16.41.125/AppService.asmx";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
//            request.addProperty("baseJPushEntityJson", jsonString);//添加数据
            request.addProperty("baseJPushEntityJson", 123);//添加数据

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.dotNet = false;//C#写的webservice必须加上这一句
            envelope.setAddAdornments(false);
            envelope.implicitTypes=true;

            envelope.setOutputSoapObject(request);
            (new MarshalBase64()).register(envelope);

            try {
                HttpTransportSE transportSE = new HttpTransportSE(URL);
                //Android传输对象
//                AndroidHttpTransport transportSE=new AndroidHttpTransport(URL);
                transportSE.debug=true;
                transportSE.call(SOAP_ACTION, envelope);



//                SoapObject result = (SoapObject) envelope.bodyIn; //获取到返回的结果，并强制转换成SoapObject对象
                Object result = (Object) envelope.bodyIn; //获取到返回的结果，并强制转换成SoapObject对象
//                Object result = (Object) envelope.getResponse();
//                SoapObject test = (SoapObject) ((SoapObject) result).getProperty(0); //该对象中还嵌套了一个SoapObject对象，需要使用getProperty(0)把这个对象提取出来

                StringBuilder builder = new StringBuilder();
//                //解析返回的数据
//                for (int i = 0; i < result.getPropertyCount(); i++) {
//                    builder.append(result.getProperty(i));
//                }
//
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }

        private String readText(InputStream is) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                int len = -1;

                while ((len = is.read(b, 0, 1024)) != -1) {
                    baos.write(b, 0, len);
                }

                is.close();

                return new String(baos.toByteArray(), "UTF-8");
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

    }


    //获取位置 经纬值
    private void getMyLocation() {
        //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认GCJ02
        //GCJ02：国测局坐标；
        //BD09ll：百度经纬度坐标；
        //BD09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标

        option.setScanSpan(1000);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效

        option.setOpenGps(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(true);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.SetIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setWifiCacheTimeOut(5*60*1000);
        //可选，V7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位

        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        mLocationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明

        mLocationClient.start();
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            double latitude = location.getLatitude();    //获取纬度信息
            mJPushEntiry.latitude = latitude;
            double longitude = location.getLongitude();    //获取经度信息
            mJPushEntiry.longitude = longitude;
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f

            String coorType = location.getCoorType();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = location.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明

            Log.i(TAG, "onReceiveLocation: latitude="+latitude+"---longitude="+longitude);
            mLocationClient.stop();


            mTestHandler.sendEmptyMessageDelayed(MSG_OPEN_HOME, 500);
        }
    }


}

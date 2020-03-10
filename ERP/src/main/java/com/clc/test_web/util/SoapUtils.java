package com.clc.test_web.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.clc.test_web.R;
import com.clc.test_web.global.GlobalConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.ServiceConnection;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

/**
 * Created by zhanglei on 2017\12\21 0021.
 */

public class SoapUtils {

    // 命名空间
    public static final String nameSpace = "http://tempuri.org/";
    // EndPoint
    public static final String endPoint = GlobalConstants.SEND_UUID_URL;
//    public static final String endPoint = "http://172.16.18.17/WebService1.asmx";

    /**
     * @param mActivity
     * @param method    调用的方法名称
     */
    public static void Post(final Activity mActivity, final String method, final Map<String, String> params, final SoapCallback callback) {
        //如果这个人是随便看看的，就正常调用接口
//        String id = SpUtils.getUserData(mActivity).getId();
//        if (id.equals("-1")) {
//            PostNoCheck(mActivity, method, params, callback);
//            return;
//        }
        //检测是否被别人挤掉了
        Map<String, String> map = new HashMap<>();
//        map.put("UserId", SpUtils.getUserData(mActivity).getId());
        map.put("baseJPushEntityJson", "UUID+reID");
        Post(method, params, new SoapCallback() {
            @Override
            public void onError(final String error) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //返回错误信息
                        if (callback != null) {
                            Log.i("baseJPushEntityJson", "run: error=="+error);
//                            callback.onError(mActivity.getResources().getString(R.string.is_login_hint));
                        }
                    }
                });
            }

            @Override
            public void onSuccess(String data) {

                Log.i("baseJPushEntityJson", "run: success=="+data);


/*
                Boolean aBoolean = Boolean.valueOf(data);
                if (aBoolean) {//没有被人挤掉，正常调用接口
                    PostNoCheck(mActivity, method, params, callback);
                } else { //被人挤掉了，提示他要不要重新登录，如果不需要杀死应用，需要的话就进入登录页
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int role = SpUtils.getRole(mActivity);
                            Intent intent = new Intent(mActivity, LoginActivity.class);
                            intent.putExtra("role", role);
                            intent.putExtra(API.ISFINISH, true);
                            mActivity.startActivity(intent);
                            Toast.makeText(mActivity, R.string.is_login_hint, Toast.LENGTH_SHORT).show();
                            //返回错误信息
                            if (callback != null) {
                                callback.onError(mActivity.getResources().getString(R.string.is_login_hint));
                            }
                        }
                    });

                }
*/
            }
        });
    }

    /**
     * @param mActivity
     * @param method    调用的方法名称
     */
    public static void PostNoCheck(final Activity mActivity, final String method, final Map<String, String> params, final SoapCallback callback) {
        new Thread() {
            @Override
            public void run() {

                // SOAP Action
                String soapAction = nameSpace + method;

                // 指定WebService的命名空间和调用的方法名
                SoapObject rpc = new SoapObject(nameSpace, method);

                // 设置需调用WebService接口需要传入的两个参数mobileCode、userId
                if (params != null) {
                    Set<Map.Entry<String, String>> entries = params.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        rpc.addProperty(entry.getKey(), entry.getValue());
                    }
                }

                // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.bodyOut = rpc;
                // 设置是否调用的是dotNet开发的WebService
                envelope.dotNet = true;

                HttpTransportSE transport = new HttpTransportSE(endPoint, 1000 * 60);
                try {
                    // 调用WebService
                    transport.call(soapAction, envelope);
                    // 获取返回的数据
                    SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                    // 获取返回的结果
                    final String result = response.toString();
                    if (callback != null) {
                        mActivity.runOnUiThread(new TimerTask() {
                            @Override
                            public void run() {
                                JSONObject object;
                                try {
                                    //先判断是否是错误消息
                                    object = new JSONObject(result);
                                    String data = object.optString("result");
                                    String Result = object.optString("Result");
                                    String success = object.optString("Success");
                                    //如果data不为空则说明有错误，返回错误消息
                                    if (!TextUtils.isEmpty(data)) {
                                        try {
                                            callback.onError(data);
                                        } catch (Exception e) {

                                        }
                                    } else if (!TextUtils.isEmpty(Result)) {
                                        callback.onError(Result);
                                    } else if (!TextUtils.isEmpty(success)) {
                                        callback.onSuccess(success);
                                    } else {
                                        callback.onSuccess(result);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    callback.onSuccess(result);
                                }
                            }
                        });
                    }
                } catch (final SoapFault soapFault) {
                    soapFault.printStackTrace();
                    if (callback != null) {
                        mActivity.runOnUiThread(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Log.e("error", soapFault.getMessage() == null ? "" : soapFault.getMessage());
                                    callback.onError("系统功能维护，请联系管理员");
                                } catch (Exception e) {

                                }
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        mActivity.runOnUiThread(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Log.e("error", e.getMessage() == null ? "" : e.getMessage());
                                    callback.onError("系统功能维护，请联系管理员");
                                } catch (Exception exception) {

                                }
                            }
                        });
                    }
                }
            }
        }.start();
    }

    /**
     * @param method 调用的方法名称
     */
    public static void Post(final String method, final Map<String, String> params, final SoapCallback callback) {
        new Thread() {
            @Override
            public void run() {

                // SOAP Action
                String soapAction = nameSpace + method;

                // 指定WebService的命名空间和调用的方法名
                SoapObject rpc = new SoapObject(nameSpace, method);

                // 设置需调用WebService接口需要传入的两个参数mobileCode、userId
                Set<Map.Entry<String, String>> entries = params.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    rpc.addProperty(entry.getKey(), entry.getValue());
                }

                // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.bodyOut = rpc;
                // 设置是否调用的是dotNet开发的WebService
                envelope.dotNet = true;

                HttpTransportSE transport = new HttpTransportSE(endPoint);
                try {
                    // 调用WebService
                    transport.call(soapAction, envelope);
                    // 获取返回的数据
                    SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                    // 获取返回的结果
                    final String result = response.toString();
                    if (callback != null) {
                        callback.onSuccess(result);
                    }
                } catch (final SoapFault soapFault) {
                    soapFault.printStackTrace();
                    if (callback != null) {
                        callback.onError(soapFault.getMessage());
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                }
            }
        }.start();
    }
}

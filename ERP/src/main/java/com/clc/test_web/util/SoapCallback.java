package com.clc.test_web.util;

/**
 * Created by zhanglei on 2017\12\21 0021.
 */

public interface SoapCallback {

    void onError(String error);

    void onSuccess(String data);
}

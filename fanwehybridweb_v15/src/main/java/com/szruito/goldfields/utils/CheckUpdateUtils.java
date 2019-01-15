package com.szruito.goldfields.utils;

import com.szruito.goldfields.bean.UpdateAppInfo;
import com.szruito.goldfields.http.HttpMethods;

import rx.Observable;
import rx.Subscriber;


public class CheckUpdateUtils {
    /**
     * 检查更新
     */
    @SuppressWarnings("unused")
//    public static void checkUpdate(String appCode, String curVersion, final CheckCallBack updateCallback) {
    public static void checkUpdate(String registrationId,String appName,final CheckCallBack updateCallback) {
        Observable<UpdateAppInfo> observable = HttpMethods.getInstance()
//                .getApi().update("GreenRoad.apk", curVersion);
                .getApi().update(registrationId,appName);
        HttpMethods.getInstance().toSubscribe(observable, new Subscriber<UpdateAppInfo>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
//                Logger.i("更新app" + e.getMessage());
            }

            @Override
            public void onNext(UpdateAppInfo updateAppInfo) {
//                Logger.i("更新app" + updateAppInfo.toString());
                if (updateAppInfo.getCode() == 201 || updateAppInfo.getData() == null ||
                        updateAppInfo.getData().getDownloadurl() == null) {
                    updateCallback.onError(); // 失败
                } else {
                    updateCallback.onSuccess(updateAppInfo);
                }
            }
        });
    }


    public interface CheckCallBack{
        void onSuccess(UpdateAppInfo updateInfo);
        void onError();
    }


}
package com.szruito.goldfields.utils;


import com.szruito.goldfields.bean.AddGroupInfo;
import com.szruito.goldfields.bean.CrashInfo;
import com.szruito.goldfields.http.HttpMethods;

import rx.Observable;
import rx.Subscriber;

public class CrashInfoUtils {
    public static void uploadErr(String info) {
        Observable<CrashInfo> observable = HttpMethods.getInstance().getApi().uploadErr(info);
        HttpMethods.getInstance().toSubscribe(observable, new Subscriber<CrashInfo>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
//                Logger.i("更新app" + e.getMessage());
            }

            @Override
            public void onNext(CrashInfo addGroupInfo) {
//                Logger.i("更新app" + updateAppInfo.toString());

            }
        });

    }
}


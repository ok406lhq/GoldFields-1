package com.szruito.goldfields.utils;


import com.szruito.goldfields.bean.AddGroupInfo;
import com.szruito.goldfields.http.HttpMethods;

import rx.Observable;
import rx.Subscriber;

public class AddGroupUtils {
    public static void addGroup(String registrationId,String appName, final AddCallBack callBack) {
        Observable<AddGroupInfo> observable = HttpMethods.getInstance().getApi().addGroup(registrationId,appName);
        HttpMethods.getInstance().toSubscribe(observable, new Subscriber<AddGroupInfo>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
//                Logger.i("更新app" + e.getMessage());
            }

            @Override
            public void onNext(AddGroupInfo addGroupInfo) {
//                Logger.i("更新app" + updateAppInfo.toString());
                if (addGroupInfo.getCode() != 200) {
                    callBack.onError(); // 失败
                } else {
                    callBack.onSuccess(addGroupInfo);
                }
            }
        });

    }


    public interface AddCallBack {
        void onSuccess(AddGroupInfo updateInfo);

        void onError();
    }
}


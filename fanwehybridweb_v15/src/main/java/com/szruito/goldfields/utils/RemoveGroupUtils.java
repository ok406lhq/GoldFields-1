package com.szruito.goldfields.utils;


import com.szruito.goldfields.bean.AddGroupInfo;
import com.szruito.goldfields.bean.RemoveGroupInfo;
import com.szruito.goldfields.http.HttpMethods;

import rx.Observable;
import rx.Subscriber;

public class RemoveGroupUtils {
    public static void removeGroup(String registrationId, final RemoveCallBack callBack) {
        Observable<RemoveGroupInfo> observable = HttpMethods.getInstance().getApi().removeGroup(registrationId);
        HttpMethods.getInstance().toSubscribe(observable, new Subscriber<RemoveGroupInfo>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
//                Logger.i("更新app" + e.getMessage());
            }

            @Override
            public void onNext(RemoveGroupInfo removeGroupInfo) {
//                Logger.i("更新app" + updateAppInfo.toString());
                if (removeGroupInfo.getCode() != 200 ) {
                    callBack.onError(); // 失败
                } else {
                    callBack.onSuccess(removeGroupInfo);
                }
            }
        });

    }


    public interface RemoveCallBack {
        void onSuccess(RemoveGroupInfo removeGroupInfo);

        void onError();
    }
}


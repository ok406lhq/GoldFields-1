package com.szruito.goldfields.http;


import com.szruito.goldfields.bean.AddGroupInfo;
import com.szruito.goldfields.bean.CheckContactsInfo;
import com.szruito.goldfields.bean.QuitAppInfo;
import com.szruito.goldfields.bean.RemoveGroupInfo;
import com.szruito.goldfields.bean.UpdateAppInfo;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * @author sineom
 * @version 1.0
 * @time 2016/7/4 17:09
 * @updateAuthor ${Author}
 * @updataTIme 2016/7/4
 * @updataDes ${描述更新内容}
 */
public interface HttpUtilsApi {

    @FormUrlEncoded
    @POST("api/app.util/version/find")
    Observable<UpdateAppInfo> update(
            @Field("registrationId") String registrationId,
            @Field("appName") String appName);
//            @Query("appname") String appname,

    @FormUrlEncoded
    @POST("/api/app.util/version/userAddGroup")
    Observable<AddGroupInfo> addGroup(
            @Field("registrationId") String registrationId,
            @Field("appName") String appName);

    @FormUrlEncoded
    @POST("/api/app.util/version/userRemoveGroup")
    Observable<RemoveGroupInfo> removeGroup(
            @Field("registrationId") String registrationId);

    @FormUrlEncoded
    @POST("api/app.user/user/clearUserState")
    Observable<QuitAppInfo> quit(@Field("_user_token") String _user_token);

    @FormUrlEncoded
    @POST("api/app.apply/phonebook/makePbFromUser")
    Observable<CheckContactsInfo> check(@Field("data") String data,
                                        @Field("_user_token") String _user_token
//                                        @Field("meid") String meid
    );
}

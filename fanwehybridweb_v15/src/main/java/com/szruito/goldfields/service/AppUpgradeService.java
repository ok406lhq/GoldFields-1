package com.szruito.goldfields.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.szruito.goldfields.activity.MainActivity;
import com.szruito.goldfields.activity.MainHelper;
import com.szruito.goldfields.activity.MainHelper;

/**
 * 更新服务
 *
 * @author yhz
 */
public class AppUpgradeService extends IntentService {
    private static final String ACTION_UPGRADE_SERVICE = "com.lhq.intentservice.action.UPGRADE_SERVICE";
    //定义notify的id，避免与其它的notification的处理冲突
    private static final int NOTIFY_ID = 0;

    public static void startUpgradeService(Context context, boolean isUpgrade) {
        Intent intent = new Intent(context, AppUpgradeService.class);
        intent.putExtra("url", isUpgrade);
        intent.setAction(ACTION_UPGRADE_SERVICE);
        context.startService(intent);
    }

    public AppUpgradeService() {
        super("ContactIntentService");
    }

    public AppUpgradeService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final String action = intent.getAction();
        if (ACTION_UPGRADE_SERVICE.equals(action)) {
            if (intent.getBooleanExtra("isUpgrade", false)) {
                MainHelper.getInstance().updateApp(getApplicationContext(),true);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("TAG", "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("TAG", "onDestroy");
    }
}
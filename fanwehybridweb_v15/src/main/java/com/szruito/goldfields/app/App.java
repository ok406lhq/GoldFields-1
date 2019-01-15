package com.szruito.goldfields.app;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.widget.Toast;

import com.fanwe.gesture.customview.LockPatternView.Cell;
import com.fanwe.gesture.utils.LockPatternUtils;
import com.fanwe.lib.cache.FDisk;
import com.fanwe.lib.eventbus.FEventBus;
import com.fanwe.lib.utils.extend.FActivityStack;
import com.fanwe.library.SDLibrary;
import com.fanwe.library.app.FApplication;
import com.fanwe.library.utils.LogUtil;
//import com.fanwei.jubaosdk.shell.FWPay;
import com.mob.MobSDK;
import com.szruito.goldfields.constant.Constant;
import com.szruito.goldfields.dao.InitActModelDao;
import com.szruito.goldfields.dao.LoginSuccessModelDao;
import com.szruito.goldfields.event.EventTag;
import com.szruito.goldfields.event.SDBaseEvent;
import com.szruito.goldfields.model.LoginSuccessModel;
import com.szruito.goldfields.utils.CrashHandler;
import com.szruito.goldfields.utils.SPUtils;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.smtt.sdk.QbSdk;

import org.xutils.x;

import java.util.List;

import com.szruito.goldfields.R;

import cn.jpush.android.api.JPushInterface;

public class App extends FApplication {
    private static App mInstance;
    public static Context sContext;
    public LockPatternUtils mLockPatternUtils;
    private static long lastJump2LoginTime = 0L;
    // APP_ID 替换为你的应用从官方网站申请到的合法appID
    private final String APP_ID = "wx9695411753c04965";
    // IWXAPI 是第三方app和微信通信的openApi接口
    private IWXAPI api;

    public static App getApplication() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        regToWx();  //集成微信
        regToShareSDK(); //集成ShareSDK
        regToJPush();   //集成极光
        sContext = this;
        super.onCreate();
        init();
    }

    private void regToWx() {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, APP_ID, true);

        // 将应用的appId注册到微信
//        api.registerApp(APP_ID);
    }

    private void regToJPush() {
        //设置调试模式
        JPushInterface.setDebugMode(true);
        //init 初始化SDK
        JPushInterface.init(this);
    }

    private void regToShareSDK() {
        MobSDK.init(this, Constant.MOB_SHARESDK_APPKEY);//注册一下shareSDK，这个方便云平台对数据进行管理
    }

    @Override
    protected void onCreateMainProcess() {
        mInstance = this;
//        FWPay.initialize(this, true);
        x.Ext.init(this);
        initSDLibrary();
        initLockConfig();
//        SDTencentMapManager.getInstance().init(this);
        FDisk.init(this);
        initTBS();
    }

    private void init() {
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        initUmengPush();
    }

    private void initSDLibrary() {
        SDLibrary.getInstance().init(mInstance);
    }

    private void initLockConfig() {
        try {
            mLockPatternUtils = new LockPatternUtils(this);
            LoginSuccessModel model = LoginSuccessModelDao.queryModelCurrentLogin();
            if (model != null && !TextUtils.isEmpty(model.getPatternpassword())) {
                @SuppressWarnings("static-access")
                List<Cell> list = mLockPatternUtils.stringToPattern(model.getPatternpassword());
                mLockPatternUtils.saveLockPattern(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initUmengPush() {
    }

    public void exitApp(boolean isBackground) {
        FActivityStack.getInstance().finishAllActivity();
        FEventBus.getDefault().post(new SDBaseEvent(null, EventTag.EVENT_EXIT_APP));
        if (!isBackground) {
            System.exit(0);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static long getLastJump2LoginTime() {
        return lastJump2LoginTime;
    }

    public static void setLastJump2LoginTime(long lastJump2LoginTime) {
        App.lastJump2LoginTime = lastJump2LoginTime;
    }

    public void clearAppsLocalUserModel() {

        FDisk.openInternalCache().cacheString().put(getString(R.string.config_session_id), "");
        InitActModelDao.delete();
        LoginSuccessModel model = LoginSuccessModelDao.queryModelCurrentLogin();
        LoginSuccessModelDao.deleteModel(String.valueOf(model.getUserid()));
        CookieManager.getInstance().removeAllCookie();
    }

    private void initTBS() {
        // 搜集本地 tbs 内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                LogUtil.d("TBS onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                LogUtil.d("TBS onCoreInitFinished");
            }
        };
        QbSdk.setDownloadWithoutWifi(true);
        //x5 内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);
    }
}

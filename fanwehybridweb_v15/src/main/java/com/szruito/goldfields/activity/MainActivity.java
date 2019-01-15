package com.szruito.goldfields.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.classic.common.MultipleStatusView;
import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;
import com.szruito.goldfields.app.App;
import com.szruito.goldfields.bean.LoginBackData;
import com.szruito.goldfields.bean.ShareData;
import com.szruito.goldfields.constant.ApkConstant;
import com.szruito.goldfields.constant.Constant;
import com.szruito.goldfields.constant.Constant.JsFunctionName;
import com.szruito.goldfields.dialog.BotPhotoPopupView;
import com.szruito.goldfields.dialog.CustomDialog;
import com.szruito.goldfields.dialog.DialogCropPhoto.OnCropBitmapListner;
import com.szruito.goldfields.event.SDBaseEvent;
import com.szruito.goldfields.jshandler.AppJsHandler;
import com.szruito.goldfields.model.CutPhotoModel;
import com.szruito.goldfields.netstate.TANetWorkUtil;
import com.szruito.goldfields.utils.DataCleanManager;
import com.szruito.goldfields.utils.IntentUtil;
import com.szruito.goldfields.dialog.LoadingDialog;
import com.szruito.goldfields.utils.PhoneFormatCheckUtils;
import com.szruito.goldfields.utils.SDImageUtil;
import com.szruito.goldfields.utils.SPUtils;
import com.szruito.goldfields.webview.CustomWebView;
import com.szruito.goldfields.webview.DefaultWebChromeClient;
import com.szruito.goldfields.webview.WebChromeClientListener;
import com.fanwe.library.utils.LogUtil;
import com.orhanobut.logger.Logger;
import com.szruito.goldfields.event.EventTag;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebView;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import com.szruito.goldfields.R;

import java.util.HashMap;

import cn.jpush.android.api.JPushInterface;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.wechat.friends.Wechat;

import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static com.szruito.goldfields.constant.Constant.PERMISS_ALL;
import static com.szruito.goldfields.constant.Constant.PERMISS_CAMERA;
import static com.szruito.goldfields.constant.Constant.PERMISS_CONTACT;
import static com.szruito.goldfields.constant.Constant.PERMISS_SMS;
import static com.szruito.goldfields.constant.Constant.PERMISS_DX;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends BaseActivity implements OnCropBitmapListner, PlatformActionListener {
    public static final String SAVE_CURRENT_URL = "url";
    public static final String EXTRA_URL = "extra_url";
    public static final String SHARE_TAG_ONE = "tag1";
    public static final String SHARE_TAG_TWO = "tag2";
    public static final String SHARE_TAG_THREE = "tag3";
    public static final String SINA_WEIBO_NAME = "SinaWeibo";
    public static final String QQ_NAME = "QQ";
    public static final String WECHAT_NAME = "Wechat";
    public static final int FILECHOOSER_RESULTCODE = 1;// 选择照片

    @ViewInject(R.id.ll_fl)
    private RelativeLayout mll_fl;
    @ViewInject(R.id.cus_webview)
    private CustomWebView mWebViewCustom;
    @ViewInject(R.id.loading_layout)
    private RelativeLayout loadingLayout, webParentView;
    private View mErrorView; //加载错误的视图
    private MultipleStatusView mMultipleStatusView;

    private BotPhotoPopupView mBotPhotoPopupView;
    private ValueCallback<Uri> mUploadMessage;
    private String mCameraFilePath, mCurrentUrl, user_token;
    private CutPhotoModel mCut_model;
    private boolean isLogout, needUpgrade;
    private boolean isDeleteCache = false;
    private String failLocationUrl = "file:///android_asset/new_no_network.html";
    private String SHARE_TITLE;
    private String SHARE_TEXT;

    String[] permissions = {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA, Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE};
    String[] initPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String phone, username, errorUrl, token, gender, icon, userId, name, platformName, pingTaiName, UnLockPTName;
    private boolean isPlay = true;
    private String registrationId;
    private String appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
//        int a = 10 / 0;//模拟Crash用

        //设置状态栏透明化
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setTranslucent(this);
        //checkNormalQuit();//清除登录状态
        checkBottomNav();//检查是否有虚拟底部导航,保存在本地

        initConstants();    //初始化一些常量
        initView();     //初始化视图

        x.view().inject(this);
        //初次进入应用后的版本更新处理（这里有个bug Tag住）
        needUpgrade = (boolean) SPUtils.getParam(MainActivity.this, "needUpgrade", false);
        if (!needUpgrade) {
            MainHelper.getInstance().updateApp(MainActivity.this, true);
        }

        MainHelper.getInstance().addPermissByPermissionList(MainActivity.this, initPermissions, PERMISS_DX);
    }

    private void initConstants() {
        mIsExitApp = true;
        SHARE_TITLE = MainHelper.getInstance().getAppName(MainActivity.this) + "——专为您打造的区块链价值共享平台！";
        SHARE_TEXT = "我正在使用《" + MainHelper.getInstance().getAppName(MainActivity.this) + "》快来跟我一起使用吧";
        //获取设备ID
        registrationId = JPushInterface.getRegistrationID(this);
        SPUtils.setParam(this, "registrationId", registrationId);
        //获取应用程序名称
        appName = MainHelper.getInstance().getAppName(MainActivity.this);
        Logger.i("是否存在底部导航：" + MainHelper.getInstance().checkHasNavigationBar(MainActivity.this));
    }

    private void initView() {
        //初始化加载界面Loading
        SpinKitView spinKitView = loadingLayout.findViewById(R.id.spin_kit);
        Button mBtnWait = loadingLayout.findViewById(R.id.btn_wait);
        Sprite wave = new Wave();
        spinKitView.setIndeterminateDrawable(wave);

        boolean isFirstLoading = (boolean) SPUtils.getParam(MainActivity.this, "isFirstLoading", true);
        if (isFirstLoading) {   //第一次加载会比较慢，提醒用户
            SPUtils.setParam(MainActivity.this, "isFirstLoading", false);
            mBtnWait.setVisibility(View.VISIBLE);
            mBtnWait.setText("初次加载数据，可能会花费几分钟...");
        } else {
            mBtnWait.setVisibility(View.VISIBLE);
            mBtnWait.setText("正在加载数据中...");
        }
        mWebViewCustom.addJavascriptInterface(new AppJsHandler(this, mWebViewCustom));
        getIntentInfo();
        initErrorPage();
        initWebView();
    }


    private void getIntentInfo() {
        if (getIntent().hasExtra(EXTRA_URL) & getIntent().getExtras() != null) {
            mCurrentUrl = getIntent().getExtras().getString(EXTRA_URL);
        }
    }

    private void checkBottomNav() {
        boolean checkHasNavigationBar = MainHelper.getInstance().checkHasNavigationBar(MainActivity.this);
        SPUtils.setParam(MainActivity.this, "checkHasNavigationBar", checkHasNavigationBar);
        Logger.i("是否存在底部导航：" + checkHasNavigationBar);
    }

    public static void setTranslucent(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /***
     * 显示加载失败时自定义的网页
     */
    private void initErrorPage() {
        if (mErrorView == null) {
            mErrorView = View.inflate(this, R.layout.layout_load_error, null);
            mMultipleStatusView = mErrorView.findViewById(R.id.multiple_status_view);
            final Dialog dialog = new LoadingDialog(MainActivity.this, R.style.MyDialogStyle, "加载中...");
            mMultipleStatusView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (MainHelper.getInstance().isNetworkAvailable(MainActivity.this)) {
                                webParentView.removeAllViews();
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                webParentView.addView(mWebViewCustom, layoutParams);
                                if (!errorUrl.isEmpty() && errorUrl.contains("/mine/center")) {
                                    dialog.dismiss();
                                    return;
                                }
                                mWebViewCustom.get("http://www.fields.gold");
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "网络连接成功", Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "网络连接出错", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 1500);
                }
            });
        }
    }


    private void initWebView() {
        String url = ApkConstant.SERVER_URL + "?version=" + MainHelper.getInstance().getVersionCode(this);

        com.tencent.smtt.sdk.WebViewClient defaultWebViewClient = new com.tencent.smtt.sdk.WebViewClient() {
            @Override
            public void onReceivedError(com.tencent.smtt.sdk.WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Logger.i("onReceivedError: ------->errorCode" + errorCode + ":" + description);
                //6.0以下执行 （网络错误的回调）
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return;
                }
                showErrorPage();// // 显示错误页面
            }

            @Override
            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                super.onReceivedError(webView, webResourceRequest, webResourceError);
                //6.0以上执行 （网络错误的回调）
                Logger.i("onReceivedError: " + webView.getUrl());
                Logger.i("onReceivedErrorCode: " + webResourceError.getErrorCode());
                errorUrl = webView.getUrl();
                showErrorPage();//显示错误页面
            }

            //刷新后WebView退出不了,重定向的解决方法
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不另跳浏览器
                // 在2.3上面不加这句话，可以加载出页面，在4.0上面必须要加入，不然出现白屏
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url);
                    mWebViewCustom.stopLoading();
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(com.tencent.smtt.sdk.WebView view, String url, Bitmap favicon) {
                LogUtil.e("onPageStarted：" + url);
            }

            @Override
            public void onPageFinished(final com.tencent.smtt.sdk.WebView view, String url) {
                if (!failLocationUrl.equals(url)) {
                    mCurrentUrl = url;
                }
                Logger.i("嘻嘻：" + url);
                username = (String) SPUtils.getParam(MainActivity.this, "username", "");
                /**
                 * 该交互实现了登录页面的记住账户功能
                 */
                view.evaluateJavascript("javascript:rememberUsername('" + username + "','" + registrationId + "')", new com.tencent.smtt.sdk.ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        Logger.i("rememberUsername" + s);
                    }
                });
                /**
                 * 该交互是为了传递App名称给vue，方便做多渠道App的开发
                 */
                view.evaluateJavascript("javascript:getAppName('" + appName + "')", new com.tencent.smtt.sdk.ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        Logger.i("appName" + s);
                    }
                });

                String[] split = url.split("/");
                String endUrl = "/";
                if (split.length > 2) {
                    endUrl = endUrl + split[split.length - 2] + "/" + split[split.length - 1];
                }
                switch (endUrl) {
                    case "/user/setup":
                        /**
                         * 该交互的目的是标记vue上版本更新的的badge
                         */
                        String json = "{'version':'" + MainHelper.getInstance().getVersionName(MainActivity.this) + "'}";
                        view.evaluateJavascript("javascript:getVersionName(" + json + ")", new com.tencent.smtt.sdk.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                Logger.i("getVersionName" + s);
                            }
                        });
                        break;
                    default:
                        break;
                }
                dimissDialog();
            }
        };

        DefaultWebChromeClient defaultWebChromeClient = new DefaultWebChromeClient();
        defaultWebChromeClient.setListener(new WebChromeClientListener() {
            @Override
            public void openFileChooser(com.tencent.smtt.sdk.ValueCallback<Uri> uploadFile, String
                    acceptType, String capture) {
                if (mUploadMessage != null)
                    return;
                mUploadMessage = uploadFile;
                startActivityForResult(MainHelper.getInstance().createDefaultOpenableIntent(new MainHelper.OnCameraPathBack() {
                    @Override
                    public void callback() {
                        mCameraFilePath = IntentUtil.getCamerFilePath();
                    }
                }), FILECHOOSER_RESULTCODE);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    if ((boolean) SPUtils.getParam(MainActivity.this, "checkHasNavigationBar", false)) {
                        hideBottomMenu();
                    }
                    loadingLayout.setVisibility(View.GONE);
                }
                Logger.i("onProgressChanged" + view.getUrl());
            }
        });

        mWebViewCustom.setWebViewClient(defaultWebViewClient);
        mWebViewCustom.setWebChromeClient(defaultWebChromeClient);
        //获取父容器
        webParentView = (RelativeLayout) mWebViewCustom.getParent();
        if (MainHelper.getInstance().isNetworkAvailable(this)) {
            mWebViewCustom.get(url);
        } else {
            //登录界面的断网处理
            mWebViewCustom.get(failLocationUrl);
        }
    }

    /**
     * 隐藏底部虚拟按键，且全屏
     */
    private void hideBottomMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT < 19) { // Android版本小于4.0或者没有底部虚拟导航栏
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else {
            //Android4.0以上，所有手机都是可以显示虚拟底部导航的，只不过有些手机厂家屏蔽了
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            //| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN; //下面这种是处理全屏，包括状态栏的情况
            decorView.setSystemUiVisibility(uiOptions);
            Logger.i("隐藏了喔");
        }
    }

    //移除加载网页错误时，默认的提示信息
    private void showErrorPage() {
        webParentView.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        webParentView.addView(mErrorView, 0, layoutParams); //添加自定义的错误提示的View
    }

    /**
     * 裁剪图片
     */
    private void clickll_head() {
        if (mBotPhotoPopupView != null) {
            if (mBotPhotoPopupView.isShowing()) {
                mBotPhotoPopupView.dismiss();
            } else {
                mBotPhotoPopupView.showAtLocation(mll_fl, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
            }
        } else {
            mBotPhotoPopupView = new BotPhotoPopupView(this);
            mBotPhotoPopupView.showAtLocation(mll_fl, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
        }
    }

    private void checkDeleteCache() {
        if (isDeleteCache) {
            //webview缓存
            //清空所有Cookie
            CookieSyncManager.createInstance(getApplicationContext());  //Create a singleton CookieSyncManager within a context
            CookieManager cookieManager = CookieManager.getInstance(); // the singleton CookieManager instance
            cookieManager.removeAllCookie();// Removes all cookies.
            CookieSyncManager.getInstance().sync(); // forces sync manager to sync now
            mWebViewCustom.setWebChromeClient(null);
            mWebViewCustom.setWebViewClient(null);
            mWebViewCustom.getSettings().setJavaScriptEnabled(false);
            mWebViewCustom.clearCache(true);
            mWebViewCustom.clearHistory();
            //安卓自带缓存
            try {
                DataCleanManager.cleanInternalCache(MainActivity.this);
                Logger.i("清除缓存后：" + DataCleanManager.getCacheSize(MainActivity.this.getCacheDir()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //第三方登录授权
    private void doAuthorize(Platform platform) {
        if (platform != null) {
            platform.removeAccount(true);   //清除本地授权缓存
            platform.SSOSetting(false);     //设置SSO，false表示SSO生效
            platform.setPlatformActionListener(this);
            if (!platform.isClientValid()) {    //判断客户端是否存在
                Toast.makeText(MainActivity.this, "请安装相关客户端", Toast.LENGTH_SHORT).show();
            }
            if (platform.isAuthValid()) {    //判断是否已经授权
                Toast.makeText(MainActivity.this, "已经授权过了", Toast.LENGTH_SHORT).show();
            }
            platform.showUser(null);    //要数据不要功能，主要体现在不会重复出现授权界面
            //platform.authorize();     //要功能不要数据
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onMainEvent(SDBaseEvent event) {
        super.onMainEvent(event);
        switch (event.getTagInt()) {
            case EventTag.EVENT_LOAD_CONTACT:   //通讯录
                String[] permissList = {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE};
                MainHelper.getInstance().addPermissByPermissionList(MainActivity.this, permissList, PERMISS_CONTACT);   //只是发送一个讯息 , 做权限的处理
                break;
            case EventTag.UPDATE:   //App更新
                Logger.i("更新App");
                final Dialog dialog = new LoadingDialog(MainActivity.this, R.style.MyDialogStyle, "加载中...");
                dialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainHelper.getInstance().updateApp(MainActivity.this, false);
                        dialog.dismiss();
                    }
                }, 1500);
                break;
            case EventTag.MOB_LOGIN:    //第三方登录
                pingTaiName = (String) event.data;
                final Dialog dialog1 = new LoadingDialog(MainActivity.this, R.style.MyDialogStyle, "请求中...");
                dialog1.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Platform platform = ShareSDK.getPlatform(pingTaiName);
                        doAuthorize(platform);
                        dialog1.dismiss();
                    }
                }, 1000);
                break;
            case EventTag.MOB_UNLOCK:   //解除第三方绑定
                UnLockPTName = (String) event.data;
                Platform platform = ShareSDK.getPlatform(pingTaiName);
                if (platform != null) {
                    platform.removeAccount(true);   //清除本地授权缓存
                }
                break;
            case EventTag.DELETE_CACHE:    //清除缓存
                final Dialog dialog2 = new LoadingDialog(MainActivity.this, R.style.MyDialogStyle, "正在清除...");
                dialog2.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isDeleteCache = true;
                        Toast.makeText(MainActivity.this, "缓存已清除！", Toast.LENGTH_SHORT).show();
                        dialog2.dismiss();
                        mWebViewCustom.get("http://www.fields.gold");
                    }
                }, 1500);
                break;
            case EventTag.SHARE:    //分享图片
                ShareData shareData = (ShareData) event.data;
                String tag = shareData.getTag();
                String url = shareData.getUrl();

                OnekeyShare oks = new OnekeyShare();
                //关闭sso授权
                oks.disableSSOWhenAuthorize();
                switch (tag) {  //三种类型的分享界面分开处理
                    case SHARE_TAG_ONE:
                        MainHelper.getInstance().getShareImage(MainActivity.this, url, tag);
                        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
                        oks.setImagePath(Environment.getExternalStorageDirectory().getPath() + "/shareImage/tag1.jpg");//确保SDcard下面存在此张图片
                        // 启动分享GUI
                        oks.show(this);
                        break;
                    case SHARE_TAG_TWO:
                        MainHelper.getInstance().getShareImage(MainActivity.this, url, tag);
                        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
                        oks.setImagePath(Environment.getExternalStorageDirectory().getPath() + "/shareImage/tag2.jpg");//确保SDcard下面存在此张图片
                        // 启动分享GUI
                        oks.show(this);
                        break;
                    case SHARE_TAG_THREE:
                        MainHelper.getInstance().getShareImage(MainActivity.this, url, tag);
                        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
                        oks.setImagePath(Environment.getExternalStorageDirectory().getPath() + "/shareImage/tag3.jpg");//确保SDcard下面存在此张图片
                        // 启动分享GUI
                        oks.show(this);
                        break;
                }
                break;
            case EventTag.SHARE_URL:   //分享链接
                OnekeyShare oks2 = new OnekeyShare();
                oks2.disableSSOWhenAuthorize();//关闭sso授权
                oks2.setTitle(SHARE_TITLE);
                oks2.setUrl(Constant.SHARE_URL);
                oks2.setText(SHARE_TEXT);
                oks2.setImageUrl(Constant.SHARE_IMAGE_URL);
                oks2.setPlatform(Wechat.NAME);
                oks2.show(this);
                break;
            case EventTag.SMS_INVITE:   //短信邀请
                phone = (String) event.data;
                Logger.i("phonesms:" + phone);
                if (PhoneNumberUtils.isGlobalPhoneNumber(phone)) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
                    intent.putExtra("sms_body", Constant.SMS_CONTENT);
                    startActivity(intent);
                }
                break;
            case EventTag.PHONE_INVITE:    //电话邀请
                phone = (String) event.data;
                Logger.i("phonesms:" + phone);
                Uri uri = Uri.parse("tel:" + phone);
                Intent it = new Intent(Intent.ACTION_DIAL, uri);
                startActivity(it);
                break;
            case EventTag.EVENT_REFRESH_RELOAD:    //刷新重载
                if (TANetWorkUtil.isNetworkConnected(getApplicationContext())) {
                    Toast.makeText(MainActivity.this, "重新加载成功", Toast.LENGTH_SHORT).show();
                    mWebViewCustom.get("http://www.fields.gold");
                } else {
                    Toast.makeText(MainActivity.this, "请检查网络连接", Toast.LENGTH_SHORT).show();
                }
                break;
            case EventTag.EVENT_LOGIN_SUCCESS:    //登录成功
                //登陆成功的时候添加权限
                MainHelper.getInstance().addPermissByPermissionList(this, permissions, PERMISS_ALL);
                Logger.i("通讯录这块");
                LoginBackData data = (LoginBackData) event.data;

                JSONObject jsonObject = data.getJsonObject();
                String token = (String) jsonObject.get("token");
                String phoneNum = data.getPhone();
                Logger.i("phoneNum:" + phoneNum);

                String newStr = phoneNum.replaceAll("\"", "");
                Logger.i("rememberUsername:" + newStr + PhoneFormatCheckUtils.isPhoneLegal(newStr));
                if (PhoneFormatCheckUtils.isPhoneLegal(newStr)) {    //判断手机号码是否符合要求
                    SPUtils.setParam(MainActivity.this, "username", newStr);
                }
                SPUtils.setParam(MainActivity.this, "token", token);

                ContactIntentService.startActionContact(this);
                break;
            case EventTag.EVENT_CUTPHOTO:   //拍照截图
                mCut_model = (CutPhotoModel) event.data;
                String[] permissCamera = {Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};

                MainHelper.getInstance().addPermissByPermissionList(this, permissCamera, PERMISS_CAMERA, new MainHelper.OnHasPermiss() {
                    @Override
                    public void callback() {
                        clickll_head();
                    }
                });
                break;
            case EventTag.EGG:
                final CustomDialog mCDialog = new CustomDialog(MainActivity.this);
                mCDialog.setTitle("提示")
                        .setMessage("该模块尚未开发...")
                        .setPositive("确定")
                        .setNegtive("取消")
                        .setSingle(false).setOnClickBottomListener(new CustomDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        mCDialog.dismiss();
                    }

                    @Override
                    public void onNegtiveClick() {
                        Toast.makeText(MainActivity.this, "发现彩蛋！", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, TestActivity.class);
                        startActivity(intent);
                        MainActivity.this.finish();
                        mCDialog.dismiss();
                    }
                }).show();
                break;
            case EventTag.VIDEO_PLAY:
                //代码级开启硬件加速
//                mWebViewCustom.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                Logger.i("硬件加速");
                isPlay = false;
                break;
            case EventTag.VIDEO_QUIT:
                //关闭硬件加速
                Logger.i("取消硬件加速");
                mWebViewCustom.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                isPlay = true;
                break;
            default:
                break;
        }
    }

    /**
     * 销毁保存当前URL
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String url = mWebViewCustom.getUrl();
        savedInstanceState.putString(SAVE_CURRENT_URL, url);
    }

    /**
     * 恢复时候加载销毁时候的URL
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String url = savedInstanceState.getString(SAVE_CURRENT_URL);
        mWebViewCustom.get(url);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (getIntent().hasExtra(EXTRA_URL) & getIntent().getExtras() != null) {
            mCurrentUrl = getIntent().getExtras().getString(EXTRA_URL);
            mWebViewCustom.get(mCurrentUrl);
        }
    }

    @Override
    protected void onStart() {
        if ((boolean) SPUtils.getParam(MainActivity.this, "checkHasNavigationBar", false)) {
            hideBottomMenu();//隐藏底部
        }
        super.onStart();
    }

    /**
     * requestPermissions的回调
     * 一个或多个权限请求结果回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllGranted = true;
        //判断是否拒绝  拒绝后要怎么处理 以及取消再次提示的处理
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                hasAllGranted = false;
                break;
            }
        }
        switch (requestCode) { //同意权限的处理
            case PERMISS_CONTACT:   //获取通讯录的动态权限
                Logger.i("通讯录");
                if (hasAllGranted) {        //同意权限做的处理,开启服务提交通讯录
                    ContactIntentService.startActionContact(MainActivity.this);
                } else {                    //拒绝权限且权限被手动不再提示做的处理,打开权限
                    MainHelper.getInstance().dealwithPermiss(MainActivity.this, permissions[0]);
                }
                break;
            case PERMISS_CAMERA:    //获取相机的动态权限
                if (!hasAllGranted) {
                    MainHelper.getInstance().dealwithPermiss(MainActivity.this, permissions[0]);
                }
                break;
            case PERMISS_SMS:       //获取短信的动态权限
                if (!hasAllGranted) {
                    MainHelper.getInstance().dealwithPermiss(MainActivity.this, permissions[0]);
                }
                break;
            case PERMISS_DX:
                Logger.i("读写权限");
                break;
            case PERMISS_ALL:       //获取所有权限
                Logger.i("所有权限");
                break;
            default:        //其他
                Logger.i("其他");
                break;
        }
    }

    @Override
    public void callbackbitmap(Bitmap bit) {
        String base64img = SDImageUtil.bitmapToBase64(bit);
        base64img = "data:image/jpg;base64," + base64img;
        Log.d("callbackbitmap", base64img);
        mWebViewCustom.loadJsFunction(JsFunctionName.CUTCALLBACK, base64img);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILECHOOSER_RESULTCODE:
                PhotoHelper.getInstace().fileChooserResultcode(MainActivity.this, data, resultCode, mUploadMessage, mCameraFilePath, new PhotoHelper.OnCallBack() {
                    @Override
                    public void callBack() {
                        mUploadMessage = null;
                    }
                });
                break;
            case BotPhotoPopupView.REQUEST_CODE_TAKE_PHOTO:
                PhotoHelper.getInstace().requestCodeTakePhoto(MainActivity.this, data, resultCode, mBotPhotoPopupView, mCut_model);
                break;
            case BotPhotoPopupView.REQUEST_CODE_SELECT_PHOTO:
                PhotoHelper.getInstace().requestCodeSelectPhoto(MainActivity.this, data, resultCode, mBotPhotoPopupView, mCut_model);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //物理返回键退出的时候的处理
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                String url = mWebViewCustom.getOriginalUrl();
                if (url != null) {      //回退指定页面：获取Webview中的一些特殊页面，作物理回退键的处理
                    System.out.println("urlll:" + url + " -- urllllength:" + url.length());
                    if (url.contains("cellbox/input") | url.contains("user/work") | url.contains("add?value") | url.contains("user/educate") | url.contains("info/index") | url.contains("user/chooseCN")) {
                        if (mWebViewCustom.canGoBack()) {
                            mWebViewCustom.evaluateJavascript("javascript:jumpJs()", new com.tencent.smtt.sdk.ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String s) {
                                    System.out.println("successJump:" + s);
                                }
                            });
                        }
                        return false;
                    }
                    if (url.contains("mine/center") || url.contains("mine/apply") || url.contains("user/center") || url.contains("login")
                            || url.length() <= 42) {     //双击退出App：获取Webview中的一些特殊页面，作物理回退键的处理
//                        if (!isPlay) {
//                            mWebViewCustom.evaluateJavascript("javascript:physicsBack()", new com.tencent.smtt.sdk.ValueCallback<String>() {
//                                @Override
//                                public void onReceiveValue(String s) {
//                                    System.out.println("physicsBack:" + s);
//                                }
//                            });
//                            return false;
//                        }
                        //清除用户登录状态及信息
                        if (System.currentTimeMillis() - mExitTime > 2000) {
                            Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                        } else {
                            checkDeleteCache(); //是否点击了清除缓存，若是，退出前清除掉Webview缓存
                            LogUtil.d("已经双击退出exit");
                            App.getApplication().exitApp(false);
                        }
                        mExitTime = System.currentTimeMillis();
                    } else {
                        mWebViewCustom.goBack();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "网络问题，请稍后尝试", Toast.LENGTH_SHORT).show();
                    finish();
                }
            default:
                return false;
        }
    }

    @Override
    protected void onDestroy() {
        LogUtil.d("应用已经退出");
        checkDeleteCache();
        super.onDestroy();
    }

    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) { //授权回调
        if (i == Platform.ACTION_USER_INFOR) {
            PlatformDb platDB = platform.getDb();//获取平台数据DB
            token = platDB.getToken();
            gender = platDB.getUserGender();
            icon = platDB.getUserIcon();
            userId = platDB.getUserId();
            name = platDB.getUserName();
            platformName = platDB.getPlatformNname();
        }
        System.out.println("wx授权登录：" + platformName);
        switch (platformName) {
            case SINA_WEIBO_NAME:
                Logger.i("授权过了的userID:" + userId + "|name:" + name + "|platformName" + platformName);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebViewCustom.evaluateJavascript("javascript:checkLogin('" + userId + "','" + name + "','" + icon + "','" + platformName + "')", new com.tencent.smtt.sdk.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                Logger.i("新浪回调：" + s);
                            }
                        });
                    }
                });
                break;
            case QQ_NAME:
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebViewCustom.evaluateJavascript("javascript:checkLogin('" + userId + "','" + name + "','" + icon + "','" + platformName + "')", new com.tencent.smtt.sdk.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                Logger.i("QQ回调：" + s);
                            }
                        });
                    }
                });
                break;
            case WECHAT_NAME:
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebViewCustom.evaluateJavascript("javascript:checkLogin('" + userId + "','" + name + "','" + icon + "','" + platformName + "')", new com.tencent.smtt.sdk.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                Logger.i("微信回调" + s);
                            }
                        });
                    }
                });
                break;
        }
        Logger.i("授权成功1" + "名字是：" + name + "——性别是：" + gender + "——头像是："
                + icon + "——用户id是：" + userId + "——token是：" + token);
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        Logger.i("授权失败！" + throwable.toString());
    }

    @Override
    public void onCancel(Platform platform, int i) {
        Logger.i("授权取消！");
    }
}
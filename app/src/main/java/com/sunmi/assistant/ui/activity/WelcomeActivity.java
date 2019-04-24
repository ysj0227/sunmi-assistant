package com.sunmi.assistant.ui.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.sunmi.apmanager.rpc.cloud.CloudApi;
import com.sunmi.apmanager.update.AppUpdate;
import com.sunmi.apmanager.utils.CommonUtils;
import sunmi.common.utils.SpUtils;
import com.sunmi.assistant.MyApplication;
import com.sunmi.assistant.R;
import com.sunmi.assistant.ui.activity.login.LoginActivity_;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatService;
import com.zhy.http.okhttp.callback.StringCallback;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;
import sunmi.common.base.BaseActivity;
import sunmi.common.base.BaseApplication;
import sunmi.common.utils.NetworkUtils;
import sunmi.common.utils.ThreadPool;
import sunmi.common.utils.log.LogCat;
import sunmi.common.view.dialog.CommonDialog;

/**
 * 欢迎页
 */
@EActivity(R.layout.activity_welcome)
public class WelcomeActivity extends BaseActivity {
    private static final int INSTALL_PERMISS_CODE = 900;
    private String appUrl = "";

    @AfterViews
    protected void init() {
        SpUtils.saveHeightPixel(this);//手机像素高度
        if (TextUtils.equals(SpUtils.getLead(), "TRUE")) {
            launch();
        } else {
            LeadPagesActivity_.intent(context).start();
            finish();
        }
        initMTA();
    }

    /**
     * 运营统计
     */
    private void initMTA() {
        try {
            StatService.startStatService(this, "A6INR132MGAI",
                    com.tencent.stat.common.StatConstants.VERSION);
        } catch (MtaSDkException e) {
            e.printStackTrace();
        }
    }

    private void launch() {
        ThreadPool.getCachedThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                LogCat.e(TAG, "ping time -- 111");
                if (!NetworkUtils.isNetPingUsable()) {
                    LogCat.e(TAG, "ping time -- 222");
                    if (TextUtils.equals(SpUtils.getLoginStatus(), "Y")) {
                        gotoMainActivity();
                    } else {
                        gotoLoginActivity();
                    }
                } else {
                    LogCat.e(TAG, "ping time -- 333");
                    checkUpdate();
                }
            }
        });
    }

    private void handleLaunch() {
        //状态登录保存，退出登录置空，检查token是否有效
        if (TextUtils.equals(SpUtils.getLoginStatus(), "Y")) {
            if (!NetworkUtils.isNetworkAvailable(this)) {
                gotoMainActivity();
            } else {
                checkToken();
            }
        } else {
            gotoLoginActivity();
        }
    }

    private void gotoLoginActivity() {
        LoginActivity_.intent(context).start();
        finish();
    }

    private void gotoMainActivity() {
        MainActivity_.intent(context).start();
        finish();
    }

    private void handlerDelay(long delayMillis, final Class<?> mClass) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openActivity(context, mClass, true);
            }
        }, delayMillis);
    }

    private void checkToken() {
        CloudApi.checkToken(new StringCallback() {
            @Override
            public void onError(Call call, Response response, Exception e, int id) {
                logout();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    if (response != null) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("code") && jsonObject.getInt("code") == 1) {
                            MyApplication.isCheckedToken = true;
                            gotoMainActivity();
                            return;
                        }
                    }
                    logout();
                } catch (Exception e) {
                    e.printStackTrace();
                    logout();
                }
            }
        });
    }

    private void logout() {
        CommonUtils.logout();
        gotoLoginActivity();
    }

    private void checkUpdate() {
        CloudApi.checkUpgrade(new StringCallback() {
            @Override
            public void onError(Call call, Response response, Exception e, int id) {
                handleLaunch();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    if (response != null) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("code") && jsonObject.getInt("code") == 1) {
                            JSONObject object = (JSONObject) jsonObject.getJSONArray("data").opt(0);
                            if (object.has("is_force_upgrade")) {
                                // 是否需要强制升级 0-否 1-是
                                int needMerge = object.getInt("is_force_upgrade");
                                if (needMerge == 1) {
                                    appUrl = object.getString("url");
                                    forceUpdate(appUrl);
                                    return;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handleLaunch();
            }
        });
    }

    @UiThread
    void forceUpdate(final String url) {
        getUpgradeDialog(url).show();
    }

    private CommonDialog getUpgradeDialog(final String url) {
        CommonDialog commonDialog = new CommonDialog.Builder(this)
                .setTitle(R.string.tip_title_upgrade)
                .setMessage(R.string.tip_message_upgrade)
                .setConfirmButton(R.string.go_upgrade, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //8.0上授权是否允许安装未知来源
                        requestPackagePermission();
                    }
                }).create();
        commonDialog.showWithOutTouchable(false);
        return commonDialog;
    }

    /**
     * 8.0上授权是否允许安装未知来源
     */
    private void requestPackagePermission() {
        if (haveInstallPermission() || TextUtils.equals("ZUK", android.os.Build.BRAND)) {
            AppUpdate.versionUpdate((Activity) context, appUrl);
        } else {
            Toast.makeText(context, R.string.str_open_permission_to_update, Toast.LENGTH_LONG).show();
            //跳转设置开启允许安装
            Uri packageURI = Uri.parse("package:" + context.getPackageName());
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
            startActivityForResult(intent, INSTALL_PERMISS_CODE);
        }
    }

    private boolean haveInstallPermission() {
        //先获取是否有安装未知来源应用的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean haveInstallPermission = context.getPackageManager().canRequestPackageInstalls();
            if (haveInstallPermission) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == INSTALL_PERMISS_CODE) {
            requestPackagePermission();
        } else {
            BaseApplication.getInstance().quit();
        }
    }

}

package com.sunmi.ipc.ipcset;

import android.content.Context;

import com.sunmi.ipc.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.nio.charset.Charset;

import sunmi.common.base.BaseMvpActivity;
import sunmi.common.model.SunmiDevice;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.view.SettingItemLayout;
import sunmi.common.view.dialog.InputDialog;

/**
 * @author YangShiJie
 * @date 2019/7/12
 */
@EActivity(resName = "activity_ipc_setting")
public class IpcSettingActivity extends BaseMvpActivity<IpcSettingPresenter>
        implements IpcSettingContract.View {

    private static final int IPC_NAME_MAX_LENGTH = 36;

    @Extra
    SunmiDevice mDevice;

    @ViewById(resName = "sil_camera_name")
    SettingItemLayout mNameView;

    @AfterViews
    void init() {
        StatusBarUtils.setStatusBarColor(this, StatusBarUtils.TYPE_DARK);
        mPresenter = new IpcSettingPresenter();
        mPresenter.attachView(this);
        mPresenter.loadConfig(mDevice);

        mNameView.setRightText(mDevice.getName());
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Click(resName = "sil_camera_name")
    void cameraNameClick() {
        new InputDialog.Builder(this)
                .setTitle(R.string.ipc_setting_name)
                .setInitInputContent(mDevice.getName())
                .setCancelButton(R.string.sm_cancel)
                .setConfirmButton(R.string.ipc_setting_save, new InputDialog.ConfirmClickListener() {
                    @Override
                    public void onConfirmClick(InputDialog dialog, String input) {
                        if (input.trim().getBytes(Charset.defaultCharset()).length > IPC_NAME_MAX_LENGTH) {
                            shortTip(R.string.ipc_setting_tip_name_length);
                            return;
                        }
                        mPresenter.updateName(input);
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    @Click(resName = "sil_camera_detail")
    void cameraDetailClick() {
        IpcSettingDetailActivity_.intent(this)
                .mDevice(mDevice)
                .start();
    }

    @Click(resName = "sil_voice_exception")
    void soundAbnormalDetection() {
        IpcSettingDetectionActivity_.intent(this)
                .mType(IpcSettingDetectionActivity.TYPE_SOUND)
                .start();
    }

    @Click(resName = "sil_active_exception")
    void activeAbnormalDetection() {
        IpcSettingDetectionActivity_.intent(this)
                .mType(IpcSettingDetectionActivity.TYPE_ACTIVE)
                .start();
    }

    @Override
    public void updateAllView(IpcSettingModel info) {
        // TODO: 更新所有View或者显示网络异常
    }

    @Override
    public void updateNameView(String name) {
        mDevice.setName(name);
        mNameView.setRightText(name);
    }

}

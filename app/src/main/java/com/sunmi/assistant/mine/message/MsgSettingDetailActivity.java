package com.sunmi.assistant.mine.message;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.sunmi.assistant.R;
import com.sunmi.assistant.mine.contract.MsgSettingDetailContract;
import com.sunmi.assistant.mine.model.MsgSettingChildren;
import com.sunmi.assistant.mine.presenter.MsgSettingDetailPresenter;
import com.sunmi.assistant.utils.MessageUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import sunmi.common.base.BaseMvpActivity;
import sunmi.common.constant.CommonNotifications;
import sunmi.common.notification.BaseNotification;
import sunmi.common.utils.NetworkUtils;
import sunmi.common.view.CommonListAdapter;
import sunmi.common.view.TitleBarView;
import sunmi.common.view.ViewHolder;

/**
 * @author linyuanpeng on 2019-08-26.
 */

@EActivity(R.layout.activity_msg_setting_detail)
public class MsgSettingDetailActivity extends BaseMvpActivity<MsgSettingDetailPresenter>
        implements MsgSettingDetailContract.View, CompoundButton.OnCheckedChangeListener {

    @ViewById(R.id.title_bar)
    TitleBarView titleBar;
    @ViewById(R.id.switch_main)
    Switch sMian;
    @ViewById(R.id.rv_setting_detail)
    RecyclerView rvSetting;
    @ViewById(R.id.tv_main)
    TextView tvMain;

    @Extra
    String title;
    @Extra
    MsgSettingChildren child;

    private Switch changedSwitch;
    private boolean allowCheck = true;


    @AfterViews
    void init() {
        titleBar.setAppTitle(getString(R.string.str_msg_setting_detail, title));
        titleBar.getLeftLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tvMain.setText(title);
        sMian.setChecked(child.getStatus() == 1);
        mPresenter = new MsgSettingDetailPresenter();
        mPresenter.attachView(this);
        sMian.setOnCheckedChangeListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        rvSetting.setLayoutManager(layoutManager);
        initDetail();
    }

    private void initDetail() {
        rvSetting.setAdapter(new CommonListAdapter<MsgSettingChildren>(context, R.layout.item_msg_device_setting_detail, child.getChildren()) {
            @Override
            public void convert(ViewHolder holder, MsgSettingChildren children) {
                holder.setText(R.id.tv_msg_setting, MessageUtils.getInstance().getMsgFirst(children.getName()));
                Switch sw = holder.getView(R.id.switch_msg);
                sw.setChecked(children.getStatus() == 1);
                sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        changeStatus(isChecked, children.getId(), sw);
                        changedSwitch = sw;
                    }
                });

            }
        });
        if (child.getStatus() == 0) {
            rvSetting.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        BaseNotification.newInstance().postNotificationName(CommonNotifications.msgSettingsChange);
        finish();
    }

    @Override
    public void updateSettingStatusSuccess(int msgId, int status) {
        if (msgId == child.getId()) {
            if (status == 1) {
                rvSetting.setVisibility(View.VISIBLE);
            } else {
                rvSetting.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void updateSettingStatusFail(int msgId, int status) {
        allowCheck = false;
        changedSwitch.setChecked(status == 0);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.switch_main) {
            changeStatus(isChecked, child.getId(), sMian);
            changedSwitch = sMian;
        }
    }

    private void changeStatus(boolean isChecked, int settingId, Switch sw) {
        if (noNetCannotClick()) {
            sw.setChecked(!isChecked);
            return;
        }
        if (!allowCheck) {
            allowCheck = true;
            return;
        }
        if (isChecked) {
            mPresenter.updateSettingStatus(settingId, 1);
        } else {
            mPresenter.updateSettingStatus(settingId, 0);
        }
    }

    private boolean noNetCannotClick() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            shortTip(R.string.toast_network_error);
            return true;
        }
        return false;
    }
}

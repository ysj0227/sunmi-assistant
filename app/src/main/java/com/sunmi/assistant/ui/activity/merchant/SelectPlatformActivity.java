package com.sunmi.assistant.ui.activity.merchant;

import android.annotation.SuppressLint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunmi.assistant.R;
import com.sunmi.assistant.ui.activity.contract.SelectPlatformContract;
import com.sunmi.assistant.ui.activity.model.PlatformInfo;
import com.sunmi.assistant.ui.activity.presenter.PlatformPresenter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import sunmi.common.base.BaseMvpActivity;
import sunmi.common.utils.CommonHelper;
import sunmi.common.utils.GotoActivityUtils;
import sunmi.common.utils.SpUtils;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.utils.log.LogCat;
import sunmi.common.view.CommonListAdapter;
import sunmi.common.view.TitleBarView;
import sunmi.common.view.ViewHolder;

/**
 * 选择平台
 *
 * @author YangShiJie
 * @date 2019/6/26
 */
@SuppressLint("Registered")
@EActivity(R.layout.activity_merchant_select_platform)
public class SelectPlatformActivity extends BaseMvpActivity<PlatformPresenter>
        implements SelectPlatformContract.View, View.OnClickListener {
    @ViewById(R.id.title_bar)
    TitleBarView titleBar;
    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;
    @ViewById(R.id.btn_next)
    Button btnNext;
    private String selectPlatform;
    private int selectSaasSource;

    @Extra
    boolean isCanBack;
    @Extra
    boolean isLoginSuccess;

    @AfterViews
    void init() {
        //状态栏
        StatusBarUtils.setStatusBarColor(this, StatusBarUtils.TYPE_DARK);
        if (isCanBack) {
            titleBar.setLeftImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_back_dark));
        }
        initRecycler();
        CommonHelper.isCanClick(btnNext, false);
        mPresenter = new PlatformPresenter();
        mPresenter.attachView(this);
        showLoadingDialog();
        mPresenter.getPlatformInfo();
    }

    @Override
    public void onBackPressed() {
        if (isCanBack) {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                onBackPressed();
                break;
            case R.id.txt_right:
                SpUtils.setSaasExist(0);//没有对接saas数据Q
                GotoActivityUtils.gotoMainActivity(this);
                break;
            default:
                break;
        }
    }

    @Click({R.id.btn_next})
    void btnNext() {
        CheckPlatformMobileActivity_.intent(this)
                .platform(selectPlatform)
                .saasSource(selectSaasSource)
                .isLoginSuccess(isLoginSuccess)
                .start();
    }

    private void initRecycler() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        titleBar.getLeftImg().setOnClickListener(this);
        titleBar.getRightTextView().setOnClickListener(this);
    }


    @Override
    public void getPlatformInfoSuccess(PlatformInfo data) {
        hideLoadingDialog();
        showViewList(data);
    }

    @Override
    public void getPlatformInfoFail(int code, String msg) {
        LogCat.e(TAG, "data onFail code=" + code + "," + msg);
        hideLoadingDialog();
    }

    private void showViewList(PlatformInfo data) {
        List<PlatformInfo.SaasListBean> list = data.getSaasList();
        recyclerView.setAdapter(new CommonListAdapter<PlatformInfo.SaasListBean>(context,
                R.layout.item_merchant_platform, list) {
            int selectedIndex = -1;

            @Override
            public void convert(ViewHolder holder, final PlatformInfo.SaasListBean bean) {
                TextView tvPlatform = holder.getView(R.id.tv_platform);
                tvPlatform.setText(bean.getSaas_name());
                ImageView ivSelect = holder.getView(R.id.iv_select);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedIndex = holder.getAdapterPosition();
                        selectPlatform = bean.getSaas_name();
                        selectSaasSource = bean.getSaas_source();
                        notifyDataSetChanged();//刷新
                        CommonHelper.isCanClick(btnNext, true);
                    }
                });
                if (selectedIndex == holder.getAdapterPosition()) {
                    tvPlatform.setTextColor(getResources().getColor(R.color.common_orange));
                    ivSelect.setVisibility(View.VISIBLE);
                } else {
                    tvPlatform.setTextColor(getResources().getColor(R.color.text_color));
                    ivSelect.setVisibility(View.GONE);
                }
            }
        });
    }

}

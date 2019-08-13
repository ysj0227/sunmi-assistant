package com.sunmi.assistant.mine.shop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.sunmi.apmanager.constant.Constants;
import com.sunmi.apmanager.utils.CommonUtils;
import com.sunmi.assistant.R;
import com.sunmi.assistant.data.SunmiStoreRemote;
import com.sunmi.assistant.mine.CreateShopActivity_;
import com.sunmi.assistant.mine.platform.SelectPlatformActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import sunmi.common.base.BaseActivity;
import sunmi.common.model.ShopListResp;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.utils.SpUtils;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.utils.log.LogCat;
import sunmi.common.view.CommonListAdapter;
import sunmi.common.view.ViewHolder;
import sunmi.common.view.bottompopmenu.BottomPopMenu;
import sunmi.common.view.bottompopmenu.PopItemAction;

/**
 * 我的店铺
 * @author yangshijie
 */
@SuppressLint("Registered")
@EActivity(R.layout.activity_mine_my_store)
public class ShopListActivity extends BaseActivity {

    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;

    @AfterViews
    void init() {
        StatusBarUtils.setStatusBarColor(this, StatusBarUtils.TYPE_DARK);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        getShopList();
    }

    @Click(R.id.btnAdd)
    void onAddClick() {
        CommonUtils.trackCommonEvent(context, "addStore",
                "主页_我的_我的店铺_添加店铺", Constants.EVENT_MY_INFO);
        BottomPopMenu choosePhotoMenu = new BottomPopMenu.Builder(this)
                .setTitle(R.string.company_shop_new_create_or_import)
                .setIsShowCircleBackground(true)
                .addItemAction(new PopItemAction(R.string.company_shop_new_create,
                        PopItemAction.PopItemStyle.Normal, this::createShop))
                .addItemAction(new PopItemAction(R.string.company_shop_import,
                        PopItemAction.PopItemStyle.Normal, this::importShop))
                .addItemAction(new PopItemAction(R.string.sm_cancel,
                        PopItemAction.PopItemStyle.Cancel))
                .create();
        choosePhotoMenu.show();

    }

    private void createShop() {
        CreateShopActivity_.intent(context)
                .companyId(SpUtils.getCompanyId())
                .isLogin(true)
                .start();
    }

    private void importShop() {
        SelectPlatformActivity_.intent(context)
                .isCanBack(true)
                .isLogin(true)
                .start();
    }

    private void getShopList() {
        SunmiStoreRemote.get().getShopList(SpUtils.getCompanyId(), new RetrofitCallback<ShopListResp>() {
            @Override
            public void onSuccess(int code, String msg, ShopListResp data) {
                updateList(data.getShop_list());
            }

            @Override
            public void onFail(int code, String msg, ShopListResp data) {
                LogCat.e(TAG, "Get shop list Failed. " + msg);
                shortTip(getString(R.string.str_store_load_error));
            }
        });
    }

    private void updateList(List<ShopListResp.ShopInfo> list) {
        recyclerView.setAdapter(new ShopListAdapter(this, R.layout.item_mine_store, list));
    }

    private static class ShopListAdapter extends CommonListAdapter<ShopListResp.ShopInfo> {

        private ShopListAdapter(Context context, int layoutId, List<ShopListResp.ShopInfo> list) {
            super(context, layoutId, list);
        }

        @Override
        public void convert(ViewHolder holder, ShopListResp.ShopInfo info) {
            holder.setText(R.id.tvName, info.getShop_name());
            holder.itemView.setOnClickListener(v -> {
                CommonUtils.trackCommonEvent(mContext, "defaultStore",
                        "主页_我的_我的店铺_默认店铺", Constants.EVENT_MY_INFO);
                ShopDetailActivity_.intent(mContext).shopId(info.getShop_id()).start();
            });
        }
    }

}
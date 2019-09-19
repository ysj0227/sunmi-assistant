package com.sunmi.assistant.dashboard.card;

import android.content.Context;
import android.support.annotation.NonNull;

import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.DashboardContract;

import retrofit2.Call;
import sunmi.common.base.recycle.BaseViewHolder;
import sunmi.common.rpc.retrofit.BaseResponse;

/**
 * @author yinhui
 * @since 2019-07-01
 */
public class EmptyDataCard extends BaseRefreshItem<EmptyDataCard.Model, Object> {

    public EmptyDataCard(Context context, DashboardContract.Presenter presenter) {
        super(context, presenter, 0);
    }

    @Override
    protected Model createModel(Context context) {
        return new Model();
    }

    @Override
    public int getLayoutId(int type) {
        return R.layout.dashboard_recycle_item_empty;
    }

    @Override
    protected Call<BaseResponse<Object>> load(int companyId, int shopId, int period, CardCallback callback) {
        return null;
    }

    @Override
    protected void setupModel(Model model, Object response) {
    }

    @Override
    protected void setupView(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
    }

    public static class Model extends BaseRefreshItem.BaseModel {
    }
}
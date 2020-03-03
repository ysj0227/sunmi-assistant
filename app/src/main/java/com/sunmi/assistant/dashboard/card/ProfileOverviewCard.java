package com.sunmi.assistant.dashboard.card;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.BaseRefreshCard;
import com.sunmi.assistant.dashboard.Constants;
import com.sunmi.assistant.dashboard.Utils;

import retrofit2.Call;
import sunmi.common.base.recycle.BaseViewHolder;
import sunmi.common.base.recycle.ItemType;
import sunmi.common.model.CustomerDataResp;
import sunmi.common.rpc.cloud.SunmiStoreApi;
import sunmi.common.rpc.retrofit.BaseResponse;

/**
 * @author yinhui
 * @since 2019-07-01
 */
public class ProfileOverviewCard extends BaseRefreshCard<ProfileOverviewCard.Model, CustomerDataResp> {

    private static ProfileOverviewCard sInstance;

    private ProfileOverviewCard(Presenter presenter, int source) {
        super(presenter, source);
    }

    public static ProfileOverviewCard get(Presenter presenter, int source) {
        if (sInstance == null) {
            sInstance = new ProfileOverviewCard(presenter, source);
        } else {
            sInstance.reset(presenter, source);
        }
        return sInstance;
    }

    @Override
    public void init(Context context) {
    }

    @Override
    public int getLayoutId(int type) {
        return R.layout.dashboard_item_profile_overview;
    }

    @Override
    protected Call<BaseResponse<CustomerDataResp>> load(int companyId, int shopId, int period, CardCallback callback) {
        SunmiStoreApi.getInstance().getCustomerData(companyId, shopId, period, callback);
        return null;
    }

    @Override
    protected Model createModel() {
        return new Model();
    }

    @Override
    protected void setupModel(Model model, CustomerDataResp response) {
        model.unknownCustomer = response.getLatestEntryHeadCount();
        model.lastUnknownCustomer = response.getEarlyEntryHeadCount();
        model.customer = response.getLatestPassengerCount() + model.unknownCustomer;
        model.lastCustomer = response.getEarlyPassengerCount() + model.lastUnknownCustomer;
        model.newCustomer = response.getLatestStrangerPassengerCount();
        model.lastNewCustomer = response.getEarlyStrangerPassengerCount();
        model.oldCustomer = response.getLatestRegularPassengerCount();
        model.lastOldCustomer = response.getEarlyRegularPassengerCount();
    }

    @NonNull
    @Override
    public BaseViewHolder<Model> onCreateViewHolder(@NonNull View view, @NonNull ItemType<Model, BaseViewHolder<Model>> type) {
        BaseViewHolder<Model> holder = super.onCreateViewHolder(view, type);
        Context context = holder.getContext();
        holder.addOnClickListener(R.id.layout_dashboard_main, (h, model, position)
                -> goToCustomerList(context));
        return holder;
    }

    @Override
    protected void setupView(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        setupPeriod(holder, model.period);
        Context context = holder.getContext();

        ProgressBar pb = holder.getView(R.id.bar_dashboard_main);
        TextView value = holder.getView(R.id.tv_dashboard_value);
        TextView subData = holder.getView(R.id.tv_dashboard_subdata);
        TextView newValue = holder.getView(R.id.tv_dashboard_new);
        TextView newSubData = holder.getView(R.id.tv_dashboard_new_subdata);
        TextView oldValue = holder.getView(R.id.tv_dashboard_old);
        TextView oldSubData = holder.getView(R.id.tv_dashboard_old_subdata);
        TextView unknownValue = holder.getView(R.id.tv_dashboard_unknown);
        TextView unknownSubData = holder.getView(R.id.tv_dashboard_unknown_subdata);
        if (model.customer > 0) {
            pb.setMax(model.customer);
            pb.setSecondaryProgress(model.newCustomer + model.oldCustomer);
            pb.setProgress(model.newCustomer);
        } else {
            pb.setMax(1);
            pb.setProgress(0);
            pb.setSecondaryProgress(0);
        }
        value.setText(Utils.formatNumber(context, model.customer, false, true));
        subData.setText(Utils.formatNumber(context, model.lastCustomer, false, false));
        newValue.setText(Utils.formatNumber(context, model.newCustomer, false, true));
        newSubData.setText(Utils.formatNumber(context, model.lastNewCustomer, false, false));
        oldValue.setText(Utils.formatNumber(context, model.oldCustomer, false, true));
        oldSubData.setText(Utils.formatNumber(context, model.lastOldCustomer, false, false));
        unknownValue.setText(Utils.formatNumber(context, model.unknownCustomer, false, true));
        unknownSubData.setText(Utils.formatNumber(context, model.lastUnknownCustomer, false, false));
    }

    @Override
    protected void showLoading(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        holder.getView(R.id.iv_dashboard_loading).setVisibility(View.VISIBLE);
        holder.getView(R.id.bar_dashboard_main).setVisibility(View.INVISIBLE);
        holder.getView(R.id.layout_dashboard_main).setVisibility(View.INVISIBLE);
        holder.getView(R.id.layout_dashboard_new).setVisibility(View.INVISIBLE);
        holder.getView(R.id.layout_dashboard_old).setVisibility(View.INVISIBLE);
        holder.getView(R.id.layout_dashboard_unknown).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void showError(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        setupPeriod(holder, model.period);

        ProgressBar pb = holder.getView(R.id.bar_dashboard_main);
        TextView value = holder.getView(R.id.tv_dashboard_value);
        TextView subData = holder.getView(R.id.tv_dashboard_subdata);
        TextView newValue = holder.getView(R.id.tv_dashboard_new);
        TextView newSubData = holder.getView(R.id.tv_dashboard_new_subdata);
        TextView oldValue = holder.getView(R.id.tv_dashboard_old);
        TextView oldSubData = holder.getView(R.id.tv_dashboard_old_subdata);
        TextView unknownValue = holder.getView(R.id.tv_dashboard_unknown);
        TextView unknownSubData = holder.getView(R.id.tv_dashboard_unknown_subdata);
        value.setText(Utils.DATA_NONE);
        subData.setText(Utils.DATA_NONE);
        newValue.setText(Utils.DATA_NONE);
        newSubData.setText(Utils.DATA_NONE);
        oldValue.setText(Utils.DATA_NONE);
        oldSubData.setText(Utils.DATA_NONE);
        unknownValue.setText(Utils.DATA_NONE);
        unknownSubData.setText(Utils.DATA_NONE);
        pb.setMax(1);
        pb.setProgress(0);
        pb.setSecondaryProgress(0);
    }

    /**
     * 设置不同Period 的文案显示
     */
    private void setupPeriod(@NonNull BaseViewHolder<Model> holder, int period) {
        // 切换展示内容
        holder.getView(R.id.iv_dashboard_loading).setVisibility(View.INVISIBLE);
        holder.getView(R.id.bar_dashboard_main).setVisibility(View.VISIBLE);
        holder.getView(R.id.layout_dashboard_main).setVisibility(View.VISIBLE);
        holder.getView(R.id.layout_dashboard_new).setVisibility(View.VISIBLE);
        holder.getView(R.id.layout_dashboard_old).setVisibility(View.VISIBLE);
        holder.getView(R.id.layout_dashboard_unknown).setVisibility(View.VISIBLE);

        TextView subTitle = holder.getView(R.id.tv_dashboard_subtitle);
        TextView newCustomerSubTitle = holder.getView(R.id.tv_dashboard_new_subtitle);
        TextView oldCustomerSubTitle = holder.getView(R.id.tv_dashboard_old_subtitle);
        switch (period) {
            case Constants.TIME_PERIOD_WEEK:
                subTitle.setText(R.string.dashboard_period_last_week);
                newCustomerSubTitle.setText(R.string.dashboard_period_last_week);
                oldCustomerSubTitle.setText(R.string.dashboard_period_last_week);
                break;
            case Constants.TIME_PERIOD_MONTH:
                subTitle.setText(R.string.dashboard_period_last_month);
                newCustomerSubTitle.setText(R.string.dashboard_period_last_month);
                oldCustomerSubTitle.setText(R.string.dashboard_period_last_month);
                break;
            case Constants.TIME_PERIOD_YESTERDAY:
                subTitle.setText(R.string.dashboard_period_last_day);
                newCustomerSubTitle.setText(R.string.dashboard_period_last_day);
                oldCustomerSubTitle.setText(R.string.dashboard_period_last_day);
                break;
            default:
                break;
        }
    }

    private void goToCustomerList(Context context) {
        // TODO: Customer List
    }

    public static class Model extends BaseRefreshCard.BaseModel {

        private int customer;
        private int lastCustomer;
        private int unknownCustomer;
        private int lastUnknownCustomer;
        private int newCustomer;
        private int lastNewCustomer;
        private int oldCustomer;
        private int lastOldCustomer;

    }
}

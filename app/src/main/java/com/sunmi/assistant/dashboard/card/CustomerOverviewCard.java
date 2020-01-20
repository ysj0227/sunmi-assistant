package com.sunmi.assistant.dashboard.card;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.BaseRefreshCard;
import com.sunmi.assistant.dashboard.Constants;
import com.sunmi.assistant.dashboard.Utils;

import retrofit2.Call;
import sunmi.common.base.recycle.BaseViewHolder;
import sunmi.common.model.CustomerDataResp;
import sunmi.common.rpc.cloud.SunmiStoreApi;
import sunmi.common.rpc.retrofit.BaseResponse;

/**
 * Description:
 *
 * @author linyuanpeng on 2020-01-14.
 */
public class CustomerOverviewCard extends BaseRefreshCard<CustomerOverviewCard.Model, CustomerDataResp> {

    private static CustomerOverviewCard sInstance;

    private static final int NUM_100_MILLION = 100_000_000;

    private CustomerOverviewCard(Presenter presenter, int source) {
        super(presenter, source);
    }

    public static CustomerOverviewCard get(Presenter presenter, int source) {
        if (sInstance == null) {
            sInstance = new CustomerOverviewCard(presenter, source);
        } else {
            sInstance.reset(presenter, source);
        }
        return sInstance;
    }


    @Override
    public void init(Context context) {
        getModel().init(context);
    }

    @Override
    public int getLayoutId(int type) {
        return R.layout.dashboard_item_customer_overview;
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
        int latestCount = response.getLatestPassengerCount();
        int earlyCount = response.getEarlyPassengerCount();
        int latestPassCount = response.getLatestPassPassengerCount();
        int earlyPassCont = response.getEarlyPassPassengerCount();
        int latestUniq = response.getLatestUniqPassengerCount();
        int earlyUniq = response.getEarlyUniqPassengerCount();
        model.latestCount = latestCount;
        model.earlyCount = earlyCount;
        if (latestCount > 0) {
            model.latestEnterRate = latestCount / (latestCount + latestPassCount);
        } else {
            model.latestEnterRate = 0;
        }
        if (earlyCount > 0) {
            model.earlyEnterRate = earlyCount / (earlyCount + earlyPassCont);
        } else {
            model.earlyEnterRate = 0;
        }
        if (latestUniq > 0) {
            model.latestEnterFrequency = latestCount / latestUniq;
        } else {
            model.latestEnterFrequency = 0;
        }
        if (earlyUniq > 0) {
            model.latestEnterFrequency = latestCount / earlyUniq;
        } else {
            model.earlyEnterRate = 0;
        }
    }

    @Override
    protected void setupView(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        setupPeriod(holder, model.period);
        Context context = holder.getContext();
        TextView enterFrequency = holder.getView(R.id.tv_enter_frequency);
        TextView enterFrequencySubData = holder.getView(R.id.tv_enter_frequency_subdata);
        TextView value = holder.getView(R.id.tv_dashboard_value);
        TextView subData = holder.getView(R.id.tv_dashboard_subdata);
        TextView enterRate = holder.getView(R.id.tv_enter_rate);
        TextView enterRateSubData = holder.getView(R.id.tv_enter_rate_subdata);

        enterFrequency.setText(Utils.createFrequencyText(context, model.period, model.getLatestEnterFrequency(), true));
        enterFrequencySubData.setText(Utils.createFrequencyText(context, model.period, model.getEarlyEnterFrequency(), false));

        value.setText(model.getLatestCount());
        subData.setText(model.getEarlyCount());
        enterRate.setText(model.getLatestEnterRate());
        enterRateSubData.setText(model.getEarlyEnterRate());
    }

    @Override
    protected void showLoading(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        View main = holder.getView(R.id.layout_dashboard_main);
        View enterRate = holder.getView(R.id.layout_enter_rate);
        View enterFrequency = holder.getView(R.id.layout_enter_frequency);
        ImageView loading = holder.getView(R.id.iv_dashboard_loading);
        loading.setImageResource(R.mipmap.dashboard_skeleton_multi);
        main.setVisibility(View.INVISIBLE);
        enterRate.setVisibility(View.INVISIBLE);
        enterFrequency.setVisibility(View.INVISIBLE);
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    protected void showError(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        setupPeriod(holder, model.period);
        TextView enterFrequency = holder.getView(R.id.tv_enter_frequency);
        TextView enterFrequencySubData = holder.getView(R.id.tv_enter_frequency_subdata);
        TextView value = holder.getView(R.id.tv_dashboard_value);
        TextView subData = holder.getView(R.id.tv_dashboard_subdata);
        TextView enterRate = holder.getView(R.id.tv_enter_rate);
        TextView enterRateSubData = holder.getView(R.id.tv_enter_rate_subdata);
        enterFrequency.setText(DATA_NONE);
        enterFrequencySubData.setText(DATA_NONE);
        value.setText(DATA_NONE);
        subData.setText(DATA_NONE);
        enterRate.setText(DATA_NONE);
        enterRateSubData.setText(DATA_NONE);
    }

    /**
     * 设置不同Period 的文案显示
     *
     * @param holder
     * @param period
     */
    private void setupPeriod(@NonNull BaseViewHolder<Model> holder, int period) {
        TextView subTitle = holder.getView(R.id.tv_dashboard_subtitle);
        TextView enterRateSubTitle = holder.getView(R.id.tv_enter_rate_subtitle);
        TextView enterFrequencySubTitle = holder.getView(R.id.tv_enter_frequency_subtitle);
        holder.getView(R.id.iv_dashboard_loading).setVisibility(View.GONE);
        holder.getView(R.id.layout_dashboard_main).setVisibility(View.VISIBLE);
        holder.getView(R.id.layout_enter_rate).setVisibility(View.VISIBLE);
        holder.getView(R.id.layout_enter_frequency).setVisibility(View.VISIBLE);
        switch (period) {
            case Constants.TIME_PERIOD_WEEK:
                subTitle.setText(R.string.dashboard_card_customer_last_week);
                enterRateSubTitle.setText(R.string.dashboard_card_customer_last_week);
                enterFrequencySubTitle.setText(R.string.dashboard_card_customer_last_week);
                break;
            case Constants.TIME_PERIOD_MONTH:
                subTitle.setText(R.string.dashboard_card_customer_last_month);
                enterRateSubTitle.setText(R.string.dashboard_card_customer_last_month);
                enterFrequencySubTitle.setText(R.string.dashboard_card_customer_last_month);
                break;
            case Constants.TIME_PERIOD_YESTERDAY:
                subTitle.setText(R.string.dashboard_card_customer_last_day);
                enterRateSubTitle.setText(R.string.dashboard_card_customer_last_day);
                enterFrequencySubTitle.setText(R.string.dashboard_card_customer_last_day);
                break;
            default:
                break;
        }
    }

    public static class Model extends BaseRefreshCard.BaseModel {
        private String mNum100Million;

        int latestCount;
        int earlyCount;
        float latestEnterRate;
        float earlyEnterRate;
        float latestEnterFrequency;
        float earlyEnterFrequency;

        public void init(Context context) {
            mNum100Million = context.getString(R.string.str_num_100_million);
        }

        private String getLatestCount() {
            if (latestCount > NUM_100_MILLION) {
                return FORMAT_THOUSANDS.format(latestCount / NUM_100_MILLION) + mNum100Million;
            } else {
                return FORMAT_THOUSANDS.format(latestCount);
            }
        }

        private String getEarlyCount() {
            if (earlyCount > NUM_100_MILLION) {
                return FORMAT_THOUSANDS.format(earlyCount / NUM_100_MILLION) + mNum100Million;
            } else {
                return FORMAT_THOUSANDS.format(earlyCount);
            }
        }

        private CharSequence getLatestEnterRate() {
            return Utils.createPercentText(latestEnterRate, true, true);
        }

        private CharSequence getEarlyEnterRate() {
            return Utils.createPercentText(earlyEnterRate, true, false);
        }

        private float getLatestEnterFrequency() {
            return latestEnterFrequency;
        }

        private float getEarlyEnterFrequency() {
            return earlyEnterFrequency;
        }
    }
}

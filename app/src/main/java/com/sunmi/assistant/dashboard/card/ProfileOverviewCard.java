package com.sunmi.assistant.dashboard.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.BaseRefreshCard;
import com.sunmi.assistant.dashboard.Constants;

import java.text.SimpleDateFormat;
import java.util.Random;

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

    private static final int NUM_100_MILLION = 100000000;
    private static final int NUM_10_THOUSANDS = 10000;

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT_PARAMS = new SimpleDateFormat("yyyy-MM-dd");

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
        getModel().init(context);
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
        model.customer = response.getLatestPassengerCount();
        model.lastCustomer = response.getEarlyPassengerCount();
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

        ProgressBar pb = holder.getView(R.id.bar_dashboard_main);
        TextView value = holder.getView(R.id.tv_dashboard_value);
        TextView subData = holder.getView(R.id.tv_dashboard_subdata);
        TextView newValue = holder.getView(R.id.tv_dashboard_new);
        TextView newSubData = holder.getView(R.id.tv_dashboard_new_subdata);
        TextView oldValue = holder.getView(R.id.tv_dashboard_old);
        TextView oldSubData = holder.getView(R.id.tv_dashboard_old_subdata);
        if (model.getCustomer() > 0) {
            pb.setMax(model.getCustomer());
            pb.setSecondaryProgress(model.getCustomer());
            pb.setProgress(model.getNewCustomer());
        } else {
            pb.setMax(1);
            pb.setProgress(0);
            pb.setSecondaryProgress(0);
        }
        value.setText(model.getCustomerString());
        subData.setText(model.getLastCustomerString());
        newValue.setText(model.getNewCustomerString());
        newSubData.setText(model.getLastNewCustomerString());
        oldValue.setText(model.getOldCustomerString());
        oldSubData.setText(model.getLastOldCustomerString());
    }

    @Override
    protected void showLoading(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        holder.getView(R.id.iv_dashboard_loading).setVisibility(View.VISIBLE);
        holder.getView(R.id.bar_dashboard_main).setVisibility(View.INVISIBLE);
        holder.getView(R.id.layout_dashboard_main).setVisibility(View.INVISIBLE);
        holder.getView(R.id.layout_dashboard_new).setVisibility(View.INVISIBLE);
        holder.getView(R.id.layout_dashboard_old).setVisibility(View.INVISIBLE);
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
        value.setText(DATA_NONE);
        subData.setText(DATA_NONE);
        newValue.setText(DATA_NONE);
        newSubData.setText(DATA_NONE);
        oldValue.setText(DATA_NONE);
        oldSubData.setText(DATA_NONE);
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

        private String mNum100Million;
        private String mNum10Thousands;

        int customer;
        int lastCustomer;
        int newCustomer;
        int lastNewCustomer;
        int oldCustomer;
        int lastOldCustomer;

        public void init(Context context) {
            mNum10Thousands = context.getString(R.string.str_num_10_thousands);
            mNum100Million = context.getString(R.string.str_num_100_million);
        }

        public int getCustomer() {
            return customer;
        }

        public int getNewCustomer() {
            return newCustomer;
        }

        public int getOldCustomer() {
            return oldCustomer;
        }

        public String getCustomerString() {
            if (customer < 0) {
                return DATA_NONE;
            } else if (customer > NUM_10_THOUSANDS) {
                return FORMAT_THOUSANDS_DOUBLE_DECIMAL.format(
                        (float) customer / NUM_10_THOUSANDS) + mNum10Thousands;
            } else {
                return FORMAT_THOUSANDS.format(customer);
            }
        }

        public String getLastCustomerString() {
            if (lastCustomer < 0) {
                return DATA_NONE;
            } else if (lastCustomer > NUM_10_THOUSANDS) {
                return FORMAT_THOUSANDS_DOUBLE_DECIMAL.format(
                        (float) lastCustomer / NUM_10_THOUSANDS) + mNum10Thousands;
            } else {
                return FORMAT_THOUSANDS.format(lastCustomer);
            }
        }

        public String getNewCustomerString() {
            if (newCustomer < 0) {
                return DATA_NONE;
            } else if (newCustomer > NUM_10_THOUSANDS) {
                return FORMAT_THOUSANDS_DOUBLE_DECIMAL.format(
                        (float) newCustomer / NUM_10_THOUSANDS) + mNum10Thousands;
            } else {
                return FORMAT_THOUSANDS.format(newCustomer);
            }
        }

        public String getLastNewCustomerString() {
            if (lastNewCustomer < 0) {
                return DATA_NONE;
            } else if (lastNewCustomer > NUM_10_THOUSANDS) {
                return FORMAT_THOUSANDS_DOUBLE_DECIMAL.format(
                        (float) lastNewCustomer / NUM_10_THOUSANDS) + mNum10Thousands;
            } else {
                return FORMAT_THOUSANDS.format(lastNewCustomer);
            }
        }

        public String getOldCustomerString() {
            if (oldCustomer < 0) {
                return DATA_NONE;
            } else if (oldCustomer > NUM_10_THOUSANDS) {
                return FORMAT_THOUSANDS_DOUBLE_DECIMAL.format(
                        (float) oldCustomer / NUM_10_THOUSANDS) + mNum10Thousands;
            } else {
                return FORMAT_THOUSANDS.format(oldCustomer);
            }
        }

        public String getLastOldCustomerString() {
            if (lastOldCustomer < 0) {
                return DATA_NONE;
            } else if (lastOldCustomer > NUM_10_THOUSANDS) {
                return FORMAT_THOUSANDS_DOUBLE_DECIMAL.format(
                        (float) lastOldCustomer / NUM_10_THOUSANDS) + mNum10Thousands;
            } else {
                return FORMAT_THOUSANDS.format(lastOldCustomer);
            }
        }

        public void random() {
            Random r = new Random(System.currentTimeMillis());
            newCustomer = r.nextInt(100000000);
            lastNewCustomer = r.nextInt(100000);
            oldCustomer = r.nextInt(100000000);
            lastOldCustomer = r.nextInt(100000);
            customer = newCustomer + oldCustomer;
            lastCustomer = lastNewCustomer + lastOldCustomer;
        }

    }
}
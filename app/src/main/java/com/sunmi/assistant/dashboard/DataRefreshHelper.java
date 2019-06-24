package com.sunmi.assistant.dashboard;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;
import com.sunmi.assistant.dashboard.data.OrderManagementRemote;
import com.sunmi.assistant.dashboard.data.response.AvgUnitSaleResponse;
import com.sunmi.assistant.dashboard.data.response.PurchaseTypeRankResponse;
import com.sunmi.assistant.dashboard.data.response.QuantityRankResponse;
import com.sunmi.assistant.dashboard.data.response.TimeDistributionResponse;
import com.sunmi.assistant.dashboard.data.response.TotalAmountResponse;
import com.sunmi.assistant.dashboard.data.response.TotalCountResponse;
import com.sunmi.assistant.dashboard.data.response.TotalRefundCountResponse;
import com.sunmi.assistant.dashboard.model.BarChartCard;
import com.sunmi.assistant.dashboard.model.BaseRefreshCard;
import com.sunmi.assistant.dashboard.model.DataCard;
import com.sunmi.assistant.dashboard.model.ListCard;
import com.sunmi.assistant.dashboard.model.PieChartCard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import sunmi.common.rpc.retrofit.RetrofitCallback;

/**
 * 为每个Card实现数据更新能力
 *
 * @author jacob
 * @since 2019-06-21
 */
public interface DataRefreshHelper<T> {

    String TAG = "DataRefreshHelper";

    void refresh(T model);

    class TotalSalesAmountRefresh implements DataRefreshHelper<DataCard> {

        private int companyId;
        private int shopId;

        TotalSalesAmountRefresh(int companyId, int shopId) {
            this.companyId = companyId;
            this.shopId = shopId;
        }

        @Override
        public void refresh(DataCard model) {
            Log.d(TAG, "HTTP request total sales amount.");
            model.state = BaseRefreshCard.STATE_LOADING;
            model.trendName = Utils.getTrendNameByTimeSpan(model.timeSpan);
            OrderManagementRemote.get().getTotalAmount(companyId, shopId,
                    model.timeSpanPair.first, model.timeSpanPair.second, 1,
                    new CardCallback<DataCard, TotalAmountResponse>(model) {
                        @Override
                        public void success(TotalAmountResponse data) {
                            Log.d(TAG, "HTTP request total sales amount success.");
                            model.data = data.getTotal_amount();
                            if (model.timeSpan == DashboardContract.TIME_SPAN_MONTH
                                    && !TextUtils.isEmpty(data.getMonth_rate())) {
                                model.trendData = Float.valueOf(data.getMonth_rate());
                            } else if (model.timeSpan == DashboardContract.TIME_SPAN_WEEK
                                    && !TextUtils.isEmpty(data.getWeek_rate())) {
                                model.trendData = Float.valueOf(data.getWeek_rate());
                            } else if (!TextUtils.isEmpty(data.getDay_rate())) {
                                model.trendData = Float.valueOf(data.getDay_rate());
                            }
                        }
                    });
        }
    }

    class CustomerPriceRefresh implements DataRefreshHelper<DataCard> {

        private int companyId;
        private int shopId;

        CustomerPriceRefresh(int companyId, int shopId) {
            this.companyId = companyId;
            this.shopId = shopId;
        }

        @Override
        public void refresh(DataCard model) {
            Log.d(TAG, "HTTP request customer price.");
            model.state = BaseRefreshCard.STATE_LOADING;
            model.trendName = Utils.getTrendNameByTimeSpan(model.timeSpan);
            OrderManagementRemote.get().getAvgUnitSale(companyId, shopId,
                    model.timeSpanPair.first, model.timeSpanPair.second, 1,
                    new CardCallback<DataCard, AvgUnitSaleResponse>(model) {
                        @Override
                        public void success(AvgUnitSaleResponse data) {
                            Log.d(TAG, "HTTP request customer price success.");
                            model.data = data.getAus();
                            if (model.timeSpan == DashboardContract.TIME_SPAN_MONTH
                                    && !TextUtils.isEmpty(data.getMonth_rate())) {
                                model.trendData = Float.valueOf(data.getMonth_rate());
                            } else if (model.timeSpan == DashboardContract.TIME_SPAN_WEEK
                                    && !TextUtils.isEmpty(data.getWeek_rate())) {
                                model.trendData = Float.valueOf(data.getWeek_rate());
                            } else if (!TextUtils.isEmpty(data.getDay_rate())) {
                                model.trendData = Float.valueOf(data.getDay_rate());
                            }
                        }
                    });
        }
    }

    class TotalSalesVolumeRefresh implements DataRefreshHelper<DataCard> {

        private int companyId;
        private int shopId;

        TotalSalesVolumeRefresh(int companyId, int shopId) {
            this.companyId = companyId;
            this.shopId = shopId;
        }

        @Override
        public void refresh(DataCard model) {
            Log.d(TAG, "HTTP request total sales volume.");
            model.state = BaseRefreshCard.STATE_LOADING;
            model.trendName = Utils.getTrendNameByTimeSpan(model.timeSpan);
            OrderManagementRemote.get().getTotalCount(companyId, shopId,
                    model.timeSpanPair.first, model.timeSpanPair.second, 1,
                    new CardCallback<DataCard, TotalCountResponse>(model) {
                        @Override
                        public void success(TotalCountResponse data) {
                            Log.d(TAG, "HTTP request total sales volume success.");
                            model.data = data.getTotal_count();
                            if (model.timeSpan == DashboardContract.TIME_SPAN_MONTH
                                    && !TextUtils.isEmpty(data.getMonth_rate())) {
                                model.trendData = Float.valueOf(data.getMonth_rate());
                            } else if (model.timeSpan == DashboardContract.TIME_SPAN_WEEK
                                    && !TextUtils.isEmpty(data.getWeek_rate())) {
                                model.trendData = Float.valueOf(data.getWeek_rate());
                            } else if (!TextUtils.isEmpty(data.getDay_rate())) {
                                model.trendData = Float.valueOf(data.getDay_rate());
                            }
                        }
                    });
        }
    }

    class TotalRefundsRefresh implements DataRefreshHelper<DataCard> {

        private int companyId;
        private int shopId;

        TotalRefundsRefresh(int companyId, int shopId) {
            this.companyId = companyId;
            this.shopId = shopId;
        }

        @Override
        public void refresh(DataCard model) {
            Log.d(TAG, "HTTP request total refunds.");
            model.state = BaseRefreshCard.STATE_LOADING;
            model.trendName = Utils.getTrendNameByTimeSpan(model.timeSpan);
            OrderManagementRemote.get().getRefundCount(companyId, shopId,
                    model.timeSpanPair.first, model.timeSpanPair.second, 1,
                    new CardCallback<DataCard, TotalRefundCountResponse>(model) {
                        @Override
                        public void success(TotalRefundCountResponse data) {
                            Log.d(TAG, "HTTP request total refunds success.");
                            model.data = data.getRefund_count();
                            if (model.timeSpan == DashboardContract.TIME_SPAN_MONTH
                                    && !TextUtils.isEmpty(data.getMonth_rate())) {
                                model.trendData = Float.valueOf(data.getMonth_rate());
                            } else if (model.timeSpan == DashboardContract.TIME_SPAN_WEEK
                                    && !TextUtils.isEmpty(data.getWeek_rate())) {
                                model.trendData = Float.valueOf(data.getWeek_rate());
                            } else if (!TextUtils.isEmpty(data.getDay_rate())) {
                                model.trendData = Float.valueOf(data.getDay_rate());
                            }
                        }
                    });
        }
    }

    class TimeDistributionRefresh implements DataRefreshHelper<BarChartCard> {

        @SuppressLint("SimpleDateFormat")
        private static final SimpleDateFormat format = new SimpleDateFormat("HH");

        private int companyId;
        private int shopId;

        TimeDistributionRefresh(int companyId, int shopId) {
            this.companyId = companyId;
            this.shopId = shopId;
        }

        @Override
        public void refresh(BarChartCard model) {
            Log.d(TAG, "HTTP request time distribution detail.");
            model.state = BaseRefreshCard.STATE_LOADING;
            OrderManagementRemote.get().getTimeDistribution(companyId, shopId,
                    model.timeSpanPair.first, model.timeSpanPair.second, 1,
                    new CardCallback<BarChartCard, TimeDistributionResponse>(model) {
                        @Override
                        public void success(TimeDistributionResponse data) {
                            Log.d(TAG, "HTTP request time distribution detail success.");
                            List<TimeDistributionResponse.TimeSpanItem> list = data.getOrder_list();
                            List<BarEntry> amountList = new ArrayList<>(list.size());
                            List<BarEntry> countList = new ArrayList<>(list.size());
                            for (TimeDistributionResponse.TimeSpanItem item : list) {
                                float x = Float.valueOf(format.format(new Date(item.getTime())));
                                amountList.add(new BarEntry(x, item.getAmount()));
                                countList.add(new BarEntry(x, item.getCount()));
                            }
                            model.dataSets[0] = new BarChartCard.BarChartDataSet(amountList);
                            model.dataSets[1] = new BarChartCard.BarChartDataSet(countList);
                        }
                    });
        }
    }

    class PurchaseTypeRankRefresh implements DataRefreshHelper<PieChartCard> {

        private int companyId;
        private int shopId;

        PurchaseTypeRankRefresh(int companyId, int shopId) {
            this.companyId = companyId;
            this.shopId = shopId;
        }

        @Override
        public void refresh(PieChartCard model) {
            Log.d(TAG, "HTTP request purchase type rank.");
            model.state = BaseRefreshCard.STATE_LOADING;
            OrderManagementRemote.get().getPurchaseTypeRank(companyId, shopId,
                    model.timeSpanPair.first, model.timeSpanPair.second,
                    new CardCallback<PieChartCard, PurchaseTypeRankResponse>(model) {
                        @Override
                        public void success(PurchaseTypeRankResponse data) {
                            Log.d(TAG, "HTTP request purchase type rank success.");
                            List<PurchaseTypeRankResponse.PurchaseTypeRankItem> list = data.getPurchase_type_list();
                            List<PieEntry> amountList = new ArrayList<>(list.size());
                            List<PieEntry> countList = new ArrayList<>(list.size());
                            for (PurchaseTypeRankResponse.PurchaseTypeRankItem item : list) {
                                String label = item.getPurchase_type_name();
                                amountList.add(new PieEntry(item.getAmount(), label));
                                countList.add(new PieEntry(item.getCount(), label));
                            }
                            Collections.sort(amountList, (o1, o2) ->
                                    o1.getValue() - o2.getValue() > 0 ? 1 :
                                            (o1.getValue() - o2.getValue() < 0 ? -1 : 0));
                            Collections.sort(countList, (o1, o2) ->
                                    o1.getValue() - o2.getValue() > 0 ? 1 :
                                            (o1.getValue() - o2.getValue() < 0 ? -1 : 0));
                            int amountSize = amountList.size();
                            int countSize = countList.size();
                            float other = 0f;
                            if (amountSize > 6) {
                                for (int i = amountSize - 1; i >= 5; i--) {
                                    other += amountList.get(i).getValue();
                                    amountList.remove(i);
                                }
                                amountList.add(new PieEntry(other, ""));
                            }
                            other = 0f;
                            if (countSize > 6) {
                                for (int i = countSize - 1; i >= 5; i--) {
                                    other += countList.get(i).getValue();
                                    countList.remove(i);
                                }
                                countList.add(new PieEntry(other, ""));
                            }
                            model.dataSets[0] = new PieChartCard.PieChartDataSet(amountList);
                            model.dataSets[1] = new PieChartCard.PieChartDataSet(countList);
                        }
                    });
        }
    }

    class QuantityRankRefresh implements DataRefreshHelper<ListCard> {

        private int companyId;
        private int shopId;

        QuantityRankRefresh(int companyId, int shopId) {
            this.companyId = companyId;
            this.shopId = shopId;
        }

        @Override
        public void refresh(ListCard model) {
            Log.d(TAG, "HTTP request quantity rank.");
            model.state = BaseRefreshCard.STATE_LOADING;
            OrderManagementRemote.get().getQuantityRank(companyId, shopId,
                    model.timeSpanPair.first, model.timeSpanPair.second,
                    new CardCallback<ListCard, QuantityRankResponse>(model) {
                        @Override
                        public void success(QuantityRankResponse data) {
                            Log.d(TAG, "HTTP request quantity rank success.");
                            List<QuantityRankResponse.QuantityRankItem> list = data.getQuantity_rank();
                            Collections.sort(list, (o1, o2) -> o1.getQuantity() - o2.getQuantity());
                            int size = list.size();
                            model.list = new ArrayList<>(size);
                            for (int i = 0; i < size; i++) {
                                QuantityRankResponse.QuantityRankItem item = list.get(i);
                                model.list.add(new ListCard.Item(i + 1, item.getName(), item.getQuantity()));
                            }
                        }
                    });
        }
    }

    abstract class CardCallback<Model extends BaseRefreshCard<Model>, Response>
            extends RetrofitCallback<Response> {

        private Model model;

        CardCallback(Model model) {
            this.model = model;
        }

        public abstract void success(Response data);

        @Override
        public void onSuccess(int code, String msg, Response data) {
            success(data);
            model.state = BaseRefreshCard.STATE_SUCCESS;
            model.flag = BaseRefreshCard.FLAG_NORMAL;
            if (model.callback != null) {
                model.callback.onSuccess();
            }
        }

        @Override
        public void onFail(int code, String msg, Response data) {
            Log.e(TAG, "HTTP request Failed. " + msg);
            model.state = BaseRefreshCard.STATE_FAILED;
            if (model.callback != null) {
                model.callback.onFail();
            }
        }
    }
}

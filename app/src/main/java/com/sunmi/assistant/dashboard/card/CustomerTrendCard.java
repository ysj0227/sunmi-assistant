package com.sunmi.assistant.dashboard.card;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.BaseRefreshCard;
import com.sunmi.assistant.dashboard.Constants;
import com.sunmi.assistant.dashboard.Utils;
import com.sunmi.assistant.dashboard.ui.ChartEntry;
import com.sunmi.assistant.dashboard.ui.CustomerLineMarkerView;
import com.sunmi.assistant.dashboard.ui.LineChartMarkerView;
import com.sunmi.assistant.dashboard.ui.VolumeYAxisLabelsRenderer;
import com.sunmi.assistant.dashboard.ui.XAxisLabelFormatter;
import com.sunmi.assistant.dashboard.ui.XAxisLabelsRenderer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import sunmi.common.base.recycle.BaseViewHolder;
import sunmi.common.base.recycle.ItemType;
import sunmi.common.model.CustomerHistoryTrendResp;
import sunmi.common.rpc.cloud.SunmiStoreApi;
import sunmi.common.rpc.retrofit.BaseResponse;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.utils.CommonHelper;

/**
 * @author yinhui
 * @since 2019-07-01
 */
public class CustomerTrendCard extends BaseRefreshCard<CustomerTrendCard.Model, CustomerHistoryTrendResp> {

    private static CustomerTrendCard sInstance;

    private static final int COLOR_ALL = 0xFF00BC7D;
    private static final int COLOR_NEW = 0xFF5A97FC;
    private static final int COLOR_OLD = 0xFFFF8000;

    private XAxisLabelsRenderer lineXAxisRenderer;
    private VolumeYAxisLabelsRenderer lineYAxisRenderer;
    private LineChartMarkerView mLineChartMarker;
    private CustomerLineMarkerView mLineComplexMarker;
    private float mDashLength;
    private float mDashSpaceLength;

    private CustomerTrendCard(Presenter presenter, int source) {
        super(presenter, source);
    }

    public static CustomerTrendCard get(Presenter presenter, int source) {
        if (sInstance == null) {
            sInstance = new CustomerTrendCard(presenter, source);
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
        return R.layout.dashboard_recycle_item_customer_trend;
    }

    @NonNull
    @Override
    public BaseViewHolder<Model> onCreateViewHolder(@NonNull View view, @NonNull ItemType<Model, BaseViewHolder<Model>> type) {
        BaseViewHolder<Model> holder = super.onCreateViewHolder(view, type);
        Context context = view.getContext();

        mDashLength = CommonHelper.dp2px(context, 4f);
        mDashSpaceLength = CommonHelper.dp2px(context, 2f);
        holder.addOnClickListener(R.id.tv_dashboard_all, (h, model, position) -> {
            if (model.type != Constants.DATA_TYPE_ALL) {
                model.type = Constants.DATA_TYPE_ALL;
                updateViews();
            }
        });
        holder.addOnClickListener(R.id.tv_dashboard_new, (h, model, position) -> {
            if (model.type != Constants.DATA_TYPE_NEW) {
                model.type = Constants.DATA_TYPE_NEW;
                updateViews();
            }
        });
        holder.addOnClickListener(R.id.tv_dashboard_old, (h, model, position) -> {
            if (model.type != Constants.DATA_TYPE_OLD) {
                model.type = Constants.DATA_TYPE_OLD;
                updateViews();
            }
        });

        LineChart lineChart = holder.getView(R.id.view_dashboard_line_chart);

        // 设置图表坐标Label格式
        lineXAxisRenderer = new XAxisLabelsRenderer(lineChart);
        lineYAxisRenderer = new VolumeYAxisLabelsRenderer(lineChart);
        lineChart.setXAxisRenderer(lineXAxisRenderer);
        lineChart.setRendererLeftYAxis(lineYAxisRenderer);

        // 设置通用图表
        lineChart.setTouchEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);

        // 设置X轴
        XAxis lineXAxis = lineChart.getXAxis();
        lineXAxis.setDrawAxisLine(true);
        lineXAxis.setDrawGridLines(false);
        lineXAxis.setTextSize(10f);
        lineXAxis.setTextColor(ContextCompat.getColor(context, R.color.text_disable));
        lineXAxis.setValueFormatter(new XAxisLabelFormatter(context));
        lineXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // 设置Y轴
        YAxis lineYAxis = lineChart.getAxisLeft();
        lineYAxis.setDrawAxisLine(false);
        lineYAxis.setGranularityEnabled(true);
        lineYAxis.setGranularity(1f);
        lineYAxis.setTextSize(10f);
        lineYAxis.setTextColor(ContextCompat.getColor(context, R.color.text_disable));
        lineYAxis.setAxisMinimum(0f);
        lineYAxis.setDrawGridLines(true);
        lineYAxis.setGridColor(ContextCompat.getColor(context, R.color.black_10));
        lineYAxis.setMinWidth(36f);

        // 设置Line图
        mLineChartMarker = new LineChartMarkerView(context);
        mLineComplexMarker = new CustomerLineMarkerView(context);
        mLineChartMarker.setChartView(lineChart);
        mLineComplexMarker.setChartView(lineChart);
        lineChart.setMarker(mLineComplexMarker);

        return holder;
    }

    @Override
    protected Call<BaseResponse<CustomerHistoryTrendResp>> load(int companyId, int shopId, int period, CardCallback callback) {
        String group = "day";
        if (period == Constants.TIME_PERIOD_YESTERDAY) {
            group = "hour";
        }
        SunmiStoreApi.getInstance().getHistoryCustomerTrend(companyId, shopId, period, group,
                new RetrofitCallback<CustomerHistoryTrendResp>() {
                    @Override
                    public void onSuccess(int code, String msg, CustomerHistoryTrendResp data) {
                        if (data == null || data.getCountList() == null) {
                            onFail(code, msg, data);
                            return;
                        }
                        callback.onSuccess(code, msg, data);
                    }

                    @Override
                    public void onFail(int code, String msg, CustomerHistoryTrendResp data) {
                        if (code == Constants.NO_CUSTOMER_DATA) {
                            callback.onSuccess(code, msg, data);
                        } else {
                            callback.onFail(code, msg, data);
                        }
                    }
                });
        return null;
    }

    @Override
    protected Model createModel() {
        return new Model("");
    }

    @Override
    protected void setupModel(Model model, CustomerHistoryTrendResp response) {
        List<ChartEntry> allList = model.dataSets.get(Constants.DATA_TYPE_ALL);
        List<ChartEntry> newList = model.dataSets.get(Constants.DATA_TYPE_NEW);
        List<ChartEntry> oldList = model.dataSets.get(Constants.DATA_TYPE_OLD);
        allList.clear();
        newList.clear();
        oldList.clear();
        if (response == null || response.getCountList() == null) {
            return;
        }
        try {
            List<CustomerHistoryTrendResp.Item> list = response.getCountList();
            int size = list.size();
            Calendar c = Calendar.getInstance();
            long timestamp;
            int timeIndex;
            for (CustomerHistoryTrendResp.Item item : list) {
                if (model.period == Constants.TIME_PERIOD_YESTERDAY) {
                    timestamp = Utils.parseDateTime("yyyy-MM-dd HH:mm", item.getTime());
                    c.setTimeInMillis(timestamp);
                    timeIndex = c.get(Calendar.HOUR_OF_DAY) + 1;
                } else if (model.period == Constants.TIME_PERIOD_WEEK) {
                    timestamp = Utils.parseDateTime("yyyy-MM-dd", item.getTime());
                    c.setTimeInMillis(timestamp);
                    timeIndex = c.get(Calendar.DAY_OF_WEEK) - 1;
                    if (timeIndex <= 0) {
                        timeIndex += 7;
                    }
                } else {
                    timestamp = Utils.parseDateTime("yyyy-MM-dd", item.getTime());
                    c.setTimeInMillis(timestamp);
                    timeIndex = c.get(Calendar.DATE);
                }
                float x = Utils.encodeChartXAxisFloat(model.period, timeIndex);
                long time = Utils.getTime(model.period, timeIndex, size);
                ChartEntry all = new ChartEntry(x, item.getTotalCount(), time);
                all.setData(new CustomerEntry(timestamp, item.getStrangerCount(), item.getRegularCount()));
                allList.add(all);
                newList.add(new ChartEntry(x, item.getStrangerCount(), time));
                oldList.add(new ChartEntry(x, item.getRegularCount(), time));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            allList.clear();
            newList.clear();
            oldList.clear();
        }

        // Test data
//        model.random();
    }

    @Override
    protected void setupView(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        // Get views
        TextView title = holder.getView(R.id.tv_dashboard_title);
        TextView all = holder.getView(R.id.tv_dashboard_all);
        TextView newCustomer = holder.getView(R.id.tv_dashboard_new);
        TextView oldCustomer = holder.getView(R.id.tv_dashboard_old);

        LineChart line = holder.getView(R.id.view_dashboard_line_chart);

        // Set button selected
        all.setSelected(model.type == Constants.DATA_TYPE_ALL);
        all.setTypeface(null, model.type == Constants.DATA_TYPE_ALL ? Typeface.BOLD : Typeface.NORMAL);
        newCustomer.setSelected(model.type == Constants.DATA_TYPE_NEW);
        newCustomer.setTypeface(null, model.type == Constants.DATA_TYPE_NEW ? Typeface.BOLD : Typeface.NORMAL);
        oldCustomer.setSelected(model.type == Constants.DATA_TYPE_OLD);
        oldCustomer.setTypeface(null, model.type == Constants.DATA_TYPE_OLD ? Typeface.BOLD : Typeface.NORMAL);

        // Get data set from model
        List<ChartEntry> dataSet = model.dataSets.get(model.type);
        if (dataSet == null) {
            dataSet = new ArrayList<>();
            model.dataSets.put(model.type, dataSet);
        }

        // Calculate min & max of axis value.
        Pair<Integer, Integer> xAxisRange = Utils.calcChartXAxisRange(model.period);
        int max = 0;
        float lastX = 0;
        for (ChartEntry entry : dataSet) {
            if (entry.getX() > lastX) {
                lastX = entry.getX();
            }
            if (entry.getY() > max) {
                max = (int) Math.ceil(entry.getY());
            }
        }
        int maxDay = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        lineXAxisRenderer.setPeriod(model.period, maxDay);
        line.getXAxis().setAxisMinimum(xAxisRange.first);
        line.getXAxis().setAxisMaximum(xAxisRange.second);
        float maxAxis = lineYAxisRenderer.setMaxValue(max);
        line.getAxisLeft().setAxisMaximum(maxAxis);

        // Get color of line
        int color;
        if (model.type == Constants.DATA_TYPE_NEW) {
            color = COLOR_NEW;
        } else if (model.type == Constants.DATA_TYPE_OLD) {
            color = COLOR_OLD;
        } else {
            color = COLOR_ALL;
        }

        // Use correct chart marker & update it.
        if (model.type == Constants.DATA_TYPE_ALL) {
            line.setMarker(mLineComplexMarker);
            mLineComplexMarker.setPointColor(color);
            mLineComplexMarker.setPeriod(model.period);
        } else {
            line.setMarker(mLineChartMarker);
            mLineChartMarker.setType(model.period, model.type);
            mLineChartMarker.setPointColor(color);
        }

        // Refresh data set
        LineDataSet set;
        LineData data = line.getData();
        ArrayList<Entry> values = new ArrayList<>(dataSet);
        if (data != null && data.getDataSetCount() > 0) {
            set = (LineDataSet) data.getDataSetByIndex(0);
            set.setColor(color);
            set.setCircleColor(color);
            set.setHighLightColor(color);
            set.setValues(values);
            data.notifyDataChanged();
            line.notifyDataSetChanged();
        } else {
            set = new LineDataSet(values, "data");
            set.setColor(color);
            set.setCircleColor(color);
            set.setHighLightColor(color);
            set.setLineWidth(2f);
            set.setDrawValues(false);
            set.setDrawCircleHole(false);
            set.setCircleRadius(1f);
            set.setDrawHorizontalHighlightIndicator(false);
            set.setHighlightLineWidth(1f);
            set.enableDashedHighlightLine(mDashLength, mDashSpaceLength, 0);
            data = new LineData(set);
            line.setData(data);
        }
        line.highlightValue(lastX, 0);
        line.animateX(300);
    }

    @Override
    protected void showLoading(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        model.period = mPeriod;
        model.dataSets.get(model.type).clear();
        setupView(holder, model, position);
    }

    @Override
    protected void showError(@NonNull BaseViewHolder<Model> holder, Model model, int position) {
        model.period = mPeriod;
        model.dataSets.get(model.type).clear();
        setupView(holder, model, position);
    }

    public static class CustomerEntry {
        private long time;
        private int newCustomer;
        private int oldCustomer;

        public CustomerEntry(long time, int newCustomer, int oldCustomer) {
            this.time = time;
            this.newCustomer = newCustomer;
            this.oldCustomer = oldCustomer;
        }

        public long getTime() {
            return time;
        }

        public int getNewCustomer() {
            return newCustomer;
        }

        public int getOldCustomer() {
            return oldCustomer;
        }
    }

    public static class Model extends BaseRefreshCard.BaseModel {
        private String title;
        private int type;
        private SparseArray<List<ChartEntry>> dataSets = new SparseArray<>(3);

        public Model(String title) {
            this.title = title;
            dataSets.put(Constants.DATA_TYPE_ALL, new ArrayList<>());
            dataSets.put(Constants.DATA_TYPE_NEW, new ArrayList<>());
            dataSets.put(Constants.DATA_TYPE_OLD, new ArrayList<>());
        }

        @Override
        public void init(int source) {
            type = Constants.DATA_TYPE_ALL;
            for (int i = 0, size = dataSets.size(); i < size; i++) {
                int key = dataSets.keyAt(i);
                dataSets.get(key).clear();
            }
        }

        public void random() {
            List<ChartEntry> allList = dataSets.get(Constants.DATA_TYPE_ALL);
            List<ChartEntry> newList = dataSets.get(Constants.DATA_TYPE_NEW);
            List<ChartEntry> oldList = dataSets.get(Constants.DATA_TYPE_OLD);
            allList.clear();
            newList.clear();
            oldList.clear();
            int count = period == Constants.TIME_PERIOD_WEEK ? 5 : 20;
            int min = count / 3;
            for (int i = 1; i < count + 1; i++) {
                float x = Utils.encodeChartXAxisFloat(period, i);
                long time = Utils.getTime(period, i, count);
                if (i <= min + 1) {
                    ChartEntry all = new ChartEntry(x, 0f, time);
                    all.setData(new CustomerEntry(System.currentTimeMillis(), 0, 0));
                    allList.add(all);
                    newList.add(new ChartEntry(x, 0f, time));
                    oldList.add(new ChartEntry(x, 0f, time));
                } else {
                    int n = (int) (Math.random() * 1000);
                    int o = (int) (Math.random() * 1000);
                    ChartEntry all = new ChartEntry(x, n + o, time);
                    all.setData(new CustomerEntry(System.currentTimeMillis(), n, o));
                    allList.add(all);
                    newList.add(new ChartEntry(x, n, time));
                    oldList.add(new ChartEntry(x, o, time));
                }
            }
        }

    }
}
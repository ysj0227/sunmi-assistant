package com.sunmi.assistant.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;
import android.util.SparseArray;

import com.github.mikephil.charting.data.BarEntry;
import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.DashboardContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sunmi.common.constant.CommonConfig;
import sunmi.common.rpc.retrofit.BaseRequest;
import sunmi.common.utils.DateTimeUtils;
import sunmi.common.utils.SafeUtils;

/**
 * @author yinhui
 * @since 2019-06-21
 */
public class Utils {

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat HOUR_MINUTE_TIME = new SimpleDateFormat("HH:mm");
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_HOUR_MINUTE_TIME = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private static SparseArray<Pair<Long, Long>> sPeriodCache = new SparseArray<>(3);
    private static Calendar sLastCalendar = Calendar.getInstance();
    private static Calendar sTempCalendar = Calendar.getInstance();

    public static BaseRequest createRequestBody(String params) {
        String timeStamp = DateTimeUtils.currentTimeSecond() + "";
        String randomNum = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String isEncrypted = "0";
        String sign = SafeUtils.md5(params + isEncrypted +
                timeStamp + randomNum + SafeUtils.md5(CommonConfig.CLOUD_TOKEN));
        return new BaseRequest.Builder()
                .setTimeStamp(timeStamp)
                .setRandomNum(randomNum)
                .setIsEncrypted(isEncrypted)
                .setParams(params)
                .setSign(sign)
                .setLang("zh").createBaseRequest();
    }

    public static String getTrendNameByPeriod(Context context, int period) {
        if (period == DashboardContract.TIME_PERIOD_MONTH) {
            return context.getResources().getString(R.string.dashboard_month_ratio);
        } else if (period == DashboardContract.TIME_PERIOD_WEEK) {
            return context.getResources().getString(R.string.dashboard_week_ratio);
        } else {
            return context.getResources().getString(R.string.dashboard_day_ratio);
        }
    }

    public static Pair<Long, Long> getPeriodTimestamp(int period) {
        sTempCalendar = Calendar.getInstance();
        if (sLastCalendar != null
                && (sTempCalendar.get(Calendar.DAY_OF_YEAR) != sLastCalendar.get(Calendar.DAY_OF_YEAR)
                || sTempCalendar.get(Calendar.YEAR) != sLastCalendar.get(Calendar.YEAR)
                || sTempCalendar.get(Calendar.ERA) != sLastCalendar.get(Calendar.ERA))) {
            sPeriodCache.clear();
        }
        sLastCalendar = sTempCalendar;
        sTempCalendar = Calendar.getInstance();
        Pair<Long, Long> periodTimestamp = sPeriodCache.get(period);
        if (periodTimestamp != null) {
            return periodTimestamp;
        }
        long timeStart;
        long timeEnd;
        Calendar c = sTempCalendar;
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        c.clear();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(year, month, date);
        if (period == DashboardContract.TIME_PERIOD_MONTH) {
            c.clear();
            c.set(year, month, 1);
            timeStart = c.getTimeInMillis();
            c.add(Calendar.MONTH, 1);
            timeEnd = c.getTimeInMillis();
        } else if (period == DashboardContract.TIME_PERIOD_WEEK) {
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            int offset = c.getFirstDayOfWeek() - dayOfWeek;
            c.add(Calendar.DATE, offset > 0 ? offset - 7 : offset);
            timeStart = c.getTimeInMillis();
            c.add(Calendar.DATE, 7);
            timeEnd = c.getTimeInMillis();
        } else {
            timeStart = c.getTimeInMillis();
            c.add(Calendar.DATE, 1);
            timeEnd = c.getTimeInMillis();
        }
        periodTimestamp = new Pair<>(timeStart / 1000, timeEnd / 1000);
        sPeriodCache.put(period, periodTimestamp);
        return periodTimestamp;
    }

    public static float encodeBarChartXAxisFloat(int period, long timestamp) {
        sTempCalendar.setTimeInMillis(timestamp * 1000);
        if (period == DashboardContract.TIME_PERIOD_MONTH) {
            return (float) (sTempCalendar.get(Calendar.DAY_OF_MONTH) + 10000);
        } else if (period == DashboardContract.TIME_PERIOD_WEEK) {
            int index = sTempCalendar.get(Calendar.DAY_OF_WEEK);
            index = (index + 5) % 7;
            return index + 100;
        } else {
            return sTempCalendar.get(Calendar.HOUR_OF_DAY);
        }
    }

    public static String decodeBarChartXAxisFloat(float value, String[] weekName) {
        if (value >= 10000) {
            return String.valueOf((int) (value - 10000));
        } else if (value >= 100) {
            return weekName[(int) (value - 100)];
        } else {
            return String.format(Locale.getDefault(), "%02.0f:00", value);
        }
    }

    public static float[] getBarChartXAxisFloatLabels(int period, List<BarEntry> data) {
        if (period == DashboardContract.TIME_PERIOD_TODAY) {
            return new float[]{0, 4, 8, 12, 16, 20, 24};
        } else if (period == DashboardContract.TIME_PERIOD_WEEK) {
            return new float[]{100, 101, 102, 103, 104, 105, 106};
        } else {
            return new float[]{10001, 10006, 10012, 10018, 10024, 10000 + data.size()};
        }
    }

    public static String getHourMinuteTime(long timestamp) {
        return HOUR_MINUTE_TIME.format(new Date(timestamp));
    }

    public static String getDateHourMinuteTime(long timestamp) {
        return DATE_HOUR_MINUTE_TIME.format(new Date(timestamp));
    }
}

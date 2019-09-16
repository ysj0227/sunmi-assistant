package com.sunmi.assistant.dashboard.ui;

import android.content.Context;
import android.graphics.Rect;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.sunmi.assistant.R;

import java.util.Locale;

/**
 * @author yinhui
 * @date 2019-09-11
 */
public class LineChartMarkerView extends MarkerView {

    private ImageView mIvPoint;
    private TextView mTvTitle;
    private TextView mTvValue;
    private TextView mTvLabel;

    private int mOffsetPoint;

    private MPPointF mOffset;
    private MPPointF mRealOffset;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     */
    public LineChartMarkerView(Context context) {
        super(context, R.layout.dashboard_chart_line_marker);
        mTvTitle = findViewById(R.id.tv_dashboard_marker_title);
        mTvValue = findViewById(R.id.tv_dashboard_marker_value);
        mTvLabel = findViewById(R.id.tv_dashboard_marker_label);
        mIvPoint = findViewById(R.id.iv_dashboard_marker_point);
        mOffsetPoint = mIvPoint.getWidth() / 2;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String value = String.format(Locale.getDefault(), "%.2f%%", e.getY());
        mTvValue.setText(value);
        mTvLabel.setText("时间 周一");
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
        if (mRealOffset == null) {
            mRealOffset = new MPPointF();
        }
        MPPointF offset = getOffset();
        mRealOffset.x = offset.x;
        mRealOffset.y = offset.y;

        Chart chart = getChartView();

        float width = getWidth();
        float height = getHeight();

        int pointSize = mOffsetPoint * 2;
        Rect r = new Rect(getWidth() / 2 - mOffsetPoint, getHeight() - pointSize,
                getWidth() / 2 + mOffsetPoint, getHeight());

        if (posX + mRealOffset.x < 0) {
            mRealOffset.x = -posX;
            r.left = Math.max((int) (posX - mOffsetPoint), 0);
            r.right = r.left + pointSize;
        } else if (chart != null && posX + width + mRealOffset.x > chart.getWidth()) {
            mRealOffset.x = chart.getWidth() - posX - width;
            r.right = Math.min((int) (-mRealOffset.x + mOffsetPoint), getWidth());
            r.left = r.right - pointSize;
        }

        if (posY + mRealOffset.y < 0) {
            mRealOffset.y = -mOffsetPoint;
            r.top = 0;
            r.bottom = pointSize;
        } else if (chart != null && posY + height + mRealOffset.y > chart.getHeight()) {
            mRealOffset.y = chart.getHeight() - posY - height;
        }

        mIvPoint.layout(r.left, r.top, r.right, r.bottom);

        return mRealOffset;
    }

    @Override
    public MPPointF getOffset() {
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-((float) getWidth() / 2), -getHeight() + mOffsetPoint);
        }
        return mOffset;
    }
}

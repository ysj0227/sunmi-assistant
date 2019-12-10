package com.sunmi.ipc.motion;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sunmi.ipc.R;
import com.sunmi.ipc.calendar.Config;
import com.sunmi.ipc.calendar.VerticalCalendar;
import com.sunmi.ipc.config.IpcConstants;
import com.sunmi.ipc.contract.MotionVideoListContract;
import com.sunmi.ipc.model.MotionVideo;
import com.sunmi.ipc.presenter.MotionVideoListPresenter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import sunmi.common.base.BaseMvpActivity;
import sunmi.common.base.recycle.BaseViewHolder;
import sunmi.common.base.recycle.SimpleArrayAdapter;
import sunmi.common.model.FilterItem;
import sunmi.common.utils.DateTimeUtils;
import sunmi.common.utils.NetworkUtils;
import sunmi.common.utils.Utils;
import sunmi.common.view.DropdownAdapterNew;
import sunmi.common.view.DropdownAnimNew;
import sunmi.common.view.DropdownMenuNew;
import sunmi.common.view.TitleBarView;
import sunmi.common.view.dialog.BottomDialog;

/**
 * @author yinhui
 * @date 2019-12-09
 */
@EActivity(resName = "activity_motion_video_list")
public class MotionVideoListActivity extends BaseMvpActivity<MotionVideoListPresenter>
        implements MotionVideoListContract.View, BGARefreshLayout.BGARefreshLayoutDelegate {

    @ViewById(resName = "title_bar")
    TitleBarView tbTitle;
    @ViewById(resName = "dm_motion_date")
    View dmFilterDate;
    @ViewById(resName = "dropdown_title")
    TextView dmFilterDateTitle;
    @ViewById(resName = "dm_motion_source")
    DropdownMenuNew dmFilterSource;
    @ViewById(resName = "layout_refresh")
    BGARefreshLayout refreshLayout;
    @ViewById(resName = "rv_motion_list")
    RecyclerView rvList;

    @ViewById(resName = "view_divider")
    View viewDivider;
    @ViewById(resName = "layout_error")
    View layoutError;
    @ViewById(resName = "tv_empty")
    View layoutEmpty;

    @Extra
    int deviceId;

    private DropdownAdapterNew mFilterSourceAdapter;
    private SimpleArrayAdapter<MotionVideo> mAdapter;
    private SparseArray<String> mSourceName = new SparseArray<>(3);

    private Dialog calendarDialog;
    private VerticalCalendar calendarView;
    private Calendar calendarSelected;

    @AfterViews
    void init() {
        mPresenter = new MotionVideoListPresenter(deviceId);
        mPresenter.attachView(this);
        initResource();
        initFilters();
        initRefresh();
        initList();
        mPresenter.loadTimeSlots(false);
        mPresenter.load();
        showLoadingDialog();
    }

    private void initResource() {
        mSourceName.put(IpcConstants.MOTION_DETECTION_SOURCE_VIDEO,
                getString(R.string.motion_detection_source_video_name));
        mSourceName.put(IpcConstants.MOTION_DETECTION_SOURCE_SOUND,
                getString(R.string.motion_detection_source_sound_name));
        mSourceName.put(IpcConstants.MOTION_DETECTION_SOURCE_BOTH,
                getString(R.string.motion_detection_source_both_name));
    }

    private void initFilters() {
        dmFilterDateTitle.setText(DateTimeUtils.formatDateTime(new Date()));
        dmFilterDate.setOnClickListener(v -> openCalendar());

        mFilterSourceAdapter = new DropdownAdapterNew(this);
        mFilterSourceAdapter.setOnItemClickListener((adapter, model, position) ->
                mPresenter.load(model.getId()));
        dmFilterSource.setAnim(new DropdownAnimNew());
        dmFilterSource.setAdapter(mFilterSourceAdapter);
        initFilterData();
    }

    private void initFilterData() {
        List<FilterItem> filterItems = new ArrayList<>(4);
        filterItems.add(new FilterItem(-1, getString(R.string.motion_detection_source_all),
                true));
        filterItems.add(new FilterItem(IpcConstants.MOTION_DETECTION_SOURCE_SOUND,
                getString(R.string.motion_detection_source_sound)));
        filterItems.add(new FilterItem(IpcConstants.MOTION_DETECTION_SOURCE_VIDEO,
                getString(R.string.motion_detection_source_video)));
        filterItems.add(new FilterItem(IpcConstants.MOTION_DETECTION_SOURCE_BOTH,
                getString(R.string.motion_detection_source_both)));
        mFilterSourceAdapter.setData(filterItems);
    }

    private void initRefresh() {
        refreshLayout.setDelegate(this);
        BGANormalRefreshViewHolder refreshViewHolder =
                new BGANormalRefreshViewHolder(this, true);
        refreshLayout.setRefreshViewHolder(refreshViewHolder);
        refreshLayout.setPullDownRefreshEnable(false);
        refreshLayout.setIsShowLoadingMoreView(true);
    }

    private void initList() {
        mAdapter = new Adapter();
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(mAdapter);
    }

    private void openCalendar() {
        if (isFastClick(1000)) {
            return;
        }
        if (calendarView == null) {
            // view为空，则还没有TimeSlots数据，请求接口
            showLoadingDialog();
            mPresenter.loadTimeSlots(true);
            return;
        }
        if (calendarDialog == null) {
            int height = (int) (Utils.getScreenHeight(context) * 0.85);
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, height);
            calendarDialog = new BottomDialog.Builder(context)
                    .setTitle(R.string.str_title_calendar)
                    .setContent(calendarView, lp)
                    .setCancelButton(R.string.sm_cancel)
                    .setOkButton(R.string.str_confirm, (dialog, which) -> {
                        if (calendarSelected != null) {
                            mPresenter.load(calendarSelected);
                        }
                    }).create();
        }
        calendarSelected = null;
        calendarDialog.show();
    }

    @Click(resName = "btn_refresh")
    void refresh() {
        mPresenter.load();
    }

    @Override
    public void updateCalendar(List<Calendar> selected, boolean open) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -3);
        Config config = new Config.Builder()
                .setMinDate(c)
                .setPoint(selected)
                .build();
        calendarView = new VerticalCalendar(this, config);
        calendarView.setOnCalendarSelectListener(date -> calendarSelected = date);
        if (open) {
            hideLoadingDialog();
            openCalendar();
        }
    }

    @Override
    public void setData(List<MotionVideo> data) {
        showContent();
        mAdapter.setData(data);
    }

    @Override
    public void addData(List<MotionVideo> data) {
        mAdapter.add(data);
    }

    private void showContent() {
        refreshLayout.setVisibility(View.VISIBLE);
        viewDivider.setVisibility(View.VISIBLE);
        dmFilterSource.setVisibility(View.VISIBLE);
        dmFilterDate.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.INVISIBLE);
        layoutEmpty.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showError(int code) {
        refreshLayout.setVisibility(View.INVISIBLE);
        viewDivider.setVisibility(View.INVISIBLE);
        dmFilterSource.setVisibility(View.INVISIBLE);
        dmFilterDate.setVisibility(View.INVISIBLE);
        layoutError.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showEmpty() {
        refreshLayout.setVisibility(View.INVISIBLE);
        viewDivider.setVisibility(View.VISIBLE);
        dmFilterSource.setVisibility(View.VISIBLE);
        dmFilterDate.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.INVISIBLE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            shortTip(R.string.toast_networkIsExceptional);
            return false;
        }
        return mPresenter.loadMore();
    }

    private class Adapter extends SimpleArrayAdapter<MotionVideo> {

        public Adapter() {
            setOnItemClickListener((holder, model, position) -> {
                // TODO
            });
        }

        @Override
        public int getLayoutId() {
            return R.layout.item_motion_video;
        }

        @Override
        public void setupView(@NonNull BaseViewHolder<MotionVideo> holder, MotionVideo model, int position) {
            TextView title = holder.getView(R.id.item_motion_title);
            TextView content = holder.getView(R.id.item_motion_content);
            ImageView snapshot = holder.getView(R.id.item_motion_image);
            title.setText(DateTimeUtils.secondToDate(model.getDetectTime(), "HH:mm:ss"));
            String type = mSourceName.get(model.getSource());
            content.setText(type == null ? "" : type);
            Glide.with(holder.getContext()).load(model.getSnapshotAddress()).into(snapshot);
        }

    }
}

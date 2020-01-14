package com.sunmi.assistant.dashboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sunmi.assistant.R;
import com.sunmi.assistant.dashboard.ui.RefreshLayout;
import com.sunmi.assistant.dashboard.ui.ScrollableViewPager;
import com.sunmi.ipc.config.IpcConstants;
import com.sunmi.sunmiservice.cloud.WebViewCloudServiceActivity_;
import com.xiaojinzi.component.impl.Router;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import sunmi.common.base.BaseMvpFragment;
import sunmi.common.constant.CommonConstants;
import sunmi.common.constant.CommonNotifications;
import sunmi.common.model.FilterItem;
import sunmi.common.router.IpcApi;
import sunmi.common.utils.SpUtils;
import sunmi.common.utils.StatusBarUtils;
import sunmi.common.utils.WebViewParamsUtils;
import sunmi.common.utils.log.LogCat;
import sunmi.common.view.DropdownMenu;
import sunmi.common.view.tablayout.CommonTabLayout;
import sunmi.common.view.tablayout.listener.CustomTabEntity;
import sunmi.common.view.tablayout.listener.OnTabSelectListener;

/**
 * 首页数据Dashboard的展示
 *
 * @author yinhui
 * @since 2019-06-13
 */
@EFragment(R.layout.dashboard_fragment_main)
public class DashboardFragment extends BaseMvpFragment<DashboardPresenter>
        implements DashboardContract.View {

    @ViewById(R.id.cl_dashboard_content)
    ConstraintLayout mContent;

    @ViewById(R.id.pager_dashboard_pager)
    ScrollableViewPager mPager;
    @ViewById(R.id.tab_dashboard_pager)
    CommonTabLayout mPageTab;

    @ViewById(R.id.layout_dashboard_tab)
    FrameLayout mTopPageTab;
    @ViewById(R.id.layout_shop_title)
    DropdownMenu mTopShopMenu;
    private LinearLayout mShopMenuList;
    @ViewById(R.id.view_dashboard_overlay)
    View mOverlay;

    @ViewById(R.id.layout_top_period_tab)
    ViewGroup mTopStickyPeriodTab;
    @ViewById(R.id.tv_dashboard_top_today)
    TextView mTodayView;
    @ViewById(R.id.tv_dashboard_top_yesterday)
    TextView mYesterdayView;
    @ViewById(R.id.tv_dashboard_top_week)
    TextView mWeekView;
    @ViewById(R.id.tv_dashboard_top_month)
    TextView mMonthView;

    @ViewById(R.id.group_dashboard_content)
    Group mContentGroup;
    @ViewById(R.id.layout_dashboard_no_fs_tip)
    View mNoFsTip;
    @ViewById(R.id.layout_dashboard_error)
    View mLayoutError;

    @ViewById(R.id.ll_floating)
    LinearLayout llFloating;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private List<PageHost> mPages;

    private ShopMenuAdapter mShopMenuAdapter;
    private ShopMenuPopupHelper mShopMenuPopupHelper;
    private TextView mShopMenuTitle;
    private ImageView mShopMenuTitleArrow;
    private FilterItem mShopMenuItem;

    private int mStatusBarHeight;
    private int mTopStickyPeriodHeight;
    private int mTopShopMenuHeight;
    private int mTopPageTabHeight;
    private int mTopRadiusHeight;
    private int mTopHeaderHeight;

    private boolean mHasInit = false;
    private boolean mHasData = false;
    private boolean mIsStickyPeriodTop = false;
    private boolean mIsStickyShopMenu = false;

    @AfterViews
    void init() {
        mPresenter = new DashboardPresenter();
        mPresenter.attachView(this);
        showLoadingDialog();
        mHandler.post(this::initView);
        mPresenter.init();
    }

    private void initView() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        initDimens(context);
        initTopBar(context);
        initViewPager(context);
    }

    private void initDimens(Context context) {
        mStatusBarHeight = sunmi.common.utils.Utils.getStatusBarHeight(context);
        mTopStickyPeriodHeight = mTopStickyPeriodTab.getMeasuredHeight() + mStatusBarHeight;
        mTopShopMenuHeight = mTopShopMenu.getMeasuredHeight() + mStatusBarHeight;
        mTopPageTabHeight = mTopPageTab.getMeasuredHeight();
        mTopRadiusHeight = (int) context.getResources().getDimension(R.dimen.dp_16);
        mTopHeaderHeight = mTopShopMenuHeight + mTopPageTabHeight;
    }

    private void initTopBar(Context context) {
        // 初始化设置状态栏
        FragmentActivity activity = getActivity();
        if (activity != null && !activity.isDestroyed()) {
            StatusBarUtils.setStatusBarFullTransparent(activity);
        }

        // 初始化设置TopBar高度和Padding
        mTopStickyPeriodTab.getLayoutParams().height = mTopStickyPeriodHeight;
        mTopStickyPeriodTab.setPadding(0, mStatusBarHeight, 0, 0);
        mTopShopMenu.getLayoutParams().height = mTopShopMenuHeight;
        mTopShopMenu.setPadding(0, mStatusBarHeight, 0, 0);

        // 初始化设置顶部门店选择下拉列表
        mShopMenuAdapter = new ShopMenuAdapter(context);
        mShopMenuAdapter.setOnItemClickListener((adapter, model, position) -> {
            if (position == 0) {
                return;
            }
            showLoadingDialog();
            mPresenter.setShop(model);
            mShopMenuItem.setChecked(false);
            mShopMenuItem = model;
            model.setChecked(true);
            List<FilterItem> data = adapter.getData();
            data.remove(position);
            data.add(0, model);
            adapter.notifyItemRangeChanged(0, position + 1);
        });
        mTopShopMenu.setAdapter(mShopMenuAdapter);
        mShopMenuTitle = mShopMenuAdapter.getTitle().getView(R.id.dropdown_item_title);
        mShopMenuTitleArrow = mShopMenuAdapter.getTitle().getView(R.id.dropdown_item_arrow);
        mShopMenuPopupHelper = new ShopMenuPopupHelper(context, mContent, mOverlay, mShopMenuTitleArrow);
        mTopShopMenu.setLayoutManager(new ShopMenuLayoutManager(context));
        mTopShopMenu.setPopupHelper(mShopMenuPopupHelper);
        mOverlay.setOnClickListener(v -> mTopShopMenu.getPopup().dismiss(true));
    }

    private void initViewPager(Context context) {
        mPages = mPresenter.createPages();
        mPager.setAdapter(new PageAdapter(getChildFragmentManager()));
        mPager.addOnPageChangeListener(new PageListener());
        ArrayList<CustomTabEntity> tabs = new ArrayList<>(mPages.size());
        for (PageHost page : mPages) {
            tabs.add(new CustomTabEntity() {
                @Override
                public String getTabTitle() {
                    return getString(page.getTitle());
                }

                @Override
                public int getTabSelectedIcon() {
                    return page.getIcon();
                }

                @Override
                public int getTabUnselectedIcon() {
                    return page.getIcon();
                }
            });
        }
        mPageTab.setTabData(tabs);
        mPageTab.setCurrentTab(0);
        mPageTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });
    }

    public void updateStatusBar() {
        resetTop();
        mPresenter.scrollToTop();
        if (mTopShopMenu.getPopup().isShowing()) {
            mTopShopMenu.getPopup().dismiss(false);
        }
    }

    private void showContent() {
        updateStickyPeriodTab(getActivity(), mIsStickyPeriodTop, false);
        mContentGroup.setVisibility(View.VISIBLE);
        mOverlay.setVisibility(View.GONE);
        mLayoutError.setVisibility(View.GONE);
        if (mTopShopMenu.getPopup().isShowing()) {
            mTopShopMenu.getPopup().dismiss(true);
        }
        hideLoadingDialog();
    }

    private void showError() {
        StatusBarUtils.setStatusBarFullTransparent(getActivity());
        mContentGroup.setVisibility(View.INVISIBLE);
        mOverlay.setVisibility(View.INVISIBLE);
        mTopStickyPeriodTab.setVisibility(View.INVISIBLE);
        mLayoutError.setVisibility(View.VISIBLE);
        hideLoadingDialog();
    }

    private boolean debugEnable = false;

    @LongClick(R.id.debug)
    void clickDebug() {
        debugEnable = !debugEnable;
        LogCat.e(TAG, "status h=" + mStatusBarHeight + "; top shop h=" + mTopShopMenuHeight + "; top page h=" + mTopPageTabHeight + "; top h=" + mTopHeaderHeight);
        LogCat.e(TAG, "shop menu: v=" + mTopShopMenu.getVisibility() + "; h=" + mTopShopMenu.getHeight() + "; y=" + mTopShopMenu.getTranslationY());
        LogCat.e(TAG, "page menu: v=" + mTopPageTab.getVisibility() + "; h=" + mTopPageTab.getHeight() + "; y=" + mTopPageTab.getTranslationY());
        View refresh = mPager.getChildAt(0);
        if (refresh instanceof RefreshLayout) {
            View recycler = ((RefreshLayout) refresh).getChildAt(1);
            if (recycler instanceof RecyclerView) {
                View first = ((RecyclerView) recycler).getChildAt(0);
                if (first != null) {
                    LogCat.e(TAG, "card m=" + ((ViewGroup.MarginLayoutParams) first.getLayoutParams()).topMargin);
                }
            }
        }
    }

    @Click(R.id.tv_dashboard_top_today)
    void clickPeriodToday() {
        mPresenter.setPeriod(Constants.TIME_PERIOD_TODAY);
    }

    @Click(R.id.tv_dashboard_top_yesterday)
    void clickPeriodYesterday() {
        mPresenter.setPeriod(Constants.TIME_PERIOD_YESTERDAY);
    }

    @Click(R.id.tv_dashboard_top_week)
    void clickPeriodWeek() {
        mPresenter.setPeriod(Constants.TIME_PERIOD_WEEK);
    }

    @Click(R.id.tv_dashboard_top_month)
    void clickPeriodMonth() {
        mPresenter.setPeriod(Constants.TIME_PERIOD_MONTH);
    }

    @Click(R.id.btn_dashboard_tip_add_fs)
    void clickAddFs() {
        Router.withApi(IpcApi.class).goToIpcStartConfig(getContext(), CommonConstants.TYPE_IPC_FS, CommonConstants.CONFIG_IPC_FROM_COMMON);
    }

    @Click(R.id.btn_refresh)
    void clickReload() {
        showLoadingDialog();
        mPresenter.init();
    }

    @Click(R.id.iv_close)
    void clickClose() {
        llFloating.setVisibility(View.GONE);
        mPresenter.saveShopBundledCloudInfo(false);
    }

    @Click(R.id.btn_floating)
    void clickFloating() {
        if (isFastClick(500)) {
            return;
        }
        WebViewCloudServiceActivity_.intent(mActivity).mUrl(CommonConstants.H5_CLOUD_STORAGE)
                .params(WebViewParamsUtils.getCloudStorageParams(new ArrayList<>(), "")).start();
    }

    @Override
    public PageContract.ParentPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public int getHeaderHeight() {
        return mTopHeaderHeight;
    }

    @Override
    public void setShopList(List<FilterItem> list) {
        for (FilterItem item : list) {
            if (item.isChecked()) {
                mShopMenuItem = item;
                break;
            }
        }
        mShopMenuAdapter.setData(list);
    }

    @Override
    public void setSource(int source) {
        mHasInit = true;
        mHasData = Utils.hasAuth(source) || Utils.hasFs(source);
        mNoFsTip.setVisibility(!Utils.hasFs(source)
                && Utils.hasCustomer(source)
                && !Utils.hasFloating(source) ?
                View.VISIBLE : View.INVISIBLE);
        showContent();
    }

    @Override
    public void updateTab(int pageType, int period) {
        if (period == Constants.TIME_PERIOD_INIT) {
            return;
        }
        if (pageType != mPresenter.getPageType()) {
            return;
        }
        if (pageType == Constants.PAGE_OVERVIEW) {
            mTodayView.setVisibility(View.VISIBLE);
            mYesterdayView.setVisibility(View.GONE);
        } else {
            mTodayView.setVisibility(View.GONE);
            mYesterdayView.setVisibility(View.VISIBLE);
        }
        mTodayView.setSelected(period == Constants.TIME_PERIOD_TODAY);
        mTodayView.setTypeface(null, period == Constants.TIME_PERIOD_TODAY ? Typeface.BOLD : Typeface.NORMAL);
        mYesterdayView.setSelected(period == Constants.TIME_PERIOD_YESTERDAY);
        mYesterdayView.setTypeface(null, period == Constants.TIME_PERIOD_YESTERDAY ? Typeface.BOLD : Typeface.NORMAL);
        mWeekView.setSelected(period == Constants.TIME_PERIOD_WEEK);
        mWeekView.setTypeface(null, period == Constants.TIME_PERIOD_WEEK ? Typeface.BOLD : Typeface.NORMAL);
        mMonthView.setSelected(period == Constants.TIME_PERIOD_MONTH);
        mMonthView.setTypeface(null, period == Constants.TIME_PERIOD_MONTH ? Typeface.BOLD : Typeface.NORMAL);
    }

    @Override
    public void updateTopPosition(int position) {
        int offset = Math.min(position - mTopHeaderHeight, 0);
        mTopShopMenu.setTranslationY(offset);
        mTopPageTab.setTranslationY(offset);
        mShopMenuPopupHelper.setOffset(offset);

        if (debugEnable) {
            LogCat.e(TAG, "position=" + position + "; offset=" + offset);
        }

        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (position > 0) {
            if (mIsStickyPeriodTop) {
                updateStickyPeriodTab(activity, false, true);
            }
        } else {
            if (!mIsStickyPeriodTop) {
                updateStickyPeriodTab(activity, true, true);
            }
        }
    }

    @Override
    public void resetTop() {
        mTopShopMenu.setTranslationY(0);
        mTopPageTab.setTranslationY(0);
        mShopMenuPopupHelper.setOffset(0);
        updateStickyPeriodTab(getActivity(), false, false);
    }

    @Override
    public void loadDataFailed() {
        if (mHasInit) {
            hideLoadingDialog();
            shortTip(R.string.toast_network_error);
        } else {
            showError();
        }
    }

    @UiThread
    @Override
    public void updateFloating(boolean showFloating) {
        if (showFloating) {
            llFloating.setVisibility(View.VISIBLE);
        } else {
            llFloating.setVisibility(View.GONE);
        }
    }

    private void updateStickyPeriodTab(Activity activity, boolean isShow, boolean animated) {
        if (activity == null) {
            return;
        }
        if (isShow) {
            StatusBarUtils.setStatusBarColor(activity, StatusBarUtils.TYPE_DARK);
            mTopShopMenu.setVisibility(View.INVISIBLE);
            mTopPageTab.setVisibility(View.INVISIBLE);
            mTopStickyPeriodTab.setVisibility(View.VISIBLE);
            mPager.setScrollable(false);
            mIsStickyPeriodTop = true;
        } else {
            StatusBarUtils.setStatusBarFullTransparent(activity);
            mTopShopMenu.setVisibility(View.VISIBLE);
            mTopPageTab.setVisibility(View.VISIBLE);
            mTopStickyPeriodTab.setVisibility(View.INVISIBLE);
            mPager.setScrollable(true);
            mIsStickyPeriodTop = false;
        }
    }

    @Override
    public int[] getStickNotificationId() {
        return new int[]{
                CommonNotifications.netConnected,
                CommonNotifications.companySwitch,
                CommonNotifications.companyNameChanged,
                CommonNotifications.shopSwitched,
                CommonNotifications.shopNameChanged,
                CommonNotifications.importShop,
                CommonNotifications.shopCreate,
                CommonNotifications.shopSaasDock,
                IpcConstants.refreshIpcList,
                CommonNotifications.cloudStorageChange
        };
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == CommonNotifications.netConnected) {
            mPresenter.reload(Constants.FLAG_ALL_MASK);
        } else if (id == CommonNotifications.companySwitch) {
            mHasInit = false;
            mShopMenuPopupHelper.setCompanyName(SpUtils.getCompanyName());
            mPresenter.reload(Constants.FLAG_ALL_MASK);
        } else if (id == CommonNotifications.companyNameChanged) {
            mShopMenuPopupHelper.setCompanyName(SpUtils.getCompanyName());
        } else if (id == CommonNotifications.shopSwitched) {
            mHasInit = false;
            mPresenter.reload(Constants.FLAG_SAAS | Constants.FLAG_FS | Constants.FLAG_CUSTOMER | Constants.FLAG_BUNDLED_LIST);
        } else if (id == CommonNotifications.shopNameChanged
                || id == CommonNotifications.importShop
                || id == CommonNotifications.shopCreate) {
            mPresenter.reload(Constants.FLAG_SHOP);
        } else if (id == CommonNotifications.shopSaasDock) {
            mPresenter.reload(Constants.FLAG_SAAS);
        } else if (id == IpcConstants.refreshIpcList) {
            mPresenter.reload(Constants.FLAG_FS | Constants.FLAG_BUNDLED_LIST);
        } else if (id == CommonNotifications.cloudStorageChange) {
            mPresenter.reload(Constants.FLAG_BUNDLED_LIST);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.startAutoRefresh();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.stopAutoRefresh();
    }

    private class PageAdapter extends FragmentStatePagerAdapter {

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return mPages.get(i).getFragment();
        }

        @Override
        public int getCount() {
            return mPages.size();
        }
    }

    private class PageListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int i, float v, int i1) {
        }

        @Override
        public void onPageSelected(int position) {
            mPageTab.setCurrentTab(position);
            mPresenter.setPage(mPages.get(position).getType());
            updateTab(mPresenter.getPageType(), mPresenter.getPeriod());
            resetTop();
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }
    }

    private static class ShopMenuLayoutManager extends LinearLayoutManager {

        private ShopMenuLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
            if (getChildCount() == 0) {
                super.onMeasure(recycler, state, widthSpec, heightSpec);
                return;
            }
            View firstChildView = recycler.getViewForPosition(0);
            measureChild(firstChildView, widthSpec, heightSpec);
            int itemHeight = firstChildView.getMeasuredHeight();
            setMeasuredDimension(View.MeasureSpec.getSize(widthSpec),
                    getChildCount() > 7 ? (int) (itemHeight * 7.5f) : itemHeight * getChildCount());
        }
    }
}

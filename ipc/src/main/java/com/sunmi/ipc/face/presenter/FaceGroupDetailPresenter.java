package com.sunmi.ipc.face.presenter;

import com.sunmi.ipc.R;
import com.sunmi.ipc.face.contract.FaceGroupDetailContract;
import com.sunmi.ipc.face.model.FaceGroup;
import com.sunmi.ipc.model.FaceGroupUpdateReq;
import com.sunmi.ipc.rpc.IpcCloudApi;

import sunmi.common.base.BasePresenter;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.utils.SpUtils;
import sunmi.common.utils.log.LogCat;

/**
 * @author yinhui
 * @date 2019-08-20
 */
public class FaceGroupDetailPresenter extends BasePresenter<FaceGroupDetailContract.View>
        implements FaceGroupDetailContract.Presenter {

    private static final String TAG = FaceGroupDetailPresenter.class.getSimpleName();

    private int mShopId;
    private FaceGroup mFaceGroup;
    private int mOccupiedCapacity;

    public FaceGroupDetailPresenter(int shopId, FaceGroup model, int occupiedCapacity) {
        mShopId = shopId;
        mFaceGroup = model;
        mOccupiedCapacity = occupiedCapacity;
    }

    @Override
    public void updateName(final String name) {
        if (name == null || name.length() == 0 || name.length() > FaceGroup.MAX_LENGTH_NAME) {
            if (isViewAttached()) {
                mView.shortTip(R.string.ipc_face_group_name_error);
            }
            return;
        }
        if (isViewAttached()) {
            mView.showLoadingDialog();
        }
        FaceGroupUpdateReq request = new FaceGroupUpdateReq(SpUtils.getCompanyId(), mShopId, mFaceGroup);
        request.setName(name);
        IpcCloudApi.updateFaceGroup(request, new RetrofitCallback<Object>() {
            @Override
            public void onSuccess(int code, String msg, Object data) {
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mFaceGroup.setGroupName(name);
                    mView.updateNameView(name);
                }
            }

            @Override
            public void onFail(int code, String msg, Object data) {
                LogCat.e(TAG, "Update face group name Failed. " + msg);
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.shortTip(R.string.toast_network_error);
                }
            }
        });
    }

    @Override
    public void updateCapacity(final int capacity) {
        if (capacity <= 0 || capacity > FaceGroup.MAX_CAPACITY_ALL_GROUP - mOccupiedCapacity) {
            if (isViewAttached()) {
                mView.shortTip(R.string.ipc_face_group_capacity_error);
            }
            return;
        }
        if (isViewAttached()) {
            mView.showLoadingDialog();
        }
        FaceGroupUpdateReq request = new FaceGroupUpdateReq(SpUtils.getCompanyId(), mShopId, mFaceGroup);
        request.setCapacity(capacity);
        IpcCloudApi.updateFaceGroup(request, new RetrofitCallback<Object>() {
            @Override
            public void onSuccess(int code, String msg, Object data) {
                mFaceGroup.setCapacity(capacity);
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.updateCapacityView(capacity);
                }
            }

            @Override
            public void onFail(int code, String msg, Object data) {
                LogCat.e(TAG, "Update face group capacity Failed. " + msg);
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.shortTip(R.string.toast_network_error);
                }
            }
        });
    }

    @Override
    public void updateThreshold(final int times, final int days) {
        if (times <= 0 || times > FaceGroup.MAX_THRESHOLD || days <= 0 || days > FaceGroup.MAX_THRESHOLD) {
            if (isViewAttached()) {
                mView.shortTip(R.string.ipc_face_group_threshold_error);
            }
            return;
        }
        if (isViewAttached()) {
            mView.showLoadingDialog();
        }
        FaceGroupUpdateReq request = new FaceGroupUpdateReq(SpUtils.getCompanyId(), mShopId, mFaceGroup);
        request.setThreshold(times, days);
        IpcCloudApi.updateFaceGroup(request, new RetrofitCallback<Object>() {
            @Override
            public void onSuccess(int code, String msg, Object data) {
                mFaceGroup.setThreshold(times, days);
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.updateThresholdView(times, days);
                }
            }

            @Override
            public void onFail(int code, String msg, Object data) {
                LogCat.e(TAG, "Update face group threshold Failed. " + msg);
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.shortTip(R.string.toast_network_error);
                }
            }
        });
    }

    @Override
    public void updateMark(final String mark) {
        if (mark == null || mark.length() == 0 || mark.length() > FaceGroup.MAX_LENGTH_NAME) {
            if (isViewAttached()) {
                mView.shortTip(R.string.ipc_face_group_mark_error);
            }
            return;
        }
        if (isViewAttached()) {
            mView.showLoadingDialog();
        }
        FaceGroupUpdateReq request = new FaceGroupUpdateReq(SpUtils.getCompanyId(), mShopId, mFaceGroup);
        request.setMark(mark);
        IpcCloudApi.updateFaceGroup(request, new RetrofitCallback<Object>() {
            @Override
            public void onSuccess(int code, String msg, Object data) {
                mFaceGroup.setMark(mark);
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.updateMarkView(mark);
                }
            }

            @Override
            public void onFail(int code, String msg, Object data) {
                LogCat.e(TAG, "Update face group mark Failed. " + msg);
                if (isViewAttached()) {
                    mView.hideLoadingDialog();
                    mView.shortTip(R.string.toast_network_error);
                }
            }
        });
    }

    @Override
    public void delete() {
        if (isViewAttached()) {
            mView.showLoadingDialog();
        }
        IpcCloudApi.deleteFaceGroup(SpUtils.getCompanyId(), mShopId, mFaceGroup.getGroupId(),
                new RetrofitCallback<Object>() {
                    @Override
                    public void onSuccess(int code, String msg, Object data) {
                        if (isViewAttached()) {
                            mView.hideLoadingDialog();
                            mView.deleteSuccess();
                        }
                    }

                    @Override
                    public void onFail(int code, String msg, Object data) {
                        LogCat.e(TAG, "Delete face group Failed. " + msg);
                        if (isViewAttached()) {
                            mView.hideLoadingDialog();
                            mView.shortTip(R.string.toast_network_error);
                        }
                    }
                });
    }
}
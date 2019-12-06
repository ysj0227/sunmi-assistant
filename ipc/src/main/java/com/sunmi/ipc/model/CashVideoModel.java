package com.sunmi.ipc.model;

import android.annotation.SuppressLint;

import com.sunmi.ipc.rpc.IpcCloudApi;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sunmi.common.model.SunmiDevice;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.utils.ThreadPool;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-12-05.
 */
public class CashVideoModel {

    private HashMap<Integer, String> ipcName;
    private int pageNum, pageSize = 10;
    private boolean hasMore;
    private int deviceId, videoType;
    private long startTime, endTime;
    private int total;

    public CashVideoModel() {
        initMap();
    }

    private void initMap() {
        ThreadPool.getSingleThreadPool().submit(new Runnable() {
            @SuppressLint("UseSparseArrays")
            @Override
            public void run() {
                List<SunmiDevice> devices = DataSupport.where("type=?", "IPC").find(SunmiDevice.class);
                ipcName = new HashMap<>(devices.size());
                for (SunmiDevice device : devices) {
                    ipcName.put(device.getId(), device.getName());
                }
            }
        });
    }

    public void load(int deviceId, int videoType, long startTime, long endTime, CallBack callBack) {
        pageNum = 1;
        this.deviceId = deviceId;
        this.videoType = videoType;
        this.startTime = startTime;
        this.endTime = endTime;
        hasMore = true;
        IpcCloudApi.getInstance().getCashVideoList(deviceId, videoType, startTime,
                endTime, pageNum, pageSize, new RetrofitCallback<CashVideoResp>() {
                    @Override
                    public void onSuccess(int code, String msg, CashVideoResp data) {
                        List<CashVideoResp.AuditVideoListBean> beans = data.getAuditVideoList();
                        int n = beans.size();
                        if (n > 0) {
                            for (int i = 0; i < beans.size(); i++) {
                                beans.get(i).setDeviceName(ipcName.get(beans.get(i).getDeviceId()));
                            }
                            total = data.getTotalCount();
                            if (n == total) {
                                hasMore = false;
                            } else {
                                pageNum++;
                            }
                        } else {
                            hasMore = false;
                            total = 0;
                        }
                        callBack.getCashVideoSuccess(beans, hasMore, total, pageNum - 1);
                    }

                    @Override
                    public void onFail(int code, String msg, CashVideoResp data) {
                        callBack.getCashVideoFail(code, msg, -1);
                    }
                });
    }

    public void loadMore(CallBack callBack) {
        if (!hasMore) {
            callBack.getCashVideoSuccess(new ArrayList<>(0), hasMore, total, pageNum);
            return;
        }
        IpcCloudApi.getInstance().getCashVideoList(deviceId, videoType, startTime,
                endTime, pageNum, pageSize, new RetrofitCallback<CashVideoResp>() {
                    @Override
                    public void onSuccess(int code, String msg, CashVideoResp data) {
                        List<CashVideoResp.AuditVideoListBean> beans = data.getAuditVideoList();
                        int n = beans.size();
                        if (n > 0) {
                            for (int i = 0; i < beans.size(); i++) {
                                beans.get(i).setDeviceName(ipcName.get(beans.get(i).getDeviceId()));
                            }
                            total = data.getTotalCount();
                            if (total > (pageNum - 1) * pageSize + n) {
                                pageNum++;
                            } else {
                                hasMore = false;
                            }
                        } else {
                            hasMore = false;
                        }
                        callBack.getCashVideoSuccess(beans, hasMore, total, pageNum - 1);
                    }

                    @Override
                    public void onFail(int code, String msg, CashVideoResp data) {
                        callBack.getCashVideoFail(code, msg, (pageNum - 1) * pageSize);
                    }
                });

    }

    public HashMap<Integer, String> getIpcNameMap() {
        return ipcName;
    }

    public interface CallBack {
        void getCashVideoSuccess(List<CashVideoResp.AuditVideoListBean> beans, boolean hasMore, int total, int pageNum);

        void getCashVideoFail(int code, String msg, int count);

    }
}

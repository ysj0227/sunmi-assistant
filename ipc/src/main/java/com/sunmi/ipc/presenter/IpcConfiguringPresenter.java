package com.sunmi.ipc.presenter;

import android.text.TextUtils;

import com.sunmi.ipc.contract.IpcConfiguringContract;
import com.sunmi.ipc.model.IpcListResp;
import com.sunmi.ipc.rpc.IPCCloudApi;

import java.util.ArrayList;
import java.util.List;

import sunmi.common.base.BasePresenter;
import sunmi.common.rpc.retrofit.RetrofitCallback;
import sunmi.common.utils.SpUtils;
import sunmi.common.utils.log.LogCat;

/**
 * Description:
 * Created by bruce on 2019/3/29.
 */
public class IpcConfiguringPresenter extends BasePresenter<IpcConfiguringContract.View>
        implements IpcConfiguringContract.Presenter {

    @Override
    public void ipcBind(String shopId, final String sn, String token, float longitude, float latitude) {
        IPCCloudApi.bindIPC(SpUtils.getCompanyId() + "", shopId, sn, TextUtils.isEmpty(token) ? 1 : 0,
                token, longitude, latitude, new RetrofitCallback() {
                    @Override
                    public void onSuccess(int code, String msg, Object data) {
                        LogCat.e("IpcConfiguringPresenter", "onSuccess 111");
                        if (isViewAttached()) mView.ipcBindSuccess(sn);
                    }

                    @Override
                    public void onFail(int code, String msg, Object data) {
                        LogCat.e("IpcConfiguringPresenter", "onFail 111");
                        if (isViewAttached()) mView.ipcBindFail(sn, code, msg);
                    }
                });
    }

    @Override
    public void getIpcList(int companyId, String shopId) {
        IPCCloudApi.getDetailList(companyId, SpUtils.getShopId(), new RetrofitCallback<IpcListResp>() {
            @Override
            public void onSuccess(int code, String msg, IpcListResp data) {
                LogCat.e("111111", "666666 getIpcList onResponse response = " + data.toString());
                List<IpcListResp.SsListBean> ipcList = new ArrayList<>();
                if (data.getFs_list() != null) {
                    ipcList.addAll(data.getFs_list());
                }
                if (data.getFs_list() != null) {
                    ipcList.addAll(data.getSs_list());
                }
                if (isViewAttached()) mView.getIpcListSuccess(ipcList);
            }

            @Override
            public void onFail(int code, String msg, IpcListResp data) {
                LogCat.e("111111", "666666 getIpcList onFail code = " + code);
                if (isViewAttached()) mView.getIpcListFail(code, msg);
            }
        });
    }

}

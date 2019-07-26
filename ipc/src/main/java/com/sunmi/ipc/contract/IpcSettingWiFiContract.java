package com.sunmi.ipc.contract;

import sunmi.common.base.BaseView;

/**
 * Created by YangShiJie on 2019/7/25.
 */
public interface IpcSettingWiFiContract {
    interface View extends BaseView {

        /**
         * 0离线 1在线
         *
         * @param ipcStatus
         */
        void ipcStatusSuccessView(int ipcStatus);
    }

    interface Presenter {

        void getIpcStatus(String deviceid, String model);
    }
}

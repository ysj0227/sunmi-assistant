package com.sunmi.assistant.dashboard.data;

import com.sunmi.assistant.dashboard.Utils;
import com.sunmi.assistant.dashboard.data.response.ShopListResponse;
import com.sunmi.ipc.rpc.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import sunmi.common.rpc.retrofit.RetrofitCallback;

/**
 * 订单管理远程接口
 *
 * @author yinhui
 * @since 2019-06-20
 */
public class ShopManagementRemote {

    private ShopManageService mService;

    private ShopManagementRemote() {
        mService = RetrofitClient.getInstance().create(ShopManageService.class);
    }

    private static final class Holder {
        private static final ShopManagementRemote INSTANCE = new ShopManagementRemote();
    }

    public static ShopManagementRemote get() {
        return Holder.INSTANCE;
    }

    public void getShopList(int companyId, RetrofitCallback<ShopListResponse> callback) {
        try {
            String params = new JSONObject()
                    .put("company_id", companyId)
                    .toString();
            mService.getList(Utils.createRequestBody(params)).enqueue(callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

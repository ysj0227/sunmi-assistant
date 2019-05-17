package sunmi.common.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.commonlibrary.R;

import sunmi.common.view.SimpleRecyclerViewAdapter;
import sunmi.common.view.activity.StartConfigSMDeviceActivity_;

/**
 * Description:
 * Created by bruce on 2019/4/12.
 */
public class ChooseDeviceDialog extends Dialog {

    String shopId;

    public ChooseDeviceDialog(Context context, String shopId) {
        super(context);
        this.shopId = shopId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_choose_device, null);
        setContentView(contentView);
        RecyclerView recyclerView = findViewById(R.id.rv_devices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(getAdapter());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = getContext().getResources().getDisplayMetrics().widthPixels;
        getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_corner_top_white);
        getWindow().setAttributes(lp);//设置宽度适应屏幕
        getWindow().setGravity(Gravity.BOTTOM);
//        getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    /**
     * 只有图标和文字的简单adapter
     */
    private SimpleRecyclerViewAdapter getAdapter() {
        int[] imageIds = {R.mipmap.ic_sunmi_ap, R.mipmap.ic_sunmi_fs};
        String[] names = getContext().getResources().getStringArray(R.array.sunmi_devices);
        SimpleRecyclerViewAdapter adapter = new SimpleRecyclerViewAdapter(
                R.layout.item_choose_device, imageIds, names);
        adapter.setOnItemClickListener(new SimpleRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                dismiss();
                StartConfigSMDeviceActivity_.intent(getContext())
                        .deviceType(pos).shopId(shopId).start();
            }
        });
        return adapter;
    }

}
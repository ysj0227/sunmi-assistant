package com.sunmi.ipc.cash.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sunmi.ipc.R;
import com.sunmi.ipc.model.CashVideoResp;

import java.util.ArrayList;
import java.util.List;

import sunmi.common.utils.DateTimeUtils;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-12-06.
 */
public class CashVideoAdapter extends RecyclerView.Adapter<CashVideoAdapter.ViewHolder> {

    private ArrayList<CashVideoResp.AuditVideoListBean> data;
    private Context context;
    private OnItemClickListener listener;
    private int selectPosition = -1;

    public CashVideoAdapter(ArrayList<CashVideoResp.AuditVideoListBean> data, Context context) {
        this.data = data;
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(ArrayList<CashVideoResp.AuditVideoListBean> data, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSelectPosition(int selectPosition) {
        notifyItemChanged(this.selectPosition);
        notifyItemChanged(selectPosition);
        this.selectPosition = selectPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cash_item_trade_details,
                viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if (selectPosition != -1) {
            viewHolder.itemView.setSelected(selectPosition == i);
        }
        CashVideoResp.AuditVideoListBean bean = data.get(i);
        viewHolder.tvTime.setText(DateTimeUtils.secondToDate(bean.getPurchaseTime(), "HH:mm:ss"));
        if (!TextUtils.isEmpty(bean.getDescription())) {
            viewHolder.tvDescription.setVisibility(View.VISIBLE);
            viewHolder.tvDescription.setText(bean.getDescription());
        } else {
            viewHolder.tvDescription.setVisibility(View.GONE);
        }
        viewHolder.tvAmount.setText(bean.getAmount());
        viewHolder.tvOrderNum.setText(bean.getOrderNo());
        Glide.with(context).load(bean.getSnapshotUrl()).into(viewHolder.ivPreview);
        if (i == data.size() - 1) {
            viewHolder.tvLineBottom.setVisibility(View.INVISIBLE);
        } else if (i == 0) {
            viewHolder.tvLineTop.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.tvLineTop.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTime;
        TextView tvDescription;
        TextView tvAmount;
        TextView tvOrderNum;
        ImageView ivPreview;
        TextView tvLineTop;
        TextView tvLineBottom;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvDescription = itemView.findViewById(R.id.tv_exception_des);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvOrderNum = itemView.findViewById(R.id.tv_order_num);
            ivPreview = itemView.findViewById(R.id.iv_preview_img);
            tvLineTop = itemView.findViewById(R.id.tv_left_top_line);
            tvLineBottom = itemView.findViewById(R.id.tv_left_bottom_line);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(data, getAdapterPosition());
            }
        }
    }
}
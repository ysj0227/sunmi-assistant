package com.sunmi.assistant.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.sunmi.assistant.data.response.AdListResp;
import com.youth.banner.loader.ImageLoader;

/**
 * Description:
 * Created by bruce on 2019/7/1.
 */
public class GlideImageLoader extends ImageLoader {
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        Glide.with(context).load(((AdListResp.AdListBean) path).getImage_addr())
                .apply(new RequestOptions().transforms(new CenterCrop(),
                        new RoundedCorners(20))).into(imageView);
    }

}
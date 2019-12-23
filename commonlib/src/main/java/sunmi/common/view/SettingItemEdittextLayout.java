package sunmi.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.commonlibrary.R;

import sunmi.common.utils.CommonHelper;

/**
 * @author yangShiJie
 * @date 2019-10-11
 */
public class SettingItemEdittextLayout extends RelativeLayout {

    public RelativeLayout parentLayout;
    public ImageView ivMark;
    public TextView tvLeft;
    public TextView tvRight;
    public ClearableEditText etContent;
    public View divider;

    private Context mContext;

    private int defaultLeftTextColor = 0xFF525866;
    private int defaultRightTextColor = 0xFFA1A7B3;
    private int defaultEditTextColor = 0xFF303540;
    private int defaultEditTextHintColor = 0xFFA1A7B3;
    private int defaultDividerColor = 0x1A333C4F;

    public SettingItemEdittextLayout(Context context) {
        super(context);
        this.mContext = context;
    }

    public SettingItemEdittextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initLayout();
        attributeSet(attrs);
    }

    public SettingItemEdittextLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initLayout();
        attributeSet(attrs);
    }

    private void initLayout() {
        View view = View.inflate(mContext, R.layout.view_edittext_setting_item, this);
        parentLayout = view.findViewById(R.id.rl_setting_item);
        ivMark = view.findViewById(R.id.iv_mark);
        tvLeft = view.findViewById(R.id.tv_left);
        tvRight = view.findViewById(R.id.tv_right);
        etContent = view.findViewById(R.id.et_content);
        divider = view.findViewById(R.id.divider);
    }

    private void attributeSet(AttributeSet attrs) {
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.SettingItemEdittextLayout);
        float height = a.getDimension(R.styleable.SettingItemEdittextLayout_parentHeight, CommonHelper.dp2px(mContext, 48));

        if (parentLayout != null) {
            parentLayout.getLayoutParams().height = (int) height;
        }

        Drawable leftMark = a.getDrawable(R.styleable.SettingItemEdittextLayout_imageLeft);
        ivMark.setImageDrawable(leftMark);

        float leftImageSize = a.getDimension(R.styleable.SettingItemEdittextLayout_imageLeftSize, CommonHelper.dp2px(mContext, 0));
        if (leftImageSize > 0) {
            ivMark.getLayoutParams().width = (int) leftImageSize;
            ivMark.getLayoutParams().height = (int) leftImageSize;
        }

        //左侧textView
        tvLeft.setText(a.getString(R.styleable.SettingItemEdittextLayout_leftText));
        if (a.hasValue(R.styleable.SettingItemEdittextLayout_leftTextSize)) {
            tvLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    a.getDimensionPixelSize(R.styleable.SettingItemEdittextLayout_leftTextSize, CommonHelper.dp2px(mContext, 16)));
        }

        if (a.hasValue(R.styleable.SettingItemEdittextLayout_leftTextColor)) {
            tvLeft.setTextColor(a.getColor(R.styleable.SettingItemEdittextLayout_leftTextColor, defaultLeftTextColor));
        }

        //右侧textView
        tvRight.setText(a.getString(R.styleable.SettingItemEdittextLayout_rightText));
        if (a.hasValue(R.styleable.SettingItemEdittextLayout_rightTextSize)) {
            tvRight.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    a.getDimensionPixelSize(R.styleable.SettingItemEdittextLayout_rightTextSize, CommonHelper.dp2px(mContext, 16)));
        }
        if (a.hasValue(R.styleable.SettingItemEdittextLayout_rightTextColor)) {
            tvRight.setTextColor(a.getColor(R.styleable.SettingItemEdittextLayout_rightTextColor, defaultRightTextColor));
        }

        //editText
        etContent.setText(a.getString(R.styleable.SettingItemEdittextLayout_editTextContent));
        etContent.setHint(a.getString(R.styleable.SettingItemEdittextLayout_editTextHint));
        etContent.setInputType(a.getInt(R.styleable.SettingItemEdittextLayout_editTextInputType, EditorInfo.TYPE_NULL));
        if (a.hasValue(R.styleable.SettingItemEdittextLayout_editTextSize)) {
            etContent.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    a.getDimension(R.styleable.SettingItemEdittextLayout_editTextSize, CommonHelper.dp2px(mContext, 16)));
        }
        if (a.hasValue(R.styleable.SettingItemEdittextLayout_editTextColor)) {
            etContent.setTextColor(a.getColor(R.styleable.SettingItemEdittextLayout_editTextColor, defaultEditTextColor));
        }
        if (a.hasValue(R.styleable.SettingItemEdittextLayout_editTextHintColor)) {
            etContent.setHintTextColor(a.getColor(R.styleable.SettingItemEdittextLayout_editTextHintColor, defaultEditTextHintColor));
        }
//        if (a.hasValue(R.styleable.SettingItemEdittextLayout_editable)) {
////            etContent.setEnabled(false);
//            etContent.setFocusable(false);
//            etContent.setActivated(false);
//            requestFocus();
//            tvRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_arrow_small, 0);
//        }
        if (a.hasValue(R.styleable.SettingItemEdittextLayout_editTextRightPadding)) {
            etContent.setPadding(0, 0, (int) a.getDimension(R.styleable.SettingItemEdittextLayout_editTextRightPadding,
                    CommonHelper.dp2px(mContext, 20)), 0);
        }

        boolean dividerShow = a.getBoolean(R.styleable.SettingItemEdittextLayout_dividerShow, false);
        //分割线
        if (dividerShow) {
            divider.setVisibility(VISIBLE);
            if (a.hasValue(R.styleable.SettingItemEdittextLayout_dividerColor)) {
                divider.setBackgroundColor(a.getColor(R.styleable.SettingItemEdittextLayout_dividerColor, defaultDividerColor));
            }
            if (a.hasValue(R.styleable.SettingItemEdittextLayout_dividerHeight)) {
                float dividerHeight = a.getDimension(R.styleable.SettingItemEdittextLayout_dividerHeight, mContext.getResources().getDimension(R.dimen.dp_0_5));
                ViewGroup.LayoutParams lp = divider.getLayoutParams();
                lp.height = (int) dividerHeight;
                divider.setLayoutParams(lp);
            }
        } else {
            divider.setVisibility(GONE);
        }
        a.recycle();
    }

    public void setLeftText(String text) {
        tvLeft.setText(text);
    }

    public void setLeftTextSize(float size) {
        tvLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setLeftTextColor(int color) {
        tvLeft.setTextColor(color);
    }

    public TextView getRightText() {
        return tvRight;
    }

    public void setRightText(String text) {
        tvRight.setText(text);
    }

    public void setRightTextSize(float size) {
        tvRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setRightTextColor(int color) {
        tvRight.setTextColor(color);
    }

    public ClearableEditText getEditTextText() {
        return etContent;
    }

    public void setEditTextText(String text) {
        etContent.setText(text);
    }

    public void setEditTextSize(float size) {
        etContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setEditTextColor(int color) {
        etContent.setTextColor(color);
    }

    public void setEditTextHintColor(int color) {
        etContent.setHintTextColor(color);
    }

}

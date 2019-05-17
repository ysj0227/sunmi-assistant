package sunmi.common.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.commonlibrary.R;

/**
 * Description:
 * Created by bruce on 2019/3/29.
 */
public class InputDialog extends Dialog {
    EditText etInput;

    private InputDialog(Context context, int theme) {
        super(context, theme);
    }

    public void showWithOutTouchable(boolean touchable) {
        this.setCanceledOnTouchOutside(touchable);
        this.show();
    }

    /**
     * builder class for creating a custom dialog
     */
    public static class Builder {
        private Context context;
        private String title;
        private String backBtnText; // 对话框返回按钮文本
        private String confirmBtnText; // 对话框确定文本
        private int confirmBtnTextColor; // 对话框确定文本颜色
        // 对话框按钮监听事件
        private DialogInterface.OnClickListener cancelButtonClickListener;
        private ConfirmClickListener confirmClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置title显示，resource获取
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * 设置title显示，直接设置字符串
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * 设置back按钮的事件和文本（resource资源）
         */
        public Builder setCancelButton(int text, DialogInterface.OnClickListener listener) {
            this.backBtnText = (String) context.getText(text);
            this.cancelButtonClickListener = listener;
            return this;
        }

        /**
         * 设置back按钮的事件和文本（resource资源）,直接dismiss
         */
        public Builder setCancelButton(int text) {
            this.backBtnText = (String) context.getText(text);
            this.cancelButtonClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            };
            return this;
        }

        /**
         * 设置确定按钮事件和文本（resource资源）
         */
        public Builder setConfirmButton(String text, ConfirmClickListener listener) {
            this.confirmBtnText = text;
            this.confirmClickListener = listener;
            return this;
        }

        /**
         * 设置确定按钮事件和文本（resource资源）
         */
        public Builder setConfirmButton(int text, ConfirmClickListener listener) {
            return setConfirmButton((String) context.getText(text), listener);
        }

        /**
         * 设置确定按钮事件和文本（resource资源）
         */
        public Builder setConfirmButton(int text) {
            return setConfirmButton((String) context.getText(text));
        }

        /**
         * 设置确定按钮事件和文本（resource资源）
         */
        public Builder setConfirmButton(String text) {
            this.confirmBtnText = text;
            this.confirmClickListener = new ConfirmClickListener() {
                @Override
                public void onConfirmClick(InputDialog dialog, String input) {
                    dialog.dismiss();
                }
            };
            return this;
        }

        /**
         * 设置确定按钮事件和文本(颜色)（resource资源）
         */
        public Builder setConfirmButton(int text, int textColor, ConfirmClickListener listener) {
            this.confirmBtnText = (String) context.getText(text);
            this.confirmBtnTextColor = textColor;
            this.confirmClickListener = listener;
            return this;
        }

        /**
         * 提示框，设置确定按钮事件和文本(颜色)（resource资源）
         */
        public Builder setConfirmButton(int text, int textColor) {
            this.confirmBtnText = (String) context.getText(text);
            this.confirmBtnTextColor = textColor;
            this.confirmClickListener = new ConfirmClickListener() {
                @Override
                public void onConfirmClick(InputDialog dialog, String input) {
                    dialog.dismiss();
                }
            };
            return this;
        }

        /**
         * 设置确定按钮事件和文本（字符串）
         */
        public Builder setConfirmButton(String text, int textColor, ConfirmClickListener listener) {
            this.confirmBtnText = text;
            this.confirmBtnTextColor = textColor;
            this.confirmClickListener = listener;
            return this;
        }

        /**
         * 创建自定义的对话框
         */
        public InputDialog create() {
            final LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // 实例化自定义的对话框主题
            final InputDialog dialog = new InputDialog(context, R.style.Son_dialog);
            View layout = inflater.inflate(R.layout.dialog_input, null);

            dialog.addContentView(layout, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            TextView tvTitle = layout.findViewById(R.id.tv_title);
            if (TextUtils.isEmpty(title)) {
                tvTitle.setVisibility(View.GONE);
            } else {
                tvTitle.setText(title);
            }

            final EditText etInput = layout.findViewById(R.id.et_input);

            // 设置返回按钮事件和文本
            if (backBtnText != null) {
                Button bckButton = layout.findViewById(R.id.btn_cancel);
                bckButton.setText(backBtnText);

                if (cancelButtonClickListener != null) {
                    bckButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.cancel();
                            cancelButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.btn_cancel).setVisibility(View.GONE);
                layout.findViewById(R.id.btn_divider).setVisibility(View.GONE);
            }

            // 设置确定按钮事件和文本
            if (confirmBtnText != null) {
                Button cfmButton = layout.findViewById(R.id.btn_sure);
                cfmButton.setText(confirmBtnText);
                if (confirmBtnTextColor > 0)
                    cfmButton.setTextColor(context.getResources().getColor(confirmBtnTextColor));

                if (confirmClickListener != null) {
                    cfmButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            confirmClickListener.onConfirmClick(dialog, etInput.getText().toString());
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.btn_sure).setVisibility(View.GONE);
                layout.findViewById(R.id.btn_divider).setVisibility(View.GONE);
            }
            dialog.setContentView(layout);
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }

    public interface ConfirmClickListener {
        void onConfirmClick(InputDialog dialog, String input);
    }

}
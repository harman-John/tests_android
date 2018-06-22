package jbl.stc.com.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import jbl.stc.com.R;
import jbl.stc.com.listener.OnDialogListener;

/**
 * CreateMyOwnEqDialog
 * Created by darren.lu on 2017/11/1.
 */
public class CreateMyOwnEqDialog extends Dialog {
    private OnDialogListener onDialogListener;

    public void setOnDialogListener(OnDialogListener onDialogListener) {
        this.onDialogListener = onDialogListener;
    }

    public CreateMyOwnEqDialog(Activity context) {
        super(context, R.style.AppDialog);
        initUI();
    }

    private void initUI() {
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        setContentView(R.layout.dialog_create_my_own_eq);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        Window window = getWindow();
        window.setWindowAnimations(R.style.dialog_style);
        window.setAttributes(lp);

        findViewById(R.id.buttonConfirm).setOnClickListener(onClickListener);
        findViewById(R.id.closeImageView).setOnClickListener(onClickListener);
    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonConfirm:
                    dismiss();
                    if (onDialogListener != null) {
                        onDialogListener.onConfirm();
                    }
                    break;
                case R.id.closeImageView:
                    dismiss();
                    break;
            }
        }
    };

}

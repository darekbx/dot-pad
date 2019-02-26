package com.dotpad.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

public class BaseDialog extends Dialog {

    public BaseDialog(Context context) {

        super(context);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);
    }

    protected String getString(int resourceId, Object... formatArgs) {
        return getContext().getString(resourceId, formatArgs);
    }
}

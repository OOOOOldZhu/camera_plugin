package com.z.camera_plugin;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

/*
 * ：Created by z on 2019/1/16
 */
public abstract class CameraDialog extends BaseDialog {

    public CameraDialog(Activity activity) {
        super(activity, R.layout.msg_dialog);
    }

    @Override
    public void initView(View view) {
        view.findViewById(R.id.line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAfter(v);
                dismissDialog();
            }
        });
    }
    public abstract void onAfter(View v);
}

package com.z.camera_plugin;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.z.camera_plugin.R;

/*
 * ：Created by z on 2019/1/16
 */

public abstract class BaseDialog {

    public Dialog dialog;
    public Activity activity;

    public BaseDialog(Activity activity, int res) {
        this.activity = activity;
        dialog = new Dialog(activity, R.style.BuildDialog);
        dialog.setCanceledOnTouchOutside(true);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(res, null);
        initView(view);

        initWindow();

        dialog.setContentView(view);
    }

    private void initWindow() {
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距

        //设置去除dialog中的系统状态栏
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // window.setWindowAnimations(R.style.dialog_anima);

        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
    }

    public abstract void initView(View view);

    public void showDialog() {
        Log.d("xiaoqq", " " + (dialog != null) + " " + (!dialog.isShowing()) + " " + (activity != null) + " " + (!activity.isFinishing()));
        if (dialog != null && !dialog.isShowing() && activity != null && !activity.isFinishing()) {
//        if (dialog != null && !dialog.isShowing() && activity != null) {
            hide();
            dialog.show();
        }
    }

    /*
     *  隐藏系统底部栏 方式2
     */
    private void hide() {
        if (dialog != null) {
            dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            dialog.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            //布局位于状态栏下方
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            //全屏
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            //隐藏导航栏
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    if (Build.VERSION.SDK_INT >= 19) {
                        uiOptions |= 0x00001000;
                    } else {
                        uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
                    }
                    dialog.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                }
            });
        }
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            hideSystemNavigationBar(activity);
        }

    }

    public static void hideSystemNavigationBar(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View view = activity.getWindow().getDecorView();
            view.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}

package com.z.camera_plugin;

import android.util.Log;
import android.view.View;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * CameraPlugin
 */
public class CameraPlugin implements MethodCallHandler {

    static Registrar registrar;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar register) {
        registrar = register;
        final MethodChannel channel = new MethodChannel(register.messenger(), "camera_plugin");
        channel.setMethodCallHandler(new CameraPlugin());

    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("gotoCamera")) {
            Log.d("", "java 中接收到的参数 - - - - - - - - - - - - - - - - - - - - - > " + call.arguments);
            showScreen(call.arguments.toString(), result);
        } else {
            result.notImplemented();
        }
    }

    /* let data = {
                url_camp:"mic",
                isPeaple:isNotPlant,
                peapleCamera:{
                    delayTime:args.mode, // 延时几秒执行  -1 3 2 1
                    type: args.picture   // 识别类型 age skin glasses等
                },
                platOrAnimal:{
                    delayTime:args.mode,            // 延时  -1 3 2 1
                    type: args.animal_or_plant,     // 识别类型，0 植物  1 动物
                    resultCount:args.value_number   // 识别结果取几个值？ 0~5 ,0 为取多个值
                }
            }
    */
    private void showScreen(String jsonStr, final Result result) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            //更换上传图片的URL
            String url_camp = jsonObject.getString("url_camp");
            ApiConstant.baseUrl = "https://mproxy." + url_camp + ".cn";

            Log.d("", "- - - - - - - - - - - - - - - - - - - - - > " + jsonStr);
            new CameraDialog(registrar.activity()) {
                @Override
                public void onAfter(String str) {
                    //todo 拍照完毕后要做的事情
                    result.success(str);
                }
            }.setDefult(jsonStr).showDialog();
        } catch (Exception e) {
            //e.printStackTrace();
            Log.d("", "showScreen: - - - - - - - - - - - - - - - - - - - - - > "+e);
        }
    }

}

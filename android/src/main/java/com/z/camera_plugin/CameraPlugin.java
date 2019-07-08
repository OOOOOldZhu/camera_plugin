package com.z.camera_plugin;

import android.view.View;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** CameraPlugin */
public class CameraPlugin implements MethodCallHandler {

  static Registrar registrar;

  /** Plugin registration. */
  public static void registerWith(Registrar register) {
    registrar = register;
    final MethodChannel channel = new MethodChannel(register.messenger(), "camera_plugin");
    channel.setMethodCallHandler(new CameraPlugin());

  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if(call.method.equals("gotoCamera")){
      showScreen(result);
    }else{
      result.notImplemented();
    }
  }

  private void showScreen(final Result result){
    new CameraDialog(registrar.activity()) {
      @Override
      public void onAfter(String str) {
        //todo 拍照完毕后要做的事情
        result.success(str);
      }
    }.setDefult("0,ocr").showDialog();
  }

}

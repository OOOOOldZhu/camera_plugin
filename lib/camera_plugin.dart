import 'dart:async';

import 'package:flutter/services.dart';

class CameraPlugin {
  static const MethodChannel _channel =
      const MethodChannel('camera_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}

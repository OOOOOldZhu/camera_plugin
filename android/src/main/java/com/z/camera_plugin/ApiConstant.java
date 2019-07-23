package com.z.camera_plugin;

/*
 * ：Created by z on 2019/7/8
 */

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiConstant {

    static String baseUrl = "https://mproxy.zz.cn";

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
    static String getCompany(String jsonStr) {
        if (jsonStr.equalsIgnoreCase("")) {
            return "ms";
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.getBoolean("isPeaple") &&
                jsonObject.getJSONObject("peapleCamera").getString("type").equalsIgnoreCase("emotion")) {
                return "ms";
            }
            return "baidu";
        } catch (Exception e) {
            Log.d("", "getCompany: " + e);
            return "ms";
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
    static String getService(String jsonStr) {
        if (jsonStr.equalsIgnoreCase("")) {
            return "emotion";
        }
        try {
            String type = "";
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.getBoolean("isPeaple")) {
                type = jsonObject.getJSONObject("peapleCamera").getString("type");
            } else {
                type = jsonObject.getJSONObject("platOrAnimal").getString("type");
            }
            switch (type) {
                case "emotion":
                    return "emotion";

                case "gender":
                case "age":
                case "skin":
                case "glasses":
                case "beauty":
                    return "faceDetect";

                case "carLicense":
                    return "carLicensePlate";

                case "ocr":
                    return "ocr";

                case "0":
                    return "plantDetect";

                case "1":
                    return "animalDetect";

                default:
                    return "emotion";
            }
        } catch (Exception e) {
            //e.printStackTrace();
            Log.d("", "getService: - - - - - - - - - - - - - - - - - - - - - > " + e);
            return "emotion";
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
    static int getDelayTime(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.getBoolean("isPeaple")) {
                return jsonObject.getJSONObject("peapleCamera").getInt("delayTime");
            }
            return jsonObject.getJSONObject("platOrAnimal").getInt("delayTime");
        } catch (Exception e) {
            //e.printStackTrace();
            Log.d("", "getDelayTime: - - - - - - - - - - - - - - - - - - - - - > " + e);
            return -1;
        }
    }

}

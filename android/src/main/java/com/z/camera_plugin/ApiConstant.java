package com.z.camera_plugin;

/*
 * ：Created by z on 2019/7/8
 */

public class ApiConstant {

    static String baseUrl = "https://mproxy.zz.cn";

    // 0,emotion

    static String getCompany(String initedStr){
        if(initedStr.equalsIgnoreCase("")){
            return "ms";
        }
        String[] split = initedStr.split(",");
        if(split[1].equalsIgnoreCase("emotion")){
            return "ms";
        }
        return "baidu";
    }

    // 0,emotion
    static String getService(String initedStr){
        if(initedStr.equalsIgnoreCase("")){
            return "emotion";
        }
        String[] split = initedStr.split(",");
        switch (split[1]) {
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
            default:
                return "emotion";
        }
    }

}

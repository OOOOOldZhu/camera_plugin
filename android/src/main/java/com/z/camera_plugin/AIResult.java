package com.z.camera_plugin;

/*
 * ：Created by z on 2019/7/8
 */

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AIResult {

    // initedStr   // 0,emotion
    static String getResult(String initedStr, String res) {

        String[] split = initedStr.toString().split(",");
        String isFace = split[0]; // 0 or 1
        String type = split[1]; // emotion  、 gender 、  age 、  skin
        Log.d("", "getResult: " + isFace + " " + type);

        if (isFace.equalsIgnoreCase("0")) {
            switch (type) {
                case "emotion":
                    String emotion = "unknow";
                    try {
                        JSONObject jsonObject = new JSONObject(res);
                        JSONArray ar = jsonObject.getJSONArray("data");
                        JSONObject scores = ar.getJSONObject(0).getJSONObject("scores");

                        HashMap map = new Gson().fromJson(scores.toString(), HashMap.class);

                        Double d = 0.0;
                        String Vkey = "";
                        Iterator iter = map.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry entry = (Map.Entry) iter.next();
                            String key = entry.getKey().toString();
                            Object val = entry.getValue();
                            Double v = Double.parseDouble(val.toString());
                            if (v > d) {
                                d = v;
                                Vkey = key;
                            }
                        }
                        return emotion = Vkey;
                    } catch (Exception e) {
                        return emotion;
                    }
                case "gender":
                    try {
                        String s = new JSONObject(res).getJSONObject("data").getJSONArray("result").getJSONObject(0).get("gender").toString();
                        return s;
                    } catch (Exception e) {
                        return "fail";
                    }
                case "age":
                    try {
                        String s = new JSONObject(res).getJSONObject("data").getJSONArray("result").getJSONObject(0).get("age").toString();
                        return s;
                    } catch (Exception e) {
                        return "fail";
                    }
                case "skin":
                    try {
                        String s = new JSONObject(res).getJSONObject("data").getJSONArray("result").getJSONObject(0).get("skin").toString();
                        return s;
                    } catch (Exception e) {
                        return "fail";
                    }
                case "glasses":
                    try {
                        String s = new JSONObject(res).getJSONObject("data").getJSONArray("result").getJSONObject(0).get("glasses").toString();
                        return s;
                    } catch (Exception e) {
                        return "fail";
                    }
                case "beauty":
                    try {
                        String s = new JSONObject(res).getJSONObject("data").getJSONArray("result").getJSONObject(0).get("beauty").toString();
                        return s;
                    } catch (Exception e) {
                        return "fail";
                    }
                case "carLicense":
                    try {
                        String s = new JSONObject(res).getJSONObject("data").getJSONObject("words_result").get("number").toString();
                        return s;
                    } catch (Exception e) {
                        return "fail";
                    }
                case "ocr":
                    try {
                        String s = new JSONObject(res).getJSONObject("data").get("result").toString().replaceAll("\\n", "");
                        return s;
                    } catch (Exception e) {
                        return "fail";
                    }
                default:
                    return "fail";
            }
        } else if (isFace.equalsIgnoreCase("1")) {
            switch (type) {
                case "0": //  植物
                case "1": // 动物
                    try {
                        JSONArray jsonArray = new JSONObject(res).getJSONObject("data").getJSONArray("result");
                        double bigest = 0.0;
                        String name = "";
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String s = jsonArray.getJSONObject(i).get("score").toString();
                            if (Double.parseDouble(s) > bigest) {
                                bigest = Double.parseDouble(s);
                                name = jsonArray.getJSONObject(i).get("name").toString();
                            }
                        }
                        return name;
                    } catch (Exception e) {
                        return "fail";
                    }
                default:
                    return "fail";
            }
        } else {
            return "fail";
        }
    }

}

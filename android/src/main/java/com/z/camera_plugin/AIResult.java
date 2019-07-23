package com.z.camera_plugin;

/*
 * ：Created by z on 2019/7/8
 */

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AIResult {
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
    static String getResult(String jsonStr, String res) {
        try {
            JSONObject jsonOBJ = new JSONObject(jsonStr);
            boolean isPeaple = jsonOBJ.getBoolean("isPeaple"); // 0 or 1
            String type = "emotion"; // emotion  、 gender 、  age 、  skin
            if (isPeaple) {
                type = jsonOBJ.getJSONObject("peapleCamera").getString("type");
            } else {
                type = jsonOBJ.getJSONObject("platOrAnimal").getString("type");
            }
            if (isPeaple) {
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
                            String s = new JSONObject(res).getJSONObject("data").getJSONArray("result").getJSONObject(0).get("race").toString();
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
            } else {
                switch (type) {
                    case "0": //  植物
                    case "1": // 动物
                        try {
                            JSONArray jsonArray = new JSONObject(res).getJSONObject("data").getJSONArray("result");

                            ArrayList<JSONObject> arrayList = new ArrayList();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                arrayList.add((JSONObject) jsonArray.get(i));
                            }
                            for (int i = 0; i < arrayList.size(); i++) {
                                for (int j = 0; j < arrayList.size() - 1 - i; j++) {
                                    JSONObject temp;
                                    if (arrayList.get(j + 1).getDouble("score") >
                                        arrayList.get(j).getDouble("score")) {

                                        temp = arrayList.get(j + 1);
                                        arrayList.set(j + 1, arrayList.get(j));
                                        arrayList.set(j, temp);

                                    }
                                }
                            }
                            int num = jsonOBJ.getJSONObject("platOrAnimal").getInt("resultCount");

                            String name = "";
                            for (int i = 0; i < arrayList.size(); i++) {
                                name = arrayList.get(i).getString("name")+",";
                            }
                            if(name.lastIndexOf(",") == name.length()-1){
                                //'哈士奇,兔八哥,'
                                name= name.substring(0,name.length()-1);
                            }
                            Log.d("", "动植物识别的最后多项个结果 - - - - - - - - - - - - - - - - - - > "+name);
                            if( num == 0 ){
                                return name;
                            }
                            //比如取第三个值 3
                            return arrayList.get(num-1).getString("name");
                        } catch (Exception e) {
                            return "fail";
                        }
                    default:
                        return "fail";
                }
            }
        } catch (Exception e) {
            return "fail";
        }
    }

}

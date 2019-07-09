package com.z.camera_plugin;

import android.content.Context;

import com.google.gson.Gson;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by z on 2017/9/26.
 */

public class AIApi {
    private static Retrofit retrofit;
    private static AIService aiService;
    public static void  initByBaseURL(Context context, String BaseURL){
         retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                 .addConverterFactory(GsonConverterFactory.create(new Gson()))//convert就是将json解析成javaBean
                 .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                 .build();//执行创建方法
        aiService = retrofit.create(AIService.class);
    }
    public interface AIService{
        //图像识别
        @Multipart
        @POST("{specialMode_company}/{specialMode_services}")
        Call<ResponseBody> uploadPic(@Path("specialMode_company") String specialMode_company,
                                     @Path("specialMode_services") String specialMode_services,
                                     @Part MultipartBody.Part part);
    }
    public static Call<ResponseBody> uploadPic(String specialMode_company,
                                               String specialMode_services,
                                               MultipartBody.Part part){
        return aiService.uploadPic(specialMode_company,specialMode_services, part);
    }
}

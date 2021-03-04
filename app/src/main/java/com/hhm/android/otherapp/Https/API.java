package com.hhm.android.otherapp.Https;


import com.hhm.android.otherapp.utils.RatVo;

import java.io.File;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * @author huangche
 * 后端请求接口定义
 */
public interface API {

    // 注册请求
    @Headers({"Content-Type:application/json;charset=UTF-8","Accept:application/json"})
    @POST("/devices/register")
    Observable<RatVo> register(@Body RatVo hashVo);

    // 心跳请求
    @Headers({"Content-Type: application/json","Accept: application/json"})
    @POST("/devices/heart")
    Observable<RatVo> heart(@Body RatVo hashVo);

    // 请求指令
    @Headers({"Content-Type: application/json","Accept: application/json"})
    @POST("/devices/commands")
    Observable<RatVo> commands(@Body RatVo hashVo);

    // 数据结果上报
    @Headers({"Content-Type: application/json","Accept: application/json"})
    @POST("/devices/report/data")
    Observable<RatVo> datas(@Body RatVo hashVo);

    // 实时数据结果上报
    @Headers({"Content-Type: application/json","Accept: application/json"})
    @POST("/devices/rt_report/data")
    Observable<RatVo> datas_rt(@Body RatVo hashVo);


    // 文件结果上报
    @POST("/devices/report/file")
    Call<RatVo> file(@Body RequestBody Body);

    // 实时文件结果上报
    @POST("/devices/rt_report/file")
    Call<RatVo> file_rt(@Body RequestBody Body);

}

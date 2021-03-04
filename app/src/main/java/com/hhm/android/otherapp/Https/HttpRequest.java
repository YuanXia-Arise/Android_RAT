package com.hhm.android.otherapp.Https;


import android.accounts.Account;
import android.content.Context;

import com.google.gson.JsonObject;
import com.hhm.android.otherapp.TelegramManager;
import com.hhm.android.otherapp.utils.RatVo;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author huangche
 * 后端请求接口实现类
**/
public class HttpRequest {

    // 注册请求
    public static Observable<RatVo> Register(JsonObject common, JsonObject params, String url){
        RatVo ratVo = new RatVo();
        ratVo.setCommon(common);
        ratVo.setParams(params);
        /*OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(SSLHelper.getSSLCertifcation(context))//获取SSLSocketFactory
                .hostnameVerifier(new UnSafeHostnameVerifier())//添加hostName验证器
                .build();*/
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
                //.client(okHttpClient)
                .client(genericClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        API api = retrofit.create(API.class);
        Observable<RatVo> register = api.register(ratVo).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
        return register;
    }

    // 心跳请求
    public static Observable<RatVo> Heart(JsonObject common, String url) {
        RatVo ratVo = new RatVo();
        ratVo.setCommon(common);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
                .client(genericClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        API api = retrofit.create(API.class);
        Observable<RatVo> heart = api.heart(ratVo).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
        return heart;
    }

    // 请求指令
    public static Observable<RatVo> Commands(JsonObject common, String url){
        RatVo ratVo = new RatVo();
        ratVo.setCommon(common);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
                .client(genericClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        API api = retrofit.create(API.class);
        Observable<RatVo> commands = api.commands(ratVo).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
        return commands;
    }

    // 数据结果上报
    public static Observable<RatVo> Datas(JsonObject common, JsonObject params, String url){
        RatVo ratVo = new RatVo();
        ratVo.setCommon(common);
        ratVo.setParams(params);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
                .client(genericClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        API api = retrofit.create(API.class);
        Observable<RatVo> datas = api.datas(ratVo).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
        return datas;
    }


    // 实时数据结果上报
    public static Observable<RatVo> Datas_rt(JsonObject common, JsonObject params, String url){
        RatVo ratVo = new RatVo();
        ratVo.setCommon(common);
        ratVo.setParams(params);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
                .client(genericClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        API api = retrofit.create(API.class);
        Observable<RatVo> datas = api.datas_rt(ratVo).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
        return datas;
    }

    /*public static OkHttpClient genericClient() {
//        File cacheDir = new File(context.getCacheDir(), "Cache");
//        Cache mCache = new Cache(cacheDir, 10 * 1024 * 1024);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        int onlineCacheTime = 0;
                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("Connection","close")
                                .addHeader("Cache-Control","public, max-age="+onlineCacheTime)
                                .build();
                        return chain.proceed(request);
                    }
                }).connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                //.cache(mCache)
                .connectionPool(new ConnectionPool(5,1,TimeUnit.SECONDS))
                .build();
        return httpClient;
    }*/
    public static OkHttpClient genericClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request.Builder builder = chain.request()
                                .newBuilder()
                                .addHeader("Connection", "close"
                                );
                        return chain.proceed(builder.build());
                    }
                }).connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .connectionPool(new ConnectionPool(5,1,TimeUnit.SECONDS))
                .build();
    }




}

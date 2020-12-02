package com.hhm.android.otherapp.Https;


import com.google.gson.JsonObject;
import com.hhm.android.otherapp.utils.RatVo;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
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
        /*HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();*/
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
                //.client(client)
                //.client(new SslContextFactory().getHttpsClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        API api = retrofit.create(API.class);
        Observable<RatVo> register = api.register(ratVo).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
        return register;
    }

    // 心跳请求
    public static Observable<RatVo> Heart(JsonObject common, String url){
        RatVo ratVo = new RatVo();
        ratVo.setCommon(common);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
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
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        API api = retrofit.create(API.class);
        Observable<RatVo> datas = api.datas_rt(ratVo).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
        return datas;
    }





}

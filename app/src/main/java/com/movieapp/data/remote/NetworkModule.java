package com.movieapp.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/** Retrofit 网络模块，管理 API 客户端实例 */
public class NetworkModule {
    // 模拟器用 10.0.2.2，真机改成电脑局域网 IP
    private static final String BASE_URL = "http://10.0.2.2:8888/";
    private static volatile TmdbApi api;

    public static TmdbApi getApi() {
        if (api == null) {
            synchronized (NetworkModule.class) {
                if (api == null) {
                    OkHttpClient ok = new OkHttpClient.Builder()
                        .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();
                    api = new Retrofit.Builder()
                        .baseUrl(BASE_URL).client(ok)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build().create(TmdbApi.class);
                }
            }
        }
        return api;
    }
}

package com.movieapp.data.remote;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Retrofit 网络模块
 *
 * 当前通过本地代理 (proxy.py) 转发到 TMDB API：
 *   Android 模拟器 → 10.0.2.2:8888 → proxy.py → api.themoviedb.org
 *
 * 如用真机调试，把 BASE_URL 改成电脑的局域网 IP，如 http://192.168.1.100:8888/
 * 如能直连 TMDB，直接改成 https://api.themoviedb.org/3/
 */
public class NetworkModule {
    private static final String BASE_URL = "http://10.0.2.2:8888/";

    // TMDB API Read Access Token（从 themoviedb.org/settings/api 获取）
    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI4OTUzNzFhZmQ4MWU5OTkzOGVjZDQ5MjgyMjEwMmVkNiIsIm5iZiI6MTc3NjE0Nzk4MS42MDUsInN1YiI6IjY5ZGRkZTBkOGUwYzJkNjkyODliOTI4NCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.kQ-3zMFnrs9Z1PzNiM6LP7Qc2bwuul50a798fCcLTSY";

    private static volatile TmdbApi api;

    public static TmdbApi getApi() {
        if (api == null) {
            synchronized (NetworkModule.class) {
                if (api == null) {
                    // 自动给每个请求加 Bearer Token 认证头
                    Interceptor authInterceptor = chain -> {
                        Request modified = chain.request().newBuilder()
                                .header("Authorization", "Bearer " + BEARER_TOKEN)
                                .header("Accept", "application/json")
                                .build();
                        return chain.proceed(modified);
                    };

                    OkHttpClient ok = new OkHttpClient.Builder()
                            .addInterceptor(authInterceptor)
                            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .build();

                    api = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(ok)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                            .create(TmdbApi.class);
                }
            }
        }
        return api;
    }
}

package com.movieapp.data.repository;

import android.content.Context;
import android.util.Log;
import com.movieapp.data.local.*;
import com.movieapp.data.remote.*;
import com.movieapp.model.*;
import java.util.*;
import java.util.concurrent.*;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 电影数据仓库
 * 统一管理网络请求（TMDB API）和本地存储（Room 数据库）
 */
public class MovieRepository {

    private static final String TAG = "MovieRepo";

    /** 成功回调 */
    public interface Success<T> { void on(T data); }

    /** 失败回调 */
    public interface Error { void on(String msg); }

    private final TmdbApi api;
    private final MovieDao dao;
    private final ExecutorService io = Executors.newFixedThreadPool(2);

    public MovieRepository(Context ctx) {
        this.api = NetworkModule.getApi();
        this.dao = MovieDatabase.getInstance(ctx).movieDao();
    }

    // ═══════ 网络请求 ═══════

    public void getHotMovies(int page, Success<List<MovieBrief>> cb, Error err) {
        api.getHotMovies(page).enqueue(new ApiCallback<>(cb, r -> r.results, err));
    }

    public void getTop250(int page, Success<List<MovieBrief>> cb, Error err) {
        api.getTop250(page).enqueue(new ApiCallback<>(cb, r -> r.results, err));
    }

    public void getByTag(String tag, int page, Success<List<MovieBrief>> cb, Error err) {
        api.getByTag(tag, page).enqueue(new ApiCallback<>(cb, r -> r.results, err));
    }

    public void getTags(Success<List<String>> cb, Error err) {
        api.getTags().enqueue(new ApiCallback<>(cb, r -> r.tags, err));
    }

    public void search(String q, int page, Success<List<MovieBrief>> cb, Error err) {
        api.search(q, page).enqueue(new ApiCallback<>(cb, r -> r.results, err));
    }

    /** 获取电影详情，带 ID 校验 */
    public void getDetail(String id, Success<MovieDetail> cb, Error err) {
        if (id == null || id.isEmpty()) {
            err.on("电影ID无效");
            return;
        }
        api.getDetail(id).enqueue(new retrofit2.Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> c, Response<MovieDetail> r) {
                if (r.isSuccessful() && r.body() != null) {
                    MovieDetail detail = r.body();
                    if (detail.getId() != null && detail.getTitle() != null) {
                        cb.on(detail);
                    } else {
                        err.on("电影信息不完整");
                    }
                } else {
                    err.on("获取详情失败: " + (r.message() != null ? r.message() : "未知错误"));
                }
            }
            @Override
            public void onFailure(Call<MovieDetail> c, Throwable t) {
                err.on("网络请求失败: " + t.getMessage());
            }
        });
    }

    // ═══════ 本地收藏（Room 数据库）═══════

    /** 添加电影到收藏（从列表项） */
    public void addToCollection(MovieBrief m, CollectionType t) {
        io.execute(() -> {
            try {
                dao.insert(MovieEntity.fromBrief(m, t));
            } catch (Exception e) {
                Log.e(TAG, "收藏失败(brief): " + e.getMessage());
            }
        });
    }

    /** 添加电影到收藏（从详情页） */
    public void addToCollection(MovieDetail m, CollectionType t) {
        io.execute(() -> {
            try {
                dao.insert(MovieEntity.fromDetail(m, t));
                Log.d(TAG, "已收藏: " + m.getTitle() + " → " + t.name());
            } catch (Exception e) {
                Log.e(TAG, "收藏失败(detail): " + e.getMessage());
            }
        });
    }

    /** 从收藏中移除 */
    public void removeFromCollection(String id, CollectionType t) {
        io.execute(() -> {
            try {
                dao.deleteFromCollection(id, t.name());
                Log.d(TAG, "已取消收藏: " + id + " → " + t.name());
            } catch (Exception e) {
                Log.e(TAG, "取消收藏失败: " + e.getMessage());
            }
        });
    }

    /** 检查电影是否已在指定收藏中 */
    public void isInCollection(String id, CollectionType t, Success<Boolean> cb, Error err) {
        io.execute(() -> {
            try {
                cb.on(dao.exists(id, t.name()));
            } catch (Exception e) {
                cb.on(false);
            }
        });
    }

    /** 获取指定收藏列表 */
    public void getCollection(CollectionType t, Success<List<MovieBrief>> cb, Error err) {
        io.execute(() -> {
            try {
                List<MovieBrief> out = new ArrayList<>();
                for (MovieEntity e : dao.getByCollection(t.name())) {
                    out.add(e.toBrief());
                }
                cb.on(out);
            } catch (Exception e) {
                cb.on(new ArrayList<>());
            }
        });
    }

    /** 同步详情信息到已收藏记录（丰富收藏数据） */
    public void syncCollectionToDetail(String movieId, MovieDetail detail, CollectionType type) {
        io.execute(() -> {
            try {
                if (dao.exists(movieId, type.name())) {
                    dao.insert(MovieEntity.fromDetail(detail, type));
                }
            } catch (Exception e) {
                Log.e(TAG, "同步详情失败: " + e.getMessage());
            }
        });
    }

    // ═══════ Retrofit 回调适配器 ═══════

    private static class ApiCallback<T, R> implements retrofit2.Callback<T> {
        private final Success<R> cb;
        private final java.util.function.Function<T, R> mapper;
        private final Error err;

        ApiCallback(Success<R> cb, java.util.function.Function<T, R> mapper, Error err) {
            this.cb = cb; this.mapper = mapper; this.err = err;
        }

        @Override
        public void onResponse(Call<T> c, Response<T> r) {
            if (r.isSuccessful() && r.body() != null) cb.on(mapper.apply(r.body()));
            else err.on("请求失败");
        }

        @Override
        public void onFailure(Call<T> c, Throwable t) {
            err.on(t.getMessage());
        }
    }
}

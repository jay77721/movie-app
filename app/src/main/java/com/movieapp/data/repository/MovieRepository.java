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
 * 统一管理：TMDB 网络请求 + 数据格式转换 + Room 本地收藏
 */
public class MovieRepository {

    private static final String TAG = "MovieRepo";
    private static final String LANG = "zh-CN";                         // TMDB 请求语言
    private static final String IMG_BASE = "https://image.tmdb.org/t/p"; // 图片 CDN 前缀

    public interface Success<T> { void on(T data); }
    public interface Error { void on(String msg); }

    private final TmdbApi api;
    private final MovieDao dao;
    private final ExecutorService io = Executors.newFixedThreadPool(2);

    // ═══════ 类型映射 ═══════
    // 中文名 → TMDB genre_id（用于分类筛选时把中文标签转成 ID）
    private static final Map<String, Integer> GENRE_MAP = new LinkedHashMap<>();
    static {
        GENRE_MAP.put("动作", 28);   GENRE_MAP.put("冒险", 12);
        GENRE_MAP.put("动画", 16);   GENRE_MAP.put("喜剧", 35);
        GENRE_MAP.put("犯罪", 80);   GENRE_MAP.put("剧情", 18);
        GENRE_MAP.put("家庭", 10751); GENRE_MAP.put("奇幻", 14);
        GENRE_MAP.put("历史", 36);   GENRE_MAP.put("恐怖", 27);
        GENRE_MAP.put("音乐", 10402); GENRE_MAP.put("悬疑", 9648);
        GENRE_MAP.put("爱情", 10749); GENRE_MAP.put("科幻", 878);
        GENRE_MAP.put("惊悚", 53);   GENRE_MAP.put("战争", 10752);
    }
    // genre_id → 中文名（用于 TMDB 返回的数字 ID 转成中文显示）
    private static final Map<Integer, String> GENRE_ID_TO_NAME = new HashMap<>();
    static {
        for (Map.Entry<String, Integer> e : GENRE_MAP.entrySet()) {
            GENRE_ID_TO_NAME.put(e.getValue(), e.getKey());
        }
    }

    public MovieRepository(Context ctx) {
        this.api = NetworkModule.getApi();
        this.dao = MovieDatabase.getInstance(ctx).movieDao();
    }

    // ═══════ TMDB 数据 → App 模型转换 ═══════

    /** TMDB 电影列表项 → MovieBrief */
    private MovieBrief toBrief(TmdbResponse.Movie m) {
        MovieBrief b = new MovieBrief();
        b.setId(String.valueOf(m.id));
        b.setTitle(m.title != null ? m.title : "");
        b.setRating(Math.round(m.voteAverage * 10.0) / 10.0);
        b.setCover(buildImageUrl(m.posterPath, "/w500"));
        b.setYear(m.getYear());
        b.setReleaseDate(m.releaseDate != null ? m.releaseDate : "");
        b.setGenres(genreIdsToNames(m.genreIds));
        return b;
    }

    /** TMDB 电影详情 + 演职员 → MovieDetail */
    private MovieDetail toDetail(TmdbResponse.Movie m, TmdbResponse.Credits credits) {
        MovieDetail d = new MovieDetail();
        d.setId(String.valueOf(m.id));
        d.setTitle(m.title != null ? m.title : "");
        d.setOriginalTitle(m.originalTitle != null ? m.originalTitle : "");
        d.setRating(Math.round(m.voteAverage * 10.0) / 10.0);
        d.setRatingCount(m.voteCount);
        d.setCover(buildImageUrl(m.posterPath, "/w500"));
        d.setBackdrop(buildImageUrl(m.backdropPath, "/w780"));
        d.setYear(m.getYear());
        d.setReleaseDate(m.releaseDate != null ? m.releaseDate : "");
        d.setDuration(m.runtime > 0 ? m.runtime + "分钟" : "");
        d.setSummary(m.overview != null ? m.overview : "");

        // 类型（优先显示中文）
        List<String> genreNames = new ArrayList<>();
        if (m.genres != null) {
            for (TmdbResponse.Genre g : m.genres) {
                String cn = GENRE_ID_TO_NAME.get(g.id);
                genreNames.add(cn != null ? cn : g.name);
            }
        }
        d.setGenres(genreNames);

        // 导演（从 crew 中筛选 job=Director，最多 3 个）
        List<String> directors = new ArrayList<>();
        if (credits != null && credits.crew != null) {
            for (TmdbResponse.Crew c : credits.crew) {
                if ("Director".equals(c.job)) {
                    directors.add(c.name);
                    if (directors.size() >= 3) break;
                }
            }
        }
        d.setDirectors(directors);

        // 主演（最多 12 位）
        List<String> cast = new ArrayList<>();
        if (credits != null && credits.cast != null) {
            for (int i = 0; i < Math.min(12, credits.cast.size()); i++) {
                cast.add(credits.cast.get(i).name);
            }
        }
        d.setCast(cast);

        // 制片国家
        List<String> regions = new ArrayList<>();
        if (m.productionCountries != null) {
            for (TmdbResponse.Country c : m.productionCountries) {
                regions.add(c.name);
            }
        }
        d.setRegions(regions);

        // 语言
        List<String> langs = new ArrayList<>();
        if (m.spokenLanguages != null) {
            for (TmdbResponse.SpokenLanguage l : m.spokenLanguages) {
                langs.add(l.name);
            }
        }
        d.setLanguages(langs);

        d.setAka(new ArrayList<>());
        return d;
    }

    /** 拼接 TMDB 图片完整 URL，path 为空时返回空串 */
    private String buildImageUrl(String path, String size) {
        return (path != null && !path.isEmpty()) ? IMG_BASE + size + path : "";
    }

    /** genre_ids 数字列表 → 中文名字符串（用/分隔） */
    private String genreIdsToNames(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int id : ids) {
            String name = GENRE_ID_TO_NAME.get(id);
            if (name != null) {
                if (sb.length() > 0) sb.append("/");
                sb.append(name);
            }
        }
        return sb.toString();
    }

    // ═══════ 网络请求（→ TMDB API）═══════

    public void getHotMovies(int page, Success<List<MovieBrief>> cb, Error err) {
        api.getHotMovies(LANG, page).enqueue(new ListCallback(cb, err));
    }

    public void getTop250(int page, Success<List<MovieBrief>> cb, Error err) {
        api.getTop250(LANG, page).enqueue(new ListCallback(cb, err));
    }

    /** 按中文类型名筛选，未命中则回退到关键词搜索 */
    public void getByTag(String tag, int page, Success<List<MovieBrief>> cb, Error err) {
        Integer genreId = GENRE_MAP.get(tag);
        if (genreId != null) {
            api.getByGenre(LANG, page, genreId, "popularity.desc").enqueue(new ListCallback(cb, err));
        } else {
            api.search(LANG, page, tag).enqueue(new ListCallback(cb, err));
        }
    }

    public void search(String q, int page, Success<List<MovieBrief>> cb, Error err) {
        api.search(LANG, page, q).enqueue(new ListCallback(cb, err));
    }

    /** 获取电影详情（先请求详情，再请求演职员，合并返回） */
    public void getDetail(String id, Success<MovieDetail> cb, Error err) {
        if (id == null || id.isEmpty()) {
            err.on("电影ID无效");
            return;
        }
        int movieId;
        try {
            movieId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            err.on("电影ID格式错误");
            return;
        }

        api.getDetail(movieId, LANG).enqueue(new retrofit2.Callback<TmdbResponse.Movie>() {
            @Override
            public void onResponse(Call<TmdbResponse.Movie> c, Response<TmdbResponse.Movie> r) {
                if (r.isSuccessful() && r.body() != null) {
                    TmdbResponse.Movie movie = r.body();
                    // 再请求演职员信息
                    api.getCredits(movieId, LANG).enqueue(new retrofit2.Callback<TmdbResponse.Credits>() {
                        @Override
                        public void onResponse(Call<TmdbResponse.Credits> c2, Response<TmdbResponse.Credits> r2) {
                            cb.on(toDetail(movie, r2.isSuccessful() ? r2.body() : null));
                        }
                        @Override
                        public void onFailure(Call<TmdbResponse.Credits> c2, Throwable t) {
                            cb.on(toDetail(movie, null)); // 演职员失败也返回详情
                        }
                    });
                } else {
                    err.on("获取详情失败");
                }
            }
            @Override
            public void onFailure(Call<TmdbResponse.Movie> c, Throwable t) {
                err.on("网络请求失败: " + t.getMessage());
            }
        });
    }

    /** 返回可用的中文类型标签（硬编码，不请求网络） */
    public void getTags(Success<List<String>> cb, Error err) {
        cb.on(new ArrayList<>(GENRE_MAP.keySet()));
    }

    // ═══════ Retrofit 回调 ═══════

    /** 电影列表统一回调：TMDB 响应 → MovieBrief 列表 */
    private class ListCallback implements retrofit2.Callback<TmdbResponse.MovieList> {
        private final Success<List<MovieBrief>> cb;
        private final Error err;

        ListCallback(Success<List<MovieBrief>> cb, Error err) {
            this.cb = cb;
            this.err = err;
        }

        @Override
        public void onResponse(Call<TmdbResponse.MovieList> c, Response<TmdbResponse.MovieList> r) {
            if (r.isSuccessful() && r.body() != null && r.body().results != null) {
                List<MovieBrief> list = new ArrayList<>();
                for (TmdbResponse.Movie m : r.body().results) {
                    list.add(toBrief(m));
                }
                cb.on(list);
            } else {
                err.on("请求失败");
            }
        }

        @Override
        public void onFailure(Call<TmdbResponse.MovieList> c, Throwable t) {
            err.on(t.getMessage());
        }
    }

    // ═══════ 本地收藏（Room 数据库）═══════

    /** 添加收藏（从列表项，信息较少） */
    public void addToCollection(MovieBrief m, CollectionType t) {
        io.execute(() -> {
            try {
                dao.insert(MovieEntity.fromBrief(m, t));
            } catch (Exception e) {
                Log.e(TAG, "收藏失败: " + e.getMessage());
            }
        });
    }

    /** 添加收藏（从详情页，信息完整） */
    public void addToCollection(MovieDetail m, CollectionType t) {
        io.execute(() -> {
            try {
                dao.insert(MovieEntity.fromDetail(m, t));
            } catch (Exception e) {
                Log.e(TAG, "收藏失败: " + e.getMessage());
            }
        });
    }

    /** 取消收藏 */
    public void removeFromCollection(String id, CollectionType t) {
        io.execute(() -> {
            try {
                dao.deleteFromCollection(id, t.name());
            } catch (Exception e) {
                Log.e(TAG, "取消收藏失败: " + e.getMessage());
            }
        });
    }

    /** 检查是否已收藏 */
    public void isInCollection(String id, CollectionType t, Success<Boolean> cb, Error err) {
        io.execute(() -> {
            try {
                cb.on(dao.exists(id, t.name()));
            } catch (Exception e) {
                cb.on(false);
            }
        });
    }

    /** 获取收藏列表 */
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

    /** 用详情信息更新已收藏记录（丰富收藏数据） */
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
}

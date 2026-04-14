package com.movieapp.data.remote;

import com.movieapp.model.MovieBrief;
import com.movieapp.model.MovieDetail;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

/** TMDB 电影 API 接口定义 */
public interface TmdbApi {

    /** 电影列表响应 */
    class MovieListResponse {
        public List<MovieBrief> results;
        public int page;
        public String tag;
    }

    /** 标签列表响应 */
    class TagsResponse {
        public List<String> tags;
    }

    /** 热门电影 */
    @GET("api/movies/hot")
    Call<MovieListResponse> getHotMovies(@Query("page") int page);

    /** 高分电影 (Top250 替代) */
    @GET("api/movies/top250")
    Call<MovieListResponse> getTop250(@Query("page") int page);

    /** 按类型筛选 */
    @GET("api/movies/tag/{tag}")
    Call<MovieListResponse> getByTag(@Path("tag") String tag, @Query("page") int page);

    /** 获取可用类型标签 */
    @GET("api/movies/tags")
    Call<TagsResponse> getTags();

    /** 搜索电影 */
    @GET("api/movies/search")
    Call<MovieListResponse> search(@Query("q") String query, @Query("page") int page);

    /** 电影详情 */
    @GET("api/movies/{id}")
    Call<MovieDetail> getDetail(@Path("id") String id);
}

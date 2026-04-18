package com.movieapp.data.remote;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * TMDB 电影 API 接口定义
 * Bearer Token 由 NetworkModule 的拦截器自动注入
 */
public interface TmdbApi {

    /** 热门电影 */
    @GET("movie/popular")
    Call<TmdbResponse.MovieList> getHotMovies(
            @Query("language") String lang,
            @Query("page") int page
    );

    /** 高分电影 */
    @GET("movie/top_rated")
    Call<TmdbResponse.MovieList> getTop250(
            @Query("language") String lang,
            @Query("page") int page
    );

    /** 按类型筛选（with_genres 传 TMDB genre_id） */
    @GET("discover/movie")
    Call<TmdbResponse.MovieList> getByGenre(
            @Query("language") String lang,
            @Query("page") int page,
            @Query("with_genres") int genreId,
            @Query("sort_by") String sortBy
    );

    /** 搜索电影 */
    @GET("search/movie")
    Call<TmdbResponse.MovieList> search(
            @Query("language") String lang,
            @Query("page") int page,
            @Query("query") String query
    );

    /** 电影详情 */
    @GET("movie/{id}")
    Call<TmdbResponse.Movie> getDetail(
            @Path("id") int movieId,
            @Query("language") String lang
    );

    /** 电影演职员 */
    @GET("movie/{id}/credits")
    Call<TmdbResponse.Credits> getCredits(
            @Path("id") int movieId,
            @Query("language") String lang
    );
}

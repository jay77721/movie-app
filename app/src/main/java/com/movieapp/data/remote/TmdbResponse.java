package com.movieapp.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * TMDB API 原始响应模型
 * 直接对应 TMDB 返回的 JSON 字段，由 MovieRepository 转换为 App 层的 MovieBrief/MovieDetail
 */
public class TmdbResponse {

    /** 电影列表响应（热门/搜索/分类筛选共用） */
    public static class MovieList {
        public int page;
        public List<Movie> results;
    }

    /**
     * TMDB 电影原始对象
     * 列表接口返回 genre_ids + poster_path 等
     * 详情接口额外返回 genres、production_countries、spoken_languages、runtime 等
     */
    public static class Movie {
        public int id;
        public String title;
        @SerializedName("original_title") public String originalTitle;
        @SerializedName("vote_average") public double voteAverage;
        @SerializedName("vote_count") public int voteCount;
        @SerializedName("poster_path") public String posterPath;
        @SerializedName("backdrop_path") public String backdropPath;
        @SerializedName("release_date") public String releaseDate;
        public String overview;     // 剧情简介
        public int runtime;         // 时长（分钟）

        @SerializedName("genre_ids") public List<Integer> genreIds; // 列表接口
        public List<Genre> genres;                                   // 详情接口
        @SerializedName("production_countries") public List<Country> productionCountries;
        @SerializedName("spoken_languages") public List<SpokenLanguage> spokenLanguages;

        /** 从 release_date 提取年份 */
        public String getYear() {
            return (releaseDate != null && releaseDate.length() >= 4)
                    ? releaseDate.substring(0, 4) : "";
        }
    }

    /** 类型（id + 英文名） */
    public static class Genre {
        public int id;
        public String name;
    }

    /** 制片国家 */
    public static class Country {
        public String name;
    }

    /** 语言 */
    public static class SpokenLanguage {
        public String name;
    }

    /** 演职员响应 */
    public static class Credits {
        public List<Cast> cast;
        public List<Crew> crew;
    }

    /** 演员 */
    public static class Cast {
        public String name;
    }

    /** 工作人员 */
    public static class Crew {
        public String name;
        public String job;
    }
}

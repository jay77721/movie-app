package com.movieapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 电影详情 — 用于详情页展示
 * 由 MovieRepository 的 toDetail() 方法从 TMDB 数据转换而来
 */
public class MovieDetail {
    private String id;                          // TMDB 电影 ID
    private String title;                       // 中文标题
    private String originalTitle;               // 原始语言标题
    private double rating;                      // 评分（满分 10）
    private int ratingCount;                    // 评分人数
    private String cover;                       // 海报完整 URL
    private String backdrop;                    // 背景大图完整 URL
    private String year;                        // 上映年份
    private List<String> directors;             // 导演列表
    private List<String> cast;                  // 主演列表（最多12位）
    private List<String> genres;                // 类型列表（中文）
    private List<String> regions;               // 制片国家
    private List<String> languages;             // 语言
    private String duration;                    // 时长，如 "120分钟"
    private String releaseDate;                 // 上映日期
    private String summary;                     // 剧情简介
    private List<String> aka;                   // 又名

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String v) { id = v; }

    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }

    public String getOriginalTitle() { return originalTitle; }
    public void setOriginalTitle(String v) { originalTitle = v; }

    public double getRating() { return rating; }
    public void setRating(double v) { rating = v; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int v) { ratingCount = v; }

    public String getCover() { return cover; }
    public void setCover(String v) { cover = v; }

    public String getBackdrop() { return backdrop; }
    public void setBackdrop(String v) { backdrop = v; }

    public String getYear() { return year; }
    public void setYear(String v) { year = v; }

    public List<String> getDirectors() { return directors; }
    public void setDirectors(List<String> v) { directors = v; }

    public List<String> getCast() { return cast; }
    public void setCast(List<String> v) { cast = v; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> v) { genres = v; }

    public List<String> getRegions() { return regions; }
    public void setRegions(List<String> v) { regions = v; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> v) { languages = v; }

    public String getDuration() { return duration; }
    public void setDuration(String v) { duration = v; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String v) { releaseDate = v; }

    public String getSummary() { return summary; }
    public void setSummary(String v) { summary = v; }

    public List<String> getAka() { return aka; }
    public void setAka(List<String> v) { aka = v; }

    // 拼接方法 — 供 UI 直接显示
    public String joinDirectors() { return directors != null ? String.join("/", directors) : ""; }
    public String joinCast() { return cast != null ? String.join("/", cast) : ""; }
    public String joinGenres() { return genres != null ? String.join("/", genres) : ""; }
    public String joinRegions() { return regions != null ? String.join("/", regions) : ""; }
}

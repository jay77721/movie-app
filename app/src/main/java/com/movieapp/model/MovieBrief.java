package com.movieapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * 电影列表项 — 用于首页列表、搜索结果、收藏列表
 * 字段由 MovieRepository 转换后赋值，此处不直接对接 JSON
 */
public class MovieBrief {
    private String id;          // TMDB 电影 ID
    private String title;       // 中文标题
    private double rating;      // 评分（满分 10）
    private String cover;       // 封面海报完整 URL
    private String year;        // 上映年份，如 "2024"
    private String releaseDate; // 上映日期，如 "2024-01-15"
    private String genres;      // 类型，用/分隔，如 "动作/科幻"

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String v) { id = v; }

    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }

    public double getRating() { return rating; }
    public void setRating(double v) { rating = v; }

    public String getCover() { return cover; }
    public void setCover(String v) { cover = v; }

    public String getYear() { return year; }
    public void setYear(String v) { year = v; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String v) { releaseDate = v; }

    public String getGenres() { return genres != null ? genres : ""; }
    public void setGenres(String v) { genres = v; }
}

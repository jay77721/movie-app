package com.movieapp.model;

import com.google.gson.annotations.SerializedName;

/** 电影列表项 — 用于首页列表和搜索结果 */
public class MovieBrief {
    private String id;
    private String title;
    private double rating;
    @SerializedName("cover") private String cover;
    @SerializedName("year") private String year;
    @SerializedName("release_date") private String releaseDate;
    @SerializedName("genres") private Object genres;

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

    public String getGenres() {
        if (genres == null) return "";
        if (genres instanceof String) return (String) genres;
        if (genres instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) genres;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append("/");
                sb.append(list.get(i) != null ? list.get(i).toString() : "");
            }
            return sb.toString();
        }
        return genres.toString();
    }

    public void setGenres(Object v) { genres = v; }
}

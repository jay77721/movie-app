package com.movieapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** 电影详情 — 用于详情页展示 */
public class MovieDetail {
    private String id;
    private String title;
    @SerializedName("original_title") private String originalTitle;
    private double rating;
    @SerializedName("rating_count") private int ratingCount;
    @SerializedName("cover") private String cover;
    @SerializedName("backdrop") private String backdrop;
    @SerializedName("year") private String year;
    @SerializedName("directors") private List<String> directors;
    @SerializedName("cast") private List<String> cast;
    @SerializedName("genres") private List<String> genres;
    @SerializedName("regions") private List<String> regions;
    @SerializedName("languages") private List<String> languages;
    @SerializedName("duration") private String duration;
    @SerializedName("release_date") private String releaseDate;
    @SerializedName("summary") private String summary;
    @SerializedName("aka") private List<String> aka;

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

    /** 拼接导演列表 */
    public String joinDirectors() { return directors != null ? String.join("/", directors) : ""; }
    /** 拼接演员列表 */
    public String joinCast() { return cast != null ? String.join("/", cast) : ""; }
    /** 拼接类型列表 */
    public String joinGenres() { return genres != null ? String.join("/", genres) : ""; }
    /** 拼接地区列表 */
    public String joinRegions() { return regions != null ? String.join("/", regions) : ""; }
}

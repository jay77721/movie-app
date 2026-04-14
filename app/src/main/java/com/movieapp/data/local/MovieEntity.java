package com.movieapp.data.local;

import androidx.room.*;
import com.movieapp.model.CollectionType;
import com.movieapp.model.MovieBrief;
import com.movieapp.model.MovieDetail;

/**
 * 收藏电影本地存储实体
 * 复合主键 (id, collectionType)：同一部电影可同时存在于"想看"和"已看"列表
 */
@Entity(tableName = "movies", primaryKeys = {"id", "collectionType"})
public class MovieEntity {
    @androidx.annotation.NonNull private String id;
    @androidx.annotation.NonNull private String collectionType;
    private String title;
    private double rating;
    private String cover;
    private String year;
    private String directors;
    private String genres;
    private String summary;
    private String cast;
    private String duration;
    private String releaseDate;
    private String regions;
    private String languages;
    private String aka;

    /** 从列表项创建 */
    public static MovieEntity fromBrief(MovieBrief b, CollectionType t) {
        MovieEntity e = new MovieEntity();
        e.id = b.getId(); e.title = b.getTitle(); e.rating = b.getRating(); e.cover = b.getCover();
        e.year = s(b.getYear()); e.directors = ""; e.genres = s(b.getGenres());
        e.summary = ""; e.cast = ""; e.duration = ""; e.releaseDate = ""; e.regions = "";
        e.collectionType = t.name();
        return e;
    }

    /** 从详情页创建（信息更完整） */
    public static MovieEntity fromDetail(MovieDetail d, CollectionType t) {
        MovieEntity e = new MovieEntity();
        e.id = d.getId(); e.title = d.getTitle(); e.rating = d.getRating(); e.cover = d.getCover();
        e.year = s(d.getYear()); e.directors = d.joinDirectors(); e.genres = d.joinGenres();
        e.summary = s(d.getSummary()); e.cast = d.joinCast(); e.duration = s(d.getDuration());
        e.releaseDate = s(d.getReleaseDate()); e.regions = d.joinRegions();
        e.languages = d.getLanguages() != null ? String.join("/", d.getLanguages()) : "";
        e.aka = d.getAka() != null ? String.join("、", d.getAka()) : "";
        e.collectionType = t.name();
        return e;
    }

    /** 转换为列表项 */
    public MovieBrief toBrief() {
        MovieBrief b = new MovieBrief();
        b.setId(id); b.setTitle(title); b.setRating(rating); b.setCover(cover);
        b.setYear(year); b.setGenres(genres);
        return b;
    }

    private static String s(String v) { return v != null ? v : ""; }

    // Getters & Setters
    @androidx.annotation.NonNull public String getId() { return id; }
    public void setId(@androidx.annotation.NonNull String v) { id = v; }
    @androidx.annotation.NonNull public String getCollectionType() { return collectionType; }
    public void setCollectionType(@androidx.annotation.NonNull String v) { collectionType = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public double getRating() { return rating; }
    public void setRating(double v) { rating = v; }
    public String getCover() { return cover; }
    public void setCover(String v) { cover = v; }
    public String getYear() { return year; }
    public void setYear(String v) { year = v; }
    public String getDirectors() { return directors; }
    public void setDirectors(String v) { directors = v; }
    public String getGenres() { return genres; }
    public void setGenres(String v) { genres = v; }
    public String getSummary() { return summary; }
    public void setSummary(String v) { summary = v; }
    public String getCast() { return cast; }
    public void setCast(String v) { cast = v; }
    public String getDuration() { return duration; }
    public void setDuration(String v) { duration = v; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String v) { releaseDate = v; }
    public String getRegions() { return regions; }
    public void setRegions(String v) { regions = v; }
    public String getLanguages() { return languages; }
    public void setLanguages(String v) { languages = v; }
    public String getAka() { return aka; }
    public void setAka(String v) { aka = v; }
}

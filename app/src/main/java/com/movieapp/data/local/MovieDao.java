package com.movieapp.data.local;

import androidx.room.*;

/** 收藏电影数据访问对象 */
@Dao
public interface MovieDao {

    /** 按收藏类型查询列表（最新在前） */
    @Query("SELECT * FROM movies WHERE collectionType = :type ORDER BY rowid DESC")
    java.util.List<MovieEntity> getByCollection(String type);

    /** 插入或更新（复合主键 id+collectionType 唯一） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MovieEntity movie);

    /** 从指定收藏类型中移除 */
    @Query("DELETE FROM movies WHERE id = :id AND collectionType = :type")
    void deleteFromCollection(String id, String type);

    /** 检查是否已在指定收藏中 */
    @Query("SELECT EXISTS(SELECT 1 FROM movies WHERE id = :id AND collectionType = :type)")
    boolean exists(String id, String type);
}

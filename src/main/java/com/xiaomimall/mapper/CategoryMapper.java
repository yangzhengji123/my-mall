package com.xiaomimall.mapper;

import com.xiaomimall.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 商品分类Mapper接口
 * 提供分类的CRUD操作
 */
@Mapper
public interface CategoryMapper {
    
    // 插入新分类
    @Insert("INSERT INTO categories (name, parent_id, level, sort, created_at, updated_at) " +
            "VALUES (#{name}, #{parentId}, #{level}, #{sort}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);
    
    // 更新分类信息
    @Update("UPDATE categories SET name = #{name}, sort = #{sort}, parent_id = #{parentId}, level = #{level}, updated_at = NOW() WHERE id = #{id}")
    int update(Category category);

    // 逻辑删除分类
    @Update("UPDATE categories SET is_deleted = 1 WHERE id = #{id}")
    int deleteById(Long id);
    
    // 根据ID查询分类
    @Select("SELECT * FROM categories WHERE id = #{id} AND is_deleted = 0")
    Category findById(Long id);
    
    // 查询所有顶级分类
    @Select("SELECT * FROM categories WHERE parent_id = 0 AND is_deleted = 0 ORDER BY sort ASC")
    List<Category> findRootCategories();
    
    // 查询子分类
    @Select("SELECT * FROM categories WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort ASC")
    List<Category> findChildrenByParentId(Long parentId);

    // 查询所有分类（包括已删除和未删除）
    @Select("SELECT * FROM categories")
    List<Category> findAll();

}
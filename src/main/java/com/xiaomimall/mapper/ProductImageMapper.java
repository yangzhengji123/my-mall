package com.xiaomimall.mapper;

import com.xiaomimall.entity.ProductImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品图片Mapper接口
 * 提供商品图片的管理
 */
@Mapper
public interface ProductImageMapper {
    
    // 插入商品图片
    @Insert("INSERT INTO product_images (product_id, url, sort, is_main) " +
            "VALUES (#{productId}, #{url}, #{sort}, #{isMain})")
    int insert(ProductImage image);
    
    // 删除商品的所有图片
    @Delete("DELETE FROM product_images WHERE product_id = #{productId}")
    int deleteByProductId(Long productId);
    
    // 根据商品ID查询图片
    @Select("SELECT * FROM product_images WHERE product_id = #{productId} ORDER BY sort ASC")
    List<ProductImage> findByProductId(Long productId);

    // 根据商品ID查询主图片
    @Select("SELECT * FROM product_images WHERE product_id = #{productId} AND is_main = true ORDER BY sort ASC LIMIT 1")
    ProductImage findMainImageByProductId(Long productId);
}
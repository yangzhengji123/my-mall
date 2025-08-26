package com.xiaomimall.mapper;

import com.xiaomimall.dto.ProductQueryDTO;
import com.xiaomimall.entity.Product;
import org.apache.ibatis.annotations.*;
import org.mybatis.spring.annotation.MapperScan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商品Mapper接口
 * 提供商品的CRUD操作
 */
@Mapper
@MapperScan("com.xiaomimall.mapper")
public interface ProductMapper {
    
    // 插入新商品
    int insert(Product product);
    
    // 条件更新商品信息
    int update(Product product);

    // 逻辑删除商品
    int deleteById(Long id);
    
    // 根据ID查询商品
    Product findById(Long id);

    // 根据ID列表查询商品列表
    List<Product> findByIds(@Param("productIds") List<Long> productIds);

    @Select("SELECT COUNT(*) FROM products WHERE category_id = #{categoryId} AND is_deleted = 0")
    int countByCategoryId(Long categoryId);


    // 条件查询商品列表
    List<Product> findByCondition(ProductQueryDTO queryDTO);
    
    // 更新商品库存
    int reduceStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Update("UPDATE products SET stock = stock + #{quantity} WHERE id = #{productId}")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Select("SELECT p.*, " +
            "pi.id as image_id, pi.url as image_url, pi.is_main as image_is_main " +
            "FROM products p " +
            "LEFT JOIN product_images pi ON p.id = pi.product_id AND pi.is_deleted = 0 " +
            "WHERE p.id = #{id} AND p.is_deleted = 0 " +
            "ORDER BY pi.sort ASC")
    @MapKey("id") // 指定使用id字段作为Map的key
    List<Map<String, Object>> findProductWithImagesById(Long id);

    @Select({
            "<script>",
            "SELECT p.*, ",
            "pi.id as image_id, pi.url as image_url, pi.is_main as image_is_main ",
            "FROM products p ",
            "LEFT JOIN product_images pi ON p.id = pi.product_id AND pi.is_deleted = 0 ",
            "WHERE p.is_deleted = 0 ",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (p.name LIKE CONCAT('%', #{keyword}, '%') OR p.description LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "<if test='categoryId != null'>",
            "AND p.category_id = #{categoryId}",
            "</if>",
            "<if test='minPrice != null'>",
            "AND p.price >= #{minPrice}",
            "</if>",
            "<if test='maxPrice != null'>",
            "AND p.price <= #{maxPrice}",
            "</if>",
            "<if test='status != null'>",
            "AND p.status = #{status}",
            "</if>",
            "ORDER BY ",
            "<choose>",
            "<when test='sortBy == \"price\" and sortOrder == \"desc\"'>p.price DESC</when>",
            "<when test='sortBy == \"price\" and sortOrder == \"asc\"'>p.price ASC</when>",
            "<when test='sortBy == \"sales\" and sortOrder == \"desc\"'>p.sales DESC</when>",
            "<when test='sortBy == \"sales\" and sortOrder == \"asc\"'>p.sales ASC</when>",
            "<otherwise>p.id DESC</otherwise>",
            "</choose>",
            "LIMIT #{offset}, #{limit}",
            "</script>"
    })
    List<Map<String, Object>> findByConditionWithImages(@Param("queryDTO") ProductQueryDTO queryDTO,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);

    /**
     * 根据条件统计商品数量
     */
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM products p WHERE p.is_deleted = 0",
            "<if test='keyword != null and keyword != \"\"'> AND (p.name LIKE CONCAT('%', #{keyword}, '%') OR p.description LIKE CONCAT('%', #{keyword}, '%')) </if>",
            "<if test='categoryId != null'> AND p.category_id = #{categoryId} </if>",
            "<if test='minPrice != null'> AND p.price >= #{minPrice} </if>",
            "<if test='maxPrice != null'> AND p.price <= #{maxPrice} </if>",
            "<if test='status != null'> AND p.status = #{status} </if>",
            "</script>"
    })
    Long countByCondition(ProductQueryDTO queryDTO);

}

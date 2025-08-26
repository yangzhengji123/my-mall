package com.xiaomimall.service;

import com.xiaomimall.dto.*;
import com.xiaomimall.exception.ConflictException;

import java.util.List;

/**
 * 商品服务接口
 * 定义商品相关的业务操作
 */
public interface ProductService {
    // 商品管理
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    void deleteProduct(Long id);
    ProductDTO getProductById(Long id);
    PageResponse<ProductDTO> searchProducts(ProductQueryDTO queryDTO);
    
    // 分类管理
    List<CategoryDTO> getAllCategories();
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
    void deleteCategory(Long id) throws ConflictException;
    
    // 轮播图管理
    BannerDTO createBanner(BannerDTO bannerDTO);
    BannerDTO updateBanner(Long id, BannerDTO bannerDTO);
    void deleteBanner(Long id);
    List<BannerDTO> getActiveBanners();
}

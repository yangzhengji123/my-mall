package com.xiaomimall.service;

import com.xiaomimall.dto.*;
import com.xiaomimall.entity.Banner;
import com.xiaomimall.entity.Category;
import com.xiaomimall.entity.Product;
import com.xiaomimall.entity.ProductImage;
import com.xiaomimall.exception.ConflictException;
import com.xiaomimall.exception.NotFoundException;
import com.xiaomimall.mapper.BannerMapper;
import com.xiaomimall.mapper.CategoryMapper;
import com.xiaomimall.mapper.ProductImageMapper;
import com.xiaomimall.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 * 实现商品相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ProductImageMapper productImageMapper;
    private final BannerMapper bannerMapper;
    private static final int MAX_CATEGORY_LEVEL = 3;

    // 创建商品
    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        // 验证分类是否存在
        Category category = categoryMapper.findById(productDTO.getCategoryId());
        if (category == null || category.getIsDeleted()) {
            throw new NotFoundException("分类不存在");
        }

        // 创建商品基本信息
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setCategoryId(productDTO.getCategoryId());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setDescription(productDTO.getDescription());
        product.setDetail(productDTO.getDetail());
        product.setStatus(productDTO.getStatus());

        productMapper.insert(product);

        // 保存商品图片
        saveProductImages(product.getId(), productDTO.getImages());

        // 返回DTO对象
        return convertToProductDTO(product);
    }


    // 更新商品
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "products", key = "#id"),
            @CacheEvict(cacheNames = "products", key = "'search_' + #productDTO.getPageNum() + '_' + #productDTO.getPageSize()")
    })
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        // 检查商品是否存在
        Product existingProduct = productMapper.findById(id);
        if (existingProduct == null || existingProduct.getIsDeleted()) {
            throw new NotFoundException("商品不存在");
        }

        // 更新商品基本信息
        existingProduct.setName(productDTO.getName());
        existingProduct.setCategoryId(productDTO.getCategoryId());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setDetail(productDTO.getDetail());
        existingProduct.setStatus(productDTO.getStatus());

        productMapper.update(existingProduct);

        // 删除原有图片并保存新图片
        productImageMapper.deleteByProductId(id);
        saveProductImages(id, productDTO.getImages());

        return convertToProductDTO(existingProduct);
    }

    // 删除商品（逻辑删除）
    @Override
    @CacheEvict(cacheNames = "products", key = "#id")
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productMapper.findById(id);
        if (product == null || product.getIsDeleted()) {
            throw new NotFoundException("商品不存在");
        }
        productMapper.deleteById(id);
    }

    // 根据ID获取商品详情
    @Override
    @Cacheable(cacheNames = "products", key = "#id", unless = "#result == null")
    public ProductDTO getProductById(Long id) {
        try {
            List<Map<String, Object>> results = productMapper.findProductWithImagesById(id);
            if (results == null || results.isEmpty()) {
                return null;
            }

            Map<String, Object> firstRow = results.get(0);
            Product product = new Product();
            product.setId(((Long) firstRow.get("id")));
            product.setName((String) firstRow.get("name"));
            product.setCategoryId(((Long) firstRow.get("category_id")));
            product.setPrice((BigDecimal) firstRow.get("price"));
            product.setStock((Integer) firstRow.get("stock"));
            product.setSales((Integer) firstRow.get("sales"));
            product.setDescription((String) firstRow.get("description"));
            product.setDetail((String) firstRow.get("detail"));
            product.setStatus((Integer) firstRow.get("status"));
            product.setIsDeleted((Boolean) firstRow.get("is_deleted"));

            if (product.getIsDeleted()) {
                return null;
            }
            ProductDTO dto = convertToProductDTO(product);
            List<ProductDTO.ProductImageDTO> images = new ArrayList<>();
            for (Map<String, Object> row : results) {
                if (row.get("image_id") != null) {
                    ProductDTO.ProductImageDTO imageDTO = new ProductDTO.ProductImageDTO();
                    imageDTO.setUrl((String) row.get("image_url"));
                    imageDTO.setIsMain((Boolean) row.get("image_is_main"));
                    images.add(imageDTO);
                }
            }
            dto.setImages(images);
            return dto;
        }catch (NotFoundException e) {
            return null;
        }

    }


    // 商品搜索
    @Override
    public PageResponse<ProductDTO> searchProducts(ProductQueryDTO queryDTO) {
        // 1. 查询总数
        Long total = productMapper.countByCondition(queryDTO);

        // 2. 计算总页数
        int pageSize = queryDTO.getPageSize();
        int pageNum = queryDTO.getPageNum();
        int pages = (int) Math.ceil((double) total / pageSize);

        // 3. 查询当前页数据
        int offset = (pageNum - 1) * pageSize;
        List<Map<String, Object>> results = productMapper.findByConditionWithImages(queryDTO, offset, pageSize);

        // 4. 分组处理商品及图片
        Map<Long, ProductDTO> productMap = new LinkedHashMap<>();
        for (Map<String, Object> row : results) {
            Long productId = (Long) row.get("id");

            if (!productMap.containsKey(productId)) {
                Product product = new Product();
                product.setId(productId);
                product.setName((String) row.get("name"));
                product.setCategoryId((Long) row.get("category_id"));
                product.setPrice((BigDecimal) row.get("price"));
                product.setStock((Integer) row.get("stock"));
                product.setSales((Integer) row.get("sales"));
                product.setDescription((String) row.get("description"));
                product.setDetail((String) row.get("detail"));
                product.setStatus((Integer) row.get("status"));
                product.setIsDeleted((Boolean) row.get("is_deleted"));

                ProductDTO dto = convertToProductDTO(product);
                dto.setImages(new ArrayList<>()); // 初始化图片列表
                productMap.put(productId, dto);
            }

            if (row.get("image_id") != null) {
                ProductDTO.ProductImageDTO imageDTO = new ProductDTO.ProductImageDTO();
                imageDTO.setUrl((String) row.get("image_url"));
                imageDTO.setIsMain((Boolean) row.get("image_is_main"));
                productMap.get(productId).getImages().add(imageDTO);
            }
        }

        // 5. 构建分页响应
        PageResponse<ProductDTO> response = new PageResponse<>();
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setTotal(total);
        response.setPages(pages);
        response.setList(new ArrayList<>(productMap.values()));

        return response;
    }

    // 获取所有分类（树形结构）
    @Override
    @Cacheable(cacheNames = "categories", key = "'all'")
    public List<CategoryDTO> getAllCategories() {
        // 1. 查询并过滤未删除的分类
        List<Category> allCategories = categoryMapper.findAll().stream()
                .filter(category -> !category.getIsDeleted())
                .collect(Collectors.toList());

        // 2. 一次性构建树（无 N+1 查询）
        return buildCategoryTree(allCategories);
    }

    // 创建大分类，例如：大类电子产品1，中类手机或者电脑2，小类智能手机3
    @Override
    @Transactional//开启事务
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Long parentId = categoryDTO.getParentId() != null ? categoryDTO.getParentId() : 0L;
        if (parentId != 0) {
            Category parent = categoryMapper.findById(parentId);
            if (parent == null || parent.getIsDeleted()) {
                throw new NotFoundException("父分类不存在");
            }
            // 检查层级深度
            if (parent.getLevel() >= MAX_CATEGORY_LEVEL) {
                throw new IllegalArgumentException("分类层级不能超过" + MAX_CATEGORY_LEVEL + "级");
            }
        }


        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setParentId(categoryDTO.getParentId() != null ? categoryDTO.getParentId() : 0L);
        if (categoryDTO.getParentId() != null && categoryDTO.getParentId() != 0) {
            // 如果有父分类，查询父分类的层级并加1
            Category parent = categoryMapper.findById(categoryDTO.getParentId());
            category.setLevel(parent.getLevel() + 1);
        } else {
            // 顶级分类设置为层级1
            category.setLevel(1);
        }//有父级，二级层级，无父亲，一级层级
        category.setSort(categoryDTO.getSort() != null ? categoryDTO.getSort() : 0);

        categoryMapper.insert(category);
        return convertToCategoryDTO(category);
    }

    // 更新分类  类名，排序，父分类，时间戳
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "categories", key = "'all'"),
            @CacheEvict(cacheNames = "categories", key = "#id")
    })
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryMapper.findById(id);
        if (category == null || category.getIsDeleted()) {
            throw new NotFoundException("分类不存在");
        }

        // 更新基本信息
        category.setName(categoryDTO.getName());
        category.setSort(categoryDTO.getSort());

        // 如果提供了parentId且不等于当前parentId，则更新父分类和层级
        if (categoryDTO.getParentId() != null && !categoryDTO.getParentId().equals(category.getParentId())) {
            // 验证新的父分类是否存在
            if (categoryDTO.getParentId() != 0) {
                Category newParent = categoryMapper.findById(categoryDTO.getParentId());
                if (newParent == null || newParent.getIsDeleted()) {
                    throw new NotFoundException("父类不存在,设置失败");
                }
                // 检查不能将分类设置为其子分类的子分类（避免循环引用）
                if (isCircularReference(category.getId(), categoryDTO.getParentId())) {
                    throw new ConflictException("操作将导致循环引用");
                }
                category.setParentId(categoryDTO.getParentId());
                category.setLevel(newParent.getLevel() + 1);
            } else {
                // 设置为顶级分类
                category.setParentId(0L);
                category.setLevel(1);
            }
        }

        // 更新时间戳
        category.setUpdatedAt(LocalDateTime.now());

        categoryMapper.update(category);
        return convertToCategoryDTO(category);
    }


    // 删除分类（逻辑删除）
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "categories", key = "'all'"),
            @CacheEvict(cacheNames = "categories", key = "#id")
    })
    public void deleteCategory(Long id) {
        Category category = categoryMapper.findById(id);
        if (category == null || category.getIsDeleted()) {
            throw new NotFoundException("分类不存在");
        }

        // 检查是否有子分类
        List<Category> children = categoryMapper.findChildrenByParentId(id);
        if (!children.isEmpty()) {
            throw new ConflictException("分类存在关联商品，无法删除");
        }

        Integer productCount = productMapper.countByCategoryId(id);
        if (productCount != null && productCount > 0) {
            throw new ConflictException("分类下存在商品，无法删除");
        }

        categoryMapper.deleteById(id);
    }

    // 创建轮播图
    @Override
    @Transactional
    public BannerDTO createBanner(BannerDTO bannerDTO) {
        // 业务逻辑时间验证
        validateBannerTime(bannerDTO);

        Banner banner = new Banner();
        banner.setTitle(bannerDTO.getTitle());
        banner.setImageUrl(bannerDTO.getImageUrl());
        banner.setSort(bannerDTO.getSort());
        banner.setIsActive(bannerDTO.getIsActive());
        banner.setStartTime(bannerDTO.getStartTime());
        banner.setEndTime(bannerDTO.getEndTime());

        bannerMapper.insert(banner);
        return convertToBannerDTO(banner);
    }

    // 更新轮播图
    @Override
    @Transactional
    public BannerDTO updateBanner(Long id, BannerDTO bannerDTO) {
        Banner banner = bannerMapper.findById(id);
        if (banner == null) {
            throw new NotFoundException("轮播图不存在");
        }

        validateBannerTimeForUpdate(bannerDTO, banner);

        banner.setTitle(bannerDTO.getTitle());
        banner.setImageUrl(bannerDTO.getImageUrl());
        banner.setSort(bannerDTO.getSort());
        banner.setIsActive(bannerDTO.getIsActive());
        banner.setStartTime(bannerDTO.getStartTime());
        banner.setEndTime(bannerDTO.getEndTime());

        bannerMapper.update(banner);
        return convertToBannerDTO(banner);
    }

    // 删除轮播图
    @Override
    @Transactional
    @CacheEvict(cacheNames = "banners", key = "#id")
    public void deleteBanner(Long id) {
        Banner banner = bannerMapper.findById(id);
        if (banner == null) {
            throw new NotFoundException("轮播图不存在");
        }
        bannerMapper.deleteById(id);
    }

    // 获取所有启用的轮播图
    @Override
    @Cacheable(cacheNames = "banners", key = "'active'")
    public List<BannerDTO> getActiveBanners() {
        List<Banner> banners = bannerMapper.findActiveBanners();
        return banners.stream()
                .map(this::convertToBannerDTO)
                .collect(Collectors.toList());
    }

    // 保存商品图片
    private void saveProductImages(Long productId, List<ProductDTO.ProductImageDTO> images) {
        if (images == null || images.isEmpty()) return;

        for (int i = 0; i < images.size(); i++) {
            ProductDTO.ProductImageDTO imgDto = images.get(i);
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setUrl(imgDto.getUrl());
            image.setIsMain(imgDto.getIsMain());
            image.setSort(i); // 使用顺序作为排序值

            productImageMapper.insert(image);
        }
    }

    // 转换Product到DTO
    private ProductDTO convertToProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategoryId(product.getCategoryId());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setDescription(product.getDescription());
        dto.setDetail(product.getDetail());
        dto.setStatus(product.getStatus());
        return dto;
    }

    // 转换Category到DTO（递归处理子分类）
    private CategoryDTO convertToCategoryDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setParentId(category.getParentId());
        dto.setLevel(category.getLevel());
        dto.setSort(category.getSort()); // 添加这一行

        // 递归查询子分类
        List<Category> children = categoryMapper.findChildrenByParentId(category.getId());
        dto.setChildren(children.stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList()));

        return dto;
    }


    // 转换Banner到DTO
    private BannerDTO convertToBannerDTO(Banner banner) {
        BannerDTO dto = new BannerDTO();
        dto.setId(banner.getId());
        dto.setTitle(banner.getTitle());
        dto.setImageUrl(banner.getImageUrl());
        dto.setSort(banner.getSort());
        dto.setIsActive(banner.getIsActive());
        dto.setStartTime(banner.getStartTime());
        dto.setEndTime(banner.getEndTime());
        return dto;
    }

    // 辅助方法：检查一个分类是否是另一个分类的子类
    // 迭代方式检查循环引用
    private boolean isCircularReference(Long categoryId, Long targetParentId) {
        if (categoryId.equals(targetParentId)) {
            return true; // 自己不能是自己的父类
        }

        Set<Long> visited = new HashSet<>();
        Long currentId = targetParentId;

        // 向上遍历父节点，检查是否会出现循环
        while (currentId != null && currentId != 0L) {
            if (visited.contains(currentId)) {
                // 发现循环
                return true;
            }
            visited.add(currentId);

            Category currentCategory = categoryMapper.findById(currentId);
            if (currentCategory == null || currentCategory.getIsDeleted()) {
                break;
            }

            // 如果找到了 categoryId，说明会出现循环引用
            if (currentId.equals(categoryId)) {
                return true;
            }
            currentId = currentCategory.getParentId();
        }
        return false;
    }

    // 创建时的时间验证
    private void validateBannerTime(BannerDTO bannerDTO) {
        LocalDateTime now = LocalDateTime.now();

        // 开始时间不能早于当前时间超过1小时（允许少量误差）
        if (bannerDTO.getStartTime() != null && bannerDTO.getStartTime().isBefore(now.minusHours(1))) {
            throw new IllegalArgumentException("开始时间不能是过去的时间");
        }

        // 结束时间必须晚于开始时间
        if (bannerDTO.getStartTime() != null && bannerDTO.getEndTime() != null
                && bannerDTO.getEndTime().isBefore(bannerDTO.getStartTime())) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }
    }

    // 更新时的时间验证（更宽松）
    private void validateBannerTimeForUpdate(BannerDTO newBanner, Banner existingBanner) {
        LocalDateTime now = LocalDateTime.now();

        // 如果活动已经开始或结束，限制某些时间字段的修改
        if (existingBanner.getStartTime() != null && existingBanner.getStartTime().isBefore(now)) {
            // 活动已经开始，不允许修改开始时间
            if (newBanner.getStartTime() != null &&
                    !newBanner.getStartTime().isEqual(existingBanner.getStartTime())) {
                throw new IllegalArgumentException("活动已经开始，不允许修改开始时间");
            }
        }

        // 结束时间必须晚于开始时间
        if (newBanner.getStartTime() != null && newBanner.getEndTime() != null
                && newBanner.getEndTime().isBefore(newBanner.getStartTime())) {
            throw new ConflictException("活动已结束，不允许修改时间");
        }

        // 如果活动已结束，不允许修改时间
        if (existingBanner.getEndTime() != null && existingBanner.getEndTime().isBefore(now)) {
            if ((newBanner.getStartTime() != null &&
                    !newBanner.getStartTime().isEqual(existingBanner.getStartTime())) ||
                    (newBanner.getEndTime() != null &&
                            !newBanner.getEndTime().isEqual(existingBanner.getEndTime()))) {
                throw new IllegalArgumentException("活动已结束，不允许修改时间");
            }
        }
    }

    private List<CategoryDTO> buildCategoryTree(List<Category> categories) {
        // 创建一个映射，用于快速查找分类
        Map<Long, CategoryDTO> categoryMap = new HashMap<>();
        // 先将所有分类转换为 DTO 并放入映射中
        for (Category category : categories) {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(category.getId());
            dto.setName(category.getName());
            dto.setParentId(category.getParentId());
            dto.setLevel(category.getLevel());
            dto.setSort(category.getSort());
            dto.setChildren(new ArrayList<>());
            categoryMap.put(category.getId(), dto);
        }

        // 构建树形结构
        List<CategoryDTO> rootCategories = new ArrayList<>();
        for (Category category : categories) {
            CategoryDTO dto = categoryMap.get(category.getId());
            if (category.getParentId() == 0) {
                // 顶级分类
                rootCategories.add(dto);
            } else {
                // 子分类，添加到父分类的 children 列表中
                CategoryDTO parentDto = categoryMap.get(category.getParentId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            }
        }
        sortCategories(rootCategories);
        return rootCategories;
    }

    private void sortCategories(List<CategoryDTO> categories) {
        // 按照 sort 字段升序排序
        categories.sort(Comparator.comparing(CategoryDTO::getSort));

        // 递归对子分类进行排序
        for (CategoryDTO category : categories) {
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                sortCategories(category.getChildren());
            }
        }
    }
}
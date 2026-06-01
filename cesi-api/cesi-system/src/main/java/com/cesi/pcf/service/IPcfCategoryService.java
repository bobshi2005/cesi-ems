package com.cesi.pcf.service;

import com.cesi.pcf.domain.EmspcfCategory;
import com.cesi.pcf.domain.EmspcfProduct;
import com.cesi.pcf.domain.vo.CategoryTreeVO;
import java.util.List;

public interface IPcfCategoryService {

    List<CategoryTreeVO> getCategoryTree();

    void saveCategory(EmspcfCategory category);

    void removeCategory(Long id);

    List<EmspcfProduct> listProductsByCategory(Long categoryId);

    void saveProduct(EmspcfProduct product);

    void removeProduct(Long id);
}

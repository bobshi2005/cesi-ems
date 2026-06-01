package com.cesi.pcf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cesi.common.utils.SecurityUtils;
import com.cesi.pcf.domain.EmspcfCategory;
import com.cesi.pcf.domain.EmspcfProduct;
import com.cesi.pcf.domain.vo.CategoryTreeVO;
import com.cesi.pcf.mapper.EmspcfCategoryMapper;
import com.cesi.pcf.mapper.EmspcfProductMapper;
import com.cesi.pcf.service.IPcfCategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PcfCategoryServiceImpl implements IPcfCategoryService {

    private final EmspcfCategoryMapper categoryMapper;
    private final EmspcfProductMapper  productMapper;

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        List<EmspcfCategory> all = categoryMapper.selectAll();
        return buildTree(all, 0L);
    }

    private List<CategoryTreeVO> buildTree(List<EmspcfCategory> all, Long parentId) {
        return all.stream()
                .filter(c -> Objects.equals(c.getParentId(), parentId))
                .sorted(Comparator.comparing(EmspcfCategory::getOrderNum))
                .map(c -> {
                    CategoryTreeVO vo = new CategoryTreeVO();
                    vo.setId(c.getId());
                    vo.setParentId(c.getParentId());
                    vo.setCategoryName(c.getCategoryName());
                    vo.setCategoryCode(c.getCategoryCode());
                    vo.setOrderNum(c.getOrderNum());
                    vo.setStatus(c.getStatus());
                    vo.setProductCount(categoryMapper.countProductsByCategoryId(c.getId()));
                    List<CategoryTreeVO> children = buildTree(all, c.getId());
                    vo.setChildren(children.isEmpty() ? null : children);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCategory(EmspcfCategory category) {
        String user = SecurityUtils.getUsername();
        Date now = new Date();
        if (category.getId() == null) {
            category.setCreateBy(user);
            category.setCreateTime(now);
            categoryMapper.insert(category);
        } else {
            category.setUpdateBy(user);
            category.setUpdateTime(now);
            categoryMapper.updateById(category);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    @Override
    public List<EmspcfProduct> listProductsByCategory(Long categoryId) {
        return productMapper.selectByCategoryId(categoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(EmspcfProduct product) {
        String user = SecurityUtils.getUsername();
        Date now = new Date();
        if (product.getId() == null) {
            product.setCreateBy(user);
            product.setCreateTime(now);
            productMapper.insert(product);
        } else {
            product.setUpdateBy(user);
            product.setUpdateTime(now);
            productMapper.updateById(product);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeProduct(Long id) {
        productMapper.deleteById(id);
    }
}

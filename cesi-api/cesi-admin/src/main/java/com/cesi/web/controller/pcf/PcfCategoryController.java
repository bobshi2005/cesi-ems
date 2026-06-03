package com.cesi.web.controller.pcf;

import lombok.RequiredArgsConstructor;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.pcf.domain.EmspcfCategory;
import com.cesi.pcf.domain.EmspcfProduct;
import com.cesi.pcf.service.IPcfCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Api(tags = "产品碳足迹-分类与产品管理")
@RequestMapping("/pcf/category")
public class PcfCategoryController extends BaseController {

    private final IPcfCategoryService categoryService;

    @ApiOperation("获取产品分类树")
    @GetMapping("/tree")
    public AjaxResult getTree() {
        return AjaxResult.success(categoryService.getCategoryTree());
    }

    @ApiOperation("保存分类")
    @PreAuthorize("@ss.hasPermi('pcf:config:list')")
    @PostMapping("/save")
    public AjaxResult saveCategory(@RequestBody EmspcfCategory category) {
        categoryService.saveCategory(category);
        return AjaxResult.success();
    }

    @ApiOperation("删除分类")
    @PreAuthorize("@ss.hasPermi('pcf:config:list')")
    @DeleteMapping("/{id}")
    public AjaxResult removeCategory(@PathVariable Long id) {
        categoryService.removeCategory(id);
        return AjaxResult.success();
    }

    @ApiOperation("按分类获取产品列表")
    @GetMapping("/products")
    public AjaxResult listProducts(@RequestParam Long categoryId) {
        return AjaxResult.success(categoryService.listProductsByCategory(categoryId));
    }

    @ApiOperation("保存产品")
    @PreAuthorize("@ss.hasPermi('pcf:config:list')")
    @PostMapping("/product/save")
    public AjaxResult saveProduct(@RequestBody EmspcfProduct product) {
        categoryService.saveProduct(product);
        return AjaxResult.success();
    }

    @ApiOperation("删除产品")
    @PreAuthorize("@ss.hasPermi('pcf:config:list')")
    @DeleteMapping("/product/{id}")
    public AjaxResult removeProduct(@PathVariable Long id) {
        categoryService.removeProduct(id);
        return AjaxResult.success();
    }
}

package com.cesi.pcf.domain.vo;

import com.cesi.pcf.domain.EmspcfProduct;
import lombok.Data;
import java.util.List;

/**
 * 产品分类树节点 VO
 */
@Data
public class CategoryTreeVO {

    private Long id;
    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private Integer orderNum;
    private String status;

    private List<CategoryTreeVO> children;
    private List<EmspcfProduct> products;
    private Integer productCount;
}

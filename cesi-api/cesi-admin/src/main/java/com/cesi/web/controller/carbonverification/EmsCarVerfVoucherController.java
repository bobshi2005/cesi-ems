package com.cesi.web.controller.carbonverification;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfVoucher;
import com.cesi.carbonverification.service.IEmsCarVerfVoucherService;
import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.core.page.TableDataInfo;
import com.cesi.common.enums.BusinessType;
import com.cesi.common.utils.poi.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RequiredArgsConstructor
@RestController
@Api(tags = "碳核查-凭证管理")
@RequestMapping("/carbonVerification/voucher")
public class EmsCarVerfVoucherController extends BaseController {

    private final IEmsCarVerfVoucherService voucherService;

    @ApiOperation("分页查询凭证列表")
    @PreAuthorize("@ss.hasPermi('carbonVerification:voucher:list')")
    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               @RequestParam(required = false) String orgUnit,
                               @RequestParam(required = false) String emissionSource,
                               @RequestParam(required = false) String voucherType,
                               @RequestParam(required = false) String voucherName,
                               @RequestParam(defaultValue = "1")  Long pageNum,
                               @RequestParam(defaultValue = "20") Long pageSize) {
        Page<EmsCarVerfVoucher> page = voucherService.listPage(year, month, orgUnit,
                emissionSource, voucherType, voucherName, pageNum, pageSize);
        return getDataTable(page);
    }

    @ApiOperation("上传凭证")
    @PreAuthorize("@ss.hasPermi('carbonVerification:voucher:upload')")
    @Log(title = "碳核查-凭证管理", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam MultipartFile file,
                              @RequestParam Integer year,
                              @RequestParam Integer month,
                              @RequestParam(defaultValue = "全厂") String orgUnit,
                              @RequestParam String emissionSource,
                              @RequestParam String voucherType,
                              @RequestParam String voucherName,
                              @RequestParam(required = false) String remark) {
        try {
            EmsCarVerfVoucher voucher = new EmsCarVerfVoucher();
            voucher.setYear(year);
            voucher.setMonth(month);
            voucher.setOrgUnit(orgUnit);
            voucher.setEmissionSource(emissionSource);
            voucher.setVoucherType(voucherType);
            voucher.setVoucherName(voucherName);
            voucher.setRemark(remark);
            EmsCarVerfVoucher saved = voucherService.upload(file, voucher);
            return AjaxResult.success("上传成功", saved);
        } catch (Exception e) {
            return AjaxResult.error("上传失败：" + e.getMessage());
        }
    }

    @ApiOperation("预览凭证（浏览器内打开）")
//    @PreAuthorize("@ss.hasPermi('carbonVerification:voucher:list')")
    @GetMapping("/preview/{id}")
    public void preview(@PathVariable Long id, HttpServletResponse response) {
        try {
            voucherService.preview(id, response);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @ApiOperation("下载凭证")
    @PreAuthorize("@ss.hasPermi('carbonVerification:voucher:download')")
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        try {
            voucherService.download(id, response);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @ApiOperation("批量下载凭证（ZIP）")
    @PreAuthorize("@ss.hasPermi('carbonVerification:voucher:download')")
    @PostMapping("/batchDownload")
    public void batchDownload(@RequestBody List<Long> ids, HttpServletResponse response) {
        try {
            voucherService.batchDownload(ids, response);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @ApiOperation("删除凭证")
    @PreAuthorize("@ss.hasPermi('carbonVerification:voucher:remove')")
    @Log(title = "碳核查-凭证管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        return toAjax(voucherService.deleteById(id));
    }

    @ApiOperation("导出凭证清单")
    @PreAuthorize("@ss.hasPermi('carbonVerification:voucher:export')")
    @Log(title = "碳核查-凭证管理", businessType = BusinessType.EXPORT)
    @PostMapping("/exportList")
    public void exportList(HttpServletResponse response,
                            @RequestParam Integer year,
                            @RequestParam(defaultValue = "全厂") String orgUnit) throws Exception {
        List<EmsCarVerfVoucher> list = voucherService.exportList(year, orgUnit);
        ExcelUtil<EmsCarVerfVoucher> util = new ExcelUtil<>(EmsCarVerfVoucher.class);
        util.exportExcel(response, list, year + "年凭证清单");
    }
}

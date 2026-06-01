package com.cesi.carbonverification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfVoucher;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface IEmsCarVerfVoucherService {

    Page<EmsCarVerfVoucher> listPage(Integer year, Integer month, String orgUnit,
                                     String emissionSource, String voucherType,
                                     String voucherName, long pageNum, long pageSize);

    EmsCarVerfVoucher upload(MultipartFile file, EmsCarVerfVoucher voucher) throws Exception;

    void preview(Long id, HttpServletResponse response) throws Exception;

    void download(Long id, HttpServletResponse response) throws Exception;

    void batchDownload(List<Long> ids, HttpServletResponse response) throws Exception;

    int deleteById(Long id);

    List<EmsCarVerfVoucher> exportList(Integer year, String orgUnit);
}

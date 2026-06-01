package com.cesi.carbonverification.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfVoucher;
import com.cesi.carbonverification.mapper.EmsCarVerfVoucherMapper;
import com.cesi.carbonverification.service.IEmsCarVerfVoucherService;
import com.cesi.common.config.BaseConfig;
import com.cesi.common.utils.SecurityUtils;
import com.cesi.common.utils.file.FileUploadUtils;
import com.cesi.common.utils.file.FileUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@AllArgsConstructor
public class EmsCarVerfVoucherServiceImpl implements IEmsCarVerfVoucherService {

    private final EmsCarVerfVoucherMapper voucherMapper;

    @Override
    public Page<EmsCarVerfVoucher> listPage(Integer year, Integer month, String orgUnit,
                                             String emissionSource, String voucherType,
                                             String voucherName, long pageNum, long pageSize) {
        Page<EmsCarVerfVoucher> page = new Page<>(pageNum, pageSize);
        return voucherMapper.selectPage(page, year, month, orgUnit, emissionSource,
                voucherType, voucherName);
    }

    @Override
    public EmsCarVerfVoucher upload(MultipartFile file, EmsCarVerfVoucher voucher) throws Exception {
        String baseDir = BaseConfig.getUploadPath() + "/carbonVerification/vouchers";
        String savedPath = FileUploadUtils.uploadAll(baseDir, file);

        String ext = FilenameUtils.getExtension(file.getOriginalFilename()).toUpperCase();
        String operator = SecurityUtils.getUsername();
        Date now = new Date();

        voucher.setFileName(file.getOriginalFilename());
        voucher.setFilePath(savedPath);
//        voucher.setFileSize(file.getSize());
        voucher.setFileFormat(ext);
        voucher.setUploadBy(operator);
        voucher.setUploadTime(now);
        voucher.setCreateBy(operator);
        voucher.setCreateTime(now);
        voucher.setUpdateBy(operator);
        voucher.setUpdateTime(now);

        voucherMapper.insert(voucher);
        return voucher;
    }

    @Override
    public void preview(Long id, HttpServletResponse response) throws Exception {
        EmsCarVerfVoucher voucher = voucherMapper.selectById(id);
        if (voucher == null || voucher.getFilePath() == null) throw new RuntimeException("凭证不存在");
        String fmt = voucher.getFileFormat() != null ? voucher.getFileFormat().toLowerCase() : "";
        String contentType;
        switch (fmt) {
            case "pdf":  contentType = "application/pdf"; break;
            case "jpg":
            case "jpeg": contentType = "image/jpeg"; break;
            case "png":  contentType = "image/png"; break;
            default:     contentType = "application/octet-stream"; break;
        }
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline; filename=\"" +
                java.net.URLEncoder.encode(voucher.getFileName(), "UTF-8") + "\"");
        FileUtils.writeBytes(voucher.getFilePath(), response.getOutputStream());
    }

    @Override
    public void download(Long id, HttpServletResponse response) throws Exception {
        EmsCarVerfVoucher voucher = voucherMapper.selectById(id);
        if (voucher == null || voucher.getFilePath() == null) {
            throw new RuntimeException("凭证不存在");
        }
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        FileUtils.setAttachmentResponseHeader(response, voucher.getFileName());
        FileUtils.writeBytes(voucher.getFilePath(), response.getOutputStream());
    }

    @Override
    public void batchDownload(List<Long> ids, HttpServletResponse response) throws Exception {
        response.setContentType("application/zip");
        FileUtils.setAttachmentResponseHeader(response, "凭证批量下载.zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Long id : ids) {
                EmsCarVerfVoucher voucher = voucherMapper.selectById(id);
                if (voucher == null || voucher.getFilePath() == null) continue;
                File f = new File(voucher.getFilePath());
                if (!f.exists()) continue;
                zos.putNextEntry(new ZipEntry(voucher.getVoucherName() + "." + voucher.getFileFormat().toLowerCase()));
                try (FileInputStream fis = new FileInputStream(f)) {
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = fis.read(buf)) > 0) zos.write(buf, 0, len);
                }
                zos.closeEntry();
            }
        }
    }

    @Override
    public int deleteById(Long id) {
        EmsCarVerfVoucher voucher = voucherMapper.selectById(id);
        if (voucher != null && voucher.getFilePath() != null) {
            FileUtils.deleteFile(voucher.getFilePath());
        }
        return voucherMapper.deleteById(id);
    }

    @Override
    public List<EmsCarVerfVoucher> exportList(Integer year, String orgUnit) {
        return voucherMapper.selectByYear(year, orgUnit);
    }
}

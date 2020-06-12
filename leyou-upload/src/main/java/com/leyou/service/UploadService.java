package com.leyou.service;


import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.config.UpLoadProperties;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Service
public class UploadService {
    private Logger logger = LoggerFactory.getLogger(UploadService.class);

    @Autowired
    private UpLoadProperties prop;

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;

    public String upLoadImage(MultipartFile file) {
        try {
            // 校验文件是否为图片
            String contentType = file.getContentType();
            if (prop.getAllowType().contains(contentType)){
                logger.info("上传的不是图片 " + contentType);
                throw new LyException(ExceptionEnums.INVALID_FILE_TYPE);
            }

            BufferedImage read = ImageIO.read(file.getInputStream());
            if (read == null){
                logger.info("上传的文件不符合规范!");
                throw new LyException(ExceptionEnums.INVALID_FILE_TYPE);
            }
            // 上传文件
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(),".");

            StorePath storePath = this.storageClient.uploadFile(
                    file.getInputStream(), file.getSize(), extension, null);
            logger.info("原图完整访问路径:" + prop.getBaseUrl() + storePath.getFullPath());

            StorePath storeThumbPath = this.storageClient.uploadImageAndCrtThumbImage(
                    file.getInputStream(), file.getSize(), extension, null);
            logger.info("缩略图图完整访问路径：" + prop.getBaseUrl() + storeThumbPath.getFullPath());

            return prop.getBaseUrl() + storePath.getFullPath();
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnums.UPLOAD_FILE_ERROR);
        }
    }
}

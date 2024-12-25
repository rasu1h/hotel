package com.phegondev.PhegonHotel.service;

import com.phegondev.PhegonHotel.exception.OurException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AwsS3Service {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.access.key}")
    private String awsS3AccessKey;

    @Value("${aws.s3.secret.key}")
    private String awsS3SecretKey;

    private final S3Client s3Client;

    public AwsS3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private String detectingFileName(String URL){
        String pattern = "^[^/]+/(.*)$";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(URL);

        if (m.find()) {
            String fileName = m.group(1);
            System.out.println("File part after '/': " + fileName);
            return fileName;
        }
        return null;
    }
    public String uploadImageToAWS(MultipartFile file) { // Uploading from cloud
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        try {
            // Загружаем файл в S3
            s3Client.putObject(software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Возвращаем URL, по которому файл будет доступен
            return "https://" + bucketName + ".s3." + "eu-north-1" + ".amazonaws.com/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке файла в S3", e);
        }
    }
    public void deleteFile(String file){ // deleting from cloud AWS
        String fileName = detectingFileName(file);
        if (fileName!= null){
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build());
            System.out.println("File deleted successfully: " + fileName);
        } else {
            System.out.println("File name not found or invalid file format");
        }
    }



}


















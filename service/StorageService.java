package com.sthreebucketexample.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class StorageService implements StorageServiceInterface
{
    
    @Value("${application.bucket.name}")
    private String bucketName;
    
    @Autowired
    private S3Client s3Client;
    
    public String uploadFile(MultipartFile file) {
        File fileObj = convertMultiPartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(fileName).build(), RequestBody.fromFile(fileObj));
        fileObj.delete();
        return "File uploaded: " + fileName;
    }
    
    
    
    public byte[] downloadFile(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();
        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            return toByteArray(s3Object);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    
    private byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }



	public String deleteFile(String fileName) 
    {
        s3Client.deleteObject(builder -> builder.bucket(bucketName).key(fileName));
        return fileName + " removed....";
    }
    
	private File convertMultiPartFileToFile(MultipartFile file) {
	    File convertedFile = new File(file.getOriginalFilename());
	    try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
	        fos.write(file.getBytes());
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return convertedFile;
	}
}

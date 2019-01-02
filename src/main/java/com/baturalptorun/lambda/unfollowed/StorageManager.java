package com.baturalptorun.lambda.unfollowed;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Manages storage resources and read/write files from/into S3.
 *
 * @author baturalp
 */
public class StorageManager {

    private static final Logger LOG = LoggerFactory.getLogger(StorageManager.class);

    private final AmazonS3 s3Client;
    private final Gson gson;
    private final String bucketName, fileName;

    @Inject
    public StorageManager(AmazonS3 s3Client, Gson gson,
                          @Named("S3Bucket") String bucketName,
                          @Named("S3File") String fileName) {
        this.s3Client = s3Client;
        this.gson = gson;
        this.bucketName = bucketName;
        this.fileName = fileName;
    }

    public Collection<Long> readFile() {
        if (!s3Client.doesObjectExist(bucketName, fileName))
            return ImmutableList.of();

        S3Object obj = s3Client.getObject(bucketName, fileName);
        List<Long> items = new ArrayList<>();
        try (JsonReader reader = new JsonReader(
                new InputStreamReader(obj.getObjectContent()))) {
            reader.beginArray();
            while (reader.hasNext())
                items.add(reader.nextLong());
            reader.endArray();
        } catch (IOException e) {
            LOG.error("Cannot read s3://{}/{}", bucketName, fileName);
            throw new RuntimeException(e);
        }
        return items;
    }

    public void writeFile(Collection<Long> items) {
        try {
            s3Client.putObject(bucketName, fileName, gson.toJson(items));
        } catch (AmazonServiceException e) {
            LOG.error("Cannot put object at s3://{}/{}", bucketName, fileName);
        }
    }
}

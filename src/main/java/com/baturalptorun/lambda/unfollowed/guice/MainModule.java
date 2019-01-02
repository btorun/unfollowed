package com.baturalptorun.lambda.unfollowed.guice;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;

/**
 * Main module for guice
 *
 * @author baturalp
 */
public class MainModule extends AbstractModule {

    private static final Map<String, String> ENV = System.getenv();
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String ACCESS_TOKEN_SECRET = "ACCESS_TOKEN_SECRET";
    private static final String CONSUMER_API_KEY = "CONSUMER_API_KEY";
    private static final String CONSUMER_API_SECRET = "CONSUMER_API_SECRET";
    private static final String S3_BUCKET_NAME = "S3_BUCKET_NAME";
    private static final String S3_FOLLOWERS_FILE = "S3_FOLLOWERS_FILE";
    private static final String S3_REGION = "S3_REGION";

    @Override
    protected void configure() {
        bind(String.class)
                .annotatedWith(Names.named("S3Bucket"))
                .toInstance(ENV.get(S3_BUCKET_NAME));

        bind(String.class)
                .annotatedWith(Names.named("S3File"))
                .toInstance(ENV.get(S3_FOLLOWERS_FILE));
    }

    @Provides
    Gson gson() {
        return new GsonBuilder().create();
    }

    @Provides
    Twitter twitter() {
        ConfigurationBuilder cb = new ConfigurationBuilder()
                .setOAuthAccessToken(ENV.get(ACCESS_TOKEN))
                .setOAuthAccessTokenSecret(ENV.get(ACCESS_TOKEN_SECRET))
                .setOAuthConsumerKey(ENV.get(CONSUMER_API_KEY))
                .setOAuthConsumerSecret(ENV.get(CONSUMER_API_SECRET));
        return new TwitterFactory(cb.build()).getInstance();
    }

    @Provides
    AmazonS3 s3Client() {
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(ENV.get(S3_REGION)))
                .build();
    }
}

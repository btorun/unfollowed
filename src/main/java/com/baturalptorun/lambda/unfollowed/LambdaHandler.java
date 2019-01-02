package com.baturalptorun.lambda.unfollowed;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.baturalptorun.lambda.unfollowed.ApiGatewayResponse.Builder;
import com.baturalptorun.lambda.unfollowed.ApiGatewayResponse.Response;
import com.baturalptorun.lambda.unfollowed.guice.MainModule;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_NOT_MODIFIED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * AWS Lambda Handler
 *
 * @author baturalp
 */
public class LambdaHandler implements
        RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(
            LambdaHandler.class);

    private static final Map<String, String> HEADERS = ImmutableMap.of(
            "X-Powered-By", "AWS Lambda",
            "Content-Type", "application/json");

    private static final Response ERROR_RESPONSE = new Response(
            "failed", "Unable to process your request. Check logs.");

    @Override
    public ApiGatewayResponse handleRequest(
            Map<String, Object> input, Context context) {
        LOG.info("Received: " + input);

        Injector injector = Guice.createInjector(new MainModule());
        Builder responseBuilder = injector.getInstance(Builder.class);

        Collection<String> unfollowed;
        try {
            unfollowed = injector.getInstance(Unfollow.class).getUnfollowers();
        } catch (Exception e) {
            LOG.error("Cannot get unfollowers.", e);
            return responseBuilder.withObjectBody(ERROR_RESPONSE)
                    .withStatusCode(SC_OK)
                    .withHeaders(HEADERS)
                    .build();
        }


        String msg = unfollowed.isEmpty() ? "No change" :
                "Unfollowed by " + unfollowed;
        Response response = new Response("ok", msg);
        int status = unfollowed.isEmpty() ? SC_NOT_MODIFIED : SC_OK;

        return responseBuilder.withObjectBody(response)
                .withStatusCode(status)
                .withHeaders(HEADERS)
                .build();
    }

    /**
     * Not used in AWS Lambda but useful for local development
     *
     * @param args
     */
    public static void main(String[] args) {
        ApiGatewayResponse response = new LambdaHandler().handleRequest(ImmutableMap.of(), null);
        LOG.info(response.toString());
    }

}

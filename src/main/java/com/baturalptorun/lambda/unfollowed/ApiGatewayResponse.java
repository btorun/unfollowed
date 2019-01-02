package com.baturalptorun.lambda.unfollowed;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * API Gateway Response for Lambda proxy. More info can be found at
 * https://docs.aws.amazon.com/apigateway/latest/developerguide/set-up-lambda-proxy-integrations.html#api-gateway-simple-proxy-for-lambda-output-format
 *
 * @author baturalp
 */
public class ApiGatewayResponse {

    @SerializedName("isBase64Encoded")
    private final boolean isBase64Encoded;

    @SerializedName("statusCode")
    private final int statusCode;

    @SerializedName("headers")
    private final Map<String, String> headers;

    @SerializedName("body")
    private final String body;

    /**
     * Use #ApiGatewayResponse.Builder
     *
     * @param isBase64Encoded
     * @param statusCode
     * @param headers
     * @param body
     */
    private ApiGatewayResponse(boolean isBase64Encoded, int statusCode,
                               Map<String, String> headers, String body) {
        this.isBase64Encoded = isBase64Encoded;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean isBase64Encoded() {
        return isBase64Encoded;
    }

    @Override
    public String toString() {
        return "ApiGatewayResponse{" +
                "isBase64Encoded=" + isBase64Encoded +
                ", statusCode=" + statusCode +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }

    public static class Response {

        @SerializedName("status")
        private final String status;

        @SerializedName("message")
        private final String message;

        public Response(String status, String message) {
            this.status = status;
            this.message = message;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "status='" + status + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    public static class Builder {

        private static final Logger LOG = LoggerFactory.getLogger(
                ApiGatewayResponse.Builder.class);

        private final Gson gson;

        /* Default response values */
        private boolean isBase64Encoded = false;
        private int statusCode = HttpStatus.SC_OK;
        private Map<String, String> headers = ImmutableMap.of();

        /* Only one of the below can be set at a time. */
        private String body;
        private Object objectBody;
        private byte[] binaryBody;

        @Inject
        public Builder(Gson gson) {
            this.gson = gson;
        }

        public Builder withStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder withHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        /**
         * Builds the {@link ApiGatewayResponse} using the passed body string.
         * Sets other body instances to null.
         */
        public Builder withBody(String body) {
            this.body = body;
            this.objectBody = null;
            this.binaryBody = null;
            return this;
        }

        /**
         * Builds the {@link ApiGatewayResponse} using the passed object. Sets
         * other body instances to null.
         */
        public Builder withObjectBody(Object objectBody) {
            this.objectBody = objectBody;
            this.body = null;
            this.binaryBody = null;
            return this;
        }

        /**
         * Builds the {@link ApiGatewayResponse} using the passed binary body.
         * Sets other body instances to null.
         */
        public Builder withBinaryBody(byte[] binaryBody) {
            this.binaryBody = binaryBody;
            this.body = null;
            this.objectBody = null;
            return this;
        }

        public ApiGatewayResponse build() {
            String body = null;
            if (this.body != null)
                body = this.body;
            else if (objectBody != null) {
                try {
                    body = gson.toJson(objectBody);
                } catch (JsonIOException e) {
                    LOG.error("failed to serialize object", e);
                    throw new RuntimeException(e);
                }
            } else if (binaryBody != null) {
                body = new String(
                        Base64.getEncoder().encode(binaryBody),
                        StandardCharsets.UTF_8);
                isBase64Encoded = true;
            }
            return new ApiGatewayResponse(
                    isBase64Encoded, statusCode, headers, body);
        }
    }
}

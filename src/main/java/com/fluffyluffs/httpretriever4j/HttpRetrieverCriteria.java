/*
 * Copyright 2021 HTTPRetriever4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fluffyluffs.httpretriever4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * HTTP Retriever Criteria
 */
public class HttpRetrieverCriteria {

    private final URL url;
    private final HTTPMethod hTTPMethod;
    private final String body;
    private final ContentType bodyContentType;
    private final ContentType acceptContentType;
    private final char[] authorization;
    private final String userAgent;

    private HttpRetrieverCriteria(char[] authorization,
             URL url,
             HTTPMethod hTTPMethod,
             String body,
             ContentType bodyContentType,
             ContentType acceptContentType,
             String userAgent) {
        this.authorization = authorization;
        this.url = url;
        this.hTTPMethod = hTTPMethod;
        this.body = body;
        this.bodyContentType = bodyContentType;
        this.acceptContentType = acceptContentType;
        this.userAgent = userAgent;
    }

    /**
     * Get Authorisation
     *
     * @return {@link char[]}
     */
    public char[] getAuthorization() {
        return authorization;
    }

    /**
     * Get URL
     *
     * @return {@link URL}
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Get HTTP Method
     *
     * @return {@link HTTPMethod}
     */
    public HTTPMethod gethTTPMethod() {
        return hTTPMethod;
    }

    /**
     * Get Body
     *
     * @return {@link String}
     */
    public String getBody() {
        return body;
    }

    /**
     * Get Body Content Type
     *
     * @return {@link ContentType}
     */
    public ContentType getBodyContentType() {
        return bodyContentType;
    }

    /**
     * Get Accepted Content Type
     *
     * @return {@link ContentType}
     */
    public ContentType getAcceptContentType() {
        return acceptContentType;
    }

    /**
     * Get User Agent
     * @return {@link String}
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * HTTP Retriever Criteria Builder
     */
    public static class HttpRetrieverCriteriaBuilder {

        private char[] authorization;
        private URL url;
        private HTTPMethod hTTPMethod;
        private String body;
        private ContentType bodyContentType;
        private ContentType acceptContentType;
        private String userAgent;

        /**
         * Set authorization in UTF-8 Base64. Using {@link HttpRetrieverAuthorization}
         * <pre>
         *     DataRetrieverAuthorization.BASIC.setAuthorization(String.format("%s:%s", "user", "passwd"))
         * </pre>
         *
         * @param authorization {@link char[]}
         * @return {@link HttpRetrieverCriteriaBuilder}
         */
        public HttpRetrieverCriteriaBuilder setAuthorization(char[] authorization) {
            this.authorization = authorization;
            return this;
        }

        /**
         * Set URL
         * @param url {@link String}
         * @return {@link HttpRetrieverCriteriaBuilder}
         */
        public HttpRetrieverCriteriaBuilder setURL(String url) {
            try {
                this.url = new URL(url);
            } catch (MalformedURLException murlex) {
                throw new RuntimeException(murlex);
            }

            return this;
        }

        /**
         * Set HTTP Method
         * @param hTTPMethod {@link HTTPMethod}
         * @return {@link HttpRetrieverCriteriaBuilder}
         */
        public HttpRetrieverCriteriaBuilder setHTTPMethod(HTTPMethod hTTPMethod) {
            this.hTTPMethod = hTTPMethod;
            return this;
        }

        /**
         * Set Body
         * @param body {@link String}
         * @return {@link HttpRetrieverCriteriaBuilder}
         */
        public HttpRetrieverCriteriaBuilder setBody(String body) {
            this.body = body;
            return this;
        }

        /**
         * Set Accepted Content Type
         * @param acceptContentType {@link ContentType}
         * @return {@link HttpRetrieverCriteriaBuilder}
         */
        public HttpRetrieverCriteriaBuilder setAcceptContentType(ContentType acceptContentType) {
            this.acceptContentType = acceptContentType;
            return this;
        }

        /**
         * Set Body Content Type
         * @param bodyContentType {@link ContentType}
         * @return {@link HttpRetrieverCriteriaBuilder}
         */
        public HttpRetrieverCriteriaBuilder setBodyContentType(ContentType bodyContentType) {
            this.bodyContentType = bodyContentType;
            return this;
        }

        /**
         * Set User Agent: https://developers.whatismybrowser.com/useragents/explore
         * @param userAgent {@link String}
         * @return {@link HttpRetrieverCriteriaBuilder}
         */
        public HttpRetrieverCriteriaBuilder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Build {@link HttpRetrieverCriteria}. May throw {@link NoSuchElementException} where a required element is missing.
         * @return 
         */
        public HttpRetrieverCriteria build() {

            return new HttpRetrieverCriteria(authorization,
                     validate(url, "URL"),
                     validate(hTTPMethod, "HTTP Method GET, POST etc"),
                     body,
                     bodyContentType,
                     acceptContentType,
                     validate(userAgent, "Mozilla/5.0 etc"));
        }

        private <T> T validate(T criterionValue, String field) {
            return Optional.ofNullable(criterionValue).orElseThrow(()
                    -> new NoSuchElementException(String.format("Missing required %s.", field)));
        }
    }

    public enum ContentType {
        JSON("application/json;charset=UTF-8"),
        TEXT("text/html; charset=UTF-8");
        //TODO add additional content types

        private final String contentType;

        private ContentType(String contentType) {
            this.contentType = contentType;
        }

        /**
         * Get Content Type
         * @return {@link String}
         */
        public String getContentType() {
            return contentType;
        }

    }

    public enum HTTPMethod {
        GET,
        PUT,
        DELETE,
        TRACE,
        POST;
    }
}

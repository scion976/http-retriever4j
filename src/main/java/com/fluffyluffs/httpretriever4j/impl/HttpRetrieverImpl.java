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

package com.fluffyluffs.httpretriever4j.impl;

import com.fluffyluffs.httpretriever4j.HttpRetriever;
import com.fluffyluffs.httpretriever4j.HttpRetrieverCriteria;
import com.fluffyluffs.httpretriever4j.HttpRetrieverCriteria.ContentType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpRetrieverImpl implements HttpRetriever<HttpRetrieverCriteria> {

    private static final Logger LOGGER = LogManager.getLogger(HttpRetrieverImpl.class);
    private static final String AUTH = "Authorization";
    private static final String USER_AGENT = "User-Agent";
    private static final String ACCEPT = "Accept";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final int RETRY_LIMIT = 5;

    @Override
    public String retrieve(HttpRetrieverCriteria criteria) {
        StringBuilder response = new StringBuilder();
        int retryCounter = 0;
        boolean success = false;
        do {
            retryCounter++;
            try {
                URL url = criteria.getUrl();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                try {
                    connection.addRequestProperty(AUTH, Optional.ofNullable(criteria.getAuthorization()).map(auth -> String.valueOf(auth)).orElse(null));
                    connection.setRequestProperty(USER_AGENT, criteria.getUserAgent());
                    connection.setRequestProperty(ACCEPT, Optional.ofNullable(criteria.getAcceptContentType()).map(ContentType::getContentType).orElse(null));
                    connection.setRequestProperty(CACHE_CONTROL, "no-cache");
                    connection.setRequestMethod(criteria.gethTTPMethod().name());
                    connection.setConnectTimeout(Long.valueOf(TimeUnit.SECONDS.toMillis(5)).intValue());
                    connection.setReadTimeout(Long.valueOf(TimeUnit.MINUTES.toMillis(1)).intValue());
                    connection.setUseCaches(false);

                    Optional.ofNullable(criteria.getBodyContentType()).ifPresent(contentType -> {
                        connection.setRequestProperty(CONTENT_TYPE, contentType.getContentType());
                    });
                    Optional.ofNullable(criteria.getBody()).ifPresent(body -> writeBody(connection, body));

                    connection.connect();

                    switch (connection.getResponseCode()) {
                        case 200:
                        case 201 :
                            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                String outputString;
                                while ((outputString = in.readLine()) != null) {
                                    response.append(outputString);
                                }
                            }
                            success = true;
                            
                            break;
                        case 204:
                            LOGGER.atInfo().log("Completed: {}", connection.getResponseCode());
                            success = true;
                            
                            break;
                        case 202:
                            if (retryCounter == RETRY_LIMIT) {
                                LOGGER.atWarn().log("Reached retry limit of {} for {}, aborting.", RETRY_LIMIT, url);
                                success = true;
                            }
                            LOGGER.atInfo().log("Status {}:{}, resubmitting {}.", connection.getResponseCode(), connection.getResponseMessage(), url);
                            
                            break;
                        case 404:
                            success = true;
                            LOGGER.atError().log("Status {} for {}:{} - will not be reattempted.", url, connection.getResponseCode(), connection.getResponseMessage());
                            
                            break;
                        default:
                            success = true;
                            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                String outputString;
                                while ((outputString = in.readLine()) != null) {
                                    response.append(outputString);
                                }
                            }
                            LOGGER.atInfo().log("Connection to {} returned an unhandled {}:{} and response body {} - will not be reattempted."
                                    , url
                                    , connection.getResponseCode()
                                    , connection.getResponseMessage()
                                    , response.toString());
                    }
                } catch(UnknownHostException ex) {
                    success = true;
                    LOGGER.atError().log(ex.getMessage(), ex);
                    throw new RuntimeException(ex);
                } finally {
                    connection.disconnect();
                }
            } catch (IOException ex) {
                success = true;
                LOGGER.atError().log(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }

        } while (!success);

        return response.toString();
    }

    private void writeBody(HttpURLConnection secureConnection, String body) {
        secureConnection.setDoOutput(true);
        secureConnection.setRequestProperty("Content-Length", "" + Integer.toString(body.getBytes().length));
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(secureConnection.getOutputStream())) {
            outputStreamWriter.write(body);
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
        } 
    }

}

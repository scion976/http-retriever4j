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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpRetrieverImpl implements HttpRetriever<HttpRetrieverCriteria> {

  private static final Logger LOGGER = Logger.getLogger(HttpRetrieverImpl.class.getName());

  private static final String AUTH = "Authorization";
  private static final String USER_AGENT = "User-Agent";
  private static final String ACCEPT = "Accept";
  private static final String CACHE_CONTROL = "Cache-Control";
  private static final String CONTENT_TYPE = "Content-Type";

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
          connection.addRequestProperty(
              AUTH,
              Optional.ofNullable(criteria.getAuthorization())
                  .map(auth -> String.valueOf(auth))
                  .orElse(null));
          connection.setRequestProperty(USER_AGENT, criteria.getUserAgent());
          connection.setRequestProperty(
              ACCEPT,
              Optional.ofNullable(criteria.getAcceptContentType())
                  .map(ContentType::getContentType)
                  .orElse(null));
          connection.setRequestProperty(CACHE_CONTROL, "no-cache");
          connection.setRequestMethod(criteria.gethTTPMethod().name());
          connection.setConnectTimeout(Long.valueOf(TimeUnit.SECONDS.toMillis(5)).intValue());
          connection.setReadTimeout(Long.valueOf(TimeUnit.MINUTES.toMillis(1)).intValue());
          connection.setUseCaches(false);

          Optional.ofNullable(criteria.getBodyContentType())
              .ifPresent(
                  contentType -> {
                    connection.setRequestProperty(CONTENT_TYPE, contentType.getContentType());
                  });
          Optional.ofNullable(criteria.getBody()).ifPresent(body -> writeBody(connection, body));

          criteria
              .getHeaders()
              .forEach(
                  header -> {
                    String headerType =
                        Optional.ofNullable(header.getType())
                            .orElseThrow(
                                () -> new NoSuchElementException("Header type cannot be null"));
                    String headerString =
                        Optional.ofNullable(header.getHeader())
                            .orElseThrow(() -> new NoSuchElementException("Header cannot be null"));

                    connection.setRequestProperty(headerType, headerString);
                  });

          connection.connect();

          switch (connection.getResponseCode()) {
            case 200:
            case 201:
              try (BufferedReader in =
                  new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String outputString;
                while ((outputString = in.readLine()) != null) {
                  response.append(outputString);
                }
              }
              success = true;

              break;
            case 204:
              LOGGER.log(Level.INFO, "Completed: {}", connection.getResponseCode());
              success = true;

              break;
            case 202:
              int retryLimit = criteria.getRetryLimit();
              if (retryCounter == retryLimit) {
                LOGGER.log(
                    Level.WARNING,
                    "Reached retry limit of {} for {}, aborting.",
                    new Object[] {retryLimit, url});
                success = true;
              }

              LOGGER.log(
                  Level.INFO,
                  "Status {}:{}, resubmitting {}.",
                  new Object[] {
                    connection.getResponseCode(), connection.getResponseMessage(), url
                  });

              break;
            case 404:
              success = true;
              LOGGER.log(
                  Level.WARNING,
                  "Status {} for {}:{} - will not be reattempted.",
                  new Object[] {
                    url, connection.getResponseCode(), connection.getResponseMessage()
                  });

              break;
            default:
              success = true;
              try (BufferedReader in =
                  new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String outputString;
                while ((outputString = in.readLine()) != null) {
                  response.append(outputString);
                }
              }
              LOGGER.log(
                  Level.WARNING,
                  "Connection to {} returned an unhandled {}:{} and response body {} - will not be reattempted.",
                  new Object[] {
                    url,
                    connection.getResponseCode(),
                    connection.getResponseMessage(),
                    response.toString()
                  });
          }
        } catch (UnknownHostException ex) {
          success = true;
          LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
          throw new RuntimeException(ex);
        } finally {
          connection.disconnect();
        }
      } catch (IOException ex) {
        success = true;
        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        throw new RuntimeException(ex);
      }

    } while (!success);

    return response.toString();
  }

  private void writeBody(HttpURLConnection secureConnection, String body) {
    secureConnection.setDoOutput(true);
    secureConnection.setRequestProperty(
        "Content-Length", "" + Integer.toString(body.getBytes().length));
    try (OutputStreamWriter outputStreamWriter =
        new OutputStreamWriter(secureConnection.getOutputStream())) {
      outputStreamWriter.write(body);
      outputStreamWriter.flush();
      outputStreamWriter.close();
    } catch (IOException ex) {
      throw new RuntimeException(ex.getLocalizedMessage(), ex);
    }
  }
}

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

import com.fluffyluffs.httpretriever4j.HttpRetrieverCriteria;
import com.fluffyluffs.httpretriever4j.HttpRetrieverCriteria.ContentType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpRetrieverImpl {

  private static final Logger LOGGER = Logger.getLogger(HttpRetrieverImpl.class.getName());

  private static final String AUTH = "Authorization";
  private static final String USER_AGENT = "User-Agent";
  private static final String ACCEPT = "Accept";
  private static final String CACHE_CONTROL = "Cache-Control";
  private static final String CONTENT_TYPE = "Content-Type";

  private static boolean success;

  private final HttpRetrieverCriteria httpRetrieverCriteria;

  public HttpRetrieverImpl(HttpRetrieverCriteria httpRetrieverCriteria) {
    this.httpRetrieverCriteria = httpRetrieverCriteria;
  }

  public InputStream retrieve() {

    HttpURLConnection connection = getHttpURLConnection();

    try {
      Response response =
          Response.of(connection.getResponseCode()).orElse(Response.HTTP_INTERNAL_ERROR);
      success = response.hasStatus();

      if (success) {
        log(response, Level.INFO);
        return new ByteArrayInputStream(connection.getInputStream().readAllBytes());
      }

      log(response, Level.WARNING);
      return InputStream.nullInputStream();

    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      throw new RuntimeException(ex);
    } finally {
      connection.disconnect();
    }
  }

  private void log(Response response, Level level) {
    LOGGER.log(
        level, "Recieved {0}:{1}", new Object[] {response.name(), response.getReponseCode()});
  }

  private HttpURLConnection getHttpURLConnection() {

    try {

      HttpURLConnection connection =
          (HttpURLConnection) httpRetrieverCriteria.getUrl().openConnection();
      connection.addRequestProperty(
          AUTH,
          Optional.ofNullable(httpRetrieverCriteria.getAuthorization())
              .map(auth -> String.valueOf(auth))
              .orElse(null));
      connection.setRequestProperty(USER_AGENT, httpRetrieverCriteria.getUserAgent());
      connection.setRequestProperty(
          ACCEPT,
          Optional.ofNullable(httpRetrieverCriteria.getAcceptContentType())
              .map(ContentType::getContentType)
              .orElse(null));
      connection.setRequestProperty(CACHE_CONTROL, "no-cache");
      connection.setRequestMethod(httpRetrieverCriteria.gethTTPMethod().name());
      connection.setConnectTimeout(Long.valueOf(TimeUnit.SECONDS.toMillis(5)).intValue());
      connection.setReadTimeout(Long.valueOf(TimeUnit.MINUTES.toMillis(1)).intValue());
      connection.setUseCaches(false);

      Optional.ofNullable(httpRetrieverCriteria.getBodyContentType())
          .ifPresent(
              contentType -> {
                connection.setRequestProperty(CONTENT_TYPE, contentType.getContentType());
              });
      Optional.ofNullable(httpRetrieverCriteria.getBody())
          .ifPresent(body -> writeBody(connection, body));

      httpRetrieverCriteria
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

      return connection;
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      throw new RuntimeException(ex);
    }
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

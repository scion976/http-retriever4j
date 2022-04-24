/*
 * Copyright 2022 HTTPRetriever4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fluffyluffs.httpretriever4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utils
 *
 * @author Chris Luff
 */
public class Utils {

  private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

  public static String convertToString(InputStream inputStream) {

    var response = new StringBuilder();
    Optional.ofNullable(inputStream)
        .ifPresent(
            in -> {
              try (BufferedReader bufferedReader =
                  new BufferedReader(new InputStreamReader(inputStream))) {
                String outputString;
                while ((outputString = bufferedReader.readLine()) != null) {
                  response.append(outputString);
                }
              } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
              }
            });

    return response.toString();
  }
}

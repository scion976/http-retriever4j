/*
 * Copyright 2021 cl011157.
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public enum HttpRetrieverAuthorization {

    BEARER {
        @Override
        public char [] getAuthorization(String auth) {
            return ("Bearer " + String.valueOf(Optional.ofNullable(auth).orElseThrow(() -> new IllegalStateException("Missing authentication")))).toCharArray();
        }
        
    },
    BASIC {
        @Override
        public char [] getAuthorization(String auth) {
            return ("Basic " + encodeString(auth)).toCharArray();
        }
    };
    
    /**
     * Obtains the correct structure for secure authorization.
     * @param auth A personal access token or a colon separated username and password.
     * @return {@link char[]}
     */
    public abstract char [] getAuthorization(String auth);
    
    private static String encodeString(String auth) {
         return Base64.getEncoder().encodeToString(Optional.ofNullable(auth).orElseThrow(() -> new IllegalStateException("Missing authentication")).getBytes(StandardCharsets.UTF_8));
    }
}

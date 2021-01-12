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
package com.fluffy.luffs.httpretriever4j.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fluffyluffs.httpretriever4j.HttpRetriever;
import com.fluffyluffs.httpretriever4j.HttpRetrieverCriteria;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

/**
 *
 * TestHttpRetriverImpl
 * 
 * HttpURLConnection mocked with assistance of Stackoverflow https://stackoverflow.com/a/40073597/1338769
 */
@PowerMockIgnore({"javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(BlockJUnit4ClassRunner.class)
public class TestHttpRetriverImpl {

    private static URLStreamHandlerFactory urlStreamHandlerFactory;

    @BeforeClass
    public static void setup() {
        urlStreamHandlerFactory = Mockito.mock(URLStreamHandlerFactory.class);
        URL.setURLStreamHandlerFactory(urlStreamHandlerFactory);
    }

    private abstract class AbstractPublicStreamHandler extends URLStreamHandler {

        @Override
        public URLConnection openConnection(URL url) throws IOException {
            return null;
        }
    }

    @Test
    public void test_no_content_simple_httpRetriever() throws IOException {
        AbstractPublicStreamHandler publicStreamHandler = Mockito.mock(AbstractPublicStreamHandler.class);
        Mockito.doReturn(publicStreamHandler).when(urlStreamHandlerFactory).createURLStreamHandler(ArgumentMatchers.eq("http"));

        String responseString = "";

        HttpURLConnection mockHttpURLConnection = Mockito.mock(HttpURLConnection.class);
        PowerMockito.doReturn(mockHttpURLConnection).when(publicStreamHandler).openConnection(ArgumentMatchers.any(URL.class));
        PowerMockito.doNothing().when(mockHttpURLConnection).connect();
        PowerMockito.when(mockHttpURLConnection.getResponseCode()).thenReturn(204);
        PowerMockito.doReturn(new ByteArrayInputStream(responseString.getBytes("UTF-8"))).when(mockHttpURLConnection).getInputStream();

        HttpRetrieverCriteria httpRetrieverCriteria = new HttpRetrieverCriteria.HttpRetrieverCriteriaBuilder()
                .setURL("http://cabbage.com/api/v1/")
                .setUserAgent("Mozzila/5.0")
                .setHTTPMethod(HttpRetrieverCriteria.HTTPMethod.GET)
                .build();

        String urlResponse = HttpRetriever.Factory.create().retrieve(httpRetrieverCriteria);
        assertTrue(urlResponse.isEmpty());
    }

    @Test
    public void test_simple_httpRetriever() throws IOException, Exception {
        AbstractPublicStreamHandler publicStreamHandler = Mockito.mock(AbstractPublicStreamHandler.class);
        Mockito.doReturn(publicStreamHandler).when(urlStreamHandlerFactory).createURLStreamHandler(ArgumentMatchers.eq("http"));

        String responseString = "CABBAGE";

        HttpURLConnection mockHttpURLConnection = Mockito.mock(HttpURLConnection.class);
        PowerMockito.doReturn(mockHttpURLConnection).when(publicStreamHandler).openConnection(ArgumentMatchers.any(URL.class));
        PowerMockito.doNothing().when(mockHttpURLConnection).connect();
        PowerMockito.when(mockHttpURLConnection.getResponseCode()).thenReturn(200);
        PowerMockito.doReturn(new ByteArrayInputStream(responseString.getBytes("UTF-8"))).when(mockHttpURLConnection).getInputStream();

        HttpRetrieverCriteria httpRetrieverCriteria = new HttpRetrieverCriteria.HttpRetrieverCriteriaBuilder()
                .setURL("http://cabbage.com/api/v1/")
                .setUserAgent("Mozzila/5.0")
                .setHTTPMethod(HttpRetrieverCriteria.HTTPMethod.GET)
                .build();

        String urlResponse = HttpRetriever.Factory.create().retrieve(httpRetrieverCriteria);
        assertEquals(responseString, urlResponse);
    }
}

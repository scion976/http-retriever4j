package com.fluffy.luffs.httpretriever4j.test;

import static org.junit.Assert.assertEquals;

import com.fluffyluffs.httpretriever4j.HttpRetrieverCriteria;
import com.fluffyluffs.httpretriever4j.QueryParameter;
import org.junit.Test;

/**
 *
 * @author Todd Lomaskin
 */
public class TestHttpRetrieverCriteria {
    @Test
    public void testMultipleQueryParameters() {
        HttpRetrieverCriteria hrc = new HttpRetrieverCriteria.HttpRetrieverCriteriaBuilder()
                .setQueryParameter(new QueryParameter("Order", "Brassicales"))
                .setQueryParameter(new QueryParameter("Family", "Brassicaceae"))
                .setQueryParameter(new QueryParameter("Genus", "Brassica"))
                .setHTTPMethod(HttpRetrieverCriteria.HTTPMethod.GET)
                .setUserAgent("Mozilla/5.0 etc")
                .setURL("http://cabbage.com")
                .build();
        assertEquals(hrc.getUrl().toString(), "http://cabbage.com?Order=Brassicales&Family=Brassicaceae&Genus=Brassica");
    }

    @Test
    public void testSingleQueryParameters() {
        HttpRetrieverCriteria hrc = new HttpRetrieverCriteria.HttpRetrieverCriteriaBuilder()
                .setQueryParameter(new QueryParameter("Order", "Brassicales"))
                .setHTTPMethod(HttpRetrieverCriteria.HTTPMethod.GET)
                .setUserAgent("Mozilla/5.0 etc")
                .setURL("http://cabbage.com")
                .build();
        assertEquals(hrc.getUrl().toString(), "http://cabbage.com?Order=Brassicales");
    }

    @Test
    public void testNoQueryParameters() {
        HttpRetrieverCriteria hrc = new HttpRetrieverCriteria.HttpRetrieverCriteriaBuilder()
                .setHTTPMethod(HttpRetrieverCriteria.HTTPMethod.GET)
                .setUserAgent("Mozilla/5.0 etc")
                .setURL("http://cabbage.com")
                .build();
        assertEquals(hrc.getUrl().toString(), "http://cabbage.com");
    }

}

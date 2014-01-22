package org.apache.isis.applib.services.queryresultscache;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class QueryResultsCache_KeyTest {

    private QueryResultsCache.Key cacheKey;

    static class A {}
    @Test
    public void toStringIs() {

        cacheKey = new QueryResultsCache.Key(A.class, "foo", "key1", 2, 3, "key4");
        assertThat(cacheKey.toString(), is("org.apache.isis.applib.services.queryresultscache.QueryResultsCache_KeyTest$A#foo[key1, 2, 3, key4]"));
    }

}

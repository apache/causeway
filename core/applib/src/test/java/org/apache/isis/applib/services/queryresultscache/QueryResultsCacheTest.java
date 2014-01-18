package org.apache.isis.applib.services.queryresultscache;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

public class QueryResultsCacheTest {

    private QueryResultsCache queryResultsCache;

    @Before
    public void setUp() throws Exception {
        queryResultsCache = new QueryResultsCache();
    }
    @Test
    public void execute() {
        
        String value = queryResultsCache.execute(new Callable<String>(){

            @Override
            public String call() throws Exception {
                return "foo";
            }
            
        }, QueryResultsCacheTest.class, "execute");
        
        assertThat(value, is("foo"));
    }

    @Test
    public void caching() {
        
        final int[] i = new int[]{0};
        
        Callable<String> callable = new Callable<String>(){
            
            @Override
            public String call() throws Exception {
                i[0]++;
                return "foo";
            }
            
        };
        assertThat(i[0], is(0));
        assertThat(queryResultsCache.execute(callable, QueryResultsCacheTest.class, "caching", "a","b",1,2), is("foo"));
        assertThat(i[0], is(1));
        
        // should be a cache hit
        assertThat(queryResultsCache.execute(callable, QueryResultsCacheTest.class, "caching", "a","b",1,2), is("foo"));
        assertThat(i[0], is(1));
        
        // changing any of the keys results in a cache miss
        assertThat(queryResultsCache.execute(callable, QueryResultsCacheTest.class, "XXXcaching", "a","b",1,2), is("foo"));
        assertThat(i[0], is(2));
        assertThat(queryResultsCache.execute(callable, QueryResultsCache.class, "caching", "a","b",1,2), is("foo"));
        assertThat(i[0], is(3));
        assertThat(queryResultsCache.execute(callable, QueryResultsCacheTest.class, "caching", "XXX","b",1,2), is("foo"));
        assertThat(i[0], is(4));
        assertThat(queryResultsCache.execute(callable, QueryResultsCacheTest.class, "caching", "a","b",1,2, "x"), is("foo"));
        assertThat(i[0], is(5));
    }
    
}

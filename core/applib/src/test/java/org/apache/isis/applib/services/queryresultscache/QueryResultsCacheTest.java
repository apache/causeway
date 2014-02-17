/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.queryresultscache;

import static org.hamcrest.CoreMatchers.is;
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

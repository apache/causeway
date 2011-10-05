package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class RestfulRequestQueryParameterTest_valueOf {

    private Map<String,String[]> parameterMap;

    @Before
    public void setUp() throws Exception {
        parameterMap = Maps.newHashMap();
    }
    
    @Test
    public void mapContainsList() {
        final QueryParameter<List<List<String>>> queryParameter = RestfulRequest.QueryParameter.FOLLOW_LINKS;
        parameterMap.put("x-ro-follow-links", new String[]{"a", "b.c"});
        List<List<String>> valueOf = queryParameter.valueOf(parameterMap);
        
        assertThat(valueOf.size(), is(2));
        assertThat(valueOf.get(0).size(), is(1));
        assertThat(valueOf.get(0).get(0), is("a"));
        assertThat(valueOf.get(1).size(), is(2));
        assertThat(valueOf.get(1).get(0), is("b"));
        assertThat(valueOf.get(1).get(1), is("c"));
    }

    @Test
    public void mapHasNoKey() {
        final QueryParameter<List<List<String>>> queryParameter = RestfulRequest.QueryParameter.FOLLOW_LINKS;
        parameterMap.put("something-else", new String[]{"a", "b.c"});
        List<List<String>> valueOf = queryParameter.valueOf(parameterMap);
        
        assertThat(valueOf.size(), is(0));
    }

    @Test
    public void mapIsEmpty() {
        final QueryParameter<List<List<String>>> queryParameter = RestfulRequest.QueryParameter.FOLLOW_LINKS;
        List<List<String>> valueOf = queryParameter.valueOf(parameterMap);
        
        assertThat(valueOf.size(), is(0));
    }

    @Test
    public void mapIsNull() {
        final QueryParameter<List<List<String>>> queryParameter = RestfulRequest.QueryParameter.FOLLOW_LINKS;
        List<List<String>> valueOf = queryParameter.valueOf(null);
        
        assertThat(valueOf.size(), is(0));
    }

    @Test
    public void mapContainsCommaSeparatedList() {
        
        final QueryParameter<List<List<String>>> queryParameter = RestfulRequest.QueryParameter.FOLLOW_LINKS;
        parameterMap.put("x-ro-follow-links", new String[]{"a,b.c"});
        List<List<String>> valueOf = queryParameter.valueOf(parameterMap);
        
        assertThat(valueOf.size(), is(2));
        assertThat(valueOf.get(0).size(), is(1));
        assertThat(valueOf.get(0).get(0), is("a"));
        assertThat(valueOf.get(1).size(), is(2));
        assertThat(valueOf.get(1).get(0), is("b"));
        assertThat(valueOf.get(1).get(1), is("c"));
    }



}

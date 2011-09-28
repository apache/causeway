package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import com.google.common.collect.Iterables;

public class ParserTest {

    @Test
    public void forBoolean() {
        for (Boolean v : new Boolean[] {Boolean.TRUE, Boolean.FALSE}) {
            assertRoundTrips(Parser.forBoolean(), v);
        }
    }

    @Test
    public void forString() {
        for (String v : new String[] {"", "foo", "foz"}) {
            assertRoundTrips(Parser.forString(), v);
        }
    }

    @Test
    public void forListOfStrings() {
        Parser<List<String>> parser = Parser.forListOfStrings();
        String[] strings = { "", "foo", "foz" };
        List<String> v = Arrays.asList(strings);
        final String asString = parser.asString(v);
        final List<String> valueOf = parser.valueOf(asString);
        
        assertThat(v, sameContentsAs(valueOf));
    }

    @Test
    public void forDate() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.MILLISECOND, 0);
        Date nowToNearestSecond = cal.getTime();
        for (Date v : new Date[] {nowToNearestSecond}) {
            assertRoundTrips(Parser.forDate(), v);
        }
    }

    @Test
    public void forInteger() {
        for (Integer v : new Integer[] {1, 2, 3, -5, -100, 0, Integer.MAX_VALUE, Integer.MIN_VALUE}) {
            assertRoundTrips(Parser.forInteger(), v);
        }
    }

    @Test
    public void forMediaType() {
        for (MediaType v : new MediaType[] { 
                MediaType.APPLICATION_ATOM_XML_TYPE, 
                MediaType.APPLICATION_JSON_TYPE, 
                MediaType.APPLICATION_XHTML_XML_TYPE, 
                MediaType.valueOf(RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT) }) {
            assertRoundTrips(Parser.forMediaType(), v);
        }
    }


    @Test
    public void forCacheControl() {
        final CacheControl cc1 = createCacheControl();
        cc1.setMaxAge(2000);
        final CacheControl cc2 = createCacheControl();
        cc2.setNoCache(true);
        for (CacheControl v : new CacheControl[] { cc1, cc2 }) {
            assertRoundTrips(Parser.forCacheControl(), v);
        }
    }

    private static <T> void assertRoundTrips(final Parser<T> parser, T v) {
        final String asString = parser.asString(v);
        final T valueOf = parser.valueOf(asString);
        assertThat(v, is(equalTo(valueOf)));
    }

    private static CacheControl createCacheControl() {
        final CacheControl cacheControl = new CacheControl();
        cacheControl.getCacheExtension(); // workaround for bug in CacheControl's equals() method
        cacheControl.getNoCacheFields(); // workaround for bug in CacheControl's equals() method
        return cacheControl;
    }

    public static <T> Matcher<List<T>> sameContentsAs(final List<T> expected) {
        return new TypeSafeMatcher<List<T>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("same sequence as " + expected);
            }

            @Override
            public boolean matchesSafely(List<T> actual) {
                return actual.containsAll(expected) && expected.containsAll(actual);
            }
        };
    }

}

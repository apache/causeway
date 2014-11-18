package org.apache.isis.applib.fixturescripts;

import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ExecutionContextTest {

    public static class AsKeyValueMap extends ExecutionContextTest {

        @Test
        public void happyCase() throws Exception {
            final Map<String, String> map = FixtureScript.ExecutionContext.asKeyValueMap("foo=bar\nbop=baz");
            assertThat(map.size(), is(2));

            assertThat(map.get("foo"), is("bar"));
            assertThat(map.get("bop"), is("baz"));
        }

        @Test
        public void givenNull() throws Exception {
            final Map<String, String> map = FixtureScript.ExecutionContext.asKeyValueMap(null);
            assertThat(map.size(), is(0));
        }

        @Test
        public void givenEmpty() throws Exception {
            final Map<String, String> map = FixtureScript.ExecutionContext.asKeyValueMap("");
            assertThat(map.size(), is(0));
        }

        @Test
        public void trim() throws Exception {

            final Map<String, String> map = FixtureScript.ExecutionContext.asKeyValueMap(" foo=bar\nbop=baz \n bip = bap ");
            assertThat(map.size(), is(3));

            assertThat(map.get("foo"), is("bar"));
            assertThat(map.get("bop"), is("baz"));
            assertThat(map.get("bip"), is("bap"));
        }

        @Test
        public void malformed() throws Exception {
            final Map<String, String> map = FixtureScript.ExecutionContext.asKeyValueMap("abcde");
            assertThat(map.size(), is(0));
        }

        @Test
        public void partiallyMalformed() throws Exception {
            final Map<String, String> map = FixtureScript.ExecutionContext.asKeyValueMap("foo=bar\nabcde\nbop=baz");
            assertThat(map.size(), is(2));

            assertThat(map.get("foo"), is("bar"));
            assertThat(map.get("bop"), is("baz"));
        }
    }


    public static class GetParameter extends ExecutionContextTest {

        @Test
        public void happyCase() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext("foo=bar\nbop=baz", null);

            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(2));

            assertThat(map.get("foo"), is("bar"));
            assertThat(map.get("bop"), is("baz"));

            assertThat(executionContext.getParameter("foo"), is("bar"));
            assertThat(executionContext.getParameter("bop"), is("baz"));

        }

        @Test
        public void givenNull() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext(null, null);
            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(0));

            assertThat(executionContext.getParameter("foo"), is(nullValue()));
        }

        @Test
        public void givenEmpty() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext("", null);
            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(0));

            assertThat(executionContext.getParameter("foo"), is(nullValue()));
        }

        @Test
        public void malformed() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext("abcde", null);
            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(0));

            assertThat(executionContext.getParameter("foo"), is(nullValue()));
        }

        @Test
        public void partiallyMalformed() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext("foo=bar\nabcde\nbop=baz", null);

            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(2));

            assertThat(map.get("foo"), is("bar"));
            assertThat(map.get("bop"), is("baz"));

            assertThat(executionContext.getParameter("foo"), is("bar"));
            assertThat(executionContext.getParameter("bop"), is("baz"));
        }

        @Test
        public void trim() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext(" foo=bar\nbop=baz \n bip = bap ", null);

            final Map<String, String> map = executionContext.getParameterMap();
            assertThat(map.size(), is(3));

            assertThat(map.get("foo"), is("bar"));
            assertThat(map.get("bop"), is("baz"));
            assertThat(map.get("bip"), is("bap"));

            assertThat(executionContext.getParameter("foo"), is("bar"));
            assertThat(executionContext.getParameter("bop"), is("baz"));
            assertThat(executionContext.getParameter("bip"), is("bap"));
        }

    }
    public static class SetParameterIfNotPresent extends ExecutionContextTest {

        @Test
        public void whenNotPresent() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext(null, null);
            executionContext.setParameterIfNotPresent("foo", "bar");

            assertThat(executionContext.getParameter("foo"), is("bar"));
        }

        @Test
        public void whenPresent() throws Exception {
            final FixtureScript.ExecutionContext executionContext = new FixtureScript.ExecutionContext("foo=bop\n", null);
            executionContext.setParameterIfNotPresent("foo", "bar");

            assertThat(executionContext.getParameter("foo"), is("bop"));
        }

    }

    public static class RoundUp extends ExecutionContextTest {

        @Test
        public void happyCase() throws Exception {
            Assert.assertThat(FixtureScript.ExecutionContext.roundup(5, 20), Matchers.is(20));
            Assert.assertThat(FixtureScript.ExecutionContext.roundup(19, 20), Matchers.is(20));
            Assert.assertThat(FixtureScript.ExecutionContext.roundup(20, 20), Matchers.is(40));
            Assert.assertThat(FixtureScript.ExecutionContext.roundup(21, 20), Matchers.is(40));
            Assert.assertThat(FixtureScript.ExecutionContext.roundup(39, 20), Matchers.is(40));
            Assert.assertThat(FixtureScript.ExecutionContext.roundup(40, 20), Matchers.is(60));
        }

    }

}
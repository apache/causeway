package org.apache.isis.applib.fixturescripts;

import java.util.Map;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExecutionParameters_AsKeyValueMap_Test extends ExecutionContextTest {

    @Test
    public void happyCase() throws Exception {
        final Map<String, String> map = ExecutionParameters.asKeyValueMap("foo=bar\nbop=baz");
        assertThat(map.size(), is(2));

        assertThat(map.get("foo"), is("bar"));
        assertThat(map.get("bop"), is("baz"));
    }

    @Test
    public void givenNull() throws Exception {
        final Map<String, String> map = ExecutionParameters.asKeyValueMap(null);
        assertThat(map.size(), is(0));
    }

    @Test
    public void givenEmpty() throws Exception {
        final Map<String, String> map = ExecutionParameters.asKeyValueMap("");
        assertThat(map.size(), is(0));
    }

    @Test
    public void trim() throws Exception {

        final Map<String, String> map = ExecutionParameters.asKeyValueMap(" foo=bar\nbop=baz \n bip = bap ");
        assertThat(map.size(), is(3));

        assertThat(map.get("foo"), is("bar"));
        assertThat(map.get("bop"), is("baz"));
        assertThat(map.get("bip"), is("bap"));
    }

    @Test
    public void malformed() throws Exception {
        final Map<String, String> map = ExecutionParameters.asKeyValueMap("abcde");
        assertThat(map.size(), is(0));
    }

    @Test
    public void partiallyMalformed() throws Exception {
        final Map<String, String> map = ExecutionParameters.asKeyValueMap("foo=bar\nabcde\nbop=baz");
        assertThat(map.size(), is(2));

        assertThat(map.get("foo"), is("bar"));
        assertThat(map.get("bop"), is("baz"));
    }
}

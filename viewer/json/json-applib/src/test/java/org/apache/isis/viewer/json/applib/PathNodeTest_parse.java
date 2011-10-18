package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;

public class PathNodeTest_parse {


    @Test
    public void simple() throws Exception {
        final PathNode node = PathNode.parse("foo");
        assertThat(node.getKey(), is("foo"));
        assertThat(node.getCriteria().isEmpty(), is(true));
    }

    @Test
    public void oneCriterium() throws Exception {
        final PathNode node = PathNode.parse("foo[bar=coz]");
        assertThat(node.getKey(), is("foo"));
        final Map<String, String> criteria = node.getCriteria();
        assertThat(criteria.isEmpty(), is(false));
        assertThat(criteria.size(), is(1));
        assertThat(criteria.get("bar"), is("coz"));
    }

    @Test
    public void moreThanOneCriterium() throws Exception {
        final PathNode node = PathNode.parse("foo[bar=coz dat=ein]");
        assertThat(node.getKey(), is("foo"));
        final Map<String, String> criteria = node.getCriteria();
        assertThat(criteria.isEmpty(), is(false));
        assertThat(criteria.size(), is(2));
        assertThat(criteria.get("bar"), is("coz"));
        assertThat(criteria.get("dat"), is("ein"));
    }

    @Test
    public void whiteSpace() throws Exception {
        final PathNode node = PathNode.parse("foo[bar=coz\tdat=ein]");
        assertThat(node.getKey(), is("foo"));
        final Map<String, String> criteria = node.getCriteria();
        assertThat(criteria.isEmpty(), is(false));
        assertThat(criteria.size(), is(2));
        assertThat(criteria.get("bar"), is("coz"));
        assertThat(criteria.get("dat"), is("ein"));
    }


}

package org.apache.isis.viewer.json.applib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class PathNodeTest_equalsHashcode {


    @Test
    public void simple() throws Exception {
        final PathNode node = PathNode.parse("foo");
        final PathNode node2 = PathNode.parse("foo");
        assertEquals(node, node2);
    }

    @Test
    public void oneCriterium() throws Exception {
        final PathNode node = PathNode.parse("a[b=c]");
        final PathNode node2 = PathNode.parse("a");
        assertEquals(node, node2);
    }

    @Test
    public void moreThanOneCriterium() throws Exception {
        final PathNode node = PathNode.parse("a[b=c d=e]");
        final PathNode node2 = PathNode.parse("a");
        assertEquals(node, node2);
    }

    @Test
    public void notEqual() throws Exception {
        final PathNode node = PathNode.parse("a[b=c d=e]");
        final PathNode node2 = PathNode.parse("b");
        assertFalse(node.equals(node2));
    }

}

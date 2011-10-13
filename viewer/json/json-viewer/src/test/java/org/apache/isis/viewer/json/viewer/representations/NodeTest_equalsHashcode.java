package org.apache.isis.viewer.json.viewer.representations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class NodeTest_equalsHashcode {


    @Test
    public void simple() throws Exception {
        final Node node = Node.parse("foo");
        final Node node2 = Node.parse("foo");
        assertEquals(node, node2);
    }

    @Test
    public void oneCriterium() throws Exception {
        final Node node = Node.parse("a[b=c]");
        final Node node2 = Node.parse("a");
        assertEquals(node, node2);
    }

    @Test
    public void moreThanOneCriterium() throws Exception {
        final Node node = Node.parse("a[b=c d=e]");
        final Node node2 = Node.parse("a");
        assertEquals(node, node2);
    }

    @Test
    public void notEqual() throws Exception {
        final Node node = Node.parse("a[b=c d=e]");
        final Node node2 = Node.parse("b");
        assertFalse(node.equals(node2));
    }

}

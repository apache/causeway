package org.apache.isis.viewer.json.viewer.representations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.apache.isis.viewer.json.applib.Parser;
import org.apache.isis.viewer.json.applib.PathNode;
import org.junit.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GraphTest_asGraph {

    @Test
    public void simple() throws Exception {
        List<List<String>> links = asListOfLists("a.b.c,a.b.d,d.b,e,e");
        final Map<PathNode, Map> root = GraphUtil.asGraph(links);
        
        assertThat(root.size(), is(3));
        Map<String,Map> nodeA = root.get(PathNode.parse("a"));
        assertThat(nodeA.size(), is(1));
        Map<String,Map> nodeAB = nodeA.get(PathNode.parse("b"));
        assertThat(nodeAB.size(), is(2));
        Map<String,Map> nodeABC = nodeAB.get(PathNode.parse("c"));
        assertThat(nodeABC.size(), is(0));
        Map<String,Map> nodeABD = nodeAB.get(PathNode.parse("d"));
        assertThat(nodeABD.size(), is(0));
        
        Map<String,Map> nodeD = root.get(PathNode.parse("d"));
        assertThat(nodeD.size(), is(1));
        Map<String,Map> nodeDB = nodeD.get(PathNode.parse("b"));
        assertThat(nodeDB.size(), is(0));
        
        Map<String,Map> nodeE = root.get(PathNode.parse("e"));
        assertThat(nodeE.size(), is(0));
    }

    @Test
    public void empty() throws Exception {
        List<List<String>> links = asListOfLists("");
        final Map<PathNode, Map> root = GraphUtil.asGraph(links);
        
        assertThat(root.size(), is(0));
    }

    @Test
    public void whenNull() throws Exception {
        final Map<PathNode, Map> root = GraphUtil.asGraph(null);
        
        assertThat(root.size(), is(0));
    }

    private List<List<String>> asListOfLists(String string) {
        return Parser.forListOfListOfStrings().valueOf(string);
    }
}

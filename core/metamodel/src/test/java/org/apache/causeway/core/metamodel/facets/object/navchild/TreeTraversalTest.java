/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.core.metamodel.facets.object.navchild;

import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.navchild._TreeSample.A;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;

class TreeTraversalTest
extends FacetFactoryTestAbstract {

    private MetaModelContext mmc;
    private ObjectTreeAdapter treeAdapter;
    private A a;
    private TreeNode<Object> tree;

    @BeforeEach
    void setUp() {
        mmc = MetaModelContext_forTesting.builder()
                .enablePostprocessors(true)
                .build();
        treeAdapter = new ObjectTreeAdapter(mmc.getSpecificationLoader());
        // sample tree, that we then traverse
        a = _TreeSample.sampleA();
        tree = TreeNode.root(a, treeAdapter);

        //TODO[causeway-core-metamodel-CAUSEWAY-2297] ObjectSpecification#streamAssociations seems to have the side effect
        // of initializing the various members with the NavigableSubtreeSequenceFacet,
        // which otherwise does not happen (is this test specific? if so then good, but we should fix that)
        var specs = Can.of(_TreeSample.A.class, _TreeSample.B.class, _TreeSample.C.class, _TreeSample.D.class)
            .map(mmc.getSpecificationLoader()::specForType)
            .map(opt->opt.orElse(null));
        specs.forEach(spec->spec.streamAssociations(MixedIn.EXCLUDED).forEach(assoc->{}));
    }

    @Test
    void preconditions() {
        var specLoader = mmc.getSpecificationLoader();
        var specA = specLoader.loadSpecification(_TreeSample.A.class);
        // including this one to test presence of NavigableSubtreeFacet w/o the side-effect of calling the getAssociation method
        var specB = specLoader.loadSpecification(_TreeSample.B.class);

        // first: members must have the NavigableSubtreeSequenceFacet
        var assocAB = specA.getAssociationElseFail("childrenB");
        assertTrue(assocAB.isCollection());
        assertTrue(assocAB.containsFacet(NavigableSubtreeSequenceFacet.class));

        // java map support
        var assocAC = specA.getAssociationElseFail("childrenC");
        assertTrue(assocAC.isCollection());
        assertTrue(assocAC.containsFacet(NavigableSubtreeSequenceFacet.class));
        assertEquals(_TreeSample.C.class, assocAC.getElementType().getCorrespondingClass());

        // second: post-processor should generate NavigableSubtreeFacet
        assertTrue(specA.containsFacet(NavigableSubtreeFacet.class));
        assertTrue(specB.containsFacet(NavigableSubtreeFacet.class));

        // tree sanity checks
        assertEquals(a, tree.value());
        assertNotNull(tree.rootNode());
        assertEquals(tree, tree.rootNode());
        assertTrue(tree.isRoot());
        assertFalse(tree.isLeaf());

        // node a is expected to have 4 children
        var navigableSubtreeFacet = specA.getFacet(NavigableSubtreeFacet.class);
        assertEquals(4, navigableSubtreeFacet.childCountOf(a));
        assertEquals(4, navigableSubtreeFacet.childrenOf(a).toList().size());
        assertEquals(4, treeAdapter.childCountOf(a));
        assertEquals(4, treeAdapter.childrenOf(a).toList().size());
        assertEquals(4, tree.childCount());
        assertEquals(4, tree.streamChildren().toList().size());

        var firstChildOfA = treeAdapter.childrenOf(a).findFirst().orElseThrow();

        // first child node of a is expected to have 3 children
        assertEquals(3, treeAdapter.childCountOf(firstChildOfA));
        assertEquals(3, treeAdapter.childrenOf(firstChildOfA).toList().size());

        //TODO[causeway-core-metamodel-CAUSEWAY-2297] add property support
        // count all nodes
        assertEquals(17, Can.ofIterable(tree::iteratorDepthFirst).size());
        assertEquals(17, Can.ofIterable(tree::iteratorBreadthFirst).size());
    }

    @Test
    void depthFirstTraversal() {
        var nodeNames = tree.streamDepthFirst()
            .map(TreeNode::value)
            .map(_TreeSample::nameOf)
            .collect(Collectors.joining(", "));

        assertEquals(
                "a, b1, d1, d2, d3, b2, d1, d2, d3, c1, d1, d2, d3, c2, d1, d2, d3",
                nodeNames);
    }

    @Test
    void leafToRootTraversal() {
        // pick an arbitrary leaf value,
        // from which we then traverse up to the root
        var d = a.childrenB().getFirstElseFail().childrenD().getLastElseFail();

        // find d's node
        var leafNode = tree.streamDepthFirst()
                .filter((final TreeNode<Object> treeNode)->d.equals(treeNode.value()))
                .findFirst()
                .orElseThrow();

        var nodeNames = leafNode.streamHierarchyUp()
            .map(TreeNode::value)
            .map(_TreeSample::nameOf)
            .collect(Collectors.joining(", "));

        assertEquals(
                "d3, b1, a",
                nodeNames);
    }

}


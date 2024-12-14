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
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;

//TODO[causeway-core-metamodel-CAUSEWAY-2297] WIP
class TreeTraversalTest
extends FacetFactoryTestAbstract {

    MetaModelContext mmc;
    ObjectTreeAdapter treeAdapter;

    @BeforeEach
    void setUp() {
        mmc = MetaModelContext_forTesting.buildDefault();
        treeAdapter = new ObjectTreeAdapter(mmc.getSpecificationLoader());
    }

    //@Test
    void preconditions() {
        var specLoader = mmc.getSpecificationLoader();
        var specA = specLoader.loadSpecification(_TreeSample.A.class);
        var assocAB = specA.getAssociationElseFail("childrenB");

        System.err.printf("assocA %s%n", assocAB.streamFacets().collect(Can.toCan()).join("\n"));
    }

    //@Test
    void depthFirstTraversal() {
        // instantiate a tree, that we later traverse
        var a = _TreeSample.sampleA();

        // traverse the tree
        var tree = TreeNode.root(a, treeAdapter);

        var nodeNames = tree.streamDepthFirst()
            .map(TreeNode::value)
            .map(_TreeSample::nameOf)
            .collect(Collectors.joining(", "));

        assertEquals(
                "a, b1, d1, d2, d3, b2, d1, d2, d3, c1, d1, d2, d3, c2, d1, d2, d3",
                nodeNames);
    }

    //@Test
    void leafToRootTraversal() {
        // instantiate a tree and pick an arbitrary leaf value,
        // from which we later traverse up to the root
        var a = _TreeSample.sampleA();
        var d = a.childrenB().getFirstElseFail().childrenD().getLastElseFail();

        // traverse the tree
        var tree = TreeNode.root(a, treeAdapter);

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


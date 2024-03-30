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
package org.apache.causeway.applib.graph.tree;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @since 2.0 {@index}
 */
public class TreeState implements Serializable {

    // -- FACTORIES
    
    public static TreeState rootCollapsed() {
        return new TreeState();
    }

    // -- CONSTRUCTION
    
    private static final long serialVersionUID = 7971539034663543462L;

    private final Set<TreePath> expandedNodes = new HashSet<>();
    private final Set<TreePath> selectedNodes = new HashSet<>();

    public Set<TreePath> getExpandedNodePaths() {
        return expandedNodes;
    }

    public Set<TreePath> getSelectedNodePaths() {
        return selectedNodes;
    }

}

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
package org.apache.causeway.viewer.wicket.ui.components.tree;

import java.util.Objects;

import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;

/**
 * Extending the UiObjectWkt to also provide a TreePath.
 */
class _TreeModel extends UiObjectWkt {
    private static final long serialVersionUID = 8916044984628849300L;

    private final TreePath treePath;
    private final boolean isTreePathModelOnly;

    public _TreeModel(final MetaModelContext commonContext, final TreePath treePath) {
        super(commonContext, commonContext.getObjectManager().adapt(0)); // any bookmarkable will do
        this.treePath = treePath;
        this.isTreePathModelOnly = true;
    }

    public _TreeModel(final MetaModelContext commonContext, final ManagedObject adapter, final TreePath treePath) {
        super(commonContext, Objects.requireNonNull(adapter));
        this.treePath = treePath;
        this.isTreePathModelOnly = false;
    }

    public TreePath getTreePath() {
        return treePath;
    }

    public boolean isTreePathModelOnly() {
        return isTreePathModelOnly;
    }

}
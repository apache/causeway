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

import java.io.Serializable;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.applib.graph.tree.TreeAdapterWithConverter;
import org.apache.causeway.applib.graph.tree.TreeConverter;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;

/**
 *  {@link TreeAdapter} for _TreeModel nodes.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class _TreeModelTreeAdapter
extends TreeAdapterWithConverter<Object, _TreeNodeMemento>
implements
    TreeAdapter<_TreeNodeMemento>,
    TreeConverter<Object, _TreeNodeMemento>,
    HasMetaModelContext,
    Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<? extends TreeAdapter> delegateClass;

    /** non serializable delegate */
    private transient TreeAdapter delegate;

    _TreeModelTreeAdapter(
            final TreeAdapter delegate) {
        this.delegate = delegate;
        this.delegateClass = delegate.getClass();
    }
    
    // -- TREE CONVERTER
    
    @Override
    public _TreeNodeMemento fromUnderlyingNode(
            final Object pojoNode, final _TreeNodeMemento parentNode, final int siblingIndex) {
        return mementify(pojoNode, parentNode.getTreePath().append(siblingIndex));
    }

    @Override
    public @Nullable Object toUnderlyingNode(_TreeNodeMemento node) {
        return node!=null
                ? node.getPojo()
                : null;
    }
    
    // -- TREE ADAPTER WITH CONVERTER
    
    @Override
    protected TreeConverter<Object, _TreeNodeMemento> converter() {
        return this;
    }
    
    @Override
    protected TreeAdapter<Object> underlyingAdapter() {
        if(delegate!=null) {
            return delegate;
        }
        try {
            return getFactoryService().getOrCreate(delegateClass);
        } catch (Exception e) {
            throw _Exceptions.unrecoverable(e, "failed to instantiate tree adapter of type %s", 
                    delegateClass.getName());
        }
    }
    
    // -- HELPER
    
    _TreeNodeMemento mementify(final Object pojo, final TreePath treePath) {
        return new _TreeNodeMemento(
                treePath,
                ManagedObject.adaptSingular(getSpecificationLoader(), pojo)
                    .getBookmark()
                    .orElseThrow());
    }

}
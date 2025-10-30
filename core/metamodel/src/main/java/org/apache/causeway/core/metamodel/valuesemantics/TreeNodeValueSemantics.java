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
package org.apache.causeway.core.metamodel.valuesemantics;

import java.util.Objects;
import java.util.stream.Stream;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.applib.graph.tree.TreeState;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.bookmark.HmacAuthority;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.urlencoding.UrlEncodingService;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.util.hmac.HmacUrlCodec;
import org.apache.causeway.core.metamodel.util.hmac.Memento;
import org.apache.causeway.core.metamodel.util.hmac.MementoHmacContext;
import org.apache.causeway.schema.common.v2.ValueType;

@Component
@Named("causeway.metamodel.value.TreeNodeValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class TreeNodeValueSemantics
extends ValueSemanticsAbstract<TreeNode<?>>
implements
    Renderer<TreeNode<?>> {

    private final FactoryService factoryService;
    private final MementoHmacContext mementoContext;

    @Inject
    public TreeNodeValueSemantics(
        final HmacAuthority hmacAuthority,
        final UrlEncodingService urlEncodingService,
        final FactoryService factoryService,
        final BookmarkService bookmarkService,
        final Provider<ValueSemanticsResolver> valueSemanticsResolverProvider,
        final ValueCodec valueCodec) {

        Objects.requireNonNull(hmacAuthority);
        Objects.requireNonNull(urlEncodingService);
        Objects.requireNonNull(bookmarkService);
        Objects.requireNonNull(valueSemanticsResolverProvider);
        this.factoryService = Objects.requireNonNull(factoryService);

        this.mementoContext = new MementoHmacContext(
            new HmacUrlCodec(hmacAuthority, urlEncodingService), valueCodec);
    }

    @Override
    public Class<TreeNode<?>> getCorrespondingClass() {
        return _Casts.uncheckedCast(TreeNode.class);
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final TreeNode<?> value) {
        return decomposeAsString(value, this::toEncodedString, ()->null);
    }

    @Override
    public TreeNode<?> compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, this::fromEncodedString, ()->null);
    }

    private String toEncodedString(final TreeNode<?> treeNode) {
        final Memento memento = mementoContext.newMemento();
        memento.put("rootValue", treeNode.rootValue());
        memento.put("adapterClass", treeNode.treeAdapter().getClass());
        memento.put("treeState", treeNode.treeState());
        memento.put("treePath", treeNode.treePath());
        return memento.toExternalForm();
    }

    @SuppressWarnings("unchecked")
    private TreeNode<?> fromEncodedString(final String input) {
        final Memento memento = mementoContext.parseDigitallySignedMemento(input);
        final TreeNode<?> rootNode = TreeNode.root(
                memento.get("rootValue", Object.class),
                memento.get("adapterClass", Class.class),
                memento.get("treeState", TreeState.class),
                factoryService);
        return rootNode.resolve(memento.get("treePath", TreePath.class))
                .orElse(null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final TreeNode<?> value) {
        return renderTitle(value, TreeNode::toString);
    }

    @Override
    public String htmlPresentation(final Context context, final TreeNode<?> value) {
        return renderHtml(value, TreeNode::toString);
    }

    // -- EXAMPLES

    @Override
    public Can<TreeNode<?>> getExamples() {

        class TreeAdapterString implements TreeAdapter<String> {
            @Override public int childCountOf(final String value) {
                return 0; }
            @Override public Stream<String> childrenOf(final String value) {
                return Stream.empty(); }
        }

        return Can.of(
                TreeNode.root("TreeRoot", new TreeAdapterString(), TreeState.rootCollapsed()),
                TreeNode.root("another TreeRoot", new TreeAdapterString(), TreeState.rootCollapsed()));
    }

}

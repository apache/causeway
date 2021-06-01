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

package org.apache.isis.core.metamodel.facets.value.treenode;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.graph.tree.TreeNode;
import org.apache.isis.applib.graph.tree.TreeState;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.Memento;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;

@SuppressWarnings("rawtypes")
public class TreeNodeValueSemanticsProvider extends ValueSemanticsProviderAndFacetAbstract<TreeNode>
implements TreeNodeValueFacet {

    private static final int TYPICAL_LENGTH = 0;

    private static Class<? extends Facet> type() {
        return TreeNodeValueFacet.class;
    }

    private static final TreeNode DEFAULT_VALUE = null;
    private static final Class<TreeNode> VALUE_TYPE = TreeNode.class;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public TreeNodeValueSemanticsProvider() {
        this(null);
    }

    public TreeNodeValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, VALUE_TYPE, TYPICAL_LENGTH, -1, Immutability.IMMUTABLE,
                EqualByContent.NOT_HONOURED, DEFAULT_VALUE);
    }

    @Override
    public String titleString(final Object object) {
        return object != null ? ((TreeNode<?>)object).toString() : "[null]"; //TODO implement
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(value);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    public Parser<TreeNode> getParser() {
        return null;
    }

    // //////////////////////////////////////////////////////////////////
    // DefaultsProvider
    // //////////////////////////////////////////////////////////////////

    @Override
    public DefaultsProvider<TreeNode> getDefaultsProvider() {
        return null;
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final TreeNode treeNode) {

        final Memento memento = newMemento();
        memento.put("primaryValue", treeNode.getValue());
        memento.put("adapterClass", treeNode.getTreeAdapterClass());
        memento.put("treeState", treeNode.getTreeState());
        return memento.asString();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TreeNode<?> doRestore(final String input) {
        final Memento memento = parseMemento(input);
        return TreeNode.of(
                memento.get("primaryValue", Object.class),
                memento.get("adapterClass", Class.class),
                memento.get("treeState", TreeState.class));
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "TreeNodeValueSemanticsProvider";
    }

    // -- HELPER

    private _Mementos.Memento newMemento(){
        final UrlEncodingService codec = getServiceRegistry().lookupServiceElseFail(UrlEncodingService.class);
        final SerializingAdapter serializer = getServiceRegistry().lookupServiceElseFail(SerializingAdapter.class);
        return _Mementos.create(codec, serializer);
    }

    private _Mementos.Memento parseMemento(String input){
        final UrlEncodingService codec = getServiceRegistry().lookupServiceElseFail(UrlEncodingService.class);
        final SerializingAdapter serializer = getServiceRegistry().lookupServiceElseFail(SerializingAdapter.class);
        return _Mementos.parse(codec, serializer, input);
    }

}

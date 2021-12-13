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
package org.apache.isis.core.metamodel.valuesemantics;

import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.graph.tree.TreeAdapter;
import org.apache.isis.applib.graph.tree.TreeNode;
import org.apache.isis.applib.graph.tree.TreeState;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.Memento;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.TreeNodeValueSemantics")
public class TreeNodeValueSemantics
extends ValueSemanticsAbstract<TreeNode<?>>
implements
    EncoderDecoder<TreeNode<?>>,
    Renderer<TreeNode<?>> {

    @Inject UrlEncodingService urlEncodingService;
    @Inject SerializingAdapter serializingAdapter;

    @Override
    public Class<TreeNode<?>> getCorrespondingClass() {
        return _Casts.uncheckedCast(TreeNode.class);
    }

    @Override
    public ValueType getSchemaValueType() {
        return UNREPRESENTED;
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final Context context, final TreeNode<?> value) {
        return super.render(value, TreeNode::toString);
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final TreeNode<?> treeNode) {

        final Memento memento = newMemento();
        memento.put("primaryValue", treeNode.getValue());
        memento.put("adapterClass", treeNode.getTreeAdapterClass());
        memento.put("treeState", treeNode.getTreeState());
        return memento.asString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public TreeNode<?> fromEncodedString(final String input) {
        final Memento memento = parseMemento(input);
        return TreeNode.of(
                memento.get("primaryValue", Object.class),
                memento.get("adapterClass", Class.class),
                memento.get("treeState", TreeState.class));
    }

    // -- EXAMPLES

    @Override
    public Can<TreeNode<?>> getExamples() {

        class TreeAdapterString implements TreeAdapter<String> {
            @Override public Optional<String> parentOf(final String value) {
                return null; }
            @Override public int childCountOf(final String value) {
                return 0; }
            @Override public Stream<String> childrenOf(final String value) {
                return Stream.empty(); }
        }

        return Can.of(
                TreeNode.of("TreeRoot", TreeAdapterString.class, TreeState.rootCollapsed()),
                TreeNode.of("another TreeRoot", TreeAdapterString.class, TreeState.rootCollapsed()));
    }

    // -- HELPER

    private _Mementos.Memento newMemento(){
        return _Mementos.create(urlEncodingService, serializingAdapter);
    }

    private _Mementos.Memento parseMemento(final String input){
        return _Mementos.parse(urlEncodingService, serializingAdapter, input);
    }

}

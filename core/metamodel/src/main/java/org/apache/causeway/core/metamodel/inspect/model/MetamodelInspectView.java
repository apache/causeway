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
package org.apache.causeway.core.metamodel.inspect.model;

import jakarta.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

@Named(CausewayModuleApplib.NAMESPACE + ".MetamodelInspectView")
@DomainObject(
    nature=Nature.VIEW_MODEL,
    editing = Editing.DISABLED,
    introspection = Introspection.ENCAPSULATION_ENABLED)
public class MetamodelInspectView extends TreeNodeVm<MMNode, MetamodelInspectView> {

    public static MetamodelInspectView root(final ObjectSpecification spec) {
        return new MetamodelInspectView(new TypeNode(spec.logicalTypeName()), TreePath.root());
    }

    private final Memento memento;

    public MetamodelInspectView(final String mementoString) {
        this(Memento.parse(mementoString));
    }

    MetamodelInspectView(final Memento memento) {
        super(MMNode.class, memento.root(), memento.treePath);
        this.memento = memento;
    }

    MetamodelInspectView(final TypeNode rootNode, final TreePath activeTreePath) {
        super(MMNode.class, rootNode, activeTreePath);
        this.memento = new Memento(rootNode.logicalName(), activeTreePath);
    }

    @ObjectSupport
    public String title() {
        return activeNode.title();
    }

    @ObjectSupport
    public String iconName() {
        return activeNode.getClass().getSimpleName()
            + _Strings.nonEmpty(activeNode.iconName())
                .map(suffix-> "-" + suffix)
                .orElse("");
    }

    @Override
    public String viewModelMemento() {
        return memento.stringify();
    }

    @Override
    protected MetamodelInspectView getViewModel(final MMNode node, final MetamodelInspectView parentNode, final int siblingIndex) {
        return new MetamodelInspectView((TypeNode)rootNode,
            parentNode!=null
                ? parentNode.activeTreePath.append(siblingIndex)
                : TreePath.root());
    }

    @Override
    protected TreeAdapter<MMNode> getTreeAdapter() {
        return new MMTreeAdapter();
    }

    private record Memento (
        String logicalName,
        TreePath treePath) {

        static Memento empty() {
            return new Memento(Void.class.getName(), TreePath.root());
        }

        public MMNode root() {
            return new TypeNode(logicalName);
        }

        static Memento parse(final String stringified) {
            return _Strings.splitThenApplyRequireNonEmpty(stringified, "'",
                (lhs, rhs)->new Memento(lhs, TreePath.parse(rhs, ".")))
                .orElseGet(Memento::empty);
        }

        String stringify() {
            return logicalName + "'" + treePath.stringify(".");
        }
    }

}

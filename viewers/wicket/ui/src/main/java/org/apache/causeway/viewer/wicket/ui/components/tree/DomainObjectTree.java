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
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.tree.TreeNodeMemento;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.components.object.icontitle.ObjectIconAndTitlePanelFactory;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * Wicket's tree component implemented for bookmarkable nodes,
 * where the tree is modeled by a root {@link TreeNode} (and its child-nodes recursively).
 */
class DomainObjectTree extends NestedTree<TreeNodeMemento>
implements HasMetaModelContext {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    static MarkupContainer emptyTreeComponent(final String id) {
        return new WebMarkupContainer(id);
    }

    /**
     * @param rootModel - ManagedObject representing a {@link TreeNode}
     */
    static MarkupContainer createComponent(final String id, final IModel<ManagedObject> rootModel) {
        return rootModel==null
                || ManagedObjects.isNullOrUnspecifiedOrEmpty(rootModel.getObject())
            ? emptyTreeComponent(id)
            : new DomainObjectTree(id, (TreeNode<?>)rootModel.getObject().getPojo());
    }

    // -- CONSTRUCTION

    private DomainObjectTree(
            final String id,
            final TreeNode<?> treeNode) {
        super(id, new TreeProvider(treeNode), TreeExpansionModel.of(treeNode.treeState()));
    }

    /**
     * To use a custom component for the representation of a node's content we override this method.
     */
    @Override
    protected Component newContentComponent(final String id, final IModel<TreeNodeMemento> node) {
        final TreeNodeMemento treeModel = node.getObject();
        final Component entityIconAndTitle = ObjectIconAndTitlePanelFactory.entityIconAndTitlePanel(
                id, UiObjectWkt.ofBookmark(treeModel.bookmark()));
        if(treeExpansionModel().isSelected(treeModel.treePath())) {
            Wkt.cssAppend(entityIconAndTitle, "tree-node-selected");
        }
        return entityIconAndTitle;
    }

    /**
     * To hardcode Node's <pre>AjaxFallbackLink.isEnabledInHierarchy()->true</pre> we override this method.
     */
    @Override
    public Component newNodeComponent(final String id, final IModel<TreeNodeMemento> model) {

        final Node<TreeNodeMemento> node =  new Node<TreeNodeMemento>(id, this, model) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Component createContent(final String id, final IModel<TreeNodeMemento> model) {
                return DomainObjectTree.this.newContentComponent(id, model);
            }

            @Override
            protected MarkupContainer createJunctionComponent(final String id) {

                final Node<TreeNodeMemento> node = this;
                final Runnable toggleExpandCollapse = (Runnable & Serializable) this::toggle;

                return new AjaxFallbackLink<Void>(id) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(final Optional<AjaxRequestTarget> target) {
                        toggleExpandCollapse.run();
                    }

                    @Override
                    public boolean isEnabled() {
                        return DomainObjectTree.this.getProvider().hasChildren(node.getModelObject());
                    }

                    @Override
                    public boolean isEnabledInHierarchy() {
                        return true; // hardcoded -> true
                    }
                };
            }
        };

        node.setOutputMarkupId(true);
        return node;
    }

    /**
     * To utilize the custom TreeExpansionModel for deciding a node's collapse/expand state,
     * we override this method.
     */
    @Override
    public State getState(final TreeNodeMemento t) {
        return treeExpansionModel().contains(t.treePath()) ? State.EXPANDED : State.COLLAPSED;
    }

    /**
     * To utilize the custom TreeExpansionModel for hooking into a node's expand event,
     * we override this method.
     */
    @Override
    public void expand(final TreeNodeMemento t) {
        treeExpansionModel().onExpand(t);
        super.expand(t);
    }

    /**
     * To utilize the custom TreeExpansionModel for hooking into a node's collapse event,
     * we override this method.
     */
    @Override
    public void collapse(final TreeNodeMemento t) {
        treeExpansionModel().onCollapse(t);
        super.collapse(t);
    }

    private TreeExpansionModel treeExpansionModel() {
        return (TreeExpansionModel) getModel();
    }

}
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.tree;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.tree.TreeAdapter;
import org.apache.isis.applib.tree.TreeNode;
import org.apache.isis.applib.tree.TreePath;
import org.apache.isis.applib.tree.TreeState;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.functions._Functions;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.components.entity.icontitle.EntityIconAndTitlePanel;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.val;

class IsisToWicketTreeAdapter {

    public static Component adapt(String id, ValueModel valueModel) {
        if(valueModel==null || valueModel.getObject()==null) {
            return emptyTreeComponent(id);
        }
        return new EntityTree(id, toITreeProvider(valueModel), 
                toIModelRepresentingCollapseExpandState(valueModel));
    }

    public static Component adapt(String id, ScalarModel scalarModel) {
        if(scalarModel==null || scalarModel.getObject()==null) {
            return emptyTreeComponent(id);
        }
        return new EntityTree(id, toITreeProvider(scalarModel), 
                toIModelRepresentingCollapseExpandState(scalarModel));
    }
    
    // -- FALLBACK
    
    private static Component emptyTreeComponent(String id) {
        return new Label(id);
    }

    // -- RENDERING

    /**
     * Wicket's Tree Component implemented for Isis
     */
    private static class EntityTree extends NestedTree<TreeModel> {

        private static final long serialVersionUID = 1L;

        public EntityTree(
                String id,
                ITreeProvider<TreeModel> provider,
                TreeExpansionModel collapseExpandState) {
            super(id, provider, collapseExpandState);
        }

        /**
         * To use a custom component for the representation of a node's content we override this method.
         */
        @Override
        protected Component newContentComponent(String id, IModel<TreeModel> node) {
            final TreeModel treeModel = node.getObject();
            final Component entityIconAndTitle = new EntityIconAndTitlePanel(id, treeModel);
            return entityIconAndTitle;
        }

        /**
         * To hardcode Node's <pre>AjaxFallbackLink.isEnabledInHierarchy()->true</pre> we override this method.
         */
        @Override
        public Component newNodeComponent(String id, IModel<TreeModel> model) {

            final Node<TreeModel> node =  new Node<TreeModel>(id, this, model) {
                private static final long serialVersionUID = 1L;

                @Override
                protected Component createContent(String id, IModel<TreeModel> model) {
                    return EntityTree.this.newContentComponent(id, model);
                }

                @Override
                protected MarkupContainer createJunctionComponent(String id) {

                    final Node<TreeModel> node = this;
                    final Runnable toggleExpandCollapse = (Runnable & Serializable) this::toggle;

                    return new AjaxFallbackLink<Void>(id) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClick(Optional<AjaxRequestTarget> target) {
                            toggleExpandCollapse.run();
                        }

                        @Override
                        public boolean isEnabled() {
                            return EntityTree.this.getProvider().hasChildren(node.getModelObject());
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
        public State getState(TreeModel t) {
            final TreeExpansionModel treeExpansionModel = (TreeExpansionModel) getModel();
            return treeExpansionModel.contains(t.getTreePath()) ? State.EXPANDED : State.COLLAPSED;
        }

        /**
         * To utilize the custom TreeExpansionModel for hooking into a node's expand event,
         * we override this method.
         */
        @Override
        public void expand(TreeModel t) {
            final TreeExpansionModel treeExpansionModel = (TreeExpansionModel) getModel();
            treeExpansionModel.onExpand(t);
            super.expand(t);
        }

        /**
         * To utilize the custom TreeExpansionModel for hooking into a node's collapse event,
         * we override this method.
         */
        @Override
        public void collapse(TreeModel t) {
            final TreeExpansionModel treeExpansionModel = (TreeExpansionModel) getModel();
            treeExpansionModel.onCollapse(t);
            super.collapse(t);
        }

    }

    // -- ISIS' TREE-MODEL

    /**
     * Extending the EntityModel to also provide a TreePath.
     */
    private static class TreeModel extends EntityModel {
        private static final long serialVersionUID = 8916044984628849300L;

        private final TreePath treePath;

        public TreeModel(TreePath treePath) {
            super((ObjectAdapter)null);
            this.treePath = treePath;
        }

        public TreeModel(ObjectAdapter adapter, TreePath treePath) {
            super(Objects.requireNonNull(adapter));
            this.treePath = treePath;
        }

        public TreePath getTreePath() {
            return treePath;
        }

        public boolean isTreePathModelOnly() {
            return getObject()==null;
        }

    }

    // -- ISIS' TREE ADAPTER (FOR TREES OF TREE-MODEL NODES)

    /**
     *  TreeAdapter for TreeModel nodes.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static class TreeModelTreeAdapter implements TreeAdapter<TreeModel>, Serializable {
        private static final long serialVersionUID = 1L;

        private final Class<? extends TreeAdapter> treeAdapterClass;
        private transient TreeAdapter wrappedTreeAdapter;

        private TreeModelTreeAdapter(Class<? extends TreeAdapter> treeAdapterClass) {
            this.treeAdapterClass = treeAdapterClass;
        }

        private TreeAdapter wrappedTreeAdapter() {
            if(wrappedTreeAdapter!=null) {
                return wrappedTreeAdapter;
            }
            try {
                final FactoryService factoryService = IsisContext.getServiceRegistry()
                        .lookupServiceElseFail(FactoryService.class);
                return wrappedTreeAdapter = factoryService.instantiate(treeAdapterClass);
            } catch (Exception e) {
                throw new RuntimeException("failed to instantiate tree adapter", e);
            }
        }

        @Override
        public Optional<TreeModel> parentOf(TreeModel treeModel) {
            if(treeModel==null) {
                return Optional.empty();
            }
            return wrappedTreeAdapter().parentOf(unwrap(treeModel))
                    .map(pojo->wrap(pojo, treeModel.getTreePath().getParentIfAny()));
        }

        @Override
        public int childCountOf(TreeModel treeModel) {
            if(treeModel==null) {
                return 0;
            }
            return wrappedTreeAdapter().childCountOf(unwrap(treeModel));
        }

        @Override
        public Stream<TreeModel> childrenOf(TreeModel treeModel) {
            if(treeModel==null) {
                return Stream.empty();
            }
            return wrappedTreeAdapter().childrenOf(unwrap(treeModel))
                    .map(newPojoToTreeModelMapper(treeModel));
        }

        private TreeModel wrap(Object pojo, TreePath treePath) {
            requires(pojo, "pojo");
            val pojoToAdapter = IsisContext.pojoToAdapter();
            val objectAdapter = pojoToAdapter.apply(pojo);
            return new TreeModel(objectAdapter, treePath);
        }

        private Object unwrap(TreeModel model) {
            Objects.requireNonNull(model);
            return model.getObject().getPojo();
        }

        private Function<Object, TreeModel> newPojoToTreeModelMapper(TreeModel parent) {
            return _Functions.indexAwareToFunction((indexWithinSiblings, pojo)->
            wrap(pojo, parent.getTreePath().append(indexWithinSiblings)));
        }

    }

    // -- WICKET'S TREE PROVIDER (FOR TREES OF TREE-MODEL NODES)

    /**
     * Wicket's ITreeProvider implemented for Isis
     */
    private static class TreeModelTreeProvider implements ITreeProvider<TreeModel> {

        private static final long serialVersionUID = 1L;

        /**
         * tree's root
         */
        private final TreeModel primaryValue;
        private final TreeModelTreeAdapter treeAdapter;

        private TreeModelTreeProvider(TreeModel primaryValue, TreeModelTreeAdapter treeAdapter) {
            this.primaryValue = primaryValue;
            this.treeAdapter = treeAdapter;
        }

        @Override
        public void detach() {
        }

        @Override
        public Iterator<? extends TreeModel> getRoots() {
            return _Lists.singleton(primaryValue).iterator();
        }

        @Override
        public boolean hasChildren(TreeModel node) {
            return treeAdapter.childCountOf(node)>0;
        }

        @Override
        public Iterator<? extends TreeModel> getChildren(TreeModel node) {
            return treeAdapter.childrenOf(node).iterator();
        }

        @Override
        public IModel<TreeModel> model(final TreeModel treeModel) {
            return treeModel.isTreePathModelOnly()
                    ? Model.of(treeModel)
                            : new LoadableDetachableTreeModel(treeModel);
        }

    }

    /**
     * @param model
     * @return Wicket's ITreeProvider
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static ITreeProvider<TreeModel> toITreeProvider(ModelAbstract<ObjectAdapter> model) {

        final TreeNode treeNode = (TreeNode) model.getObject().getPojo();
        final Class<? extends TreeAdapter> treeAdapterClass = treeNode.getTreeAdapterClass();
        final TreeModelTreeAdapter wrappingTreeAdapter = new TreeModelTreeAdapter(treeAdapterClass);

        return new TreeModelTreeProvider(
                wrappingTreeAdapter.wrap(treeNode.getValue(), treeNode.getPositionAsPath()),
                wrappingTreeAdapter);
    }

    // -- WICKET'S LOADABLE/DETACHABLE MODEL FOR TREE-MODEL NODES

    /**
     * Wicket's loadable/detachable model for TreeModel nodes.
     */
    private static class LoadableDetachableTreeModel extends LoadableDetachableModel<TreeModel> {
        private static final long serialVersionUID = 1L;

        private final RootOid id;
        private final TreePath treePath;
        private final int hashCode;

        public LoadableDetachableTreeModel(TreeModel tModel) {
            super(tModel);
            this.treePath = tModel.getTreePath();
            this.id = (RootOid) tModel.getObject().getOid();
            this.hashCode = Objects.hash(id.hashCode(), treePath.hashCode());
        }

        /*
         * loads EntityModel using Oid (id)
         */
        @Override
        protected TreeModel load() {

        	val rootOid = id;
            val rootOidToAdapter = IsisContext.rootOidToAdapter();

            val objAdapter = rootOidToAdapter.apply(rootOid);
            if(objAdapter==null) {
                throw new NoSuchElementException(
                        String.format("Tree creation: could not recreate TreeModel from Oid: '%s'", id));
            }

            final Object pojo = objAdapter.getPojo();
            if(pojo==null) {
                throw new NoSuchElementException(
                        String.format("Tree creation: could not recreate Pojo from Oid: '%s'", id));
            }

            return new TreeModel(objAdapter, treePath);
        }

        /*
         * Important! Models must be identifiable by their contained object. Also IDs must be
         * unique within a tree structure.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LoadableDetachableTreeModel) {
                final LoadableDetachableTreeModel other = (LoadableDetachableTreeModel) obj;
                return treePath.equals(other.treePath) && id.equals(other.id);
            }
            return false;
        }

        /*
         * Important! Models must be identifiable by their contained object.
         */
        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    // -- COLLAPSE/EXPAND

    /**
     *
     * @param model
     * @return Wicket's model for collapse/expand state
     */
    @SuppressWarnings({ "rawtypes" })
    private static TreeExpansionModel toIModelRepresentingCollapseExpandState(ModelAbstract<ObjectAdapter> model) {
        final TreeNode treeNode = (TreeNode) model.getObject().getPojo();
        final TreeState treeState = treeNode.getTreeState();
        return TreeExpansionModel.of(treeState.getExpandedNodePaths());
    }

    /**
     * Wicket's model for collapse/expand state
     */
    private static class TreeExpansionModel implements IModel<Set<TreeModel>> {
        private static final long serialVersionUID = 648152234030889164L;

        public static TreeExpansionModel of(Set<TreePath> expandedTreePaths) {
            return new TreeExpansionModel(expandedTreePaths);
        }

        /**
         * Happens on user interaction via UI.
         * @param t
         */
        public void onExpand(TreeModel t) {
            expandedTreePaths.add(t.getTreePath());
        }

        /**
         * Happens on user interaction via UI.
         * @param t
         */
        public void onCollapse(TreeModel t) {
            expandedTreePaths.remove(t.getTreePath());
        }

        public boolean contains(TreePath treePath) {
            return expandedTreePaths.contains(treePath);
        }

        private final Set<TreePath> expandedTreePaths;
        private final Set<TreeModel> expandedNodes;

        private TreeExpansionModel(Set<TreePath> expandedTreePaths) {
            this.expandedTreePaths = expandedTreePaths;
            this.expandedNodes = expandedTreePaths.stream()
                    .map(tPath->new TreeModel(tPath))
                    .collect(Collectors.toSet());
        }

        @Override
        public Set<TreeModel> getObject() {
            return expandedNodes;
        }

        @Override
        public String toString() {
            return "{" + expandedTreePaths.stream()
            .map(TreePath::toString)
            .collect(Collectors.joining(", ")) + "}";
        }

    }

}

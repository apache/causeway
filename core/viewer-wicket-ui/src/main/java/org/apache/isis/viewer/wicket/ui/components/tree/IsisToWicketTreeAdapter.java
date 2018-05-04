package org.apache.isis.viewer.wicket.ui.components.tree;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

import javax.resource.spi.IllegalStateException;

import org.apache.isis.applib.internal.collections._Lists;
import org.apache.isis.applib.tree.TreeAdapter;
import org.apache.isis.applib.tree.TreeNode;
import org.apache.isis.applib.tree.TreePath;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.components.entity.icontitle.EntityIconAndTitlePanel;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

class IsisToWicketTreeAdapter {

	public static EntityTree adapt(String id, ValueModel valueModel) {
		return new EntityTree(id, toITreeProvider( valueModel ));
	}
	
	public static EntityTree adapt(String id, ScalarModel scalarModel) {
		return new EntityTree(id, toITreeProvider( scalarModel ));
	}
	
	// -- RENDERING
	
	/**
	 * Wicket's Tree Component implemented for Isis
	 */
	private static class EntityTree extends NestedTree<TreeModel> {

		private static final long serialVersionUID = 1L;

		public EntityTree(String id, ITreeProvider<TreeModel> provider) {
			super(id, provider);
			add(new WindowsTheme()); // TODO not required if Isis provides it's own css styles for tree-nodes
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
						public void onClick(AjaxRequestTarget target) {
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
		
	}
	
	// -- HELPER
	
	/**
	 * Extending the EntityModel to also provide a TreePath.
	 */
	public static class TreeModel extends EntityModel {
		private static final long serialVersionUID = 8916044984628849300L;
		
		private final TreePath treePath;

		public TreeModel(ObjectAdapter adapter, TreePath treePath) {
			super(adapter);
			this.treePath = treePath;
		}
		
		public TreePath getTreePath() {
			return treePath;
		}
	}
	

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
				return wrappedTreeAdapter = treeAdapterClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
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
			final LongAdder indexWithinSiblings = new LongAdder(); 
			return wrappedTreeAdapter().childrenOf(unwrap(treeModel))
					.map(pojo->wrap(pojo, treeModel.getTreePath().append(indexWithinSiblings.intValue()) ))
					.peek(__->indexWithinSiblings.increment());
		}
		
		private TreeModel wrap(Object pojo, TreePath treePath) {
			Objects.requireNonNull(pojo);
			return new TreeModel(persistenceSession().getAdapterFor(pojo), treePath);
		}
		
		private Object unwrap(TreeModel model) {
			Objects.requireNonNull(model);
			return model.getObject().getObject();
		}
		
		private PersistenceSession persistenceSession() {
			return IsisContext.getPersistenceSession().orElse(null);
		}
		
	}

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
			return new LoadableDetachableEntityModel(treeModel);
		}
		
	}
	
	/**
	 * @param model
	 * @return Wicket's ITreeProvider
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ITreeProvider<TreeModel> toITreeProvider(ModelAbstract<ObjectAdapter> model) {
		
		final TreeNode treeNode = (TreeNode) model.getObject().getObject();
		final TreeModelTreeAdapter wrappingTreeAdapter = new TreeModelTreeAdapter(treeNode.getTreeAdapterClass());
		return new TreeModelTreeProvider(
				wrappingTreeAdapter.wrap(treeNode.getValue(), treeNode.getPositionAsPath()), 
				wrappingTreeAdapter);		
	}
	
	private static class LoadableDetachableEntityModel extends LoadableDetachableModel<TreeModel> {
		private static final long serialVersionUID = 1L;

		private final RootOid id;
		private final TreePath treePath;
		private final int hashCode;

		public LoadableDetachableEntityModel(TreeModel tModel) {
			super(tModel);
			this.id = (RootOid) tModel.getObject().getOid();
			this.treePath = tModel.getTreePath();
			this.hashCode = Objects.hash(id.hashCode(), treePath.hashCode());
		}

		/*
		 * loads EntityModel using Oid (id)
		 */
		@Override
		protected TreeModel load() {
			
			final PersistenceSession persistenceSession = IsisContext.getPersistenceSession()
					.orElseThrow(()->new RuntimeException(new IllegalStateException(
							String.format("Tree creation: missing a PersistenceSession to recreate EntityModel "
									+ "from Oid: '%s'", id)))
					);
			
			final ObjectAdapter objAdapter = persistenceSession.adapterFor(id);
			if(objAdapter==null) {
				throw new NoSuchElementException(
						String.format("Tree creation: could not recreate EntityModel from Oid: '%s'", id)); 
			}
			
			final Object pojo = objAdapter.getObject();
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
			if (obj instanceof LoadableDetachableEntityModel) {
				final LoadableDetachableEntityModel other = (LoadableDetachableEntityModel) obj;
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

}

package org.apache.isis.viewer.wicket.ui.components.tree;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.resource.spi.IllegalStateException;

import org.apache.isis.applib.internal.collections._Lists;
import org.apache.isis.applib.tree.TreeAdapter;
import org.apache.isis.applib.tree.TreeNode;
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
	private static class EntityTree extends NestedTree<EntityModel> {

		private static final long serialVersionUID = 1L;

		public EntityTree(String id, ITreeProvider<EntityModel> provider) {
			super(id, provider);
			add(new WindowsTheme()); // TODO not required if Isis provides it's own css styles for tree-nodes
		}
		
		/**
		 * To use a custom component for the representation of a node's content we override this method.
		 */
		@Override
		protected Component newContentComponent(String id, IModel<EntityModel> node) {
			final EntityModel entityModel = node.getObject();
			final Component entityIconAndTitle = new EntityIconAndTitlePanel(id, entityModel);
			return entityIconAndTitle;
		}
		
		/**
		 * To hardcode Node's <pre>AjaxFallbackLink.isEnabledInHierarchy()->true</pre> we override this method.
		 */
		@Override
		public Component newNodeComponent(String id, IModel<EntityModel> model) {
			
			final Node<EntityModel> node =  new Node<EntityModel>(id, this, model) {
				private static final long serialVersionUID = 1L;
				
				@Override
				protected Component createContent(String id, IModel<EntityModel> model) {
					return EntityTree.this.newContentComponent(id, model);
				}
				
				@Override
				protected MarkupContainer createJunctionComponent(String id) {
					
					final Node<EntityModel> node = this;
					final Runnable toggleExpandCollapse = (Runnable & Serializable) this::toggle;
					
					return new AjaxFallbackLink<Void>(id) {
						private static final long serialVersionUID = 1L;
						
						@Override
						public void onClick(AjaxRequestTarget target) {
							System.out.println("!!! before toggle");
							toggleExpandCollapse.run();
							System.out.println("!!! after toggle");
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
	
	private static class EntityModelTreeAdapter implements TreeAdapter<EntityModel>, Serializable {
		private static final long serialVersionUID = 1L;
		
		private final Class<? extends TreeAdapter> treeAdapterClass;
		private transient TreeAdapter wrappedTreeAdapter;
		
		private EntityModelTreeAdapter(Class<? extends TreeAdapter> treeAdapterClass) {
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
		public Optional<EntityModel> parentOf(EntityModel entityModel) {
			if(entityModel==null) {
				return Optional.empty();
			}
			return wrappedTreeAdapter().parentOf(unwrap(entityModel))
					.map(this::wrap);
		}

		@Override
		public int childCountOf(EntityModel entityModel) {
			if(entityModel==null) {
				return 0;
			}
			return wrappedTreeAdapter().childCountOf(unwrap(entityModel));
		}

		@Override
		public Stream<EntityModel> childrenOf(EntityModel entityModel) {
			if(entityModel==null) {
				return Stream.empty();
			}
			return wrappedTreeAdapter().childrenOf(unwrap(entityModel))
					.map(this::wrap);
		}
		
		private EntityModel wrap(Object pojo) {
			Objects.requireNonNull(pojo);
			return new EntityModel(persistenceSession().getAdapterFor(pojo));
		}
		
		private Object unwrap(EntityModel model) {
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
	private static class EntityModelTreeProvider implements ITreeProvider<EntityModel> {

		private static final long serialVersionUID = 1L;
		
		private final EntityModel primaryValue;
		private final EntityModelTreeAdapter treeAdapter;

		private EntityModelTreeProvider(EntityModel primaryValue, EntityModelTreeAdapter treeAdapter) {
			this.primaryValue = primaryValue;
			this.treeAdapter = treeAdapter;
		}

		@Override
		public void detach() {
		}

		@Override
		public Iterator<? extends EntityModel> getRoots() {
			return _Lists.singleton(primaryValue).iterator();
		}

		@Override
		public boolean hasChildren(EntityModel node) {
			return treeAdapter.childCountOf(node)>0;
		}

		@Override
		public Iterator<? extends EntityModel> getChildren(EntityModel node) {
			return treeAdapter.childrenOf(node).iterator();
		}

		@Override
		public IModel<EntityModel> model(final EntityModel entityModel) {
			return new LoadableDetachableEntityModel(entityModel);
		}
		
	}
	
	/**
	 * @param model
	 * @return Wicket's ITreeProvider
	 */
	private static ITreeProvider<EntityModel> toITreeProvider(ModelAbstract<ObjectAdapter> model) {
		
		final TreeNode tree = (TreeNode) model.getObject().getObject();
		final EntityModelTreeAdapter wrappingTreeAdapter = new EntityModelTreeAdapter(tree.getTreeAdapterClass());
		
		return new EntityModelTreeProvider(wrappingTreeAdapter.wrap(tree.getValue()), wrappingTreeAdapter);		
	}
	
	private static class LoadableDetachableEntityModel extends LoadableDetachableModel<EntityModel> {
		private static final long serialVersionUID = 1L;

		private final RootOid id;

		public LoadableDetachableEntityModel(EntityModel eModel) {
			super(eModel);
			id = (RootOid) eModel.getObject().getOid();
		}

		/*
		 * loads EntityModel using Oid (id)
		 */
		@Override
		protected EntityModel load() {
			
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
			
			return new EntityModel(objAdapter);
		}

		/*
		 * Important! Models must be identifiable by their contained object.
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LoadableDetachableEntityModel) {
				final LoadableDetachableEntityModel other = (LoadableDetachableEntityModel) obj;
				return id.equals(other.id);
			}
			return false;
		}

		/*
		 * Important! Models must be identifiable by their contained object.
		 */
		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

}

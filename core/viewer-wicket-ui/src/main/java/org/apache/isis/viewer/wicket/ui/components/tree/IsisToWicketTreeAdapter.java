package org.apache.isis.viewer.wicket.ui.components.tree;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.isis.applib.internal.collections._Lists;
import org.apache.isis.applib.tree.TreeAdapter;
import org.apache.isis.applib.tree.TreeNode;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.components.entity.icontitle.EntityIconAndTitlePanel;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
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
			return new EntityIconAndTitlePanel(id, entityModel);
		}

		
	}
	
	// -- HELPER
	
	private static class EntitiyModelTreeAdapter implements TreeAdapter<EntityModel>, Serializable {
		private static final long serialVersionUID = 1L;
		
		private final Class<? extends TreeAdapter> treeAdapterClass;
		private transient TreeAdapter wrappedTreeAdapter;
		
		private EntitiyModelTreeAdapter(Class<? extends TreeAdapter> treeAdapterClass) {
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
			return IsisContext.getSessionFactory().getCurrentSession().getPersistenceSession();
		}
		
	}

	
	private static class EntityModelTreeProvider implements ITreeProvider<EntityModel> {

		private static final long serialVersionUID = 1L;
		
		private final EntityModel primaryValue;
		private final EntitiyModelTreeAdapter treeAdapter;

		private EntityModelTreeProvider(EntityModel primaryValue, EntitiyModelTreeAdapter treeAdapter) {
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
	
	private static ITreeProvider<EntityModel> toITreeProvider(ModelAbstract<ObjectAdapter> model) {
		
		final TreeNode tree = (TreeNode) model.getObject().getObject();
		final EntitiyModelTreeAdapter wrappingTreeAdapter = new EntitiyModelTreeAdapter(tree.getTreeAdapterClass());
		
		return new EntityModelTreeProvider(wrappingTreeAdapter.wrap(tree.getValue()), wrappingTreeAdapter);		
	}
	
	private static class LoadableDetachableEntityModel extends LoadableDetachableModel<EntityModel> {
		private static final long serialVersionUID = 1L;

		private final Oid id;

		public LoadableDetachableEntityModel(EntityModel eModel) {
			super(eModel);

			id = eModel.getObject().getOid();
		}

		@Override
		protected EntityModel load() {
			return null; //TODO [ahuber] load model by id, possibly not required
		}

		/**
		 * Important! Models must be identifiable by their contained object.
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof EntityModel) {
				return ((EntityModel) obj).getObject().getOid().equals(id);
			}
			return false;
		}

		/**
		 * Important! Models must be identifiable by their contained object.
		 */
		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

}

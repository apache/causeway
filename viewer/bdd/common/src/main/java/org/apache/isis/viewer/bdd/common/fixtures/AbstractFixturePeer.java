package org.apache.isis.viewer.bdd.common.fixtures;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.transaction.IsisTransactionManager;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.objectstore.ObjectStorePersistence;
import org.apache.isis.runtime.persistence.objectstore.PersistenceSessionObjectStore;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;

public abstract class AbstractFixturePeer {

	private final AliasRegistry storyRegistries;
    private final List<CellBinding> cellBindings;

    public AbstractFixturePeer(AliasRegistry storyRegistries,
    		CellBinding... cellBindings) {
    	this(storyRegistries, Arrays.asList(cellBindings));
    }

    public AbstractFixturePeer(AliasRegistry storyRegistries,
    		List<CellBinding> cellBindings) {
    	this.storyRegistries = storyRegistries;
    	this.cellBindings = cellBindings;
    }

	public AliasRegistry getAliasRegistry() {
        return storyRegistries;
	}

	public List<CellBinding> getCellBindings() {
		return cellBindings;
	}
	
	
	public List<Object> getServices() {
		return IsisContext.getServices();
	}
	
    public SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

	public AuthenticationSession getAuthenticationSession() {
		return IsisContext.getAuthenticationSession();
	}
	
    public PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AdapterManager getAdapterManager() {
    	return getPersistenceSession().getAdapterManager();
    }
    
	protected ObjectStorePersistence getObjectStore() {
		final PersistenceSessionObjectStore persistenceSession = (PersistenceSessionObjectStore) getPersistenceSession();
        return persistenceSession.getObjectStore();
	}

    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

	
}

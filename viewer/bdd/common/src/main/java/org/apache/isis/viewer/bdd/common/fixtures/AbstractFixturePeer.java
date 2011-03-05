package org.apache.isis.viewer.bdd.common.fixtures;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStorePersistence;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.PersistenceSessionObjectStore;
import org.apache.isis.runtimes.dflt.runtime.transaction.IsisTransactionManager;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;

public abstract class AbstractFixturePeer {

	private final AliasRegistry aliasRegistry;
    private final List<CellBinding> cellBindings;

    public AbstractFixturePeer(AliasRegistry aliasRegistry,
    		CellBinding... cellBindings) {
    	this(aliasRegistry, Arrays.asList(cellBindings));
    }

    public AbstractFixturePeer(AliasRegistry storyRegistries,
    		List<CellBinding> cellBindings) {
    	this.aliasRegistry = storyRegistries;
    	this.cellBindings = cellBindings;
    }
    
    public AliasRegistry getAliasRegistry() {
        return aliasRegistry;
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

    public boolean isValidAlias(String alias) {
        return getAliasRegistry().getAliased(alias) != null;
    }

	
}

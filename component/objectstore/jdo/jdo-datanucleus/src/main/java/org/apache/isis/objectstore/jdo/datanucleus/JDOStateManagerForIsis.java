package org.apache.isis.objectstore.jdo.datanucleus;

import javax.jdo.listener.LoadCallback;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;

import org.datanucleus.ExecutionContext;
import org.datanucleus.cache.CachedPC;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.state.JDOStateManager;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.store.FieldValues;

import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class JDOStateManagerForIsis extends JDOStateManager implements StateManager, ObjectProvider {

	public JDOStateManagerForIsis(ExecutionContext ec, AbstractClassMetaData cmd) {
		super(ec, cmd);
	}

	public void initialiseForHollow(Object id, FieldValues fv, Class pcClass) {
		super.initialiseForHollow(id, fv, pcClass);
		mapIntoIsis(myPC);
	}

	public void initialiseForHollowAppId(FieldValues fv, Class pcClass) {
		super.initialiseForHollowAppId(fv, pcClass);
		mapIntoIsis(myPC);
	}

	public void initialiseForHollowPreConstructed(Object id, Object pc) {
		super.initialiseForHollowPreConstructed(id, pc);
		mapIntoIsis(myPC);
	}

	public void initialiseForPersistentClean(Object id, Object pc) {
		super.initialiseForPersistentClean(id, pc);
		mapIntoIsis(myPC);
	}

	public void initialiseForEmbedded(Object pc, boolean copyPc) {
		super.initialiseForEmbedded(pc, copyPc);
		mapIntoIsis(myPC);
	}

	public void initialiseForPersistentNew(Object pc,
			FieldValues preInsertChanges) {
		super.initialiseForPersistentNew(pc, preInsertChanges);
		mapIntoIsis(myPC);
	}

	public void initialiseForTransactionalTransient(Object pc) {
		super.initialiseForTransactionalTransient(pc);
		mapIntoIsis(myPC);
	}

	public void initialiseForDetached(Object pc, Object id, Object version) {
		super.initialiseForDetached(pc, id, version);
		mapIntoIsis(myPC);
	}

	public void initialiseForPNewToBeDeleted(Object pc) {
		super.initialiseForPNewToBeDeleted(pc);
		mapIntoIsis(myPC);
	}

	public void initialiseForCachedPC(CachedPC cachedPC, Object id) {
		super.initialiseForCachedPC(cachedPC, id);
		mapIntoIsis(myPC);
	}
	
	protected void mapIntoIsis(PersistenceCapable pc) {
	    getServicesInjector().injectServicesInto(pc);
	}

    protected ServicesInjectorSpi getServicesInjector() {
        return IsisContext.getPersistenceSession().getServicesInjector();
    }
}

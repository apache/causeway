package org.apache.isis.core.runtime.system.persistence;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService.FieldResetPolicy;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

public interface PersistenceSession extends AdapterManager, TransactionalResource, SessionScopedComponent {

	String SERVICE_IDENTIFIER = "1";

	boolean isFixturesInstalled();

	IsisTransactionManager getTransactionManager();

	Object instantiateAndInjectServices(ObjectSpecification spec);

	List<ObjectAdapter> getServices();

	void makePersistentInTransaction(ObjectAdapter adapter);

	void destroyObjectInTransaction(ObjectAdapter adapter);

	ObjectAdapter createTransientInstance(ObjectSpecification spec);

	ObjectAdapter createViewModelInstance(ObjectSpecification spec, String memento);

	Object lookup(Bookmark bookmark, FieldResetPolicy fieldResetPolicy);

	void resolve(Object parent);

	<T> List<ObjectAdapter> allMatchingQuery(final Query<T> query);
	<T> ObjectAdapter firstMatchingQuery(final Query<T> query);

	ServicesInjector getServicesInjector();

	void execute(List<PersistenceCommand> persistenceCommandList);

	void open();

	void close();

	ObjectAdapter adapterForAny(RootOid rootOid);

	ObjectAdapter adapterFor(RootOid oid, ConcurrencyChecking concurrencyChecking);

	Map<RootOid, ObjectAdapter> adaptersFor(List<RootOid> rootOids);

	IsisConfiguration getConfiguration();

	PersistenceManager getPersistenceManager();

	void refreshRoot(ObjectAdapter adapter);

}

package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;

public interface PersistenceQueryProcessor<T extends PersistenceQuery> {
	List<ObjectAdapter> process(T query);
}

// Copyright (c) Naked Objects Group Ltd.

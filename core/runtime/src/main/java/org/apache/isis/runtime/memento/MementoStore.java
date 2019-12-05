package org.apache.isis.runtime.memento;

import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

/**
 * 
 * @deprecated introduced as refactoring step, to decouple memento code from PersistenceSession
 *
 */
public interface MementoStore {

    // -- TODO remove ObjectAdapter references from API
    ObjectAdapter adapterOfMemento(ObjectSpecification spec, Oid oid, Data data);

    ManagedObject adapterForListOfPojos(List<Object> listOfPojos);

}

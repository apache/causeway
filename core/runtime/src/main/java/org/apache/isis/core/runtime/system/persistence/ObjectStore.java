/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.runtime.system.persistence;

import java.util.List;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.CollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.*;

public interface ObjectStore extends TransactionalResource, DebuggableWithTitle, SessionScopedComponent {

    /**
     * The name of this component.
     */
    String name();

    /**
     * Determine if the object store has been initialized with its set of start
     * up objects.
     * 
     * <p>
     * This method is called only once after the session is opened called. If it returns <code>false</code> then the
     * framework will run the fixtures to initialise the object store.
     */
    boolean isFixturesInstalled();

    // ///////////////////////////////////////////////////////
    // reset
    // ///////////////////////////////////////////////////////

    /**
     * TODO: move to {@link PersistenceSessionFactory} ??
     */
    void reset();

    // ///////////////////////////////////////////////////////
    // Command Creation
    // ///////////////////////////////////////////////////////

    /**
     * Makes an {@link ObjectAdapter} persistent. The specified object should be
     * stored away via this object store's persistence mechanism, and have an
     * new and unique OID assigned to it (by calling the object's
     * <code>setOid</code> method). The object, should also be added to the
     * cache as the object is implicitly 'in use'.
     * 
     * <p>
     * If the object has any associations then each of these, where they aren't
     * already persistent, should also be made persistent by recursively calling
     * this method.
     * </p>
     * 
     * <p>
     * If the object to be persisted is a collection, then each element of that
     * collection, that is not already persistent, should be made persistent by
     * recursively calling this method.
     * </p>
     * 
     */
    CreateObjectCommand createCreateObjectCommand(ObjectAdapter object);

    /**
     * Persists the specified object's state. Essentially the data held by the
     * persistence mechanism should be updated to reflect the state of the
     * specified objects. Once updated, the object store should issue a
     * notification to all of the object's users via the <class>UpdateNotifier
     * </class> object. This can be achieved simply, if extending the <class>
     * AbstractObjectStore </class> by calling its <method>broadcastObjectUpdate
     * </method> method.
     */
    SaveObjectCommand createSaveObjectCommand(ObjectAdapter object);

    /**
     * Removes the specified object from the object store. The specified
     * object's data should be removed from the persistence mechanism and, if it
     * is cached (which it probably is), removed from the cache also.
     */
    DestroyObjectCommand createDestroyObjectCommand(ObjectAdapter object);

    // ///////////////////////////////////////////////////////
    // Command Execution
    // ///////////////////////////////////////////////////////

    void execute(final List<PersistenceCommand> commands);

    // ///////////////////////////////////////////////////////
    // loadInstancesAndAdapt, hasInstances
    // ///////////////////////////////////////////////////////

    List<ObjectAdapter> loadInstancesAndAdapt(PersistenceQuery persistenceQuery);

    boolean hasInstances(ObjectSpecification specification);

    // ///////////////////////////////////////////////////////
    // loadInstanceAndAdapt
    // ///////////////////////////////////////////////////////

    /**
     * Retrieves the object identified by the specified {@link TypedOid} from the object
     * store, {@link AdapterManager#mapRecreatedPojo(org.apache.isis.core.metamodel.adapter.oid.Oid, Object) mapped by the adapter manager}.
     * 
     * <p>The cache should be checked first and, if the object is cached,
     * the cached version should be returned. It is important that if this
     * method is called again, while the originally returned object is in
     * working memory, then this method must return that same Java object.
     * 
     * <p>
     * Assuming that the object is not cached then the data for the object
     * should be retrieved from the persistence mechanism and the object
     * recreated (as describe previously). The specified OID should then be
     * assigned to the recreated object by calling its <method>setOID </method>.
     * Before returning the object its resolved flag should also be set by
     * calling its <method>setResolved </method> method as well.
     * 
     * <p>
     * If the persistence mechanism does not known of an object with the
     * specified {@link TypedOid} then a {@link org.apache.isis.core.runtime.persistence.ObjectNotFoundException} should be
     * thrown.
     * 
     * <p>
     * Note that the OID could be for an internal collection, and is
     * therefore related to the parent object (using a {@link CollectionOid}).
     * The elements for an internal collection are commonly stored as
     * part of the parent object, so to get element the parent object needs to
     * be retrieved first, and the internal collection can be got from that.
     * 
     * <p>
     * Returns the stored {@link ObjectAdapter} object.
     * 
     * 
     * @return the requested {@link ObjectAdapter} that has the specified
     *         {@link TypedOid}.
     * 
     * @throws org.apache.isis.core.runtime.persistence.ObjectNotFoundException
     *             when no object corresponding to the oid can be found
     */
    ObjectAdapter loadInstanceAndAdapt(TypedOid oid);


    // ///////////////////////////////////////////////////////
    // resolveImmediately
    // ///////////////////////////////////////////////////////

    /**
     * Called by the resolveImmediately method in ObjectAdapterManager.
     * 
     * @see PersistenceSession#resolveImmediately(ObjectAdapter)
     */
    void resolveImmediately(ObjectAdapter object);


    // ///////////////////////////////////////////////////////
    // Services
    // ///////////////////////////////////////////////////////

    void registerService(RootOid rootOid);

    /**
     * Returns the OID for the adapted service.
     */
    RootOid getOidForService(ObjectSpecification serviceSpec);

}

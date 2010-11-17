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


package org.apache.isis.alternatives.objectstore.xml;

import java.text.MessageFormat;
import java.util.List;

import org.apache.isis.alternatives.objectstore.xml.internal.clock.Clock;
import org.apache.isis.alternatives.objectstore.xml.internal.commands.XmlCreateObjectCommand;
import org.apache.isis.alternatives.objectstore.xml.internal.commands.XmlDestroyObjectCommand;
import org.apache.isis.alternatives.objectstore.xml.internal.commands.XmlUpdateObjectCommand;
import org.apache.isis.alternatives.objectstore.xml.internal.data.CollectionData;
import org.apache.isis.alternatives.objectstore.xml.internal.data.Data;
import org.apache.isis.alternatives.objectstore.xml.internal.data.DataManager;
import org.apache.isis.alternatives.objectstore.xml.internal.data.ObjectData;
import org.apache.isis.alternatives.objectstore.xml.internal.data.ObjectDataVector;
import org.apache.isis.alternatives.objectstore.xml.internal.data.ReferenceVector;
import org.apache.isis.alternatives.objectstore.xml.internal.data.xml.XmlDataManager;
import org.apache.isis.alternatives.objectstore.xml.internal.data.xml.XmlFile;
import org.apache.isis.alternatives.objectstore.xml.internal.services.ServiceManager;
import org.apache.isis.alternatives.objectstore.xml.internal.services.xml.XmlServiceManager;
import org.apache.isis.alternatives.objectstore.xml.internal.version.FileVersion;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.util.CollectionFacetUtils;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.PersistenceSession;
import org.apache.isis.core.runtime.persistence.PersistorUtil;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.core.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.core.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.core.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryBuiltIn;
import org.apache.isis.core.runtime.transaction.ObjectPersistenceException;
import org.apache.log4j.Logger;


public class XmlObjectStore implements ObjectStore {
	
	private static final Logger LOG = Logger.getLogger(XmlObjectStore.class);
    private static final String XMLOS_DIR = ConfigurationConstants.ROOT + "xmlos.dir";
    private final DataManager dataManager;
    private final ServiceManager serviceManager;
    private boolean isFixturesInstalled;

    public XmlObjectStore(IsisConfiguration configuration) {
        String directory = configuration.getString(XMLOS_DIR, "xml/objects");
        final XmlFile xmlFile = new XmlFile(configuration, directory);
        dataManager = new XmlDataManager(xmlFile);
        serviceManager = new XmlServiceManager(xmlFile);
        serviceManager.loadServices();
    }


    public XmlObjectStore(final DataManager dataManager, final ServiceManager serviceManager) {
        this.dataManager = dataManager;
        this.serviceManager = serviceManager;
        serviceManager.loadServices();
    }

    
    ///////////////////////////////////////////////////////////
    // name
    ///////////////////////////////////////////////////////////

    public String name() {
        return "XML";
    }

    ///////////////////////////////////////////////////////////
    // close
    ///////////////////////////////////////////////////////////

    public void close() {
        LOG.info("close " + this);
    }

    ///////////////////////////////////////////////////////////
    // reset
    ///////////////////////////////////////////////////////////

    public void reset() {}

    ///////////////////////////////////////////////////////////
    // init, shutdown, finalize
    ///////////////////////////////////////////////////////////

    
    public boolean hasInstances(final ObjectSpecification cls) {
        LOG.debug("checking instance of " + cls);
        final ObjectData data = new ObjectData(cls, null, null);
        return dataManager.numberOfInstances(data) > 0;
    }

    public void open() throws ObjectPersistenceException {
        isFixturesInstalled = dataManager.isFixturesInstalled();
    }

    public boolean isFixturesInstalled() {
        return isFixturesInstalled;
    }


    private void initObject(final ObjectAdapter object, final ObjectData data) {
        if (object.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            PersistorUtil.start(object, ResolveState.RESOLVING);

            final ObjectAssociation[] fields = object.getSpecification().getAssociations();
            for (int i = 0; i < fields.length; i++) {
                final ObjectAssociation field = fields[i];
                if (field.isNotPersisted()) {
                    continue;
                }

                final ObjectSpecification fieldSpecification = field.getSpecification();
                if (fieldSpecification.isEncodeable()) {
                    final EncodableFacet encoder = fieldSpecification.getFacet(EncodableFacet.class);
                    ObjectAdapter value;
                    final String valueData = data.value(field.getId());
                    if (valueData != null) {
                        if (valueData.equals("NULL")) {
                            value = null;
                        } else {
                            value = encoder.fromEncodedString(valueData);
                        }
                        ((OneToOneAssociation) field).initAssociation(object, value);
                    }
                } else if (field.isOneToManyAssociation()) {
                    initObjectSetupCollection(object, data, field);
                } else if (field.isOneToOneAssociation()) {
                    initObjectSetupReference(object, data, field);
                }
            }
            object.setOptimisticLock(data.getVersion());
            PersistorUtil.end(object);
        }
    }

    private void initObjectSetupReference(final ObjectAdapter object, final ObjectData data, final ObjectAssociation field) {
        final SerialOid referenceOid = (SerialOid) data.get(field.getId());
        LOG.debug("setting up field " + field + " with " + referenceOid);
        if (referenceOid == null) {
            return;
        }

        final Data fieldData = dataManager.loadData(referenceOid);

        if (fieldData == null) {
            final ObjectAdapter adapter = getPersistenceSession().recreateAdapter(referenceOid, field.getSpecification());
            if (!adapter.getResolveState().isDestroyed()) {
                adapter.changeState(ResolveState.DESTROYED);
            }
            ((OneToOneAssociation) field).initAssociation(object, adapter);

            LOG.warn("No data found for " + referenceOid + " so field '" + field.getName() + "' not set in object '"
                    + object.titleString() + "'");
        } else {
            final ObjectAdapter reference = getPersistenceSession().recreateAdapter(referenceOid, specFor(fieldData));
            ((OneToOneAssociation) field).initAssociation(object, reference);
        }

        /*
         * if (loadedObjects().isLoaded(referenceOid)) { ObjectAdapter loadedObject =
         * loadedObjects().getLoadedObject(referenceOid); LOG.debug("using loaded object " + loadedObject);
         * object.initAssociation((OneToOneAssociation) field, loadedObject); } else { ObjectAdapter
         * fieldObject; Data fieldData = (Data) dataManager.loadData((SerialOid) referenceOid);
         * 
         * if (fieldData != null) { fieldObject = (ObjectAdapter) specFor(fieldData).acquireInstance(); } else {
         * fieldObject = (ObjectAdapter) field.getSpecification().acquireInstance(); }
         * 
         * fieldObject.setOid(referenceOid);
         * 
         * if (fieldObject instanceof CollectionAdapter) { fieldObject.setResolved(); }
         * 
         * loadedObjects().loaded(fieldObject); object.initAssociation((OneToOneAssociation) field,
         * fieldObject); }
         */
    }

    private void initObjectSetupCollection(final ObjectAdapter object, final ObjectData data, final ObjectAssociation field) {
        /*
         * The internal collection is already a part of the object, and therefore cannot be recreated, but its
         * oid must be set
         */
        final ReferenceVector refs = (ReferenceVector) data.get(field.getId());
        final ObjectAdapter collection = field.get(object);
        if (collection.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            PersistorUtil.start(collection, ResolveState.RESOLVING);
            final int size = refs == null ? 0 : refs.size();
            final ObjectAdapter[] elements = new ObjectAdapter[size];
            for (int j = 0; j < size; j++) {
                final SerialOid elementOid = refs.elementAt(j);
                ObjectAdapter adapter;
                adapter = getAdapterManager().getAdapterFor(elementOid);
                if (adapter == null) {
                    adapter = getObject(elementOid, null);
                }
                elements[j] = adapter;
            }
            final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
            facet.init(collection, elements);
            PersistorUtil.end(collection);
        }
    }



    ///////////////////////////////////////////////////////////
    // Transaction Management
    ///////////////////////////////////////////////////////////

    public void startTransaction() {
        LOG.debug("start transaction");
    }

    public void endTransaction() {
        LOG.debug("end transaction");
    }


    public void abortTransaction() {
        LOG.debug("transaction aborted");
    }


    ///////////////////////////////////////////////////////////
    // createXxxCommands
    ///////////////////////////////////////////////////////////

    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        return new XmlCreateObjectCommand(object, dataManager);
    }

    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        return new XmlUpdateObjectCommand(object, dataManager);
    }

    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        return new XmlDestroyObjectCommand(object, dataManager);
    }


    ///////////////////////////////////////////////////////////
    // execute, flush
    ///////////////////////////////////////////////////////////

    public void execute(final List<PersistenceCommand> commands) {
        LOG.debug("start execution of transaction");
        for (PersistenceCommand command: commands) {
            command.execute(null);
        }
        LOG.debug("end execution");
    }

    ///////////////////////////////////////////////////////////
    // getObject, resolveImmediately, resolveField
    ///////////////////////////////////////////////////////////

    public ObjectAdapter getObject(final Oid oid, final ObjectSpecification hint) {
        LOG.debug("getObject " + oid);
        final Data data = dataManager.loadData((SerialOid) oid);
        LOG.debug("  data read " + data);

        ObjectAdapter object;

        if (data instanceof ObjectData) {
            object = recreateObject((ObjectData) data);
        } else if (data instanceof CollectionData) {
            throw new IsisException();
        } else {
            throw new ObjectNotFoundException(oid);
        }
        return object;
    }

    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) {
        final ObjectAdapter reference = field.get(object);
        resolveImmediately(reference);
    }

    public void resolveImmediately(final ObjectAdapter object) {
        final ObjectData data = (ObjectData) dataManager.loadData((SerialOid) object.getOid());
        Assert.assertNotNull("Not able to read in data during resolve", object, data);
        initObject(object, data);
    }

    /*
     * The ObjectData holds all references for internal collections, so the object should haves its internal
     * collection populated by this method.
     */
    private ObjectAdapter recreateObject(final ObjectData data) {
        final SerialOid oid = data.getOid();
        final ObjectSpecification spec = specFor(data);
        final ObjectAdapter object = getPersistenceSession().recreateAdapter(oid, spec);
        initObject(object, data);
        return object;
    }


    ///////////////////////////////////////////////////////////
    // getInstances, allInstances
    ///////////////////////////////////////////////////////////

    public ObjectAdapter[] getInstances(final PersistenceQuery persistenceQuery) {
    	
    	if (!(persistenceQuery instanceof PersistenceQueryBuiltIn)) {
    		throw new IllegalArgumentException(MessageFormat.format(
							"Provided PersistenceQuery not supported; was {0}; " +
							"the XML object store only supports {1}",
							persistenceQuery.getClass().getName(), 
							PersistenceQueryBuiltIn.class.getName()));
    	}
		PersistenceQueryBuiltIn builtIn = (PersistenceQueryBuiltIn) persistenceQuery;
    	
        LOG.debug("getInstances of " + builtIn.getSpecification() + " where " + builtIn);
        final ObjectData patternData = new ObjectData(builtIn.getSpecification(), null, null);
        final ObjectAdapter[] instances = getInstances(patternData, builtIn);
        return instances;
    }

    private ObjectAdapter[] getInstances(
    		final ObjectData patternData, 
    		final PersistenceQueryBuiltIn persistenceQuery) {
        final ObjectDataVector data = dataManager.getInstances(patternData);
        final ObjectAdapter[] instances = new ObjectAdapter[data.size()];
        int count = 0;

        for (int i = 0; i < data.size(); i++) {
            final ObjectData instanceData = data.element(i);
            LOG.debug("instance data " + instanceData);

            final SerialOid oid = instanceData.getOid();

            final ObjectSpecification spec = specFor(instanceData);
            final ObjectAdapter instance = getPersistenceSession().recreateAdapter(oid, spec);
            LOG.debug("recreated instance " + instance);
            initObject(instance, instanceData);
            
            if (persistenceQuery == null || 
            	persistenceQuery.matches(instance)) {
                instances[count++] = instance;
            }
        }

        final ObjectAdapter[] array = new ObjectAdapter[count];
        System.arraycopy(instances, 0, array, 0, count);
        return array;
    }


    private ObjectSpecification specFor(final Data data) {
        return getSpecificationLoader().loadSpecification(data.getTypeName());
    }


    ///////////////////////////////////////////////////////////
    // services
    ///////////////////////////////////////////////////////////

    public Oid getOidForService(final String name) {
        return serviceManager.getOidForService(name);
    }

    public void registerService(final String name, final Oid oid) {
        serviceManager.registerService(name, oid);
    }

    ///////////////////////////////////////////////////////////
    // debugging
    ///////////////////////////////////////////////////////////

    public void debugData(final DebugString debug) {
        debug.appendTitle("Business Objects");
        debug.appendln(dataManager.getDebugData());
    }

    public String debugTitle() {
        return "XML Object Store";
    }




    ///////////////////////////////////////////////////////////
    // Dependencies (injected)
    ///////////////////////////////////////////////////////////

    /**
     * Set the clock used to generate sequence numbers and last changed dates for version objects.
     */
    public void setClock(final Clock clock) {
        FileVersion.setClock(clock);
    }

    ///////////////////////////////////////////////////////////
    // Dependencies (from singleton)
    ///////////////////////////////////////////////////////////

    protected static SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    private static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

	protected static PersistenceSession getPersistenceSession() {
		return IsisContext.getPersistenceSession();
	}


}

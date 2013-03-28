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

package org.apache.isis.viewer.html.context;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.UpdateNotifier;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.viewer.html.component.Block;
import org.apache.isis.viewer.html.component.ComponentFactory;
import org.apache.isis.viewer.html.crumb.CollectionCrumb;
import org.apache.isis.viewer.html.crumb.Crumb;
import org.apache.isis.viewer.html.crumb.ObjectCrumb;
import org.apache.isis.viewer.html.crumb.ObjectFieldCrumb;
import org.apache.isis.viewer.html.crumb.TaskCrumb;
import org.apache.isis.viewer.html.request.Request;
import org.apache.isis.viewer.html.task.Task;

public class Context implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(Context.class);
    
    private transient ObjectHistory history;

    private final ComponentFactory componentFactory;

    private final Map<String, RootAdapterMapping> objectMap = Maps.newHashMap();
    private final Map<String, RootAdapterMapping> serviceMap = Maps.newHashMap();
    private final Map<String, CollectionMapping> collectionMap = Maps.newHashMap();
    private final Map<String, ObjectAction> actionMap = Maps.newHashMap();
    
    private final Stack<Crumb> crumbs = new Stack<Crumb>();
    private final List<String> messages = Lists.newArrayList();
    private final List<String> warnings = Lists.newArrayList();

    private AuthenticationSession session;
    
    private boolean isValid;
    private int max;

    // ////////////////////////////////////////////////////
    // constructor, init
    // ////////////////////////////////////////////////////

    public Context(final ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
        this.isValid = true;
        clearMessagesAndWarnings();
        LOG.debug(this + " created with " + componentFactory);
    }

    public void init() {
        final AdapterManager adapterManager = getAdapterManager();
        final List<Object> services = getUserProfile().getPerspective().getServices();
        for (final Object service : services) {
            final ObjectAdapter serviceAdapter = adapterManager.adapterFor(service);
            if (serviceAdapter == null) {
                LOG.warn("unable to find service: " + service + "; skipping");
                continue;
            }
            mapObject(serviceAdapter);
        }
        serviceMap.putAll(objectMap);
    }


    public ComponentFactory getComponentFactory() {
        return componentFactory;
    }


    // ////////////////////////////////////////////////////
    // validity 
    // ////////////////////////////////////////////////////

    public boolean isValid() {
        return isValid;
    }

    public void invalidate() {
        isValid = false;
    }
    
    
    // ////////////////////////////////////////////////////
    // session 
    // ////////////////////////////////////////////////////

    public boolean isLoggedIn() {
        return session != null;
    }

    public void setSession(final AuthenticationSession currentSession) {
        this.session = currentSession;
    }

    public AuthenticationSession getSession() {
        return session;
    }


    // ////////////////////////////////////////////////////
    // processChanges 
    // ////////////////////////////////////////////////////

    public void processChanges() {
        final List<ObjectAdapter> disposedObjects = getUpdateNotifier().getDisposedObjects();
        for (final ObjectAdapter adapter : disposedObjects) {
            final RootAdapterMapping mapping = persistentOrTransientObjectMappingFor(adapter);
            if (objectMap.containsValue(mapping)) {
                processChangeFor(mapping);
            }
        }
    }

    private void processChangeFor(final RootAdapterMapping mapping) {
        final String existingId = findExistingInMap(objectMap, mapping);
        getObjectHistory().remove(existingId);

        final List<Crumb> relatedCrumbs = Lists.newArrayList();
        for (final Crumb crumb : getCrumbs()) {
            /*
             * if (crumb.isFor(existingId)) { relatedCrumbs.add(crumb);
             * }
             */}
        for (final Crumb crumb : relatedCrumbs) {
            crumbs.remove(crumb);
        }

        for (final CollectionMapping collection : collectionMap.values()) {
            collection.remove(existingId);
        }
        objectMap.remove(existingId);
    }

    // ////////////////////////////////////////////////////
    // changeContext 
    // ////////////////////////////////////////////////////

    public Request changeContext(final int id) {
        while (crumbs.size() - 1 > id) {
            crumbs.pop();
        }
        final Crumb c = crumbs.lastElement();
        return c.changeContext();
    }


    
    // ////////////////////////////////////////////////////
    // Crumbs
    // ////////////////////////////////////////////////////

    public void addCollectionFieldCrumb(final String collectionFieldName) {
        crumbs.push(new ObjectFieldCrumb(collectionFieldName));
    }

    public void addCollectionCrumb(final String id) {
        while (crumbs.size() > 0 && !(crumbs.lastElement() instanceof TaskCrumb)) {
            crumbs.pop();
        }
        crumbs.push(new CollectionCrumb(id, getMappedCollection(id)));
    }

    public void setObjectCrumb(final ObjectAdapter object) {
        while (crumbs.size() > 0 && !(crumbs.lastElement() instanceof TaskCrumb)) {
            crumbs.pop();
        }
        final String id = mapObject(object);
        crumbs.push(new ObjectCrumb(id, object));
    }


    public void addTaskCrumb(final Task task) {
        while (crumbs.size() > 1 && !(crumbs.lastElement() instanceof ObjectCrumb)) {
            crumbs.pop();
        }
        Assert.assertNotNull(task);
        Assert.assertTrue(!isTask());
        task.init(this);
        crumbs.push(new TaskCrumb(task));
    }

    
    public Crumb[] getCrumbs() {
        final int size = crumbs.size();
        final Crumb[] taskList = new Crumb[size];
        for (int i = 0; i < crumbs.size(); i++) {
            taskList[i] = crumbs.get(i);
        }
        return taskList;
    }

    public boolean[] isLinked() {
        final int size = crumbs.size();
        final boolean[] isLinked = new boolean[size];
        for (int i = size - 1; i >= 0; i--) {
            final boolean isTask = crumbs.elementAt(i) instanceof TaskCrumb;
            isLinked[i] = i != size - 1;
            if (isTask) {
                break;
            }
        }
        return isLinked;
    }


    // ////////////////////////////////////////////////////
    // Mappings
    // ////////////////////////////////////////////////////

    public String mapAction(final ObjectAction action) {
        return findExistingOrAddToMap(actionMap, action);
    }

    public String mapObject(final ObjectAdapter adapter) {
        return findExistingOrAddToMap(objectMap, persistentOrTransientObjectMappingFor(adapter));
    }

    public String mapCollection(final ObjectAdapter collection) {
        return findExistingOrAddToMap(collectionMap, new CollectionMapping(this, collection));
    }

    public ObjectAction getMappedAction(final String id) {
        return getMappedInstance(actionMap, id);
    }

    public ObjectAdapter getMappedCollection(final String id) {
        final CollectionMapping map = getMappedInstance(collectionMap, id);
        return map.getCollection(this);
    }

    public ObjectAdapter getMappedObject(final String id) {
        final RootAdapterMapping mappedObject = getMappedInstance(objectMap, id);
        final ObjectAdapter adapter = mappedObject.getObject();

        // ensure resolved if currently a ghost;
        // start/end xactn if required
        if (adapter.representsPersistent() && adapter.isGhost()) {
            getPersistenceSession().resolveImmediately(adapter);
        }

        try {
            mappedObject.checkVersion(adapter);
        } catch (final ConcurrencyException e) {
            LOG.info("concurrency conflict: " + e.getMessage());
            messages.clear();
            messages.add(e.getMessage());
            messages.add("Reloaded object " + adapter.titleString());
            updateVersion(adapter);
        }
        return adapter;
    }

    
    private <T> String findExistingOrAddToMap(final Map<String,T> map, final T object) {
        Assert.assertNotNull(object);
        if (map.containsValue(object)) {
            return findExistingInMap(map, object);
        } else {
            return addToMap(map, object);
        }
    }

    private <T> String addToMap(final Map<String,T> map, final T object) {
        
        String id;
        // bit hacky...
        if(object instanceof RootAdapterMapping) {
            // object or (internal) collection
            RootAdapterMapping adapterMapping = (RootAdapterMapping) object;
            id = adapterMapping.getOidStr();
        } else {
            max++;
            id = "" + max;
        }
        map.put(id, object);

        final String mapName = map == objectMap ? "object" : (map == collectionMap ? "collection" : "action");
        if(LOG.isDebugEnabled()) {
            LOG.debug("add " + object + " to " + mapName + " as #" + id);
        }
        return id;
    }

    private <T> String findExistingInMap(final Map<String, T> map, final Object object) {
        for (final String id : map.keySet()) {
            if (object.equals(map.get(id))) {
                return id;
            }
        }
        throw new IsisException();
    }

    private <T> T getMappedInstance(final Map<String,T> map, final String id) {
        final T object = map.get(id);
        if (object == null) {
            final String mapName = mapNameFor(map);
            throw new ObjectLookupException("No object in " + mapName + " map with id " + id);
        }
        return object;
    }

    private <T> String mapNameFor(final Map<String, T> map) {
        return (map == objectMap) ? "object" : (map == collectionMap ? "collection" : "action");
    }

    private static RootAdapterMapping persistentOrTransientObjectMappingFor(final ObjectAdapter adapter) {
        return adapter.isTransient() ? new TransientRootAdapterMapping(adapter) : new PersistentRootAdapterMapping(adapter);
    }

    

    // ////////////////////////////////////////////////////
    // Instances 
    // ////////////////////////////////////////////////////

    /**
     * Returns an array of instances of the specified type that are currently
     * known in the current context, ie have been recently seen by the user.
     * 
     * <p>
     * These will be resolved if required, with a transaction created (and
     * ended) if required.
     */
    public ObjectAdapter[] getKnownInstances(final ObjectSpecification type) {

        final List<ObjectAdapter> instances = Lists.newArrayList();

        for (final String id : objectMap.keySet()) {
            final ObjectAdapter adapter = getMappedObject(id);
            
            getPersistenceSession().resolveImmediately(adapter);
            if (adapter.getSpecification().isOfType(type)) {
                instances.add(adapter);
            }
        }

        final ObjectAdapter[] array = new ObjectAdapter[instances.size()];
        instances.toArray(array);
        return array;
    }

    public void restoreAllObjectsToLoader() {
        for (Map.Entry<String, RootAdapterMapping> mapEntry : objectMap.entrySet()) {
            final RootAdapterMapping rootAdapterMapping = mapEntry.getValue();
            rootAdapterMapping.restoreToLoader();
        }
    }

    public void purgeObjectsAndCollections() {
        
        clearMessagesAndWarnings();

        final Map<String, CollectionMapping> collMappingById = Maps.newHashMap();
        final Map<String, RootAdapterMapping> objectMappingById = Maps.newHashMap();

        for (HistoryEntry entry : getObjectHistory()) {
            if (entry.type == HistoryEntry.OBJECT) {
                copyObjectMapping(objectMappingById, entry);
            } else if (entry.type == HistoryEntry.COLLECTION) {
                copyCollectionMapping(collMappingById, objectMappingById, entry);
            }
        }

        collectionMap.clear();
        collectionMap.putAll(collMappingById);
        objectMap.clear();
        objectMap.putAll(objectMappingById);
        objectMap.putAll(serviceMap);
    }

    private void copyObjectMapping(final Map<String, RootAdapterMapping> objectMappingById, HistoryEntry entry) {
        final RootAdapterMapping item = objectMap.get(entry.id);
        objectMappingById.put(entry.id, item);
        
        LOG.debug("copied object map " + entry.id + " for " + item);
        item.updateVersion();
    }
    
    private void copyCollectionMapping(final Map<String, CollectionMapping> collMappingById, final Map<String, RootAdapterMapping> objectMappingById, HistoryEntry entry) {
        
        final CollectionMapping coll = collectionMap.get(entry.id);
        collMappingById.put(entry.id, coll);
        LOG.debug("copied collection map for " + coll);
        
        for (String elementId : coll) {
            final RootAdapterMapping objMapping = objectMap.get(elementId);
            
            if (objMapping != null) {
                objectMappingById.put(elementId, objMapping);
                
                LOG.debug("copied object map " + elementId + " for " + objMapping);
                objMapping.updateVersion();
            }
        }
    }

    // ////////////////////////////////////////////////////
    // Tasks 
    // ////////////////////////////////////////////////////

    public Task getTask(final String taskId) {
        Task task = null;
        for (int i = crumbs.size() - 1; i >= 0; i--) {
            final Object crumb = crumbs.get(i);
            if (crumb instanceof TaskCrumb) {
                final TaskCrumb taskCrumb = (TaskCrumb) crumb;
                final String id = taskCrumb.getTask().getId();
                if (taskId.equals(id)) {
                    task = taskCrumb.getTask();
                    break;
                }
            }
        }
        return task;
    }

    public void endTask(final Task task) {
        for (int i = crumbs.size() - 1; i >= 0; i--) {
            final Object crumb = crumbs.get(i);
            if (crumb instanceof TaskCrumb) {
                final TaskCrumb taskCrumb = (TaskCrumb) crumb;
                if (taskCrumb.getTask() == task) {
                    crumbs.remove(taskCrumb);
                    return;
                }
            }
        }
        throw new IsisException("No crumb found for " + task);
    }


    public Request cancelTask(final Task task) {
        if (task != null) {
            endTask(task);
        }

        // REVIEW does this take us back to the right object?
        final Crumb crumb = crumbs.get(crumbs.size() - 1);
        return crumb.changeContext();
    }

    private boolean isTask() {
        final int index = crumbs.size() - 1;
        return index >= 0 && crumbs.get(index) instanceof TaskCrumb;
    }

    

    // ////////////////////////////////////////////////////
    // Messages 
    // ////////////////////////////////////////////////////

    public List<String> getMessages() {
        return messages;
    }

    public String getMessage(final int i) {
        return messages.get(i);
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public String getWarning(final int i) {
        return warnings.get(i);
    }

    public void setMessagesAndWarnings(final List<String> messages, final List<String> warnings) {
        this.messages.clear();
        this.messages.addAll(messages);
        this.warnings.clear();
        this.warnings.addAll(warnings);
    }

    public void clearMessagesAndWarnings() {
        messages.clear();
        warnings.clear();
    }


    public void listHistory(final Context context, final Block navigation) {
        getObjectHistory().listObjects(context, navigation);
    }

    public void addObjectToHistory(final String idString) {
        getObjectHistory().addObject(idString);
    }

    public void addCollectionToHistory(final String idString) {
        getObjectHistory().addCollection(idString);
    }

    // ////////////////////////////////////////////////////
    // 
    // ////////////////////////////////////////////////////


    public void updateVersion(final ObjectAdapter adapter) {
        if (adapter.isTransient()) {
            return;
        }

        // TODO refactor this for clarity: removes existing mapping and replaces
        // it with a new one as it
        // contains the new version
        final String id = mapObject(adapter);
        if (id != null) {
            final RootAdapterMapping mapping = new PersistentRootAdapterMapping(adapter);
            objectMap.put(id, mapping);
        }
    }


    // ////////////////////////////////////////////////////
    // Debug 
    // ////////////////////////////////////////////////////

    public void debug(final DebugBuilder debug) {
        debug.startSection("Web Session Context");
        debug.appendAsHexln("hash", hashCode());
        debug.appendln("session", session);
        debug.appendln("is valid", isValid);
        debug.appendln("next id", max);
        debug.appendln("factory", componentFactory);
        debug.appendln("is task", isTask());

        debug.appendln("crumbs (" + crumbs.size() + ")");

        debug.indent();
        for (int i = 0; i < crumbs.size(); i++) {
            final Crumb crumb = crumbs.get(i);
            debug.appendln(i + 1 + ". " + crumb);
            debug.indent();
            crumb.debug(debug);
            debug.unindent();
        }
        debug.unindent();

        debug.startSection("Objects");
        for (final String id : objectMap.keySet()) {
            final RootAdapterMapping object = objectMap.get(id);
            debug.appendln(id + " -> " + object.getOidStr());
            debug.indent();
            object.debugData(debug);
            debug.unindent();
        }
        debug.endSection();

        debug.startSection("Collections");
        for (final String id : collectionMap.keySet()) {
            final CollectionMapping coll = collectionMap.get(id);
            debug.appendln(id + " -> collection of " + coll.getElementSpecification().getPluralName());
            coll.debug(debug);
        }
        debug.endSection();

        debug.startSection("Actions");
        debugMap(debug, actionMap);
        debug.endSection();

        debug.startSection("History");
        getObjectHistory().debug(debug);
        debug.endSection();

        debug.endSection();
    }

    private void debugMap(final DebugBuilder debug, final Map<String,?> map) {
        final Iterator<String> names = map.keySet().iterator();
        while (names.hasNext()) {
            final String name = names.next();
            debug.appendln(name + " -> " + map.get(name));
        }
    }


    // ////////////////////////////////////////////////////
    // Non-serializable
    // ////////////////////////////////////////////////////
    
    private ObjectHistory getObjectHistory() {
        if(history == null) {
            history = new ObjectHistory();
        }
        return history;
    }


    // ////////////////////////////////////////////////////
    // Dependencies (from context)
    // ////////////////////////////////////////////////////

    protected UserProfile getUserProfile() {
        return IsisContext.getUserProfile();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }


    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected UpdateNotifier getUpdateNotifier() {
        return IsisContext.getUpdateNotifier();
    }



}

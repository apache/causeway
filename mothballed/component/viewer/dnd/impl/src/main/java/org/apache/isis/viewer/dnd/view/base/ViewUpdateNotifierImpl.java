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

package org.apache.isis.viewer.dnd.view.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.MessageBroker;
import org.apache.isis.core.runtime.system.transaction.UpdateNotifier;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewUpdateNotifier;
import org.apache.isis.viewer.dnd.view.collection.RootCollection;

public class ViewUpdateNotifierImpl implements ViewUpdateNotifier {
    
    private static final Logger LOG = LoggerFactory.getLogger(ViewUpdateNotifierImpl.class);
    
    protected Map<ObjectAdapter, List<View>> viewListByAdapter = Maps.newHashMap();

    @Override
    public void add(final View view) {
        final Content content = view.getContent();
        if (content != null && content.isObject()) {
            final ObjectAdapter adapter = content.getAdapter();

            if (adapter != null) {
                List<View> viewsToNotify;

                if (viewListByAdapter.containsKey(adapter)) {
                    viewsToNotify = viewListByAdapter.get(adapter);
                } else {
                    viewsToNotify = new Vector<View>();
                    viewListByAdapter.put(adapter, viewsToNotify);
                }

                if (viewsToNotify.contains(view)) {
                    throw new IsisException(view + " already being notified");
                }
                viewsToNotify.add(view);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("added " + view + " to observers for " + adapter);
                }
            }
        }
    }

    @Override
    public void debugData(final DebugBuilder buf) {
        for(Map.Entry<ObjectAdapter, List<View>> mapEntry: viewListByAdapter.entrySet()) {
            final ObjectAdapter objectAdapter = mapEntry.getKey();
            final List<View> viewsToNotify = mapEntry.getValue();
            
            buf.append("Views for " + objectAdapter + " \n");

            for(View view: viewsToNotify) {
                buf.append("        " + view);
                buf.append("\n");
            }
            buf.append("\n");
        }
    }

    @Override
    public String debugTitle() {
        return "Views for object details (observers)";
    }

    @Override
    public void remove(final View view) {
        final Content content = view.getContent();
        if (content == null || !content.isObject()) {
            // nothing to do
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing " + content + " for " + view);
        }

        final ObjectAdapter object = ((ObjectContent) content).getObject();
        if (object != null) {
            if (!viewListByAdapter.containsKey(object)) {
                throw new IsisException("Tried to remove a non-existant view " + view + " from observers for " + object);
            }
            
            List<View> viewsToNotify = viewListByAdapter.get(object);
            for(View v: viewsToNotify) {
                if (view == v.getView()) {
                    viewsToNotify.remove(v);
                    LOG.debug("removed " + view + " from observers for " + object);
                    break;
                }
            }

            if (viewsToNotify.size() == 0) {
                viewListByAdapter.remove(object);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("removed observer list for " + object);
                }

                // TODO need to do garbage collection instead
                // ObjectAdapterLoader loader = Isis.getObjectLoader();
                // loader.unloaded((ObjectAdapter) object);
            }
        }
    }

    public void shutdown() {
        viewListByAdapter.clear();
    }

    @Override
    public void invalidateViewsForChangedObjects() {
        for (final ObjectAdapter object : getUpdateNotifier().getChangedObjects()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("invalidate views for " + object);
            }
            final List<View> viewsVector = viewListByAdapter.get(object);
            if (viewsVector == null) {
                continue;
            }
            for(View view: viewsVector) {
                LOG.debug("   - " + view);
                view.getView().invalidateContent();
            }
        }
    }

    @Override
    public void removeViewsForDisposedObjects() {
        for (final ObjectAdapter objectToDispose : getUpdateNotifier().getDisposedObjects()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("dispose views for " + objectToDispose);
            }
            final List<View> viewsForObject = viewListByAdapter.get(objectToDispose);
            if (viewsForObject == null) {
                continue;
            }
            removeViews(viewsForObject);
            final List<View> remainingViews = viewListByAdapter.get(objectToDispose);
            if (remainingViews != null && remainingViews.size() > 0) {
                getMessageBroker().addWarning("There are still views (within other views) for the disposed object " + objectToDispose.titleString() + ".  Only objects that are shown as root views can be properly disposed of");
            } else {
                getPersistenceSession().removeAdapter(objectToDispose);
            }
        }
    }

    private void removeViews(final List<View> viewsForObject) {
        //final View[] viewsArray = new View[viewsForObject.size()];
        //viewsForObject.copyInto(viewsArray);
        
        final List<View> viewsArray = Lists.newArrayList(viewsForObject); // take a copy
        
        final View[] viewsOnWorkspace = viewsArray.get(0).getWorkspace().getSubviews();
        
        for (final View element : viewsArray) {
            final View view = element.getView();
            for (final View viewOnWorkspace : viewsOnWorkspace) {
                if (view == viewOnWorkspace) {
                    LOG.debug("   (root removed) " + view);
                    view.getView().dispose();
                    break;
                }
            }

            for (final View element2 : viewsOnWorkspace) {
                if (element2.getContent() instanceof RootCollection) {
                    final View[] subviewsOfRootView = element2.getSubviews();
                    for (final View element3 : subviewsOfRootView) {
                        if (element3 == view) {
                            LOG.debug("   (element removed) " + view);
                            view.getView().dispose();
                        }
                    }
                }
            }

            for (final View element2 : viewsOnWorkspace) {
                if (element2.contains(view)) {
                    LOG.debug("   (invalidated) " + view);
                    final View parent = view.getParent();
                    parent.invalidateContent();
                }
            }

        }

    }

    // ////////////////////////////////////////////////////////////////
    // Dependencies (from singleton)
    // ////////////////////////////////////////////////////////////////

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private static MessageBroker getMessageBroker() {
        return IsisContext.getMessageBroker();
    }

    private static UpdateNotifier getUpdateNotifier() {
        return IsisContext.inSession() ? IsisContext.getUpdateNotifier() : new NoOpUpdateNotifier();
    }

}

class NoOpUpdateNotifier implements UpdateNotifier {

    @Override
    public void addChangedObject(final ObjectAdapter object) {
    }

    @Override
    public void addDisposedObject(final ObjectAdapter adapter) {
    }

    @Override
    public void clear() {
    }

    @Override
    public void ensureEmpty() {
    }

    @Override
    public List<ObjectAdapter> getChangedObjects() {
        return new ArrayList<ObjectAdapter>();
    }

    @Override
    public List<ObjectAdapter> getDisposedObjects() {
        return new ArrayList<ObjectAdapter>();
    }

}

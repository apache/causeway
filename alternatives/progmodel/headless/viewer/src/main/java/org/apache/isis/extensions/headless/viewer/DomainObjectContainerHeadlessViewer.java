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


package org.apache.isis.extensions.headless.viewer;

import java.util.List;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.events.InteractionEvent;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.extensions.headless.applib.HeadlessViewer;
import org.apache.isis.extensions.headless.applib.listeners.InteractionListener;
import org.apache.isis.extensions.headless.viewer.internal.HeadlessViewerImpl;


/**
 * A combined {@link DomainObjectContainer} and {@link HeadlessViewer}. 
 */
public class DomainObjectContainerHeadlessViewer extends DomainObjectContainerDefault implements HeadlessViewer {

    private HeadlessViewer headlessViewerDelegate;


    // /////////////////////////////////////////////////////////////
    // Views
    // /////////////////////////////////////////////////////////////

    public <T> T view(final T domainObject) {
    	return headlessViewerDelegate.view(domainObject);
    }

    public <T> T view(final T domainObject, ExecutionMode mode) {
    	return headlessViewerDelegate.view(domainObject, mode);
    }

    public boolean isView(final Object possibleView) {
    	return headlessViewerDelegate.isView(possibleView);
    }

    // /////////////////////////////////////////////////////////////
    // Listeners
    // /////////////////////////////////////////////////////////////

    public List<InteractionListener> getListeners() {
        return headlessViewerDelegate.getListeners();
    }

    public boolean addInteractionListener(final InteractionListener listener) {
        return headlessViewerDelegate.addInteractionListener(listener);
    }

    public boolean removeInteractionListener(final InteractionListener listener) {
        return headlessViewerDelegate.removeInteractionListener(listener);
    }

    public void notifyListeners(final InteractionEvent interactionEvent) {
    	headlessViewerDelegate.notifyListeners(interactionEvent);
    }

    
    
    // /////////////////////////////////////////////////////////////
    // Dependencies (due to *Aware)
    // /////////////////////////////////////////////////////////////

    /**
     * As per superclass, but also initializes the delegate {@link HeadlessViewer}.
     */
    @Override
    public void setRuntimeContext(RuntimeContext runtimeContext) {
    	super.setRuntimeContext(runtimeContext);
    	headlessViewerDelegate = new HeadlessViewerImpl(runtimeContext);
    }


}

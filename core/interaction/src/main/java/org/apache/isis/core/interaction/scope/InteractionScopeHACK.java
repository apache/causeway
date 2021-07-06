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
package org.apache.isis.core.interaction.scope;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import lombok.val;

/**
 * @since 2.0
 */
@Log4j2
class InteractionScopeHACK implements Scope, InteractionScopeLifecycleHandler {

    @Inject private InteractionLayerTracker iInteractionLayerTracker;

    @Data(staticConstructor = "of")
    private static class ScopedObject {
        final String name;
        Object instance;
        Runnable destructionCallback;
        void preDestroy() {
            log.debug("destroy isis-interaction scoped {}", name);
            if(destructionCallback!=null) {
                destructionCallback.run();
            }
        }
    }


    static class ScopedObjects extends HashMap<String, ScopedObject> {
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {

        ensureInteractionLayerTrackerInjected();

        if(!iInteractionLayerTracker.isInInteraction()) {
            throw _Exceptions.illegalState("Creation of bean %s with @InteractionScope requires the "
                    + "calling %s to have an open Interaction on the thread-local stack. Running into "
                    + "this issue might be caused by use of ... @Inject MyScopedBean bean ..., instead of "
                    + "... @Inject Provider<MyScopedBean> provider ...", name, _Probe.currentThreadId());
        }

        val interaction = iInteractionLayerTracker.currentInteractionElseFail();
        val scopedObjects = interaction.computeAttributeIfAbsent(ScopedObjects.class, clazz -> new ScopedObjects());

        val existingScopedObject = scopedObjects.get(name);
        if(existingScopedObject!=null) {
            return existingScopedObject.getInstance();
        }

        val newScopedObject = ScopedObject.of(name);
        scopedObjects.put(name, newScopedObject); // just set a stub with a name only

        log.debug("create new isis-interaction scoped {}", name);
        newScopedObject.setInstance(objectFactory.getObject()); // triggers call to registerDestructionCallback

        return newScopedObject.getInstance();
    }


    @Override
    public Object remove(final String name) {
        ensureInteractionLayerTrackerInjected();
        return iInteractionLayerTracker.currentInteraction()
                .map(interaction -> {
                    final ScopedObjects scopedObjects = interaction.getAttribute(ScopedObjects.class);
                    if (scopedObjects == null) {
                        return null;
                    }
                    ScopedObject removedIfAny = scopedObjects.remove(name);
                    if(scopedObjects.isEmpty()) {
                        interaction.removeAttribute(ScopedObjects.class);
                    }
                    if (removedIfAny == null) {
                        return null;
                    }
                    // javadoc for Scope says to remove destruction callback.
                    removedIfAny.destructionCallback = null;
                    return removedIfAny.instance;
                })
                .orElse(null);
    }

    private void ensureInteractionLayerTrackerInjected() {
        if (iInteractionLayerTracker == null) {
            throw _Exceptions.illegalState("Management of beans with @InteractionScope requires the "
                    + "InteractionScopeBeanFactoryPostProcessor registered and initialized.");
        }
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        ensureInteractionLayerTrackerInjected();
        iInteractionLayerTracker.currentInteraction()
                .ifPresent(interaction -> {
                    final ScopedObjects scopedObjects = interaction.getAttribute(ScopedObjects.class);
                    if (scopedObjects == null) {
                        return;
                    }
                    final ScopedObject scopedObject = scopedObjects.get(name);
                    if (scopedObject == null) {
                        return;
                    }
                    scopedObject.destructionCallback = callback;
                });
    }

    @Override
    public Object resolveContextualObject(String key) {
        // null by convention if not supported
        return null;
    }

    @Override
    public String getConversationId() {
        // null by convention if not supported
        return iInteractionLayerTracker.getInteractionId()
                .map(UUID::toString)
                .orElse(null);
    }

    @Override
    public void onTopLevelInteractionOpened() {
        // nothing to do
    }

    @Override
    public void onTopLevelInteractionPreDestroy() {
        ensureInteractionLayerTrackerInjected();
        iInteractionLayerTracker.currentInteraction()
                .ifPresent(interaction -> {
                    final ScopedObjects scopedObjects = interaction.getAttribute(ScopedObjects.class);
                    if (scopedObjects == null) {
                        return;
                    }
                    val scopedObjectValues = scopedObjects.values();
                    scopedObjectValues.forEach(ScopedObject::preDestroy);
                    scopedObjects.clear();
                });
    }

    @Override
    public void onTopLevelInteractionClosed() {
        ensureInteractionLayerTrackerInjected();
        iInteractionLayerTracker.currentInteraction()
                .ifPresent(interaction -> {
                    final ScopedObjects scopedObjects = interaction.getAttribute(ScopedObjects.class);
                    if (scopedObjects == null) {
                        return;
                    }
                    scopedObjects.clear();
                    interaction.removeAttribute(ScopedObjects.class);
                });
    }

}

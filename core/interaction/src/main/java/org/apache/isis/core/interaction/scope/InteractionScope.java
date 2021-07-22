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

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Data;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0
 */
@Log4j2
class InteractionScope
implements
    Scope,
    InteractionScopeLifecycleHandler {

    private final BeanFactory beanFactory;

    public InteractionScope(final BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

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

    /**
     * An alternative design would be to store the ScopedObjects in the top-level
     * {@link org.apache.isis.applib.services.iactn.Interaction}'s
     * {@link org.apache.isis.applib.services.iactn.Interaction#getAttribute(Class) attributes}.
     *
     * <p>
     * Why the top-level? Because this class is only interested in that top-level interaction (see
     * {@link InteractionScopeLifecycleHandler#onTopLevelInteractionPreDestroy()}), not any of the stacked.
     * </p>
     */
    private ThreadLocal<Map<String, ScopedObject>> scopedObjects = ThreadLocal.withInitial(_Maps::newHashMap);

    /**
    * @return an instance of the single bean matching the required type (InteractionService)
    * @throws NoSuchBeanDefinitionException if no bean of the given type was found
    * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
    * @throws BeansException if the bean could not be created
    */
    private InteractionService interactionService() {
        return beanFactory.getBean(InteractionService.class);
    }

    @Override
    public Object get(final String name, final ObjectFactory<?> objectFactory) {

        final InteractionService interactionService;
        try {
            interactionService = interactionService();
        } catch (Exception cause) {
            throw _Exceptions.illegalState(
                    cause,
                    "Creation of bean %s with @InteractionScope requires the "
                    + "InteractionScopeBeanFactoryPostProcessor registered and initialized.", name);
        }

        if(interactionService.isInInteraction()) {
            throw _Exceptions.illegalState("Creation of bean %s with @InteractionScope requires the "
                    + "calling %s to have an open Interaction on the thread-local stack. Running into "
                    + "this issue might be caused by use of ... @Inject MyScopedBean bean ..., instead of "
                    + "... @Inject Provider<MyScopedBean> provider ...", name, _Probe.currentThreadId());
        }

        val existingScopedObject = scopedObjects.get().get(name);
        if(existingScopedObject!=null) {

            if(log.isDebugEnabled()) {
                log.debug("INTERACTION_SCOPE [{}:{}] reuse existing {}",
                        _Probe.currentThreadId(),
                        getConversationId(),
                        Integer.toHexString(existingScopedObject.hashCode()));
            }

            return existingScopedObject.getInstance();
        }

        val newScopedObject = ScopedObject.of(name);
        scopedObjects.get().put(name, newScopedObject); // just set a stub with a name only

        log.debug("create new isis-interaction scoped {}", name);
        newScopedObject.setInstance(objectFactory.getObject()); // triggers call to registerDestructionCallback

        if(log.isDebugEnabled()) {
            log.debug("INTERACTION_SCOPE [{}:{}] create new {}",
                _Probe.currentThreadId(),
                getConversationId(),
                Integer.toHexString(newScopedObject.hashCode()));
        }

        return newScopedObject.getInstance();
    }

    @Override
    public Object remove(final String name) {
        throw new UnsupportedOperationException("use IsisInteractionScope.removeAll instead");
    }

    @Override
    public void registerDestructionCallback(final String name, final Runnable callback) {
        val scopedObject = scopedObjects.get().get(name);
        if(scopedObject!=null) {
            scopedObject.setDestructionCallback(callback);
        }
        // otherwise something is off
    }

    @Override
    public Object resolveContextualObject(final String key) {
        // null by convention if not supported
        return null;
    }

    @Override
    public String getConversationId() {
        // null by convention if not supported
        return interactionService().getInteractionId()
                .map(UUID::toString)
                .orElse(null);
    }

    @Override
    public void onTopLevelInteractionOpened() {
        // nothing to do
        log.debug("INTERACTION_SCOPE opened");
    }

    @Override
    public void onTopLevelInteractionPreDestroy() {
        log.debug("INTERACTION_SCOPE pre-destroy");
        scopedObjects.get().values()
        .forEach(scopedObject->{
            try {
                scopedObject.preDestroy();
            } catch (Exception e) {
                log.error(e);
            }
        });
    }

    @Override
    public void onTopLevelInteractionClosed() {
        log.debug("INTERACTION_SCOPE closed");
        scopedObjects.remove();
    }

}

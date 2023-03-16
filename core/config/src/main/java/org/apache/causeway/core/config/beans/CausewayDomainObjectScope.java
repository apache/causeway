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
package org.apache.causeway.core.config.beans;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

@RequiredArgsConstructor
@Log4j2
class CausewayDomainObjectScope implements Scope {

    private final BeanFactory beanFactory;

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        if (!interactionService().isInInteraction()) {
            throw new IllegalStateException("No Causeway Interaction is currently active");
        }
        return bookmarkService()
                .lookup(Bookmark.parseElseFail(name))
                .orElseThrow(() -> new IllegalStateException(String.format("Could not lookup object from Bookmark '%s'", name)));
    }

    @Override
    public Object remove(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerDestructionCallback(String name, Runnable runnable) {
        if (!interactionService().isInInteraction()) {
            throw new IllegalStateException("No Causeway Interaction is currently active");
        }
        log.warn("registerDestructionCallback not supported for 'causeway-domain-object' scope");
    }

    @Override
    public Object resolveContextualObject(String key) {
        return bookmarkService();
    }

    @Override
    public String getConversationId() {
        if (!interactionService().isInInteraction()) {
            throw new IllegalStateException("No Causeway Interaction is currently active");
        }
        return interactionService()
                .currentInteraction()
                .map(Interaction::getInteractionId)
                .map(UUID::toString)
                .orElseThrow(() -> new IllegalStateException("Could not obtain interactionId from current Causeway Interaction"));
    }

    private BookmarkService bookmarkService() {
        return beanFactory.getBean(BookmarkService.class);
    }

    private InteractionService interactionService() {
        return beanFactory.getBean(InteractionService.class);
    }
}

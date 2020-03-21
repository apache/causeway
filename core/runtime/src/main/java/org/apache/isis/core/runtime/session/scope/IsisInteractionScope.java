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
package org.apache.isis.core.runtime.session.scope;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.debug._Probe;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.runtime.session.IsisSessionTracker;

import lombok.Data;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0
 */
@Log4j2
class IsisInteractionScope implements Scope, IsisInteractionScopeCloseListener {
    
    @Inject private IsisSessionTracker isisSessionTracker;

    @Data(staticConstructor = "of")
    private static class ScopedObject {
        final String name;
        Object instance;
        Runnable destructionCallback;
        void preDestroy() {
            log.debug("destroy isis-session scoped {}", name);
            if(destructionCallback!=null) {
                destructionCallback.run();
            }
        }
    }
    
    private ThreadLocal<Map<String, ScopedObject>> scopedObjects = ThreadLocal.withInitial(_Maps::newHashMap);

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        
        if(isisSessionTracker==null) {
            throw _Exceptions.illegalState("Creation of bean %s with @IsisSessionScope requires the "
                    + "IsisSessionScopeBeanFactoryPostProcessor registered and initialized.", name);
        }
        
        if(!isisSessionTracker.isInSession()) {
            throw _Exceptions.illegalState("Creation of bean %s with @IsisSessionScope requires the "
                    + "calling %s to have an open IsisSession on the thread-local stack. Running into "
                    + "this issue might be caused by use of ... @Inject MyScopedBean bean ..., instead of "
                    + "... @Inject Provider<MyScopedBean> provider ...", name, _Probe.currentThreadId());
        }
        
        val existingScopedObject = scopedObjects.get().get(name);
        if(existingScopedObject!=null) {
            return existingScopedObject.getInstance();
        }
        
        val newScopedObject = ScopedObject.of(name); 
        scopedObjects.get().put(name, newScopedObject); // just set a stub with a name only
        
        log.debug("create new isis-session scoped {}", name);
        newScopedObject.setInstance(objectFactory.getObject()); // triggers call to registerDestructionCallback
        
        return newScopedObject.getInstance();
    }

    @Override
    public Object remove(String name) {
        throw new UnsupportedOperationException("use IsisSessionScope.removeAll instead");
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        val scopedObject = scopedObjects.get().get(name);
        if(scopedObject!=null) {
            scopedObject.setDestructionCallback(callback);
        }
        // otherwise something is off
    }

    @Override
    public Object resolveContextualObject(String key) {
        // null by convention if not supported
        return null;
    }

    @Override
    public String getConversationId() {
        // null by convention if not supported
        return isisSessionTracker.getConversationId().orElse(null);
    }
    
    @Override
    public void preTopLevelIsisSessionClose() {
        removeAll();
    }
    
    private void removeAll() {
        try {
            scopedObjects.get().values().forEach(ScopedObject::preDestroy);
        } finally {
            scopedObjects.remove();    
        }
    }
    
}

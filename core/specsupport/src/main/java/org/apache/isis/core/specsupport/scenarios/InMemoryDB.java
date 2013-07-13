/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.specsupport.scenarios;

import java.util.Map;

import com.google.common.collect.Maps;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

import org.apache.isis.applib.DomainObjectContainer;

/**
 * To support unit-scope specifications, intended to work with mocks.
 * 
 * <p>
 * The {@link #findByXxx(Class, Strategy)} provides an implementation of a JMock
 * {@link Action} that can simulate searching for an object from a database, and 
 * optionally automatically creating a new one if {@link Strategy specified}.
 * 
 * <p>
 * If objects are created, then (mock) services are automatically injected.  This is performed by
 * searching for <tt>injectXxx()</tt> methods.  The (mock) {@link DomainObjectContainer container}
 * is also automatically injected, through the <tt>setXxx</tt> method.
 * 
 * <p>
 * Finally, note that the {@link #init(Object, String) init} hook method allows subclasses to
 * customize the state of any objects created.
 */
public class InMemoryDB {
    
    private final ScenarioExecution scenarioExecution;

    public InMemoryDB(ScenarioExecution scenarioExecution) {
        this.scenarioExecution = scenarioExecution;
    }
    
    public static class EntityId {
        private final String type;
        private final String id;
        public EntityId(Class<?> type, String id) {
            this.type = type.getName();
            this.id = id;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            InMemoryDB.EntityId other = (InMemoryDB.EntityId) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }
        @Override
        public String toString() {
            return "EntityId [type=" + type + ", id=" + id + "]";
        }
    }
    
    private Map<InMemoryDB.EntityId, Object> objectsById = Maps.newHashMap();
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T get(final Class<T> cls, final String id) {
        Class type = cls;
        while(type != null) {
            // search for this class and all superclasses
            final InMemoryDB.EntityId entityId = new EntityId(cls, id);
            final Object object = objectsById.get(entityId);
            if(object != null) {
                return (T) object;
            }
            type = type.getSuperclass();
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T getElseCreate(final Class<T> cls, final String id) {
        final Object object = get(cls, id);
        if(object != null) { 
            return (T) object;
        }
        Object obj = instantiateAndInject(cls);
        init(obj, id);
        
        Class type = cls;
        // put for this class and all superclasses
        while(type != null) {
            final InMemoryDB.EntityId entityId = new EntityId(cls, id);
            objectsById.put(entityId, obj);
            type = type.getSuperclass();
        }
        return (T) obj;
    }

    private Object instantiateAndInject(Class<?> cls)  {
        try {
            return scenarioExecution.injectServices(cls.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum Strategy {
        STRICT,
        AUTOCREATE
    }
    
    public Action findByXxx(final Class<?> cls, final InMemoryDB.Strategy strategy) {
        return new Action() {
            
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                if(invocation.getParameterCount() != 1) {
                    throw new IllegalArgumentException("intended for action of findByXxx");
                }
                final Object argObj = invocation.getParameter(0);
                if(!(argObj instanceof String)) {
                    throw new IllegalArgumentException("Argument must be a string");
                } 
                String arg = (String) argObj;
                if(strategy == Strategy.AUTOCREATE) {
                    return getElseCreate(cls, arg);
                } else {
                    return get(cls, arg);
                }
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText("findByXxx for " + cls.getName());
            }
        };
    }
    
    /**
     * Hook to initialize if possible.
     */
    protected void init(Object obj, String id) {
    }
}
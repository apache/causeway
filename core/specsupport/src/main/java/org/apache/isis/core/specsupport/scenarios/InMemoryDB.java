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

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.isis.schema.utils.InteractionDtoUtils.Strategy;
import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.functions._Predicates;

/**
 * Utility class to support the writing of unit-scope specs.
 *
 * <p>
 * The {@link #finds(Class, Strategy)} provides an implementation of a JMock
 * {@link Action} that can simulate searching for an object from a database, and
 * optionally automatically creating a new one if {@link Strategy specified}.
 *
 * <p>
 * If objects are created, then (mock) services are automatically injected.  This is performed by
 * searching for <tt>injectXxx()</tt> methods.
 *
 * <p>
 * Finally, note that the {@link #init(Object, String) init} hook method allows subclasses to
 * customize the state of any objects created.
 *
 * @deprecated - with no replacement
 */
@Deprecated
public class InMemoryDB {

    private final ScenarioExecution scenarioExecution;

    public InMemoryDB(ScenarioExecution scenarioExecution) {
        this.scenarioExecution = scenarioExecution;
    }

    public static class EntityId {
        private final Class<?> type;
        private final String id;
        public EntityId(Class<?> type, String id) {
            this.type = type;
            this.id = id;
        }
        Class<?> getType() {
            return type;
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

    private Map<InMemoryDB.EntityId, Object> objectsById = _Maps.newHashMap();

    /**
     * Returns the object if exists, but will NOT instantiate a new one if not present.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T getNoCreate(final Class<T> cls, final String id) {
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

    /**
     * Returns the object if exists, else will instantiate and save a new one if not present.
     *
     * <p>
     * The new object will have services injected into it (through the {@link ScenarioExecution#injectServices(Object)})
     * and will be initialized through the {@link #init(Object, String) init hook} method.
     */
    @SuppressWarnings({ "unchecked" })
    public <T> T getElseCreate(final Class<T> cls, final String id) {
        final Object object = getNoCreate(cls, id);
        if(object != null) {
            return (T) object;
        }
        Object obj = instantiateAndInject(cls);
        init(obj, id);

        return put(cls, id, obj);
    }

    /**
     * Put an object into the database.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> T put(final Class<T> cls, final String id, Object obj) {
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

    /**
     * Returns a JMock {@link Action} to return an instance of the provided class.
     *
     * <p>
     * If the object is not yet held in memory, it will be automatically created,
     * as per {@link #getElseCreate(Class, String)}.
     *
     * <p>
     * This {@link Action} can only be set for expectations to invoke a method
     * accepting a single string argument.  This string argument is taken to be an
     * identifier for the object (and is used in the caching of that object in memory).
     */
    public Action finds(final Class<?> cls) {
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
                return getElseCreate(cls, arg);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("finds an instance of " + cls.getName());
            }
        };
    }

    public <T> List<T> findAll(Class<T> cls) {
        return find(cls, _Predicates.<T>alwaysTrue());
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> find(Class<T> cls, Predicate<T> predicate) {
        final List<T> list = _Lists.newArrayList();
        for (EntityId entityId : objectsById.keySet()) {
            if(cls.isAssignableFrom(entityId.getType())) {
                final T object = (T) objectsById.get(entityId);
                if(predicate.test(object)) {
                    list.add(object);
                }
            }
        }
        return list;
    }

    /**
     * Hook to initialize if possible.
     *
     * <p>
     * The provided string is usually taken to be some sort of unique identifier for the object
     * (unique in the context of any given scenario, that is).
     */
    protected void init(Object obj, String str) {
    }


}
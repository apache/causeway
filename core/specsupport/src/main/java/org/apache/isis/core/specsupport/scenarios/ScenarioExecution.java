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

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.fixtures.InstallableFixture;
import org.apache.isis.applib.services.wrapper.WrapperFactory;


/**
 * Represents the currently executing scenario, allowing information to be shared 
 * between Cucumber step definitions (for unit- or integration- scoped), and also for
 * integration tests.
 * 
 * <p>
 * Two types of information are available:
 * <ul>
 * <li>First, there are the domain services, provided using the {@link #service(Class) method}.  
 * If running at unit-scope, then these will most likely be mocked services (and not all services
 * will necessarily be available).  If running at integration-scope, then these will most likely
 * be real instances, eg wired to the backend database.</li>
 * <li>Second, there is a map of identified objects.  This is predominantly for Cucumber
 * step definitions (either unit- or integration-scoped), such that information can be passed
 * between steps in a decoupled fashion.
 * </ul>
 * 
 * <p>
 * When instantiated, this object binds itself to the current thread (using a {@link ThreadLocal}).  
 * 
 * <p>
 * Subclasses may tailor the world for specific types of tests; for example the
 * <tt>IntegrationScenarioExecution</tt> provides additional support for fixtures and 
 * transaction management, used both by integration-scoped specs and by integration tests.
 */
public class ScenarioExecution {
    
    private static ThreadLocal<ScenarioExecution> current = new ThreadLocal<ScenarioExecution>();
    
    public static ScenarioExecution current() {
        final ScenarioExecution execution = current.get();
        if(execution == null) {
            throw new IllegalStateException("Scenario has not yet been instantiated by Cukes");
        } 
        return execution;
    }


    // //////////////////////////////////////

    protected final DomainServiceProvider dsp;
    
    public ScenarioExecution(final DomainServiceProvider dsp) {
        this.dsp = dsp;
        current.set(this);
    }

    /**
     * Returns a domain service of the specified type, ensuring that
     * it is available.
     * 
     * @throws IllegalStateException if not available
     */
    public <T> T service(Class<T> cls) {
        final T service = dsp.getService(cls);
        if(service == null) {
            throw new IllegalStateException(
                    "No service of type "
                    + cls.getSimpleName()
                    + " available");
        }
        return service;
    }

    /**
     * Convenience method, returning the {@link DomainObjectContainer},
     * first ensuring that it is available.
     * 
     * @throws IllegalStateException if not available
     */
    public DomainObjectContainer container() {
        final DomainObjectContainer container = dsp.getContainer();
        if(container == null) {
            throw new IllegalStateException(
                    "No DomainObjectContainer available");
        }
        return container;
    }

    /**
     * Convenience method, returning the {@link WrapperFactory} domain service,
     * first ensuring that it is available.
     * 
     * @throws IllegalStateException if not available
     */
    public WrapperFactory wrapperFactory() {
        return service(WrapperFactory.class);
    }


    // //////////////////////////////////////

    /**
     * Key for objects stored by steps in the scenario.
     * 
     * <p>
     * Objects can be identified in a variety of manners:
     * <ul>
     * <li>a fully qualified object provides both its type and a (unique) id; for example 'lease OXF-TOPMODEL-001'</li>
     * <li>a named object provides only its id; for example 'OXF-TOPMODEL-001'</li>
     * <li>a typed object provides only its type; for example 'the lease'.</li>
     * </ul>
     * 
     * <p>
     * Because of the second rule, the id should be unique in and of itself.
     * 
     * <p>
     * The expectation is that scenarios will use the first form (fully qualified) the first time that an
     * object is introduced within a scenario.  Thereafter either of the other forms may be used.
     * In the case of a typed object (eg "the lease"), the most recently "touched" object of that type
     * is returned.
     */
    public static class VariableId {
        private final String type;
        private final String id;
        public VariableId(String type, String id) {
            this.type = type;
            this.id = id;
        }

        /**
         * eg 'lease'
         */
        public String getType() {
            return type;
        }
        /**
         * eg 'OXF-TOPMODEL-001'
         */
        public String getId() {
            return id;
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
            VariableId other = (VariableId) obj;
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
            return "VariableId [type=" + type + ", id=" + id + "]";
        }
    }

    private final Map<VariableId, Object> objectByVariableId = Maps.newLinkedHashMap();
    private final Map<String, Object> objectsById = Maps.newLinkedHashMap();
    
    private final Map<String, Object> mostRecent = Maps.newHashMap();

    public void put(String type, String id, Object value) {
        objectByVariableId.put(new VariableId(type, id), value);
        mostRecent.put(type, value);
    }

    /**
     * Retrieve an object previously used in the scenario.
     * 
     * <p>
     * Must specify type and/or id.
     * 
     * @see VariableId - for rules on what constitutes an identifier.
     */
    public Object get(String type, String id) {
        if(type != null && id != null) {
            final VariableId variableId = new VariableId(type,id);
            final Object value = objectByVariableId.get(variableId);
            if(value != null) {
                mostRecent.put(type, value);
                return value;
            } 
            throw new IllegalStateException("No such " + variableId);
        }
        if(type != null && id == null) {
            return mostRecent.get(type);
        }
        if(type == null && id != null) {
            final Object value = objectsById.get(id);
            if(value != null) {
                mostRecent.put(type, value);
            } 
            return value;
        }
        throw new IllegalArgumentException("Must specify type and/or id");
    }

    /**
     * As {@link #get(String, String)}, but downcasting to the provided class.
     */
    @SuppressWarnings("unchecked")
    public <X> X get(String type, String id, Class<X> cls) {
        return (X) get(type, id);
    }

    // //////////////////////////////////////

    /**
     * Install arbitrary fixtures, eg before an integration tests or as part of a 
     * Cucumber step definitions or hook.
     * 
     * <p>
     * This implementation has a no-op, but subclasses of this class tailored to
     * supporting integration specs/tests are expected to override.
     */
    public void install(InstallableFixture... fixtures) {
        // do nothing
    }

    // //////////////////////////////////////

    /**
     * For Cucumber hooks to call, performing transaction management around each step.
     * 
     * <p>
     * This implementation has a no-op, but subclasses of this class tailored to
     * supporting integration specs are expected to override.  (Integration tests can use
     * the <tt>IsisTransactionRule</tt> to do transaction management transparently).
     */
    public void beginTran() {
        // do nothing
    }

    /**
     * For Cucumber hooks to call, performing transaction management around each step.
     * 
     * <p>
     * This implementation has a no-op, but subclasses of this class tailored to
     * supporting integration specs are expected to override.  (Integration tests can use
     * the <tt>IsisTransactionRule</tt> to do transaction management transparently).
     */
    public void endTran(boolean ok) {
        // do nothing
    }

}

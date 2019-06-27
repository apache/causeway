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
package org.apache.isis.specsupport.scenarios;

import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.internal.ExpectationBuilder;

import org.apache.isis.applib.fixtures.InstallableFixture;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.metamodel.exceptions.MetaModelException;
import org.apache.isis.security.authentication.AuthenticationSession;

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
 *
 * @deprecated - to be removed in 2.0, will support BDD for integration tests only
 */
@Deprecated
public abstract class ScenarioExecution {

    private static ThreadLocal<ScenarioExecution> current = new ThreadLocal<ScenarioExecution>();

    public static ScenarioExecution peek() {
        return current.get();
    }

    public static ScenarioExecution current() {
        final ScenarioExecution execution = current.get();
        if(execution == null) {
            fail();
        }
        return execution;
    }


    // //////////////////////////////////////

    protected final DomainServiceProvider dsp;
    private final ScenarioExecutionScope scope;

    protected ScenarioExecution(final DomainServiceProvider dsp, ScenarioExecutionScope scope) {
        this.dsp = dsp;
        this.scope = scope;
        current.set(this);
    }

    public boolean ofScope(ScenarioExecutionScope scope) {
        return this.scope == scope;
    }


    // //////////////////////////////////////

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
     * Replaces the service implementation with some other.
     *
     * <p>
     * Allows services to be mocked out.  It is the responsibility of the test to reinstate the &quot;original&quot;
     * service implementation afterwards.
     *
     * <p>
     * Mock services may requiring expectations to ignore any initialization that the framework would normally perform
     * on them.  For example, if mocking out the {@link org.apache.isis.applib.services.eventbus.EventBusService}, the
     * mock should be set up to ignore any calls to
     * {@link org.apache.isis.applib.services.eventbus.EventBusService#register(Object) register} and
     * {@link org.apache.isis.applib.services.eventbus.EventBusService#unregister(Object)}.
     *
     * <p>
     * Because integration tests cache services in the session, this method should typically be followed by
     * calls to {@link #closeSession() close} the current session and then to re-{@link #openSession() open} a new one.
     *
     * <p>
     *     TODO: I'm not convinced this works reliably...
     * </p>
     */
    public <T> void replaceService(T original, T replacement) {
        dsp.replaceService(original, replacement);
    }

    //    /**
    //     * Convenience method, returning the {@link DomainObjectContainer},
    //     * first ensuring that it is available.
    //     *
    //     * @throws IllegalStateException if not available
    //     */
    //    public DomainObjectContainer container() {
    //        return service(DomainObjectContainer.class);
    //    }


    /**
     * Returns the  {@link WrapperFactory} if one {@link #service(Class) is available},
     * otherwise returns a {@link WrapperFactory#NOOP no-op} implementation.
     */
    public WrapperFactory wrapperFactory() {
        return WrapperFactory.NOOP;
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

    private final Map<VariableId, Object> objectByVariableId = _Maps.newLinkedHashMap();
    private final Map<String, Object> objectsById = _Maps.newLinkedHashMap();

    private final Map<String, Object> mostRecent = _Maps.newHashMap();

    public void putVar(String type, String id, Object value) {
        if(type == null || id == null) {
            throw new IllegalArgumentException("type and id must both be provided to save a scenario variable");
        }
        if(value == null) {
            throw new IllegalArgumentException("value cannot be null; use remove() to clear an scenario variable");
        }
        final VariableId key = new VariableId(type, id);
        objectByVariableId.put(key, value);
        objectsById.put(id, value);
        mostRecent.put(type, value);
    }

    public void removeVar(String type, String id) {
        if(type != null && id != null) {
            final VariableId key = new VariableId(type, id);
            objectByVariableId.remove(key);
        }
        if(id != null) {
            objectsById.remove(id);
        }
        if(type != null) {
            mostRecent.remove(type);
        }
    }

    /**
     * Retrieve an variable previously used in the scenario.
     *
     * <p>
     * Must specify type and/or id.
     *
     * @see VariableId - for rules on what constitutes an identifier.
     */
    public Object getVar(String type, String id) {
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
     * As {@link #getVar(String, String)}, but downcasting to the provided class.
     */
    @SuppressWarnings("unchecked")
    public <X> X getVar(String type, String id, Class<X> cls) {
        return (X) getVar(type, id);
    }

    // //////////////////////////////////////

    /**
     * Whether this implementation supports mocks.
     *
     * <p>
     * This default implementation returns <tt>false</tt>, meaning that the methods to
     * support mocking ({@link #checking(ExpectationBuilder)}, {@link #assertIsSatisfied()},
     * {@link #sequence(String)} and {@link #states(String)}) may not be called.  However,
     * the {@link ScenarioExecutionForUnit} overrides this and does support mocking.
     */
    public boolean supportsMocks() {
        return false;
    }

    /**
     * Install expectations on mock domain services (if appropriate).
     *
     * <p>
     * By default, mocks are not supported.  However, {@link ScenarioExecutionForUnit} overrides this
     * method and does support mocking (delegating to an underlying JMock {@link Mockery}).
     *
     * <p>
     * Subclasses of this class tailored to supporting integration specs/tests should do nothing
     */
    public void checking(ExpectationBuilder expectations) {
        throw new IllegalStateException("Mocks are not supported");
    }

    /**
     * Install expectations on mock domain services (if appropriate).
     *
     * <p>
     * By default, mocks are not supported.  To reduce clutter in tests, this method is a no-op
     * and will silently do nothing if called when mocks are not supported.
     *
     * <p>
     * The {@link ScenarioExecutionForUnit} overrides this method and does support mocking, delegating
     * to an underlying JMock {@link Mockery}).  Not only will it assert all existing interactions
     * have been satisfied, it also resets mocks/expectations for the next interaction.
     */
    public void assertIsSatisfied() {
    }

    /**
     * Define {@link Sequence} in a (JMock) interaction  (if appropriaate).
     *
     * <p>
     * By default, mocks are not supported.  However, {@link ScenarioExecutionForUnit} overrides this
     * method and does support mocking (delegating to an underlying JMock {@link Mockery}).
     *
     * <p>
     * Subclasses of this class tailored to supporting integration specs/tests should do nothing
     */
    public Sequence sequence(String name) {
        throw new IllegalStateException("Mocks are not supported");
    }

    /**
     * Define {@link States} in a (JMock) interaction (if appropriaate).
     *
     * <p>
     * By default, mocks are not supported.  However, {@link ScenarioExecutionForUnit} overrides this
     * method and does support mocking (delegating to an underlying JMock {@link Mockery}).
     *
     * <p>
     * Subclasses of this class tailored to supporting integration specs/tests should do nothing
     */
    public States states(String name) {
        throw new IllegalStateException("Mocks are not supported");
    }

    // //////////////////////////////////////

    /**
     * Install arbitrary fixtures, eg before an integration tests or as part of a
     * Cucumber step definitions or hook.
     *
     * <p>
     * This implementation is a no-op, but subclasses of this class tailored to
     * supporting integration specs/tests are expected to override.
     */
    public void install(InstallableFixture... fixtures) {
        // do nothing
    }

    // //////////////////////////////////////

    /**
     * For Cucumber hooks to call.
     *
     * <p>
     * This implementation is a no-op, but subclasses of this class tailored to
     * supporting integration specs are expected to override.
     */
    public void openSession() {
        // do nothing
    }

    /**
     * For Cucumber hooks to call.
     *
     * <p>
     * This implementation is a no-op, but subclasses of this class tailored to
     * supporting integration specs are expected to override.
     */
    public void openSession(AuthenticationSession authenticationSession) {
        // do nothing
    }

    /**
     * For Cucumber hooks to call.
     *
     * <p>
     * This implementation is a no-op, but subclasses of this class tailored to
     * supporting integration specs are expected to override.
     */
    public void closeSession() {
        // do nothing
    }

    // //////////////////////////////////////

    /**
     * For Cucumber hooks to call, performing transaction management around each step.
     *
     * <p>
     * This implementation is a no-op, but subclasses of this class tailored to
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
     * This implementation is a no-op, but subclasses of this class tailored to
     * supporting integration specs are expected to override.  (Integration tests can use
     * the <tt>IsisTransactionRule</tt> to do transaction management transparently).
     */
    public void endTran(boolean ok) {
        // do nothing
    }

    // //////////////////////////////////////

    public Object injectServices(final Object obj) {
        try {
            final Method[] methods = obj.getClass().getMethods();
            for (Method method : methods) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if(parameterTypes.length != 1) {
                    continue;
                }
                final Class<?> serviceClass = parameterTypes[0];
                if(method.getName().startsWith("inject")) {
                    final Object service = service(serviceClass);
                    method.invoke(obj, service);
                }
                //                if(method.getName().startsWith("set") && serviceClass == DomainObjectContainer.class) {
                //                    final Object container = container();
                //                    method.invoke(obj, container);
                //                }
            }
            autowireViaFields(obj, obj.getClass());

            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void autowireViaFields(final Object object, final Class<?> cls) {
        final List<Field> fields = Arrays.asList(cls.getDeclaredFields());
        final List<Field> injectFields = _Lists.filter(fields, new Predicate<Field>() {
            @Override
            public boolean test(Field input) {
                final Inject annotation = input.getAnnotation(javax.inject.Inject.class);
                return annotation != null;
            }
        });

        for (final Field field : injectFields) {
            final Object service = service(field.getType());
            final Class<?> serviceClass = service.getClass();
            field.setAccessible(true);
            invokeInjectorField(field, object, service);
        }

        // recurse up the hierarchy
        final Class<?> superclass = cls.getSuperclass();
        if(superclass != null) {
            autowireViaFields(object, superclass);
        }
    }

    private static void invokeInjectorField(final Field field, final Object target, final Object parameter) {
        try {
            field.set(target, parameter);
        } catch (IllegalArgumentException e) {
            throw new MetaModelException(e);
        } catch (IllegalAccessException e) {
            throw new MetaModelException(String.format("Cannot access the %s field in %s", field.getName(), target.getClass().getName()));
        }
    }

}

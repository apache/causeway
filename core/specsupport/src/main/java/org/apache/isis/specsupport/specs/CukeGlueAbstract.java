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
package org.apache.isis.specsupport.specs;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.commons.internal.base._Strings;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.internal.ExpectationBuilder;

import java.util.Objects;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.specsupport.scenarios.ScenarioExecution;
import org.apache.isis.specsupport.scenarios.ScenarioExecutionForUnit;
import org.apache.isis.specsupport.scenarios.ScenarioExecutionScope;

import cucumber.api.java.Before;

/**
 * Base class for Cucumber-JVM step definitions.
 *
 * <p>
 * Simply declares that an instance of {@link ScenarioExecution} (or a subclass)
 * must be instantiated by the Cucumber-JVM runtime and injected into the step definitions.
 *
 * @deprecated  - use {@link CukeGlueAbstract2} instead.
 */
@Deprecated
public abstract class CukeGlueAbstract {

    /**
     * Access the {@link ScenarioExecution} as setup through a previous call to {@link #before(ScenarioExecutionScope)}.
     *
     * <p>
     * This corresponds, broadly, to the (Ruby) Cucumber's &quot;World&quot; object.
     */
    protected ScenarioExecution scenarioExecution() {
        if(ScenarioExecution.current() == null) {
            fail();
            return null;
        }
        return ScenarioExecution.current();
    }

    // //////////////////////////////////////

    /**
     * Intended to be called at the beginning of any 'when' (after all the 'given's)
     * or at the beginning of any 'then' (after all the 'when's)
     *
     * <p>
     * Simply {@link ScenarioExecution#endTran(boolean) ends any existing transaction} and
     * then {@link ScenarioExecution#beginTran() starts a new one}.
     */
    protected void nextTransaction() {
        final ScenarioExecution scenarioExecution = scenarioExecution();
        if(scenarioExecution != null) {
            scenarioExecution.endTran(true);
            scenarioExecution.beginTran();
        }
    }


    // //////////////////////////////////////

    /**
     * Convenience method
     */
    public Object getVar(String type, String id) {
        return scenarioExecution().getVar(type, id);
    }

    /**
     * Convenience method
     */
    public <X> X getVar(String type, String id, Class<X> cls) {
        return scenarioExecution().getVar(type, id ,cls);
    }

    /**
     * Convenience method
     */
    public void putVar(String type, String id, Object value) {
        scenarioExecution().putVar(type, id, value);
    }

    /**
     * Convenience method
     */
    public void removeVar(String type, String id) {
        scenarioExecution().removeVar(type, id);
    }

    /**
     * Convenience method
     */
    protected <T> T service(Class<T> cls) {
        return scenarioExecution().service(cls);
    }

    //    /**
    //     * Convenience method
    //     */
    //    protected DomainObjectContainer container() {
    //        return scenarioExecution().container();
    //    }

    /**
     * Convenience method
     */
    protected WrapperFactory wrapperFactory() {
        return scenarioExecution().wrapperFactory();
    }

    /**
     * Convenience method
     */
    protected <T> T wrap(T obj) {
        return wrapperFactory().wrap(obj);
    }

    /**
     * Convenience method
     */
    protected <T> T unwrap(T obj) {
        return wrapperFactory().unwrap(obj);
    }

    /**
     * Convenience method
     * @return
     */
    public boolean supportsMocks() {
        return scenarioExecution().supportsMocks();
    }

    /**
     * Convenience method
     */
    public void checking(ExpectationBuilder expectations) {
        scenarioExecution().checking(expectations);
    }

    /**
     * Convenience method
     */
    public void assertMocksSatisfied() {
        scenarioExecution().assertIsSatisfied();
    }

    /**
     * Convenience method
     */
    public Sequence sequence(String name) {
        return scenarioExecution().sequence(name);
    }

    /**
     * Convenience method
     */
    public States states(String name) {
        return scenarioExecution().states(name);
    }

    // //////////////////////////////////////

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void assertTableEquals(final List listOfExpecteds, final Iterable iterableOfActuals) {
        final List<Object> listOfActuals = _Lists.newArrayList(iterableOfActuals);
        assertThat(listOfActuals.size(), is(listOfExpecteds.size()));

        final StringBuilder buf = new StringBuilder();
        for (int i=0; i<listOfActuals.size(); i++) {

            final Object actual = listOfActuals.get(i);
            final Object expected = listOfExpecteds.get(i);

            final Field[] expectedFields = expected.getClass().getDeclaredFields();
            for (Field field : expectedFields) {
                final String propertyName = field.getName();
                final Object actualProp = getProperty(actual, propertyName );
                final Object expectedProp = getProperty(expected, propertyName);

                if(!Objects.equals(actualProp, expectedProp)) {
                    buf.append("#" + i + ": " + propertyName + ": " + expectedProp + " vs " + actualProp).append("\n");
                }
            }
        }
        if(buf.length() != 0) {
            fail("\n" + buf.toString());
        }
    }


    private static Object getProperty(Object obj, String propertyName) {
        if(obj == null) {
            return null;
        }
        final Class<? extends Object> cls = obj.getClass();
        try {
            final String methodName = "get" + _Strings.capitalize(propertyName);
            final Method method = cls.getMethod(methodName, new Class[]{});
            if(method != null) {
                return method.invoke(obj);
            }
        } catch (Exception e) {
            // continue
        }

        try {
            final String methodName = "is" + _Strings.capitalize(propertyName);
            final Method method = cls.getMethod(methodName, new Class[]{});
            if(method != null) {
                return method.invoke(obj);
            }
        } catch (Exception e) {
            // continue
        }

        try {
            final Field field = cls.getDeclaredField(propertyName);
            if(field != null) {
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field.get(obj);
            }
        } catch (Exception e) {
            // continue
        }

        return null;
    }

    // //////////////////////////////////////

    /**
     * Indicate that a scenario is starting, and specify the {@link ScenarioExecutionScope scope}
     * at which to run the scenario.
     *
     * <p>
     * This method should be called from a &quot;before&quot; hook (a method annotated with
     * Cucumber's {@link Before} annotation, in a step definition subclass.  The tag
     * should be appropriate for the scope specified.  Typically this method should be delegated to
     * twice, in two mutually exclusive before hooks.
     *
     * <p>
     * Calling this method makes the {@link ScenarioExecution} available (via {@link #scenarioExecution()}).
     * It also delegates to the scenario to {@link ScenarioExecution#beginTran() begin the transaction}.
     * (Whether this actually does anything depends in implementation of the {@link ScenarioExecution}).
     *
     * <p>
     * The boilerplate (to copy-n-paste as required) is:
     * <pre>
     *  &#64;cucumber.api.java.Before("@unit")
     *  public void beforeScenarioUnitScope() {
     *     before(ScenarioExecutionScope.UNIT);
     *  }
     *  &#64;cucumber.api.java.Before("@integration")
     *  public void beforeScenarioIntegrationScope() {
     *     before(ScenarioExecutionScope.INTEGRATION);
     *  }
     * </pre>
     * The built-in {@link ScenarioExecutionScope#UNIT unit}-level scope will instantiate a
     * {@link ScenarioExecutionForUnit}, while the built-in
     * {@link ScenarioExecutionScope#INTEGRATION integration}-level scope instantiates
     * <tt>ScenarioExecutionForIntegration</tt> (from the <tt>isis-core-integtestsupport</tt> module).
     * The former provides access to domain services as mocks, whereas the latter wraps a running
     * <tt>IsisSystemForTest</tt>.
     *
     * <p>
     * If need be, it is also possible to define custom scopes, with a different implementation of
     * {@link ScenarioExecution}.  This might be done when unit testing where a large number of specs
     * have similar expectations needing to be set on the mock domain services.
     *
     * <p>
     * Not every class holding step definitions should have these hooks, only those that correspond to the logical
     * beginning and end of scenario.  As such, this method may only be called once per scenario execution
     * (and fails fast if called more than once).
     */
    protected void before(ScenarioExecutionScope scope) {
        final ScenarioExecution scenarioExecution = scope.instantiate();
        scenarioExecution.beginTran();
    }

    /**
     * Indicate that a scenario is ending; the {@link ScenarioExecution} is discarded and no
     * longer {@link #scenarioExecution() available}.
     *
     * <p>
     * Before being discarded, the {@link ScenarioExecution} is delegated to
     * in order to {@link ScenarioExecution#endTran(boolean) end the transaction}.
     * (Whether this actually does anything depends in implementation of the {@link ScenarioExecution}).
     *
     * <p>
     * The boilerplate (to copy-n-paste as required) is:
     * <pre>
     *  &#64;cucumber.api.java.After
     *  public void afterScenario(cucumber.api.Scenario sc) {
     *     after(sc);
     *  }
     * </pre>
     *
     * <p>
     * Not every class holding step definitions should have this hook, only those that correspond to the logical
     * beginning and end of scenario.  As such, this method may only be called once per scenario execution
     * (and fails fast if called more than once).
     */
    public void after(cucumber.api.Scenario sc) {
        ScenarioExecution.current().endTran(!sc.isFailed());
    }

}

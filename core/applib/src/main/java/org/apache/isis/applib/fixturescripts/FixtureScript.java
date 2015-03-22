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
package org.apache.isis.applib.fixturescripts;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.apache.isis.applib.AbstractViewModel;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.fixtures.FixtureType;
import org.apache.isis.applib.fixtures.InstallableFixture;
import org.apache.isis.applib.services.wrapper.WrapperFactory;

@ViewModelLayout(named="Script")
public abstract class FixtureScript 
        extends AbstractViewModel 
        implements InstallableFixture {

    protected static final String PATH_SEPARATOR = "/";

    //region > constructors

    /**
     * Initializes a {@link Discoverability#NON_DISCOVERABLE} fixture, with
     * {@link #getFriendlyName()} and {@link #getLocalName()} derived from the class name.
     *
     * <p>
     * Use {@link #withDiscoverability(Discoverability)} to override.
     */
    public FixtureScript() {
        this(null, null);
    }

    /**
     * Initializes a {@link Discoverability#NON_DISCOVERABLE} fixture.
     *
     * <p>
     * Use {@link #withDiscoverability(Discoverability)} to override.
     *
     * @param friendlyName - if null, will be derived from class name
     * @param localName - if null, will be derived from class name
     */
    public FixtureScript(final String friendlyName, final String localName) {
        this(friendlyName, localName, Discoverability.NON_DISCOVERABLE);
    }

    /**
     * @param friendlyName - if null, will be derived from class name
     * @param localName - if null, will be derived from class name
     * @param discoverability - whether this fixture script can be rendered as a choice to execute through {@link org.apache.isis.applib.fixturescripts.FixtureScripts#runFixtureScript(FixtureScript, String)}}.
     */
    public FixtureScript(
            final String friendlyName, 
            final String localName, 
            final Discoverability discoverability) {
        this.localName = localNameElseDerived(localName);
        this.friendlyName = friendlyNameElseDerived(friendlyName);
        this.parentPath = "";
        this.discoverability = discoverability;

        // enable tracing by default, to stdout
        withTracing();
    }
    protected String localNameElseDerived(final String str) {
        return str != null ? str : StringUtil.asLowerDashed(friendlyNameElseDerived(str));
    }

    protected String friendlyNameElseDerived(final String str) {
        return str != null ? str : StringUtil.asNaturalName2(getClass().getSimpleName());
    }

    //endregion

    //region > tracing

    private PrintStream tracePrintStream;

    /**
     * Enable tracing of the execution to the provided {@link java.io.PrintStream}.
     */
    @Programmatic
    public FixtureScript withTracing(final PrintStream tracePrintStream) {
        this.tracePrintStream = tracePrintStream;
        return this;
    }

    /**
     * Enable tracing of the execution to stdout.
     */
    @Programmatic
    public FixtureScript withTracing() {
        return withTracing(System.out);
    }

    //endregion

    //region > viewModel impl

    @Programmatic
    @Override
    public String viewModelMemento() {
        return fixtureScripts.mementoFor(this);
    }

    @Programmatic
    @Override
    public void viewModelInit(final String mementoStr) {
        fixtureScripts.initOf(mementoStr, this);
    }

    //endregion

    //region > qualifiedName

    @Programmatic
    public String getQualifiedName() {
        return getParentPath() + getLocalName();
    }

    //endregion

    //region > friendlyName (property)

    private String friendlyName;
    
    @Title
    @PropertyLayout(hidden = Where.EVERYWHERE)
    public String getFriendlyName() {
        return friendlyName;
    }
    public void setFriendlyName(final String friendlyName) {
        this.friendlyName = friendlyName;
    }

    //endregion

    //region > localName

    private String localName;
    /**
     * Will always be populated, initially by the default name, but can be
     * {@link #setLocalName(String) overridden}.
     */
    @PropertyLayout(hidden = Where.EVERYWHERE)
    public String getLocalName() {
        return localName;
    }
    public void setLocalName(final String localName) {
        this.localName = localName;
    }

    //endregion

    //region > parentPath

    private String parentPath;
    
    /**
     * Path of the parent of this script (if any), with trailing {@value #PATH_SEPARATOR}.
     */
    @Programmatic
    public String getParentPath() {
        return parentPath;
    }
    @Programmatic
    public void setParentPath(final String parentPath) {
        this.parentPath = parentPath;
    }

    //endregion

    //region > discoverability


    /**
     * Whether this fixture script should be automatically discoverable or not.
     */
    public enum Discoverability {
        /**
         * the fixture script is discoverable and so will be listed by the
         * {@link FixtureScripts#runFixtureScript(FixtureScript,String) run fixture script} action
         */
        DISCOVERABLE,
        /**
         * The fixture script is non-discoverable and so will <i>not</i> be listed by the
         * {@link FixtureScripts#runFixtureScript(FixtureScript,String) run fixture script} action
         */
        NON_DISCOVERABLE
    }


    private Discoverability discoverability;

    /**
     * Whether this fixture script is {@link Discoverability discoverable} (in other words
     * whether it will be listed to be run in the {@link FixtureScripts#runFixtureScript(FixtureScript, String) run fixture script} action.
     * 
     * <p>
     * By default {@link DiscoverableFixtureScript}s are {@link Discoverability#DISCOVERABLE discoverable}, all other
     * {@link FixtureScript}s are {@link Discoverability#NON_DISCOVERABLE not}.  This can be overridden in the
     * constructor, however or by calling the {@link #withDiscoverability(org.apache.isis.applib.fixturescripts.FixtureScript.Discoverability) setter}.
     */
    @PropertyLayout(hidden = Where.EVERYWHERE)
    public boolean isDiscoverable() {
        return discoverability == Discoverability.DISCOVERABLE;
    }

    @Programmatic
    public FixtureScript withDiscoverability(final Discoverability discoverability) {
        this.discoverability = discoverability;
        return this;
    }

    //endregion

    //region > ExecutionContext

    public static class ExecutionContext {

        /**
         * Null implementation, to assist with unit testing of {@link org.apache.isis.applib.fixturescripts.FixtureScript}s.
         */
        public static final ExecutionContext NOOP = new ExecutionContext((String)null, null) {
            @Override
            public <T> T add(final FixtureScript script, final T object) {
                return object;
            }
            @Override
            public <T> T addResult(final FixtureScript script, final T object) {
                return object;
            }

            @Override
            public <T> T add(final FixtureScript script, final String key, final T object) {
                return object;
            }
            @Override
            public <T> T addResult(final FixtureScript script, final String key, final T object) {
                return object;
            }

            @Override
            public List<FixtureResult> getResults() {
                return Collections.emptyList();
            }
        };

        private final ExecutionParameters executionParameters;
        private final FixtureScripts fixtureScripts;
        private final FixtureResultList fixtureResultList;

        public ExecutionContext(final String parameters, final FixtureScripts fixtureScripts) {
            this(new ExecutionParameters(parameters), fixtureScripts);
        }

        @Programmatic
        public static ExecutionContext create(final ExecutionParameters executionParameters, final FixtureScripts fixtureScripts) {
            return new ExecutionContext(executionParameters, fixtureScripts);
        }

        private ExecutionContext(final ExecutionParameters executionParameters, final FixtureScripts fixtureScripts) {
            this.fixtureScripts = fixtureScripts;
            fixtureResultList = new FixtureResultList(fixtureScripts, this);
            this.executionParameters = executionParameters;
        }

        /**
         * @deprecated - this shouldn't have public visibility.
         */
        @Deprecated
        public static Map<String, String> asKeyValueMap(final String parameters) {
            return ExecutionParameters.asKeyValueMap(parameters);
        }

        public String getParameters() {
            return executionParameters.getParameters();
        }

        public Map<String,String> getParameterMap() {
            return executionParameters.getParameterMap();
        }

        public String getParameter(final String parameterName) {
            return executionParameters.getParameter(parameterName);
        }

        public <T> T getParameterAsT(final String parameterName, final Class<T> cls) {
            return executionParameters.getParameterAsT(parameterName,cls);
        }

        public Boolean getParameterAsBoolean(final String parameterName) {
            return executionParameters.getParameterAsBoolean(parameterName);
        }

        public Byte getParameterAsByte(final String parameterName) {
            return executionParameters.getParameterAsByte(parameterName);
        }

        public Short getParameterAsShort(final String parameterName) {
            return executionParameters.getParameterAsShort(parameterName);
        }

        public Integer getParameterAsInteger(final String parameterName) {
            return executionParameters.getParameterAsInteger(parameterName);
        }

        public Long getParameterAsLong(final String parameterName) {
            return executionParameters.getParameterAsLong(parameterName);
        }

        public Float getParameterAsFloat(final String parameterName) {
            return executionParameters.getParameterAsFloat(parameterName);
        }

        public Double getParameterAsDouble(final String parameterName) {
            return executionParameters.getParameterAsDouble(parameterName);
        }

        public Character getParameterAsCharacter(final String parameterName) {
            return executionParameters.getParameterAsCharacter(parameterName);
        }

        public BigInteger getParameterAsBigInteger(final String parameterName) {
            return executionParameters.getParameterAsBigInteger(parameterName);
        }

        public BigDecimal getParameterAsBigDecimal(final String parameterName) {
            return executionParameters.getParameterAsBigDecimal(parameterName);
        }

        public LocalDate getParameterAsLocalDate(final String parameterName) {
            return executionParameters.getParameterAsLocalDate(parameterName);
        }

        public LocalDateTime getParameterAsLocalDateTime(final String parameterName) {
            return executionParameters.getParameterAsLocalDateTime(parameterName);
        }

        public <T extends Enum<T>> T getParameterAsEnum(final String parameterName, final Class<T> enumClass) {
            return executionParameters.getParameterAsEnum(parameterName, enumClass);
        }


        public void setParameterIfNotPresent(final String parameterName, final String parameterValue) {
            executionParameters.setParameterIfNotPresent(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final Boolean parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final Byte parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final Short parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final Integer parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final Long parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final Float parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final Double parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final Character parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final BigInteger parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final java.util.Date parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final java.sql.Date parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final LocalDate parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final LocalDateTime parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final org.joda.time.DateTime parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final BigDecimal parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final Enum<?> parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        public void setParameter(final String parameterName, final String parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }


        public List<FixtureResult> getResults() {
            return fixtureResultList.getResults();
        }

        /**
         * @deprecated - use {@link #addResult(FixtureScript, Object)} instead.
         */
        @Deprecated
        public <T> T add(final FixtureScript script, final T object) {
            return addResult(script, object);
        }

        public <T> T addResult(final FixtureScript script, final T object) {
            fixtureResultList.add(script, object);
            return object;
        }

        /**
         * @deprecated - use {@link #addResult(FixtureScript, String, Object)} instead.
         */
        @Deprecated
        public <T> T add(final FixtureScript script, final String key, final T object) {
            return addResult(script, key, object);
        }

        public <T> T addResult(final FixtureScript script, final String key, final T object) {
            fixtureResultList.add(script, key, object);
            return object;
        }

        public <T> T lookup(final String key, final Class<T> cls) {
            return fixtureResultList.lookup(key, cls);
        }

        /**
         * Executes a child {@link FixtureScript fixture script}, injecting services into it first, and (for any results
         * that are {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that is derived from the fixture's class name.
         */
        public void executeChild(final FixtureScript callingFixtureScript, final FixtureScript childFixtureScript) {
            executeChildT(callingFixtureScript, childFixtureScript);
        }

        /**
         * Executes a child {@link FixtureScript fixture script}, injecting services into it first, and (for any results
         * that are {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that is derived from the fixture's class name.
         *
         * @return the child fixture script.
         */
        public <T extends FixtureScript> T executeChildT(final FixtureScript callingFixtureScript, final T childFixtureScript) {
             return executeChildT(callingFixtureScript, null, childFixtureScript);
        }

        /**
         * Executes a child {@link FixtureScript fixture script}, injecting services into it first, and (for any results
         * that are {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that overriding the default name of the fixture script with one more meaningful in the context of this fixture.
         */
        public void executeChild(final FixtureScript callingFixtureScript, final String localNameOverride, final FixtureScript childFixtureScript) {

            executeChildT(callingFixtureScript, localNameOverride, childFixtureScript);
        }

        /**
         * Executes a child {@link FixtureScript fixture script}, injecting services into it first, and (for any results
         * that are {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that overriding the default name of the fixture script with one more meaningful in the context of this fixture.
         *
         * @return the child fixture script.
         */
        public <T extends FixtureScript> T executeChildT(final FixtureScript callingFixtureScript, final String localNameOverride, final T childFixtureScript) {

            childFixtureScript.setParentPath(callingFixtureScript.pathWith(""));
            childFixtureScript.withTracing(callingFixtureScript.tracePrintStream); // cascade down

            if(localNameOverride != null) {
                childFixtureScript.setLocalName(localNameOverride);
            }
            callingFixtureScript.getContainer().injectServicesInto(childFixtureScript);

            executeChildIfNotAlready(childFixtureScript);

            return childFixtureScript;
        }


        static enum As { EXEC, SKIP }

        /**
         * DO <i>NOT</i> CALL DIRECTLY; instead use {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#executeChild(org.apache.isis.applib.fixturescripts.FixtureScript, String, org.apache.isis.applib.fixturescripts.FixtureScript)} or {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#executeChild(FixtureScript, FixtureScript)}.
         *
         * @deprecated - should not be called directly, but has <code>public</code> visibility so there is scope for confusion.  Replaced by method with private visibility.
         */
        @Deprecated
        public void executeIfNotAlready(final FixtureScript fixtureScript) {
            executeChildIfNotAlready(fixtureScript);
        }

        private void executeChildIfNotAlready(final FixtureScript fixtureScript) {
            if(shouldExecute(fixtureScript)) {
                trace(fixtureScript, As.EXEC);
                fixtureScript.execute(this);
            } else {
                trace(fixtureScript, As.SKIP);
            }
        }

        //region > shouldExecute

        private final Map<String,Class> fixtureScriptClasses = Maps.newLinkedHashMap();

        private boolean shouldExecute(final FixtureScript fixtureScript) {
            final boolean alreadyExecuted = fixtureScriptClasses.values().contains(fixtureScript.getClass());
            if(!alreadyExecuted) {
                fixtureScriptClasses.put(fixtureScript.getQualifiedName(), fixtureScript.getClass());
            }
            return !alreadyExecuted || fixtureScripts.getMultipleExecutionStrategy().isExecute();
        }
        //endregion

        //region > tracing

        private int traceHighwatermark = 40;
        private PrintStream tracePrintStream;

        public ExecutionContext withTracing(final PrintStream tracePrintStream) {
            this.tracePrintStream = tracePrintStream;
            return this;
        }

        private void trace(final FixtureScript fixtureScript, final As as) {
            if(tracePrintStream == null) {
                return;
            }
            final String qualifiedName = fixtureScript.getQualifiedName();
            final String trace = String.format("%1s: %2s %3s\n", pad(qualifiedName), as, fixtureScript.getClass().getName());
            tracePrintStream.print(trace);
            tracePrintStream.flush();
        }

        void trace(final FixtureResult fixtureResult) {
            if(tracePrintStream == null) {
                return;
            }
            final String key = fixtureResult.getKey();
            final String trace = String.format("%1s: %2s\n", pad(key), fixtureScripts.titleOf(fixtureResult));
            tracePrintStream.print(trace);
            tracePrintStream.flush();
        }

        private String pad(final String key) {
            traceHighwatermark = Math.max(key.length(), traceHighwatermark);
            return pad(key, roundup(traceHighwatermark, 20));
        }
        //endregion

        private static String pad(final String str, final int padTo) {
            return Strings.padEnd(str, padTo, ' ');
        }

        static int roundup(final int n, final int roundTo) {
            return ((n / roundTo) + 1) * roundTo;
        }


        private Map<Class, Object> userData = Maps.newHashMap();
        public void setUserData(final Object object) {
            userData.put(object.getClass(), object);
        }
        public <T> T getUserData(final Class<T> cls) {
            return (T) userData.get(cls);
        }
        public <T> T clearUserData(final Class<T> cls) {
            return (T) userData.remove(cls);
        }

    }

    //endregion

    //region > defaultParam, checkParam
    protected <T> T defaultParam(final String parameterName, final ExecutionContext ec, final T defaultValue) {
        final T value = valueFor(parameterName, ec, defaultValue);
        setParam(parameterName, value);
        return value;
    }

    private <T> T valueFor(final String parameterName, final ExecutionContext ec, final T defaultValue) {
        final Class<T> cls = (Class<T>) defaultValue.getClass();

        final T value = readParam(parameterName, ec, cls);
        if(value != null) { return (T) value; }

        // else default value
        return defaultValue;
    }

    protected <T> T checkParam(final String parameterName, final ExecutionContext ec, final Class<T> cls) {

        final T value = readParam(parameterName, ec, cls);
        if(value != null) { return (T) value; }

        // else throw exception
        throw new IllegalArgumentException(String.format("No value for '%s'", parameterName));
    }

    private <T> T readParam(final String parameterName, final ExecutionContext ec, final Class<T> cls) {

        // read from ExecutionContext
        T value = ec.getParameterAsT(parameterName, cls);
        if(value != null) { return (T) value; }

        // else from fixture script
        Method method;
        try {
            method = this.getClass().getMethod("get" + uppercase(parameterName));
            value = (T)method.invoke(this);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

        }
        if(value != null) { return (T) value; }

        if (cls == Boolean.class || cls == boolean.class) {
            try {
                method = this.getClass().getMethod("is" + uppercase(parameterName));
                value = (T)method.invoke(this);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

            }
            if(value != null) { return (T) value; }
        }

        return null;
    }

    private <T> void setParam(final String parameterName, final T value) {
        final String mutator = "set" + uppercase(parameterName);
        final Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            if(method.getName().equals(mutator) && method.getParameterTypes().length == 1) {
                try {
                    method.invoke(this, value);
                } catch (InvocationTargetException | IllegalAccessException ignored) {
                }
                break;
            }
        }
    }

    private String uppercase(final String parameterName) {
        return parameterName.substring(0, 1).toUpperCase() + parameterName.substring(1);
    }
    //endregion

    //region > run (entry point for FixtureScripts service to call)

    /**
     * It's a bit nasty to hold onto this as a field, but required in order to support
     */
    private ExecutionContext executionContext;

    /**
     * Entry point for {@link org.apache.isis.applib.fixturescripts.FixtureScripts} service to call.
     *
     * <p>
     *     DO <i>NOT</i> CALL DIRECTLY.
     * </p>
     */
    @Programmatic
    public final List<FixtureResult> run(final String parameters) {
        executionContext = fixtureScripts.newExecutionContext(parameters).withTracing(this.tracePrintStream);
        executionContext.executeChildIfNotAlready(this);
        return executionContext.getResults();
    }

    /**
     * Use instead {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#lookup(String, Class)} directly.
     */
    @Deprecated
    public <T> T lookup(final String key, final Class<T> cls) {
        if(executionContext == null) {
            throw new IllegalStateException("This fixture has not yet been run.");
        }
        return executionContext.lookup(key, cls);
    }


    /**
     * Optional hook to validate parameters.
     */
    @Programmatic
    public String validateRun(final String parameters) {
        return null;
    }

    //endregion

    //region > executeChild (API for subclasses to call); deprecated execute(FixtureScript, ExecutionContext)

    /**
     * @deprecated - use instead {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#executeChild(FixtureScript, String, FixtureScript)}.
     */
    @Deprecated
    protected void execute(
            final String localNameOverride,
            final FixtureScript childFixtureScript,
            final ExecutionContext executionContext) {
        executionContext.executeChild(this, localNameOverride, childFixtureScript);
    }

    /**
     * @deprecated - use instead {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#executeChild(FixtureScript, String, FixtureScript)}.
     */
    @Deprecated
    protected void executeChild(
            final String localNameOverride,
            final FixtureScript childFixtureScript,
            final ExecutionContext executionContext) {
        executionContext.executeChild(this, localNameOverride, childFixtureScript);
    }

    /**
     * @deprecated - use instead {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#executeChild(org.apache.isis.applib.fixturescripts.FixtureScript, org.apache.isis.applib.fixturescripts.FixtureScript)}
     */
    @Deprecated
    protected void execute(
            final FixtureScript childFixtureScript,
            final ExecutionContext executionContext) {
        executionContext.executeChild(this, childFixtureScript);
    }

    /**
     * @deprecated - use instead {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#executeChild(org.apache.isis.applib.fixturescripts.FixtureScript, org.apache.isis.applib.fixturescripts.FixtureScript)}
     */
    @Deprecated
    protected void executeChild(
            final FixtureScript childFixtureScript,
            final ExecutionContext executionContext) {
        executionContext.executeChild(this, childFixtureScript);
    }

    //endregion

    //region > execute (API for subclasses to implement)

    /**
     * Subclasses should <b>implement this</b> but SHOULD <i>NOT</i> CALL DIRECTLY.
     *
     * <p>
     *  Instead call sub fixture scripts using {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#executeChild(org.apache.isis.applib.fixturescripts.FixtureScript, org.apache.isis.applib.fixturescripts.FixtureScript)} or {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#executeChild(org.apache.isis.applib.fixturescripts.FixtureScript, String, org.apache.isis.applib.fixturescripts.FixtureScript)}.
     * </p>
     */
    @Programmatic
    protected abstract void execute(final ExecutionContext executionContext);

    //endregion

    //region > (legacy) InstallableFixture impl

    @Override
    public FixtureType getType() {
        return FixtureType.DOMAIN_OBJECTS;
    }
    
    @Programmatic
    public final void install() {
        run(null);
    }

    //endregion

    //region > helpers (for subclasses)

    /**
     * Returns the first non-null vaulue; for convenience of subclass implementations
     */
    protected static <T> T coalesce(final T... ts) {
        for (final T t : ts) {
            if(t != null) return t;
        }
        return null;
    }

    /**
     * Wraps domain object
     */
    protected <T> T wrap(final T domainObject) {
        return wrapperFactory.wrap(domainObject);
    }

    protected <T> T wrap(final T domainObject, final WrapperFactory.ExecutionMode executionMode) {
        return wrapperFactory.wrap(domainObject, executionMode);
    }

    /**
     * Unwraps domain object (no-arg if already wrapped).
     */
    protected <T> T unwrap(final T possibleWrappedDomainObject) {
        return wrapperFactory.unwrap(possibleWrappedDomainObject);
    }

    //endregion

    //region > helpers (local)

    @Programmatic
    String pathWith(final String subkey) {
        return (getQualifiedName() != null? getQualifiedName() + PATH_SEPARATOR: "") +  subkey;
    }
    //endregion

    //region > injected services

    @javax.inject.Inject
    protected FixtureScripts fixtureScripts;

    @javax.inject.Inject
    protected DomainObjectContainer container;

    @javax.inject.Inject
    protected WrapperFactory wrapperFactory;


    //endregion
}

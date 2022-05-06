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
package org.apache.isis.testing.fixtures.applib.fixturescripts;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptAbstract;
import org.apache.isis.applib.fixturescripts.DiscoverableFixtureScript;
import org.apache.isis.testing.fixtures.applib.personas.PersonaWithBuilderScript;
import org.apache.isis.testing.fixtures.applib.personas.WithPrereqs;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.sessmgmt.SessionManagementService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;

import lombok.Getter;

@ViewModelLayout(named="Script")
public abstract class FixtureScript
        extends AbstractViewModel
        implements InstallableFixture {

    public static final FixtureScript NOOP = new FixtureScript() {
        @Override
        protected void execute(ExecutionContext executionContext) {
        }
    };

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
    public FixtureScript(final String friendlyName, final String localName, final PrintStream printStream) {
        this(friendlyName, localName, Discoverability.NON_DISCOVERABLE, printStream);
    }

    /**
     * @param friendlyName - if null, will be derived from class name
     * @param localName - if null, will be derived from class name
     * @param discoverability - whether this fixture script can be rendered as a choice to execute through {@link FixtureScripts#runFixtureScript(FixtureScript, String)}}.
     */
    public FixtureScript(
            final String friendlyName,
            final String localName,
            final Discoverability discoverability) {
        this(friendlyName, localName, discoverability, /* no tracing */ null);
    }
    /**
     * @param friendlyName - if null, will be derived from class name
     * @param localName - if null, will be derived from class name
     * @param discoverability - whether this fixture script can be rendered as a choice to execute through {@link FixtureScripts#runFixtureScript(FixtureScript, String)}}.
     */
    public FixtureScript(
            final String friendlyName,
            final String localName,
            final Discoverability discoverability,
            final PrintStream printStream) {
        this.localName = localNameElseDerived(localName);
        this.friendlyName = friendlyNameElseDerived(friendlyName);
        this.parentPath = "";
        this.discoverability = discoverability;

        withTracing(printStream);
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
    @Programmatic
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
     * constructor, however or by calling the {@link #withDiscoverability(FixtureScript.Discoverability) setter}.
     */
    @Programmatic
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
         * Null implementation, to assist with unit testing of {@link FixtureScript}s.
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

        @Programmatic
        public String getParameters() {
            return executionParameters.getParameters();
        }

        @Programmatic
        public Map<String,String> getParameterMap() {
            return executionParameters.getParameterMap();
        }

        @Programmatic
        public String getParameter(final String parameterName) {
            return executionParameters.getParameter(parameterName);
        }

        @Programmatic
        public <T> T getParameterAsT(final String parameterName, final Class<T> cls) {
            return executionParameters.getParameterAsT(parameterName,cls);
        }

        @Programmatic
        public Boolean getParameterAsBoolean(final String parameterName) {
            return executionParameters.getParameterAsBoolean(parameterName);
        }

        @Programmatic
        public Byte getParameterAsByte(final String parameterName) {
            return executionParameters.getParameterAsByte(parameterName);
        }

        @Programmatic
        public Short getParameterAsShort(final String parameterName) {
            return executionParameters.getParameterAsShort(parameterName);
        }

        @Programmatic
        public Integer getParameterAsInteger(final String parameterName) {
            return executionParameters.getParameterAsInteger(parameterName);
        }

        @Programmatic
        public Long getParameterAsLong(final String parameterName) {
            return executionParameters.getParameterAsLong(parameterName);
        }

        @Programmatic
        public Float getParameterAsFloat(final String parameterName) {
            return executionParameters.getParameterAsFloat(parameterName);
        }

        @Programmatic
        public Double getParameterAsDouble(final String parameterName) {
            return executionParameters.getParameterAsDouble(parameterName);
        }

        @Programmatic
        public Character getParameterAsCharacter(final String parameterName) {
            return executionParameters.getParameterAsCharacter(parameterName);
        }

        @Programmatic
        public BigInteger getParameterAsBigInteger(final String parameterName) {
            return executionParameters.getParameterAsBigInteger(parameterName);
        }

        @Programmatic
        public BigDecimal getParameterAsBigDecimal(final String parameterName) {
            return executionParameters.getParameterAsBigDecimal(parameterName);
        }

        @Programmatic
        public LocalDate getParameterAsLocalDate(final String parameterName) {
            return executionParameters.getParameterAsLocalDate(parameterName);
        }

        @Programmatic
        public LocalDateTime getParameterAsLocalDateTime(final String parameterName) {
            return executionParameters.getParameterAsLocalDateTime(parameterName);
        }

        @Programmatic
        public <T extends Enum<T>> T getParameterAsEnum(final String parameterName, final Class<T> enumClass) {
            return executionParameters.getParameterAsEnum(parameterName, enumClass);
        }

        @Programmatic
        public void setParameterIfNotPresent(final String parameterName, final String parameterValue) {
            executionParameters.setParameterIfNotPresent(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final Boolean parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final Byte parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final Short parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final Integer parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final Long parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final Float parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final Double parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final Character parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final BigInteger parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final java.util.Date parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final java.sql.Date parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final LocalDate parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final LocalDateTime parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final org.joda.time.DateTime parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final BigDecimal parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final Enum<?> parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public void setParameter(final String parameterName, final String parameterValue) {
            executionParameters.setParameter(parameterName, parameterValue);
        }

        @Programmatic
        public List<FixtureResult> getResults() {
            return fixtureResultList.getResults();
        }

        /**
         * @deprecated - use {@link #addResult(FixtureScript, Object)} instead.
         */
        @Deprecated
        @Programmatic
        public <T> T add(final FixtureScript script, final T object) {
            return addResult(script, object);
        }

        @Programmatic
        public <T> T addResult(final FixtureScript script, final T object) {
            fixtureResultList.add(script, object);
            return object;
        }

        /**
         * @deprecated - use {@link #addResult(FixtureScript, String, Object)} instead.
         */
        @Programmatic
        @Deprecated
        public <T> T add(final FixtureScript script, final String key, final T object) {
            return addResult(script, key, object);
        }

        @Programmatic
        public <T> T addResult(final FixtureScript script, final String key, final T object) {
            fixtureResultList.add(script, key, object);
            return object;
        }

        @Programmatic
        public <T> T lookup(final String key, final Class<T> cls) {
            return fixtureResultList.lookup(key, cls);
        }

        @Programmatic
        public <T, F extends BuilderScriptAbstract<T,F>> void executeChild(
                final FixtureScript callingFixtureScript,
                final PersonaWithBuilderScript<T, F> personaWithBuilderScript) {
            executeChildren(callingFixtureScript, personaWithBuilderScript);
        }

        /**
         * Executes a child {@link FixtureScript fixture script}, injecting services into it first, and (for any results
         * that are {@link FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that is derived from the fixture's class name.
         */
        @Programmatic
        public void executeChild(final FixtureScript callingFixtureScript, final FixtureScript childFixtureScript) {
            executeChildT(callingFixtureScript, childFixtureScript);
        }

        @Programmatic
        public <T, F extends BuilderScriptAbstract<T,F>> void executeChildren(
                final FixtureScript callingFixtureScript,
                final PersonaWithBuilderScript<T,F>... personaWithBuilderScripts) {
            for (PersonaWithBuilderScript<T, F> builder : personaWithBuilderScripts) {
                executeChild(callingFixtureScript, builder.builder());
            }
        }

        @Programmatic
        public <
                T,
                E extends Enum<E> & PersonaWithBuilderScript<T, B>,
                B extends BuilderScriptAbstract<T,B>
                >
        void executeChildren(
                final FixtureScript callingFixtureScript,
                final Class<E> personaClass) {
            for (E enumConstant : personaClass.getEnumConstants()) {
                executeChild(callingFixtureScript, enumConstant.builder());
            }
        }

        @Programmatic
        public void executeChildren(
                final FixtureScript callingFixtureScript,
                final FixtureScript... fixtureScripts) {
            for (FixtureScript fixtureScript : fixtureScripts) {
                executeChild(callingFixtureScript, fixtureScript);
            }
        }

        /**
         * Executes a child {@link FixtureScript fixture script}, injecting services into it first, and (for any results
         * that are {@link FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that is derived from the fixture's class name.
         *
         * @return the child fixture script.
         */
        @Programmatic
        public <T extends FixtureScript> T executeChildT(final FixtureScript callingFixtureScript, final T childFixtureScript) {
             return executeChildT(callingFixtureScript, null, childFixtureScript);
        }

        /**
         * Executes a child {@link FixtureScript fixture script}, injecting services into it first, and (for any results
         * that are {@link FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that overriding the default name of the fixture script with one more meaningful in the context of this fixture.
         */
        @Programmatic
        public void executeChild(final FixtureScript callingFixtureScript, final String localNameOverride, final FixtureScript childFixtureScript) {

            if(childFixtureScript == null) {
                return;
            }
            executeChildT(callingFixtureScript, localNameOverride, childFixtureScript);
        }

        /**
         * Executes a child {@link FixtureScript fixture script}, injecting services into it first, and (for any results
         * that are {@link FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that overriding the default name of the fixture script with one more meaningful in the context of this fixture.
         *
         * @return the child fixture script.
         */
        @Programmatic
        public <T extends FixtureScript> T executeChildT(final FixtureScript callingFixtureScript, final String localNameOverride, final T childFixtureScript) {

            childFixtureScript.setParentPath(callingFixtureScript.pathWith(""));
            childFixtureScript.withTracing(callingFixtureScript.tracePrintStream); // cascade down

            if(localNameOverride != null) {
                childFixtureScript.setLocalName(localNameOverride);
            }
            callingFixtureScript.getContainer().injectServicesInto(childFixtureScript);

            final T childOrPreviouslyExecuted = executeChildIfNotAlready(childFixtureScript);

            return childOrPreviouslyExecuted;
        }


        static enum As { EXEC, SKIP }

        /**
         * DO <i>NOT</i> CALL DIRECTLY; instead use {@link FixtureScript.ExecutionContext#executeChild(FixtureScript, String, FixtureScript)} or {@link FixtureScript.ExecutionContext#executeChild(FixtureScript, FixtureScript)}.
         *
         * @deprecated - should not be called directly, but has <code>public</code> visibility so there is scope for confusion.  Replaced by method with private visibility.
         */
        @Deprecated
        @Programmatic
        public void executeIfNotAlready(final FixtureScript fixtureScript) {
            executeChildIfNotAlready(fixtureScript);
        }

        private <T extends FixtureScript> T executeChildIfNotAlready(final T childFixtureScript) {

            final FixtureScripts.MultipleExecutionStrategy executionStrategy =
                    determineExecutionStrategy(childFixtureScript);

            FixtureScript previouslyExecutedScript;
            switch (executionStrategy) {

            case IGNORE:
            case EXECUTE_ONCE_BY_CLASS:
                previouslyExecutedScript = fixtureScriptByClass.get(childFixtureScript.getClass());
                if (previouslyExecutedScript == null) {
                    if (childFixtureScript instanceof WithPrereqs) {
                        final WithPrereqs withPrereqs = (WithPrereqs) childFixtureScript;
                        withPrereqs.execPrereqs(this);
                    }
                }
                // the prereqs might now result in a match, so we check again.
                previouslyExecutedScript = fixtureScriptByClass.get(childFixtureScript.getClass());
                if (previouslyExecutedScript == null) {
                    trace(childFixtureScript, As.EXEC);
                    childFixtureScript.execute(this);
                    this.previouslyExecuted.add(childFixtureScript);
                    fixtureScriptByClass.put(childFixtureScript.getClass(), childFixtureScript);
                    return childFixtureScript;
                } else {
                    trace(childFixtureScript, As.SKIP);
                    return (T)previouslyExecutedScript;
                }

            case EXECUTE_ONCE_BY_VALUE:
                return executeChildIfNotAlreadyWithValueSemantics(childFixtureScript);

            case EXECUTE:
                trace(childFixtureScript, As.EXEC);
                childFixtureScript.execute(this);
                this.previouslyExecuted.add(childFixtureScript);
                return childFixtureScript;

            default:
                throw new IllegalArgumentException("Execution strategy: '" + executionStrategy + "' not recognized");
            }
        }

        private <T extends FixtureScript> FixtureScripts.MultipleExecutionStrategy determineExecutionStrategy(final T childFixtureScript) {
            final FixtureScripts.MultipleExecutionStrategy executionStrategy;

            if(childFixtureScript instanceof FixtureScriptWithExecutionStrategy) {
                final FixtureScriptWithExecutionStrategy fixtureScriptWithExecutionStrategy =
                        (FixtureScriptWithExecutionStrategy) childFixtureScript;
                executionStrategy = fixtureScriptWithExecutionStrategy.getMultipleExecutionStrategy();
            } else {
                executionStrategy = fixtureScripts.getMultipleExecutionStrategy();
            }
            return executionStrategy;
        }

        private <T extends FixtureScript> T executeChildIfNotAlreadyWithValueSemantics(final T childFixtureScript) {
            FixtureScript previouslyExecutedScript = fixtureScriptByValue.get(childFixtureScript);
            if (previouslyExecutedScript == null) {
                if (childFixtureScript instanceof WithPrereqs) {
                    final WithPrereqs withPrereqs = (WithPrereqs) childFixtureScript;
                    withPrereqs.execPrereqs(this);
                }
            }
            // the prereqs might now result in a match, so we check again.
            previouslyExecutedScript = fixtureScriptByValue.get(childFixtureScript);
            if (previouslyExecutedScript == null) {
                trace(childFixtureScript, As.EXEC);
                childFixtureScript.execute(this);
                this.previouslyExecuted.add(childFixtureScript);
                fixtureScriptByValue.put(childFixtureScript, childFixtureScript);
                return childFixtureScript;
            } else {
                trace(childFixtureScript, As.SKIP);
                return (T)previouslyExecutedScript;
            }
        }

        //region > previouslyExecuted

        /**
         * Always populated, irrespective of {@link FixtureScripts#getMultipleExecutionStrategy() execution strategy},
         * but used only by {@link FixtureScripts.MultipleExecutionStrategy#EXECUTE_ONCE_BY_VALUE} to determine whether
         * should execute or not.
         */
        private final List<FixtureScript> previouslyExecuted = Lists.newArrayList();

        /**
         * Returns a list of the {@link FixtureScript} instances that have already been executed.
         *
         * <p>
         *     This allows each individual {@link FixtureScript} to determine whether they need to execute; the
         *     {@link FixtureScripts#getMultipleExecutionStrategy()} can then be left as simply
         *     {@link FixtureScripts.MultipleExecutionStrategy#EXECUTE}.
         * </p>
         */
        @Programmatic
        public List<FixtureScript> getPreviouslyExecuted() {
            return Collections.unmodifiableList(previouslyExecuted);
        }

        //endregion

        /**
         * used and populated only if the {@link FixtureScripts.MultipleExecutionStrategy#EXECUTE_ONCE_BY_CLASS}
         * strategy is in use.
         */
        private final Map<Class<? extends FixtureScript>, FixtureScript> fixtureScriptByClass = Maps.newLinkedHashMap();

        /**
         * used and populated only if the {@link FixtureScripts.MultipleExecutionStrategy#EXECUTE_ONCE_BY_VALUE}
         * strategy is in use.
         */
        private final Map<FixtureScript, FixtureScript> fixtureScriptByValue = Maps.newLinkedHashMap();

        //endregion

        //region > tracing

        private int traceHighwatermark = 40;
        private PrintStream tracePrintStream;

        @Programmatic
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
        @Programmatic
        public void setUserData(final Object object) {
            userData.put(object.getClass(), object);
        }
        @Programmatic
        public <T> T getUserData(final Class<T> cls) {
            return (T) userData.get(cls);
        }
        @Programmatic
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
     * Entry point for {@link FixtureScripts} service to call.
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
     * Use instead {@link FixtureScript.ExecutionContext#lookup(String, Class)} directly.
     */
    @Programmatic
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
     * @deprecated - use instead {@link FixtureScript.ExecutionContext#executeChild(FixtureScript, String, FixtureScript)}.
     */
    @Deprecated
    protected void execute(
            final String localNameOverride,
            final FixtureScript childFixtureScript,
            final ExecutionContext executionContext) {
        executionContext.executeChild(this, localNameOverride, childFixtureScript);
    }

    /**
     * @deprecated - use instead {@link FixtureScript.ExecutionContext#executeChild(FixtureScript, String, FixtureScript)}.
     */
    @Deprecated
    protected void executeChild(
            final String localNameOverride,
            final FixtureScript childFixtureScript,
            final ExecutionContext executionContext) {
        executionContext.executeChild(this, localNameOverride, childFixtureScript);
    }

    /**
     * @deprecated - use instead {@link FixtureScript.ExecutionContext#executeChild(FixtureScript, FixtureScript)}
     */
    @Deprecated
    protected void execute(
            final FixtureScript childFixtureScript,
            final ExecutionContext executionContext) {
        executionContext.executeChild(this, childFixtureScript);
    }

    /**
     * @deprecated - use instead {@link FixtureScript.ExecutionContext#executeChild(FixtureScript, FixtureScript)}
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
     *  Instead call sub fixture scripts using {@link FixtureScript.ExecutionContext#executeChild(FixtureScript, FixtureScript)} or {@link FixtureScript.ExecutionContext#executeChild(FixtureScript, String, FixtureScript)}.
     * </p>
     */
    @Programmatic
    protected abstract void execute(final ExecutionContext executionContext);

    //endregion

    //region > (legacy) InstallableFixture impl

    @Override
    @Programmatic
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

    /**
     * Convenience method
     */
    protected <T> T mixin(final Class<T> mixinClass, final Object mixedIn) {
        return container.mixin(mixinClass, mixedIn);
    }

    /**
     * Convenience method
     */
    protected void nextTransaction() {
        transactionService.nextTransaction();
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
    @Getter
    protected DomainObjectContainer container;

    @javax.inject.Inject
    protected FactoryService factoryService;

    @javax.inject.Inject
    protected ServiceRegistry2 serviceRegistry;

    @javax.inject.Inject
    protected RepositoryService repositoryService;

    @javax.inject.Inject
    protected UserService userService;

    @javax.inject.Inject
    protected WrapperFactory wrapperFactory;

    @javax.inject.Inject
    protected TransactionService transactionService;

    @javax.inject.Inject
    protected SessionManagementService sessionManagementService;


    //endregion
}

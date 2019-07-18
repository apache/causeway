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
package org.apache.isis.extensions.fixtures.fixturescripts;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.isis.applib.AbstractViewModel;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.sessmgmt.SessionManagementService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.fixtures.api.FixtureScriptWithExecutionStrategy;
import org.apache.isis.extensions.fixtures.api.PersonaWithBuilderScript;
import org.apache.isis.extensions.fixtures.api.WithPrereqs;
import org.apache.isis.runtime.system.context.IsisContext;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.transaction.support.TransactionTemplate;

@ViewModelLayout(named="Script")
public abstract class FixtureScript
extends AbstractViewModel {

    protected static final String PATH_SEPARATOR = "/";

    // -- constructors

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
     * @param discoverability - whether this fixture script can be rendered as a choice to execute through {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts#runFixtureScript(FixtureScript, String)}}.
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
     * @param discoverability - whether this fixture script can be rendered as a choice to execute through {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts#runFixtureScript(FixtureScript, String)}}.
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
        return str != null ? str : _Strings.asLowerDashed.apply(friendlyNameElseDerived(str));
    }

    protected String friendlyNameElseDerived(final String str) {
        return str != null ? str : _Strings.asNaturalName2.apply(getClass().getSimpleName());
    }



    // -- tracing

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



    // -- viewModel impl

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



    // -- qualifiedName

    @Programmatic
    public String getQualifiedName() {
        return getParentPath() + getLocalName();
    }



    // -- friendlyName (property)

    private String friendlyName;

    @Title
    @PropertyLayout(hidden = Where.EVERYWHERE)
    public String getFriendlyName() {
        return friendlyName;
    }
    public void setFriendlyName(final String friendlyName) {
        this.friendlyName = friendlyName;
    }



    // -- localName

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



    // -- parentPath

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



    // -- discoverability


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
     * constructor, however or by calling the {@link #withDiscoverability(org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript.Discoverability) setter}.
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



    // -- ExecutionContext

    public static class ExecutionContext {

        /**
         * Null implementation, to assist with unit testing of {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript}s.
         */
        public static final ExecutionContext NOOP = new ExecutionContext((String)null, null) {
            @Override
            public <T> T addResult(final FixtureScript script, final T object) {
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

        @Programmatic
        public <T> T addResult(final FixtureScript script, final T object) {
            fixtureResultList.add(script, object);
            return object;
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
        public void executeChild(
                final FixtureScript callingFixtureScript,
                final PersonaWithBuilderScript<?> personaWithBuilderScript) {
            
            executeChildren(callingFixtureScript, personaWithBuilderScript);
        }

        @Programmatic
        public <T, F extends BuilderScriptAbstract<T>> T executeChildT(
                final FixtureScript callingFixtureScript,
                final PersonaWithBuilderScript<F> personaWithBuilderScript) {
            final F childFixtureScript = personaWithBuilderScript.builder();
            final F f = executeChildT(callingFixtureScript, childFixtureScript);
            return f.getObject();
        }

        /**
         * Executes a child {@link FixtureScript fixture script}, injecting services into it first, and (for any results
         * that are {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that is derived from the fixture's class name.
         */
        @Programmatic
        public void executeChild(final FixtureScript callingFixtureScript, final FixtureScript childFixtureScript) {
            executeChildT(callingFixtureScript, childFixtureScript);
        }

        @Programmatic
        public void executeChildren(
                final FixtureScript callingFixtureScript,
                final PersonaWithBuilderScript<?>... personaWithBuilderScripts) {
            for (PersonaWithBuilderScript<?> builder : personaWithBuilderScripts) {
                BuilderScriptAbstract<?> childFixtureScript = builder.builder();
                executeChild(callingFixtureScript, childFixtureScript);
            }
        }

        @Programmatic
        public <T extends Enum<?> & PersonaWithBuilderScript<?>> void executeChildren(
                final FixtureScript callingFixtureScript,
                final Class<T> personaClass) {
            executeChildren(callingFixtureScript, personaClass.getEnumConstants());
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
         * that are {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
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
         * that are {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that overriding the default name of the fixture script with one more meaningful in the context of this fixture.
         */
        @Programmatic
        public void executeChild(
                final FixtureScript callingFixtureScript, 
                final String localNameOverride,
                final FixtureScript childFixtureScript) {

            if(childFixtureScript == null) {
                return;
            }
            executeChildT(callingFixtureScript, localNameOverride, childFixtureScript);
        }

        /**
         * Executes a child {@link FixtureScript fixture script}, injecting services into it first, and (for any results
         * that are {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that overriding the default name of the fixture script with one more meaningful in the context of this fixture.
         *
         * @return the child fixture script.
         */
        @Programmatic
        public <T extends FixtureScript> T executeChildT(
                final FixtureScript callingFixtureScript, 
                final String localNameOverride, 
                final T childFixtureScript) {

            childFixtureScript.setParentPath(callingFixtureScript.pathWith(""));
            childFixtureScript.withTracing(callingFixtureScript.tracePrintStream); // cascade down

            if(localNameOverride != null) {
                childFixtureScript.setLocalName(localNameOverride);
            }
            callingFixtureScript.serviceInjector.injectServicesInto(childFixtureScript);

            final T childOrPreviouslyExecuted = executeChildIfNotAlready(childFixtureScript);

            return childOrPreviouslyExecuted;
        }


        static enum As { EXEC, SKIP }


        private <T extends FixtureScript> T executeChildIfNotAlready(final T childFixtureScript) {

            final FixtureScripts.MultipleExecutionStrategy executionStrategy =
                    determineExecutionStrategy(childFixtureScript);

            FixtureScript previouslyExecutedScript;
            switch (executionStrategy) {

            case EXECUTE_ONCE_BY_CLASS:
                previouslyExecutedScript = fixtureScriptByClass.get(childFixtureScript.getClass());
                if (previouslyExecutedScript == null) {
                    if (childFixtureScript instanceof WithPrereqs) {
                        final WithPrereqs<?> withPrereqs = (WithPrereqs<?>) childFixtureScript;
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
                    return _Casts.uncheckedCast(previouslyExecutedScript);
                }

            case EXECUTE_ONCE_BY_VALUE:
                return executeChildIfNotAlreadyWithValueSemantics(childFixtureScript);

            case EXECUTE:
                trace(childFixtureScript, As.EXEC);
                childFixtureScript.execute(this);
                this.previouslyExecuted.add(childFixtureScript);
                return childFixtureScript;

            default:
                throw _Exceptions.unmatchedCase("Execution strategy: '%s' not recognized", executionStrategy);
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
                    final WithPrereqs<?> withPrereqs = (WithPrereqs<?>) childFixtureScript;
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
                return _Casts.uncheckedCast(previouslyExecutedScript);
            }
        }

        // -- previouslyExecuted

        /**
         * Always populated, irrespective of {@link FixtureScripts#getMultipleExecutionStrategy() execution strategy},
         * but used only by {@link FixtureScripts.MultipleExecutionStrategy#EXECUTE_ONCE_BY_VALUE} to determine whether
         * should execute or not.
         */
        private final List<FixtureScript> previouslyExecuted = _Lists.newArrayList();

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



        /**
         * used and populated only if the {@link FixtureScripts.MultipleExecutionStrategy#EXECUTE_ONCE_BY_CLASS}
         * strategy is in use.
         */
        private final Map<Class<? extends FixtureScript>, FixtureScript> fixtureScriptByClass = _Maps.newLinkedHashMap();

        /**
         * used and populated only if the {@link FixtureScripts.MultipleExecutionStrategy#EXECUTE_ONCE_BY_VALUE}
         * strategy is in use.
         */
        private final Map<FixtureScript, FixtureScript> fixtureScriptByValue = _Maps.newLinkedHashMap();



        // -- tracing

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
            return _Strings.padEnd(key, roundup(traceHighwatermark, 20), ' ');
        }

        static int roundup(final int n, final int roundTo) {
            return ((n / roundTo) + 1) * roundTo;
        }


        private Map<Class<?>, Object> userData = _Maps.newHashMap();
        @Programmatic
        public void setUserData(final Object object) {
            userData.put(object.getClass(), object);
        }
        @Programmatic
        public <T> T getUserData(final Class<T> cls) {
            return _Casts.uncheckedCast(userData.get(cls));
        }
        @Programmatic
        public <T> T clearUserData(final Class<T> cls) {
            return _Casts.uncheckedCast(userData.remove(cls));
        }

    }



    // -- defaultParam, checkParam
    protected <T> T defaultParam(final String parameterName, final ExecutionContext ec, final T defaultValue) {
        final T value = valueFor(parameterName, ec, defaultValue);
        setParam(parameterName, value);
        return value;
    }

    private <T> T valueFor(final String parameterName, final ExecutionContext ec, final T defaultValue) {
        final Class<T> cls = _Casts.uncheckedCast(defaultValue.getClass());

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
            value = _Casts.uncheckedCast(method.invoke(this));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

        }
        if(value != null) { return (T) value; }

        if (cls == Boolean.class || cls == boolean.class) {
            try {
                method = this.getClass().getMethod("is" + uppercase(parameterName));
                value = _Casts.uncheckedCast(method.invoke(this));
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


    // -- run (entry point for FixtureScripts service to call)

    /**
     * It's a bit nasty to hold onto this as a field, but required in order to support
     */
    private ExecutionContext executionContext;

    /**
     * Entry point for {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts} service to call.
     *
     * <p>
     *     Package-visibility only, not public API.
     * </p>
     */
    final List<FixtureResult> run(final String parameters) {
        executionContext = fixtureScripts.newExecutionContext(parameters).withTracing(this.tracePrintStream);
        executionContext.executeChildIfNotAlready(this);
        return executionContext.getResults();
    }



    /**
     * Optional hook to validate parameters.
     */
    @Programmatic
    public String validateRun(final String parameters) {
        return null;
    }




    // -- execute (API for subclasses to implement)

    /**
     * Subclasses should <b>implement this</b> but SHOULD <i>NOT</i> CALL DIRECTLY.
     *
     * <p>
     *  Instead call sub fixture scripts using {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript.ExecutionContext#executeChild(org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript, org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript)} or {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript.ExecutionContext#executeChild(org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript, String, org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript)}.
     * </p>
     */
    @Programmatic
    protected abstract void execute(final ExecutionContext executionContext);


    // -- helpers (for subclasses)

    /**
     * Returns the first non-null value; for convenience of subclass implementations
     */
    @SafeVarargs
    protected static <T> T coalesce(final T... ts) {
        for (final T t : ts) {
            if(t != null) return t;
        }
        return null;
    }

    /**
     * Convenience method, simply delegates to {@link WrapperFactory#wrap(Object)}.
     */
    protected <T> T wrap(final T domainObject) {
        return wrapperFactory.wrap(domainObject);
    }

    /**
     * Convenience method, synonym for {@link #wrap(Object)}.
     */
    protected <T> T w(final T domainObject) {
        return wrap(domainObject);
    }

    /**
     * Convenience method, simply delegates to {@link WrapperFactory#wrap(Object, WrapperFactory.ExecutionMode)}.
     */
    protected <T> T wrap(final T domainObject, final WrapperFactory.ExecutionMode executionMode) {
        return wrapperFactory.wrap(domainObject, executionMode);
    }

    /**
     * Convenience method, simply delegates to {@link WrapperFactory#unwrap(Object)}.
     */
    protected <T> T unwrap(final T possibleWrappedDomainObject) {
        return wrapperFactory.unwrap(possibleWrappedDomainObject);
    }

    /**
     * Convenience method, simply delegates to {@link FactoryService#mixin(Class, Object)}.
     */
    protected <T> T mixin(final Class<T> mixinClass, final Object mixedIn) {
        return factoryService.mixin(mixinClass, mixedIn);
    }

    /**
     * Convenience method, synonym for {@link #mixin(Class, Object)}.
     */
    protected <T> T m(final Class<T> mixinClass, final Object mixedIn) {
        return factoryService.mixin(mixinClass, mixedIn);
    }

    /**
     * Convenience method, {@link #wrap(Object) wraps} a {@link #mixin(Class, Object) mixin}.
     */
    protected <T> T wrapMixin(final Class<T> mixinClass, final Object mixedIn) {
        return wrap(mixin(mixinClass, mixedIn));
    }

    /**
     * Convenience method, synonym for {@link #wrapMixin(Class, Object)}.
     */
    protected <T> T wm(final Class<T> mixinClass, final Object mixedIn) {
        return wrapMixin(mixinClass, mixedIn);
    }

    /**
     * Convenience method, simply delegates to {@link IsisContext#createTransactionTemplate}.
     */
    protected TransactionTemplate transactionTemplate() {
        return IsisContext.createTransactionTemplate();
    }


    // -- helpers (local)

    @Programmatic
    String pathWith(final String subkey) {
        return (getQualifiedName() != null? getQualifiedName() + PATH_SEPARATOR: "") +  subkey;
    }

    // -- DEPENDENCIES

    @Inject protected FixtureScripts fixtureScripts;
    @Inject protected FactoryService factoryService;
    @Inject protected ServiceRegistry serviceRegistry;
    @Inject protected ServiceInjector serviceInjector;
    @Inject protected RepositoryService repositoryService;
    @Inject protected UserService userService;
    @Inject protected WrapperFactory wrapperFactory;
    @Inject protected TransactionService transactionService;
    @Inject protected SessionManagementService sessionManagementService;



}

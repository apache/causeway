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

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.CharSource;
import org.apache.isis.applib.AbstractViewModel;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.fixtures.FixtureType;
import org.apache.isis.applib.fixtures.InstallableFixture;

@DomainObjectLayout(named="Script")
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
    protected String localNameElseDerived(String str) {
        return str != null ? str : StringUtil.asLowerDashed(friendlyNameElseDerived(str));
    }

    protected String friendlyNameElseDerived(String str) {
        return str != null ? str : StringUtil.asNaturalName2(getClass().getSimpleName());
    }

    //endregion

    //region > tracing

    private PrintStream tracePrintStream;

    /**
     * Enable tracing of the execution to the provided {@link java.io.PrintStream}.
     */
    @Programmatic
    public FixtureScript withTracing(PrintStream tracePrintStream) {
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
    public void viewModelInit(String mementoStr) {
        fixtureScripts.initOf(mementoStr, this);
    }

    //endregion

    //region > qualifiedName

    @Hidden
    public String getQualifiedName() {
        return getParentPath() + getLocalName();
    }

    //endregion

    //region > friendlyName (property)

    private String friendlyName;
    
    @Title
    @Hidden
    public String getFriendlyName() {
        return friendlyName;
    }
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    //endregion

    //region > localName

    private String localName;
    /**
     * Will always be populated, initially by the default name, but can be
     * {@link #setLocalName(String) overridden}.
     */
    @Hidden
    public String getLocalName() {
        return localName;
    }
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    //endregion

    //region > parentPath

    private String parentPath;
    
    /**
     * Path of the parent of this script (if any), with trailing {@value #PATH_SEPARATOR}.
     */
    @Hidden
    public String getParentPath() {
        return parentPath;
    }
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
    @Hidden
    public boolean isDiscoverable() {
        return discoverability == Discoverability.DISCOVERABLE;
    }
    public FixtureScript withDiscoverability(Discoverability discoverability) {
        this.discoverability = discoverability;
        return this;
    }

    //endregion

    //region > ExecutionContext

    public static class ExecutionContext {

        /**
         * Null implementation, to assist with unit testing of {@link org.apache.isis.applib.fixturescripts.FixtureScript}s.
         */
        public static final ExecutionContext NOOP = new ExecutionContext(null, null) {
            @Override
            public <T> T add(FixtureScript script, T object) {
                return object;
            }
            @Override
            public <T> T addResult(FixtureScript script, T object) {
                return object;
            }

            @Override
            public <T> T add(FixtureScript script, String key, T object) {
                return object;
            }
            @Override
            public <T> T addResult(FixtureScript script, String key, T object) {
                return object;
            }

            @Override
            public List<FixtureResult> getResults() {
                return Collections.emptyList();
            }
        };

        private final static Pattern keyEqualsValuePattern = Pattern.compile("([^=]*)=(.*)");

        private final String parameters;
        private final FixtureScripts fixtureScripts;
        private final FixtureResultList fixtureResultList;
        private final Map<String, String> parameterMap;

        public ExecutionContext(final String parameters, final FixtureScripts fixtureScripts) {
            this.fixtureScripts = fixtureScripts;
            fixtureResultList = new FixtureResultList(fixtureScripts, this);
            this.parameters = parameters;
            this.parameterMap = asKeyValueMap(parameters);
        }

        public static Map<String, String> asKeyValueMap(String parameters) {
            Map<String,String> keyValues = Maps.newLinkedHashMap();
            if(parameters != null) {
                try {
                    final ImmutableList<String> lines = CharSource.wrap(parameters).readLines();
                    for (String line : lines) {
                        if (line == null) {
                            continue;
                        }
                        final Matcher matcher = keyEqualsValuePattern.matcher(line);
                        if (matcher.matches()) {
                            keyValues.put(matcher.group(1).trim(), matcher.group(2).trim());
                        }
                    }
                } catch (IOException e) {
                    // ignore, shouldn't happen
                }
            }
            return keyValues;
        }

        public String getParameters() {
            return parameters;
        }

        public String getParameter(String parameterName) {
            return parameterMap.get(parameterName);
        }

        public Map<String,String> getParameterMap() {
            return Collections.unmodifiableMap(parameterMap);
        }

        public void setParameterIfNotPresent(String parameterName, String parameterValue) {
            if(parameterName == null) {
                throw new IllegalArgumentException("parameterName required");
            }
            if(parameterValue == null) {
                // ignore
                return;
            }
            if(parameterMap.containsKey(parameterName)) {
                // ignore; the existing parameter take precedence
                return;
            }
            parameterMap.put(parameterName, parameterValue);
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
         public void executeChild(FixtureScript callingFixtureScript, FixtureScript childFixtureScript) {
            executeChild(callingFixtureScript, null, childFixtureScript);
        }

        /**
         * Executes a child {@link FixtureScript fixture script}, injecting services into it first, and (for any results
         * that are {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#addResult(FixtureScript, Object)} added),
         * uses a key that overriding the default name of the fixture script with one more meaningful in the context of this fixture.
         */
        public void executeChild(FixtureScript callingFixtureScript, String localNameOverride, FixtureScript childFixtureScript) {

            childFixtureScript.setParentPath(callingFixtureScript.pathWith(""));
            if(localNameOverride != null) {
                childFixtureScript.setLocalName(localNameOverride);
            }
            callingFixtureScript.getContainer().injectServicesInto(childFixtureScript);
            executeChildIfNotAlready(childFixtureScript);
        }


        static enum As { EXEC, SKIP }

        /**
         * DO <i>NOT</i> CALL DIRECTLY; instead use {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#executeChild(org.apache.isis.applib.fixturescripts.FixtureScript, String, org.apache.isis.applib.fixturescripts.FixtureScript)} or {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#executeChild(FixtureScript, FixtureScript)}.
         *
         * @deprecated - should not be called directly, but has <code>public</code> visibility so there is scope for confusion.  Replaced by method with private visibility.
         */
        @Deprecated
        public void executeIfNotAlready(FixtureScript fixtureScript) {
            executeChildIfNotAlready(fixtureScript);
        }

        private void executeChildIfNotAlready(FixtureScript fixtureScript) {
            if(shouldExecute(fixtureScript)) {
                trace(fixtureScript, As.EXEC);
                fixtureScript.execute(this);
            } else {
                trace(fixtureScript, As.SKIP);
            }
        }

        //region > shouldExecute

        private final Map<String,Class> fixtureScriptClasses = Maps.newLinkedHashMap();

        private boolean shouldExecute(FixtureScript fixtureScript) {
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

        public ExecutionContext withTracing(PrintStream tracePrintStream) {
            this.tracePrintStream = tracePrintStream;
            return this;
        }

        private void trace(FixtureScript fixtureScript, As as) {
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

        private String pad(String key) {
            traceHighwatermark = Math.max(key.length(), traceHighwatermark);
            return pad(key, roundup(traceHighwatermark, 20));
        }
        //endregion

        private static String pad(String str, int padTo) {
            return Strings.padEnd(str, padTo, ' ');
        }

        static int roundup(int n, int roundTo) {
            return ((n / roundTo) + 1) * roundTo;
        }


        private Map<Class, Object> userData = Maps.newHashMap();
        public void setUserData(Object object) {
            userData.put(object.getClass(), object);
        }
        public <T> T getUserData(Class<T> cls) {
            return (T) userData.get(cls);
        }
        public <T> T clearUserData(Class<T> cls) {
            return (T) userData.remove(cls);
        }

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
    public <T> T lookup(final String key, Class<T> cls) {
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
     * Returns the first non-null string; for convenience of subclass implementations
     */
    protected static String coalesce(final String... strings) {
        for (String string : strings) {
            if(string != null) return string;
        }
        return null;
    }

    //endregion

    //region > helpers (local)

    @Programmatic
    String pathWith(String subkey) {
        return (getQualifiedName() != null? getQualifiedName() + PATH_SEPARATOR: "") +  subkey;
    }
    //endregion

    //region > injected services

    @javax.inject.Inject
    protected FixtureScripts fixtureScripts;

    @javax.inject.Inject
    protected DomainObjectContainer container;

    //endregion
}

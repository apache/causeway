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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.isis.applib.AbstractViewModel;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.fixtures.FixtureType;
import org.apache.isis.applib.fixtures.InstallableFixture;

@Named("Script")
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
    public FixtureScript withTracing(PrintStream tracePrintStream) {
        this.tracePrintStream = tracePrintStream;
        return this;
    }

    /**
     * Enable tracing of the execution to stdout.
     */
    public FixtureScript withTracing() {
        return withTracing(System.out);
    }

    //endregion

    //region > viewModel impl

    @Override
    public String viewModelMemento() {
        return fixtureScripts.mementoFor(this);
    }

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
     * {@link #setLocalName(String) overridden} if
     * {@link DiscoverableFixtureScript#execute(FixtureScript, org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)} reused} within a {@link DiscoverableFixtureScript composite fixture script}.
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
            public <T> T add(FixtureScript script, String key, T object) {
                return object;
            }

            @Override
            public List<FixtureResult> getResults() {
                return Collections.emptyList();
            }
        };


        private final String parameters;
        private final FixtureScripts fixtureScripts;
        private final FixtureResultList fixtureResults;

        public ExecutionContext(final String parameters, final FixtureScripts fixtureScripts) {
            this.fixtureScripts = fixtureScripts;
            fixtureResults = new FixtureResultList(fixtureScripts);
            this.parameters = parameters;
        }

        public String getParameters() {
            return parameters;
        }
        public List<FixtureResult> getResults() {
            return fixtureResults.getResults();
        }

        public <T> T add(final FixtureScript script, final T object) {
            return fixtureResults.add(script, object);
        }

        public <T> T add(final FixtureScript script, final String key, final T object) {
            return fixtureResults.add(script, key, object);
        }

        static enum As { EXEC, SKIP }

        /**
         * DO <i>NOT</i> CALL DIRECTLY; instead use {@link org.apache.isis.applib.fixturescripts.FixtureScript#executeChild(String, FixtureScript, org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)} or
         * {@link org.apache.isis.applib.fixturescripts.FixtureScript#executeChild(FixtureScript, org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)}.
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

        private PrintStream tracePrintStream;
        private void trace(FixtureScript fixtureScript, As as) {
            if(tracePrintStream == null) {
                return;
            }
            final String qualifiedName = fixtureScript.getQualifiedName();
            final String paddedQualifiedName = pad(qualifiedName, maxQualifiedNameLength());
            final String trace = paddedQualifiedName + ": " + as + " " + fixtureScript.getClass().getName() + "\n";
            tracePrintStream.print(trace);
            tracePrintStream.flush();
        }

        public ExecutionContext withTracing(PrintStream tracePrintStream) {
            this.tracePrintStream = tracePrintStream;
            return this;
        }

        //endregion

        private static String pad(String str, int padTo) {
            return Strings.padEnd(str, padTo, ' ');
        }

        private int maxQualifiedNameLength() {
            int max = 40;
            for (final String qualifiedName : this.fixtureScriptClasses.keySet()) {
                max = Math.max(max, qualifiedName.length());
            }
            return roundup(max, 20);
        }

        static int roundup(int n, int roundTo) {
            return ((n / roundTo) + 1) * roundTo;
        }

    }

    //endregion

    //region > run (entry point for FixtureScripts service to call)

    /**
     * Entry point for {@link org.apache.isis.applib.fixturescripts.FixtureScripts} service to call.
     *
     * <p>
     *     DO <i>NOT</i> CALL DIRECTLY.
     * </p>
     */
    @Programmatic
    public final List<FixtureResult> run(final String parameters) {
        final ExecutionContext executionContext = fixtureScripts.newExecutionContext(parameters).withTracing(this.tracePrintStream);
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

    //endregion

    //region > executeChild (API for subclasses to call); deprecated execute(FixtureScript, ExecutionContext)

    /**
     * Renamed to {@link #executeChild(String, FixtureScript, org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)}
     * to avoid confusion with {@link #execute(org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)}.
     * 
     * @deprecated
     */
    @Deprecated
    protected void execute(
            final String localNameOverride,
            final FixtureScript childFixtureScript,
            final ExecutionContext executionContext) {
        executeChild(localNameOverride, childFixtureScript, executionContext);
    }

    /**
     * Executes a child {@link FixtureScript fixture script}, overriding its default name with one more
     * meaningful in the context of this fixture.
     */
    protected void executeChild(
            final String localNameOverride,
            final FixtureScript childFixtureScript,
            final ExecutionContext executionContext) {

        childFixtureScript.setParentPath(pathWith(""));
        if(localNameOverride != null) {
            childFixtureScript.setLocalName(localNameOverride);
        }
        getContainer().injectServicesInto(childFixtureScript);
        executionContext.executeChildIfNotAlready(childFixtureScript);
    }

    /**
     * Renamed to {@link #executeChild(FixtureScript, org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)}
     * to avoid confusion with {@link #execute(org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)}.
     *
     * @deprecated
     */
    @Deprecated
    protected void execute(
            final FixtureScript childFixtureScript,
            final ExecutionContext executionContext) {
        executeChild(childFixtureScript, executionContext);
    }

    /**
     * Executes a child {@link FixtureScript fixture script} (simply using its default name).
     */
    protected void executeChild(
            final FixtureScript childFixtureScript,
            final ExecutionContext executionContext) {
        executeChild(null, childFixtureScript, executionContext);
    }

    //endregion

    //region > execute (API for subclasses to implement)

    /**
     * Subclasses should <b>implement this</b> but SHOULD <i>NOT</i> CALL DIRECTLY.
     *
     * <p>
     *  Instead call sub fixture scripts using {@link #executeChild(FixtureScript, org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)} or
     *  {@link #executeChild(String, org.apache.isis.applib.fixturescripts.FixtureScript, org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)}.
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

    //endregion
}

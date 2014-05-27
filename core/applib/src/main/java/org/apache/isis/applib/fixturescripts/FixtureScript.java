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
import java.io.PrintWriter;
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

    // //////////////////////////////////////
    
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

    // //////////////////////////////////////

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
     * @param discoverability
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

    // //////////////////////////////////////


    private PrintStream tracePrintStream;
    private PrintWriter tracePrintWriter;

    /**
     * Enable tracing of the execution to the provided {@link java.io.PrintStream}.
     */
    public FixtureScript withTracing(PrintStream tracePrintStream) {
        this.tracePrintStream = tracePrintStream;
        return this;
    }

    /**
     * Enable tracing of the execution to the provided {@link java.io.PrintWriter}.
     */
    public FixtureScript withTracing(PrintWriter tracePrintWriter) {
        this.tracePrintWriter = tracePrintWriter;
        return this;
    }

    /**
     * Enable tracing of the execution to stdout.
     */
    public FixtureScript withTracing() {
        return withTracing(System.out);
    }

    // //////////////////////////////////////

    /**
     * Returns the first non-null string; for convenience of subclass implementations
     */
    protected static String coalesce(final String... strings) {
        for (String string : strings) {
            if(string != null) return string;
        }
        return null;
    }

    // //////////////////////////////////////

    @Override
    public String viewModelMemento() {
        return fixtureScripts.mementoFor(this);
    }

    @Override
    public void viewModelInit(String mementoStr) {
        fixtureScripts.initOf(mementoStr, this);
    }

    // //////////////////////////////////////
    
    @Hidden
    public String getQualifiedName() {
        return getParentPath() + getLocalName();
    }

    // //////////////////////////////////////
    
    private String friendlyName;
    
    @Title
    @Hidden
    public String getFriendlyName() {
        return friendlyName;
    }
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
    
    // //////////////////////////////////////
    
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
    
    // //////////////////////////////////////
    
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
    
    // //////////////////////////////////////
    
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
    
    // //////////////////////////////////////

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
        private final FixtureResultList fixtureResults;
        private final Map<String,Class> fixtureScriptClasses = Maps.newLinkedHashMap();

        public ExecutionContext(String parameters, FixtureScripts fixtureScripts) {
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

        public void executeIfNotAlready(FixtureScript fixtureScript) {
            if(!hasExecuted(fixtureScript)) {
                fixtureScript.execute(this);
                executed(fixtureScript);
            }
        }

        private boolean hasExecuted(FixtureScript fixtureScript) {
            return fixtureScriptClasses.values().contains(fixtureScript.getClass());
        }

        private void executed(FixtureScript fixtureScript) {
            fixtureScriptClasses.put(fixtureScript.getQualifiedName(), fixtureScript.getClass());
        }

        public StringBuilder appendExecutedTo(StringBuilder buf) {
            final int max = maxQualifiedNameLength();
            for (final String qualifiedName : this.fixtureScriptClasses.keySet()) {
                buf.append(pad(qualifiedName, max))
                   .append(": ")
                   .append(fixtureScriptClasses.get(qualifiedName).getName())
                   .append("\n");
            }
            return buf;
        }

        private int maxQualifiedNameLength() {
            int max = 0;
            for (final String qualifiedName : this.fixtureScriptClasses.keySet()) {
                max = Math.max(max, qualifiedName.length());
            }
            return max;
        }

        private static String pad(String str, int len) {
            return Strings.padEnd(str, len, ' ');
        }

    }

    @Programmatic
    public final List<FixtureResult> run(final String parameters) {
        final ExecutionContext executionContext = new ExecutionContext(parameters, fixtureScripts);
        executionContext.executeIfNotAlready(this);
        traceIfRequired(executionContext);
        return executionContext.getResults();
    }

    private void traceIfRequired(ExecutionContext executionContext) {
        if(tracePrintStream != null || tracePrintWriter != null) {
            final String trace = executionContext.appendExecutedTo(new StringBuilder()).toString();
            if(tracePrintWriter != null) {
                tracePrintWriter.print(trace);
            }
            if(tracePrintStream != null) {
                tracePrintStream.print(trace);
            }
        }
    }

    /**
     * Optional hook to validate parameters.
     */
    @Programmatic
    public String validateRun(final String parameters) {
        return null;
    }



    // //////////////////////////////////////

    /**
     * Adds a child {@link FixtureScript fixture script}, overriding its default name with one more
     * meaningful in the context of this fixture.
     */
    protected final void execute(final String localNameOverride, final FixtureScript fixtureScript, ExecutionContext executionContext) {
        fixtureScript.setParentPath(pathWith(""));
        if(localNameOverride != null) {
            fixtureScript.setLocalName(localNameOverride);
        }
        getContainer().injectServicesInto(fixtureScript);
        executionContext.executeIfNotAlready(fixtureScript);
    }
    /**
     * Executes a child {@link FixtureScript fixture script} (simply using its default name).
     */
    protected final void execute(final FixtureScript fixtureScript, ExecutionContext executionContext) {
        execute(null, fixtureScript, executionContext);
    }

    /**
     * Do not call directly; instead use {@link ExecutionContext#executeIfNotAlready(FixtureScript)}
     */
    @Programmatic
    protected abstract void execute(final ExecutionContext executionContext);


    // //////////////////////////////////////

    @Override
    public FixtureType getType() {
        return FixtureType.DOMAIN_OBJECTS;
    }
    
    @Programmatic
    public final void install() {
        run(null);
    }

    // //////////////////////////////////////
    
    @Programmatic
    String pathWith(String subkey) {
        return (getQualifiedName() != null? getQualifiedName() + PATH_SEPARATOR: "") +  subkey;
    }

    // //////////////////////////////////////

    @javax.inject.Inject
    protected FixtureScripts fixtureScripts;



}

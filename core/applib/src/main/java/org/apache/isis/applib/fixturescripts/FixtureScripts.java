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
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryService;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryService2;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsDefault;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsSpecification;
import org.apache.isis.applib.services.memento.MementoService;
import org.apache.isis.applib.services.memento.MementoService.Memento;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.util.ObjectContracts;

/**
 * Rather than subclassing, instead implement
 * {@link org.apache.isis.applib.services.fixturespec.FixtureScriptsSpecificationProvider}.  The framework will
 * automatically provide a default implementation configured using that provider service.
 */
public abstract class FixtureScripts extends AbstractService {

    //region > Specification, nonPersistedObjectsStrategy, multipleExecutionStrategy enums

    /**
     * How to handle objects that are to be
     * {@link FixtureScripts#newFixtureResult(FixtureScript, String, Object, boolean) added}
     * into a {@link org.apache.isis.applib.fixturescripts.FixtureResult} but which are not yet persisted.
     */
    public enum NonPersistedObjectsStrategy {
        PERSIST,
        IGNORE
    }

    /**
     * How to handle fixture scripts that are submitted to be executed more than once.
     *
     * <p>
     *     Note that this is a {@link FixtureScripts#getMultipleExecutionStrategy() global setting} of the
     *     {@link FixtureScripts} service; there isn't (currently) any way to mix-and-match fixture scripts that are
     *     written with differing semantics in mind.  Ideally it should be the responsibility of the fixture script
     *     itself to determine whether it should be run.  As a partial solution to this, the
     *
     * </p>
     */
    public enum MultipleExecutionStrategy {
        /**
         * @deprecated - renamed to {@link #EXECUTE_ONCE_BY_CLASS}.
         */
        @Deprecated
        IGNORE,
        /**
         * Any given fixture script (or more precisely, any fixture script instance for a particular fixture script
         * class) can only be run once.
         *
         * <p>
         *     This strategy represents the original design of fixture scripts service.  Specifically, it allows an
         *     arbitrary graph of fixture scripts (eg A -> B -> C, A -> B -> D, A -> C -> D) to be created each
         *     specifying its dependencies, and without having to worry or co-ordinate whether those prerequisite
         *     fixture scripts have already been run.
         * </p>
         * <p>
         *     The most obvious example is a global teardown script; every fixture script can require this to be
         *     called, but it will only be run once.
         * </p>
         * <p>
         *     Note that this strategy treats fixture scripts as combining both the 'how' (which business action(s) to
         *     call) and the also the 'what' (what the arguments are to those actions).
         * </p>
         */
        EXECUTE_ONCE_BY_CLASS,
        /**
         * Any given fixture script can only be run once, where the check to determine if a fixture script has already
         * been run is performed using value semantics.
         *
         * <p>
         *     This strategy is a half-way house between the {@link #EXECUTE_ONCE_BY_VALUE} and {@link #EXECUTE}
         *     strategies, where we want to prevent a fixture from running more than once, where by "fixture" we mean
         *     the 'what' - the data to be loaded up; the 'how' is unimportant.
         * </p>
         *
         * <p>
         *     This strategy was introduced in order to better support the <tt>ExcelFixture</tt> fixture script
         *     (provided by the (non-ASF) Isis Addons'
         *     <a href="https://github.com/isisaddons/isis-module-excel">Excel module</a>.  The <tt>ExcelFixture</tt>
         *     takes an Excel spreadsheet as the 'what' and loads up each row.  So the 'how' is re-usable (therefore
         *     the {@link #EXECUTE_ONCE_BY_CLASS} doesn't apply) on the other hand we don't want the 'what' to be
         *     loaded more than once (so the {@link #EXECUTE} strategy doesn't apply either).  The solution is for
         *     <tt>ExcelFixture</tt> to have value semantics (a digest of the spreadsheet argument).
         * </p>
         *
         * @see #IGNORE
         */
        EXECUTE_ONCE_BY_VALUE,
        /**
         * Allow fixture scripts to run as requested.
         *
         * <p>
         *     This strategy is conceptually the simplest; all fixtures are run as requested.  However, it is then
         *     the responsibility of the programmer to ensure that fixtures do not interfere with each other.  For
         *     example, if fixture A calls fixture B which calls teardown, and fixture A also calls fixture C that
         *     itself calls teardown, then fixture B's setup will get removed.
         * </p>
         * <p>
         *     The workaround to the teardown issue is of course to call the teardown fixture only once in the test
         *     itself; however even then this strategy cannot cope with arbitrary graphs of fixtures.  The solution
         *     is for the fixture list to be flat, one level high.
         * </p>
         */
        EXECUTE;

        /**
         * @deprecated - use {@link #isExecuteOnceByClass()}.
         * @return
         */
        @Deprecated
        public boolean isIgnore() {
            return this == IGNORE;
        }
        public boolean isExecuteOnceByClass() {
            return this == EXECUTE_ONCE_BY_CLASS;
        }
        public boolean isExecuteOnceByValue() {
            return this == EXECUTE_ONCE_BY_VALUE;
        }
        public boolean isExecute() {
            return this == EXECUTE;
        }
    }

    //endregion


    //region > constructors

    /**
     * Defaults to {@link FixtureScripts.NonPersistedObjectsStrategy#PERSIST persist}
     * strategy (if non-persisted objects are {@link FixtureScripts#newFixtureResult(FixtureScript, String, Object, boolean) added} to a {@link FixtureResultList}),
     * defaults {@link #getMultipleExecutionStrategy()} to {@link FixtureScripts.MultipleExecutionStrategy#IGNORE ignore}
     * if multiple instances of the same fixture script class are encountered.
     *
     * @param packagePrefix - to search for fixture script implementations, eg "com.mycompany".  Note that this is ignored if an {@link org.apache.isis.applib.AppManifest} is in use.
     *
     * @deprecated - use {@link #FixtureScripts(FixtureScriptsSpecification)} instead.
     */
    @Deprecated
    public FixtureScripts(final String packagePrefix) {
        this(FixtureScriptsSpecification.builder(packagePrefix)
                                        .build());
    }

    /**
     * Defaults to {@link FixtureScripts.NonPersistedObjectsStrategy#PERSIST persist}
     * strategy (if non-persisted objects are {@link FixtureScripts#newFixtureResult(FixtureScript, String, Object, boolean) added} to a {@link FixtureResultList}).
     *
     * @param packagePrefix - to search for fixture script implementations, eg "com.mycompany".    Note that this is ignored if an {@link org.apache.isis.applib.AppManifest} is in use.
     * @param multipleExecutionStrategy - whether more than one instance of the same fixture script class can be run multiple times.  See {@link MultipleExecutionStrategy} for more details.
     *
     * @deprecated - use {@link #FixtureScripts(FixtureScriptsSpecification)} instead.
     */
    @Deprecated
    public FixtureScripts(
            final String packagePrefix,
            final MultipleExecutionStrategy multipleExecutionStrategy) {
        this(FixtureScriptsSpecification.builder(packagePrefix)
                                        .with(multipleExecutionStrategy)
                .build());
    }

    /**
     * Defaults {@link #getMultipleExecutionStrategy()} to {@link FixtureScripts.MultipleExecutionStrategy#IGNORE ignore}
     * if multiple instances of the same fixture script class are encountered.
     *
     * @param packagePrefix  - to search for fixture script implementations, eg "com.mycompany".    Note that this is ignored if an {@link org.apache.isis.applib.AppManifest} is in use.
     * @param nonPersistedObjectsStrategy - how to handle any non-persisted objects that are {@link #newFixtureResult(FixtureScript, String, Object, boolean) added} to a {@link org.apache.isis.applib.fixturescripts.FixtureResultList}.
     *
     * @deprecated - use {@link #FixtureScripts(FixtureScriptsSpecification)} instead.
     */
    @Deprecated
    public FixtureScripts(
            final String packagePrefix, final NonPersistedObjectsStrategy nonPersistedObjectsStrategy) {
        this(FixtureScriptsSpecification.builder(packagePrefix)
                                        .with(nonPersistedObjectsStrategy)
                                        .build());
    }

    /**
     * @param packagePrefix  - to search for fixture script implementations, eg "com.mycompany".    Note that this is ignored if an {@link org.apache.isis.applib.AppManifest} is in use.
     * @param nonPersistedObjectsStrategy - how to handle any non-persisted objects that are {@link #newFixtureResult(FixtureScript, String, Object, boolean) added} to a {@link org.apache.isis.applib.fixturescripts.FixtureResultList}.
     * @param multipleExecutionStrategy - whether more than one instance of the same fixture script class can be run multiple times
     *
     * @deprecated - use {@link #FixtureScripts(FixtureScriptsSpecification)} instead.
     */
    @Deprecated
    public FixtureScripts(
            final String packagePrefix,
            final NonPersistedObjectsStrategy nonPersistedObjectsStrategy,
            final MultipleExecutionStrategy multipleExecutionStrategy) {
        this(FixtureScriptsSpecification.builder(packagePrefix)
                                        .with(nonPersistedObjectsStrategy)
                                        .with(multipleExecutionStrategy)
                                        .build());
    }

    /**
     * @param specification - specifies how the service will find instances and execute them.
     */
    public FixtureScripts(final FixtureScriptsSpecification specification) {
        this.specification = specification;
    }

    //endregion


    //region > packagePrefix, nonPersistedObjectsStrategy, multipleExecutionStrategy

    private FixtureScriptsSpecification specification;

    @Programmatic
    public FixtureScriptsSpecification getSpecification() {
        return specification;
    }

    /**
     * Allows the specification to be overridden if required.
     *
     * <p>
     *     This is used by {@link FixtureScriptsDefault}.
     * </p>
     *
     * @param specification
     */
    protected void setSpecification(final FixtureScriptsSpecification specification) {
        this.specification = specification;
    }

    @Programmatic
    public String getPackagePrefix() {
        return specification.getPackagePrefix();
    }
    @Programmatic
    public NonPersistedObjectsStrategy getNonPersistedObjectsStrategy() {
        return specification.getNonPersistedObjectsStrategy();
    }

    /**
     * Global setting as to how to handle fixture scripts that are executed more than once.  See
     * {@link MultipleExecutionStrategy} for more details.
     */
    @Programmatic
    public MultipleExecutionStrategy getMultipleExecutionStrategy() {
        return specification.getMultipleExecutionStrategy();
    }

    //endregion

    //region > init
    @Programmatic
    @PostConstruct
    public void init() {
    }

    //endregion

    //region > fixtureScriptList (lazily built)

    private List<FixtureScript> fixtureScriptList;
    @Programmatic
    public List<FixtureScript> getFixtureScriptList() {
        if(fixtureScriptList == null) {
            fixtureScriptList = findAndInstantiateFixtureScripts();
        }
        return fixtureScriptList;
    }

    private List<FixtureScript> findAndInstantiateFixtureScripts() {
        final List<FixtureScript> fixtureScripts = Lists.newArrayList();
        final Set<Class<? extends FixtureScript>> fixtureScriptSubtypes =
                findFixtureScriptSubTypesInPackage();
        for (final Class<? extends FixtureScript> fixtureScriptCls : fixtureScriptSubtypes) {
            final String packageName = fixtureScriptCls.getPackage().getName();
            if(!packageName.startsWith(getPackagePrefix())) {
                // redundant check if ClassDiscoveryService2 in use because already filtered out
                continue;
            } 
            final FixtureScript fs = newFixtureScript(fixtureScriptCls);
            if(fs != null) {
                fixtureScripts.add(fs);
            }
        }
        Collections.sort(fixtureScripts, new Comparator<FixtureScript>() {
            @Override
            public int compare(final FixtureScript o1, final FixtureScript o2) {
                return ObjectContracts.compare(o1, o2, "friendlyName","qualifiedName");
            }
        });
        return fixtureScripts;
    }

    private Set<Class<? extends FixtureScript>> findFixtureScriptSubTypesInPackage() {
        return findSubTypesOfClasses(FixtureScript.class, getPackagePrefix());
    }

    private <T> Set<Class<? extends T>> findSubTypesOfClasses(Class<T> cls, final String packagePrefix) {
        if(classDiscoveryService instanceof ClassDiscoveryService2) {
            final ClassDiscoveryService2 classDiscoveryService2 = (ClassDiscoveryService2) classDiscoveryService;
            return classDiscoveryService2.findSubTypesOfClasses(cls, packagePrefix);
        } else {
            return classDiscoveryService.findSubTypesOfClasses(cls);
        }
    }

    private FixtureScript newFixtureScript(final Class<? extends FixtureScript> fixtureScriptCls) {
        try {
            final Constructor<? extends FixtureScript> constructor = fixtureScriptCls.getConstructor();
            final FixtureScript template = constructor.newInstance();
            if(!template.isDiscoverable()) {
                return null;
            }
            return getContainer().newViewModelInstance(fixtureScriptCls, mementoFor(template));
        } catch(final Exception ex) {
            // ignore if does not have a no-arg constructor or cannot be instantiated
            return null;
        }
    }

    //endregion

    //region > fixtureTracing (thread-local)

    private final ThreadLocal<PrintStream> fixtureTracing = new ThreadLocal<PrintStream>(){{
        set(System.out);
    }};

    @Programmatic
    public PrintStream getFixtureTracing() {
        return fixtureTracing.get();
    }

    @Programmatic
    public void setFixtureTracing(PrintStream fixtureTracing) {
        this.fixtureTracing.set(fixtureTracing);
    }

    //endregion

    //region > runFixtureScript (prototype action)

    /**
     * To make this action usable in the UI, override either {@link #choices0RunFixtureScript()} or 
     * {@link #autoComplete0RunFixtureScript(String)} with <tt>public</tt> visibility</tt>.
     */
    @Action(
        restrictTo = RestrictTo.PROTOTYPING
    )
    @MemberOrder(sequence="10")
    public List<FixtureResult> runFixtureScript(
            final FixtureScript fixtureScript, 
            @ParameterLayout(
                    named="Parameters",
                    describedAs="Script-specific parameters (if any).  The format depends on the script implementation (eg key=value, CSV, JSON, XML etc)",
                    multiLine = 10
            )
            @Parameter(optionality = Optionality.OPTIONAL)
            final String parameters) {

        // if this method is called programmatically, the caller may have simply new'd up the fixture script
        // (rather than use container.newTransientInstance(...).  To allow this use case, we need to ensure that
        // domain services are injected into the fixture script.
        serviceRegistry.injectServicesInto(fixtureScript);

        return fixtureScript.withTracing(fixtureTracing.get()).run(parameters);
    }
    public FixtureScript default0RunFixtureScript() {
        return getFixtureScriptList().isEmpty() ? null: getFixtureScriptList().get(0);
    }
    protected List<FixtureScript> choices0RunFixtureScript() {
        return getFixtureScriptList();
    }
    protected List<FixtureScript> autoComplete0RunFixtureScript(final @MinLength(1) String arg) {
        return Lists.newArrayList(
                Collections2.filter(getFixtureScriptList(), new Predicate<FixtureScript>() {
                    @Override
                    public boolean apply(final FixtureScript input) {
                        return contains(input.getFriendlyName()) || contains(input.getLocalName());
                    }

                    private boolean contains(final String str) {
                        return str != null && str.contains(arg);
                    }
                }));
    }
    public String disableRunFixtureScript() {
        return getFixtureScriptList().isEmpty()? "No fixture scripts found under package '" + getPackagePrefix() + "'": null;
    }
    public String validateRunFixtureScript(final FixtureScript fixtureScript, final String parameters) {
        return fixtureScript.validateRun(parameters);
    }

    //endregion

    //region > programmatic API

    @Programmatic
    public void runFixtureScript(final FixtureScript... fixtureScriptList) {
        if (fixtureScriptList.length == 1) {
            runFixtureScript(fixtureScriptList[0], null);
        } else {
            runFixtureScript(new FixtureScript() {
                protected void execute(ExecutionContext executionContext) {
                    FixtureScript[] fixtureScripts = fixtureScriptList;
                    for (FixtureScript fixtureScript : fixtureScripts) {
                        executionContext.executeChild(this, fixtureScript);
                    }
                }
            }, null);
        }

        transactionService.nextTransaction();
    }

    @Programmatic
    public <T,F extends BuilderScriptAbstract<T,F>> T runBuilderScript(final F fixtureScript) {

        serviceRegistry.injectServicesInto(fixtureScript);

        fixtureScript.run(null);

        final T object = fixtureScript.getObject();
        transactionService.nextTransaction();

        return object;
    }

    @Programmatic
    public FixtureScript findFixtureScriptFor(final Class<? extends FixtureScript> fixtureScriptClass) {
        final List<FixtureScript> fixtureScripts = getFixtureScriptList();
        for (final FixtureScript fs : fixtureScripts) {
            if(fixtureScriptClass.isAssignableFrom(fs.getClass())) {
                return fs;
            }
        }
        return null;
    }

    @Programmatic
    public FixtureScript.ExecutionContext newExecutionContext(final String parameters) {
        final ExecutionParameters executionParameters =
                executionParametersService != null
                        ? executionParametersService.newExecutionParameters(parameters)
                        : new ExecutionParameters(parameters);
        return FixtureScript.ExecutionContext.create(executionParameters, this);
    }

    //endregion

    //region > hooks

    /**
     * Optional hook.
     */
    protected FixtureScript findFixtureScriptFor(final String qualifiedName) {
        final List<FixtureScript> fixtureScripts = getFixtureScriptList();
        for (final FixtureScript fs : fixtureScripts) {
            if(fs.getQualifiedName().contains(qualifiedName)) {
                return fs;
            }
        }
        return null;
    }



    //endregion

    //region > memento support for FixtureScript


    String mementoFor(final FixtureScript fs) {
        return mementoService.create()
                .set("path", fs.getParentPath())
                .asString();
    }
    void initOf(final String mementoStr, final FixtureScript fs) {
        final Memento memento = mementoService.parse(mementoStr);
        fs.setParentPath(memento.get("path", String.class));
    }

    //endregion

    //region > helpers (package level)

    @Programmatic
    FixtureResult newFixtureResult(final FixtureScript script, final String subkey, final Object object, final boolean firstTime) {
        if(object == null) {
            return null;
        }
        if (object instanceof ViewModel || getContainer().isPersistent(object)) {
            // continue
        } else {
            switch(getNonPersistedObjectsStrategy()) {
                case PERSIST:
                    getContainer().flush();
                    break;
                case IGNORE:
                    return null;
            }
        }
        final FixtureResult fixtureResult = new FixtureResult();
        fixtureResult.setFixtureScriptClassName(firstTime ? script.getClass().getName() : null);
        fixtureResult.setFixtureScriptQualifiedName(script.getQualifiedName());
        fixtureResult.setKey(script.pathWith(subkey));
        fixtureResult.setObject(object);
        return fixtureResult;
    }

    @Programmatic
    String titleOf(final FixtureResult fixtureResult) {
        final Object object = fixtureResult.getObject();
        return object != null? getContainer().titleOf(object): "(null)";
    }

    //endregion

    //region > injected services

    @javax.inject.Inject
    MementoService mementoService;

    @javax.inject.Inject
    TransactionService transactionService;

    @javax.inject.Inject
    ClassDiscoveryService classDiscoveryService;

    @javax.inject.Inject
    ExecutionParametersService executionParametersService;

    @javax.inject.Inject
    ServiceRegistry2 serviceRegistry;

    //endregion

}
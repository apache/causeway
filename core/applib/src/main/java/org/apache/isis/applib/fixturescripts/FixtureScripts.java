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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.xml.bind.annotation.XmlRootElement;

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
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsDefault;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsSpecification;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;


/**
 * Rather than sub-classing, instead implement
 * {@link org.apache.isis.applib.services.fixturespec.FixtureScriptsSpecificationProvider}.  
 * The framework will
 * automatically provide a default implementation configured using that provider service.
 */
public abstract class FixtureScripts extends AbstractService {

    // -- Specification, nonPersistedObjectsStrategy, multipleExecutionStrategy enums

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

    // -- constructors

    /**
     * @param specification - specifies how the service will find instances and execute them.
     */
    public FixtureScripts(final FixtureScriptsSpecification specification) {
        this.specification = specification;
    }

    // -- packagePrefix, nonPersistedObjectsStrategy, multipleExecutionStrategy

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

    // -- init

    @Programmatic
    @PostConstruct
    public void init() {
    }

    // -- fixtureScriptList (lazily built)

    private List<FixtureScript> fixtureScriptList;
    @Programmatic
    public List<FixtureScript> getFixtureScriptList() {
        if(fixtureScriptList == null) {
            fixtureScriptList = findAndInstantiateFixtureScripts();
        }
        return fixtureScriptList;
    }

    private List<FixtureScript> findAndInstantiateFixtureScripts() {
        return findFixtureScriptSubTypesInPackage().stream()
                .filter(fixtureScriptCls -> {
                    final String packageName = fixtureScriptCls.getPackage().getName();
                    // redundant check if ClassDiscoveryService2 in use because already filtered out
                    return packageName.startsWith(getPackagePrefix());
                })
                .map(this::newFixtureScript)
                .filter(Objects::nonNull).
                sorted((o1, o2) -> Comparator
                        .comparing(FixtureScript::getFriendlyName)
                        .thenComparing(FixtureScript::getQualifiedName)
                        .compare(o1, o2))
                .collect(Collectors.toList());
    }

    private Set<Class<? extends FixtureScript>> findFixtureScriptSubTypesInPackage() {
        return findSubTypesOfClasses(FixtureScript.class, getPackagePrefix());
    }

    private <T> Set<Class<? extends T>> findSubTypesOfClasses(Class<T> cls, final String packagePrefix) {
        throw _Exceptions.notImplemented(); //TODO[2112] Use IsisBeanTypeRegistry or ServiceRegistry
    }

    private FixtureScript newFixtureScript(final Class<? extends FixtureScript> fixtureScriptCls) {
        try {
            final Constructor<? extends FixtureScript> constructor = fixtureScriptCls.getConstructor();
            final FixtureScript template = constructor.newInstance();
            if(!template.isDiscoverable()) {
                return null;
            }

            final FixtureScript instance = factoryService.instantiate(fixtureScriptCls);
            instance.setParentPath(template.getParentPath());

            return instance;

        } catch(final Exception ex) {
            // ignore if does not have a no-arg constructor or cannot be instantiated
            return null;
        }
    }


    // -- fixtureTracing (thread-local)

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

    // -- runFixtureScript (prototype action)

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
        serviceInjector.injectServicesInto(fixtureScript);

        return fixtureScript.withTracing(fixtureTracing.get()).run(parameters);
    }
    public FixtureScript default0RunFixtureScript() {
        return getFixtureScriptList().isEmpty() ? null: getFixtureScriptList().get(0);
    }
    protected List<FixtureScript> choices0RunFixtureScript() {
        return getFixtureScriptList();
    }
    protected List<FixtureScript> autoComplete0RunFixtureScript(final @MinLength(1) String arg) {

        final Predicate<String> contains = str -> str != null && str.contains(arg);

        return _NullSafe.stream(getFixtureScriptList())
                .filter(script->{
                    return contains.test(script.getFriendlyName()) || contains.test(script.getLocalName());
                })
                .collect(Collectors.toList());
    }
    public String disableRunFixtureScript() {
        return getFixtureScriptList().isEmpty()? "No fixture scripts found under package '" + getPackagePrefix() + "'": null;
    }
    public String validateRunFixtureScript(final FixtureScript fixtureScript, final String parameters) {
        return fixtureScript.validateRun(parameters);
    }

    protected List<FixtureResult> runScript(final FixtureScript fixtureScript, final String parameters) {
        return fixtureScript.run(parameters);
    }

    // -- programmatic API

    @Programmatic
    public void runFixtureScript(final FixtureScript... fixtureScriptList) {
        if (fixtureScriptList.length == 1) {
            runFixtureScript(fixtureScriptList[0], null);
        } else {
            runFixtureScript(new FixtureScript() {
                @Override
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
    public <T> T fixtureScript(final PersonaWithBuilderScript<BuilderScriptAbstract<T>> persona) {
        final BuilderScriptAbstract<T> fixtureScript = persona.builder();
        return runBuilderScript(fixtureScript);
    }

    @Programmatic
    public <T> T runBuilderScript(final BuilderScriptAbstract<T> fixtureScript) {

        serviceInjector.injectServicesInto(fixtureScript);

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

    // -- hooks

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


    // -- memento support for FixtureScript

    @XmlRootElement(name = "fixtureScript")
    public static class FixtureScriptMemento {
        private String path;
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }

    String mementoFor(final FixtureScript fs) {
        final FixtureScriptMemento memento = new FixtureScriptMemento();
        memento.setPath(fs.getParentPath());
        return jaxbService.toXml(memento);
    }

    void initOf(final String xml, final FixtureScript fs) {
        final FixtureScriptMemento memento = jaxbService.fromXml(FixtureScriptMemento.class, xml);
        fs.setParentPath(memento.getPath());
    }

    // -- helpers (package level)

    @Programmatic
    FixtureResult newFixtureResult(final FixtureScript script, final String subkey, final Object object, final boolean firstTime) {
        if(object == null) {
            return null;
        }
        if (object instanceof ViewModel || repositoryService.isPersistent(object)) {
            // continue
        } else {
            switch(getNonPersistedObjectsStrategy()) {
            case PERSIST:
                transactionService.flushTransaction();
                break;
            case IGNORE:
                return null;
            default:
                throw _Exceptions.unmatchedCase(getNonPersistedObjectsStrategy());
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
        return object != null? titleService.titleOf(object): "(null)";
    }

    // -- injected services

    @javax.inject.Inject
    FactoryService factoryService;

    @javax.inject.Inject
    TitleService titleService;

    @javax.inject.Inject
    JaxbService jaxbService;

    @javax.inject.Inject
    BookmarkService bookmarkService;

    @javax.inject.Inject
    ServiceRegistry serviceRegistry;
    
    @javax.inject.Inject
    ServiceInjector serviceInjector;
    
    @javax.inject.Inject
    RepositoryService repositoryService;

    @javax.inject.Inject
    TransactionService transactionService;

    @javax.inject.Inject
    ExecutionParametersService executionParametersService;

}
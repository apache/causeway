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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.fixtures.api.PersonaWithBuilderScript;
import org.apache.isis.extensions.fixtures.events.FixturesInstalledEvent;
import org.apache.isis.extensions.fixtures.events.FixturesInstallingEvent;
import org.apache.isis.extensions.fixtures.fixturespec.FixtureScriptsSpecification;
import org.apache.isis.extensions.fixtures.fixturespec.FixtureScriptsSpecificationProvider;

import lombok.Getter;
import lombok.Setter;
import lombok.val;


@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isisApplib.FixtureScriptsDefault"
)
@DomainServiceLayout(
        named="Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
public class FixtureScripts extends AbstractService {


    // -- Specification, nonPersistedObjectsStrategy, multipleExecutionStrategy enums

    /**
     * How to handle objects that are to be
     * {@link FixtureScripts#newFixtureResult(FixtureScript, String, Object, boolean) added}
     * into a {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureResult} but which are not yet persisted.
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

    // -- constructor, init
    /**
     * The package prefix to search for fixture scripts.  This default value will result in
     * no fixture scripts being found.  However, normally it will be overridden.
     */
    public static final String PACKAGE_PREFIX = FixtureScripts.class.getPackage().getName();


    public FixtureScripts(Optional<FixtureScriptsSpecificationProvider> fixtureScriptsSpecificationProvider) {
        this.specification = fixtureScriptsSpecificationProvider.orElse(() -> FixtureScriptsSpecification.builder(PACKAGE_PREFIX).build()).getSpecification();
    }



    // -- packagePrefix, nonPersistedObjectsStrategy, multipleExecutionStrategy

    @Getter
    private FixtureScriptsSpecification specification;

    private String getPackagePrefix() {
        return specification.getPackagePrefix();
    }
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

//    @Inject List<FixtureScriptsSpecificationProvider> fixtureScriptsSpecificationProvider;
//
//    @PostConstruct
//    public void init() {
//        fixtureScriptsSpecificationProvider.stream().findFirst().ifPresent(
//                provider -> this.specification = provider.getSpecification()
//        );
//    }


    // -- fixtureScriptList (lazily built)

    private List<FixtureScript> fixtureScriptList;
    private List<FixtureScript> getFixtureScriptList() {
        if(fixtureScriptList == null) {
            fixtureScriptList = findAndInstantiateFixtureScripts();
        }
        return fixtureScriptList;
    }
    private List<FixtureScript> findAndInstantiateFixtureScripts() {
        val packagePrefix = getPackagePrefix();
        return serviceRegistry.select(FixtureScript.class).stream()
                .filter(Objects::nonNull)
                .filter(fixtureScript -> fixtureScript.getClass().getPackage().getName().startsWith(packagePrefix))
                .sorted(Comparator
                        .comparing(FixtureScript::getFriendlyName)
                        .thenComparing(FixtureScript::getQualifiedName)
                        )
                .collect(Collectors.toList());
    }




    // -- runFixtureScript (using choices as the drop-down policy)

    @Action(
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa="fa fa-chevron-right"
    )
    @MemberOrder(sequence="10")
    public List<FixtureResult> runFixtureScript(
            final FixtureScript fixtureScript,
            @ParameterLayout(
                    named = "Parameters",
                    describedAs =
                            "Script-specific parameters (if any).  The format depends on the script implementation (eg key=value, CSV, JSON, XML etc)",
                    multiLine = 10)
            @Parameter(optionality = Optionality.OPTIONAL)
            final String parameters) {
        try {
            eventBusService.post(new FixturesInstallingEvent(this));

            // if this method is called programmatically, the caller may have simply new'd up the fixture script
            // (rather than use container.newTransientInstance(...).  To allow this use case, we need to ensure that
            // domain services are injected into the fixture script.
            serviceInjector.injectServicesInto(fixtureScript);

            return fixtureScript.run(parameters);
        } finally {
            eventBusService.post(new FixturesInstalledEvent(this));
        }
    }


    /**
     * Hide the actions of this service if no configuring {@link FixtureScriptsSpecificationProvider} was provided or is available.
     */
    public boolean hideRunFixtureScript() {
        return hideIfPolicyNot(FixtureScriptsSpecification.DropDownPolicy.CHOICES);
    }
    public String disableRunFixtureScript() {
        return getFixtureScriptList().isEmpty()? "No fixture scripts found under package '" + getPackagePrefix() + "'": null;
    }


    public FixtureScript default0RunFixtureScript() {
        Class<? extends FixtureScript> defaultScript = getSpecification().getRunScriptDefaultScriptClass();
        if(defaultScript == null) {
            return null;
        }
        return findFixtureScriptFor(defaultScript);
    }

    public List<FixtureScript> choices0RunFixtureScript() {
        return getFixtureScriptList();
    }

    protected List<FixtureScript> autoComplete0RunFixtureScript(final @MinLength(1) String arg) {

        final Predicate<String> contains = str -> str != null && str.contains(arg);

        return _NullSafe.stream(getFixtureScriptList())
                .filter(script-> contains.test(script.getFriendlyName()) || contains.test(script.getLocalName()))
                .collect(Collectors.toList());
    }
    public String validateRunFixtureScript(final FixtureScript fixtureScript, final String parameters) {
        return fixtureScript.validateRun(parameters);
    }

    protected List<FixtureResult> runScript(final FixtureScript fixtureScript, final String parameters) {
        return fixtureScript.run(parameters);
    }




    // -- runFixtureScript (using autoComplete as drop-down policy)
    @Action(
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            named = "Run Fixture Script"
    )
    @MemberOrder(sequence="10")
    public List<FixtureResult> runFixtureScriptWithAutoComplete(
            final FixtureScript fixtureScript,
            @ParameterLayout(
                    named = "Parameters",
                    describedAs =
                            "Script-specific parameters (if any).  The format depends on the script implementation (eg key=value, CSV, JSON, XML etc)",
                    multiLine = 10)
            @Parameter(optionality = Optionality.OPTIONAL)
            final String parameters) {
        return this.runFixtureScript(fixtureScript, parameters);
    }

    public boolean hideRunFixtureScriptWithAutoComplete() {
        return hideIfPolicyNot(FixtureScriptsSpecification.DropDownPolicy.AUTO_COMPLETE);
    }

    public String disableRunFixtureScriptWithAutoComplete() {
        return disableRunFixtureScript();
    }

    public FixtureScript default0RunFixtureScriptWithAutoComplete() {
        return default0RunFixtureScript();
    }

    public List<FixtureScript> autoComplete0RunFixtureScriptWithAutoComplete(final @MinLength(1) String arg) {
        return autoComplete0RunFixtureScript(arg);
    }

    public String validateRunFixtureScriptWithAutoComplete(final FixtureScript fixtureScript, final String parameters) {
        return validateRunFixtureScript(fixtureScript, parameters);
    }


    // -- recreateObjectsAndReturnFirst

    @Action(
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa="fa fa-refresh"
    )
    @MemberOrder(sequence="20")
    public Object recreateObjectsAndReturnFirst() {
        val recreateScriptClass =  getSpecification().getRecreateScriptClass();
        val recreateScript = findFixtureScriptFor(recreateScriptClass);
        if(recreateScript == null) {
            return null;
        }
        final List<FixtureResult> results = runScript(recreateScript, null);
        if(results.isEmpty()) {
            return null;
        }
        return results.get(0).getObject();
    }

    public boolean hideRecreateObjectsAndReturnFirst() {
        return getSpecification().getRecreateScriptClass() == null;
    }




    // -- HELPER

    private boolean hideIfPolicyNot(final FixtureScriptsSpecification.DropDownPolicy requiredPolicy) {
        return getSpecification().getRunScriptDropDownPolicy() != requiredPolicy;
    }

    // -- programmatic API

    @Programmatic
    public void run(final FixtureScript... fixtureScriptList) {
    	
    	val singleScript = toSingleScript(fixtureScriptList);
    	String parameters = null;
    	
    	transactionService.executeWithinTransaction(()->{
    		runFixtureScript(singleScript, parameters);	
    	});
    }

    @SafeVarargs
    @Programmatic
    public final void runPersonas(PersonaWithBuilderScript<? extends BuilderScriptAbstract<?>> ... personaScripts) {
        for (val personaWithBuilderScript : personaScripts) {
            
            val script = _Casts.<PersonaWithBuilderScript<BuilderScriptAbstract<Object>>>
                uncheckedCast(personaWithBuilderScript);
            
            runPersona(script);
        }
    }

    @Programmatic
    public <T> T runPersona(final PersonaWithBuilderScript<? extends BuilderScriptAbstract<? extends T>> persona) {
        val fixtureScript = persona.builder();
        return runBuilder(fixtureScript);
    }

    /**
     * Runs the builderScript within its own transactional boundary.
     * @param <T>
     * @param builderScript
     * @return
     */
    @Programmatic
    public <T> T runBuilder(final BuilderScriptAbstract<T> builderScript) {
        return transactionService.executeWithinTransaction(()->{
            return runBuilderScriptNonTransactional(builderScript);
        });
    }

    /**
     * Runs the builderScript without its own transactional boundary.<br>
     * The caller is responsible to provide a transactional context/boundary.
     * @param <T>
     * @param builderScript
     * @return
     */
    @Programmatic
    public <T> T runBuilderScriptNonTransactional(final BuilderScriptAbstract<T> builderScript) {

        serviceInjector.injectServicesInto(builderScript);

        builderScript.run(null);
        final T object = builderScript.getObject();
        return object;
    }

    @Programmatic
    public FixtureScript findFixtureScriptFor(final Class<? extends FixtureScript> fixtureScriptClass) {
        val fixtureScripts = getFixtureScriptList();
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



    // -- memento support for FixtureScript

    @XmlRootElement(name = "fixtureScriptMemento")
    public static class FixtureScriptMemento {
        @Getter @Setter
        private String path;
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
        final FixtureResult fixtureResult = serviceInjector.injectServicesInto(
                                                                new FixtureResult());
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

    // -- HELPERS - LOCAL

    private static FixtureScript toSingleScript(FixtureScript[] fixtureScriptList) {

        if (fixtureScriptList.length == 1) {
            return fixtureScriptList[0];
        }

        return new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                for (FixtureScript fixtureScript : fixtureScriptList) {
                    executionContext.executeChild(this, fixtureScript);
                }
            }
        };

    }

    // -- DEPENDENCIES

    @Inject FactoryService factoryService;
    @Inject TitleService titleService;
    @Inject JaxbService jaxbService;
    @Inject BookmarkService bookmarkService;
    @Inject ServiceRegistry serviceRegistry;
    @Inject ServiceInjector serviceInjector;
    @Inject RepositoryService repositoryService;
    @Inject TransactionService transactionService;
    @Inject ExecutionParametersService executionParametersService;

    @Inject EventBusService eventBusService;


    // -- DEPRECATIONS
 
    /**
     * @deprecated renamed to {@link #runPersona(PersonaWithBuilderScript)}
     */
    @Programmatic @Deprecated
    public <T> T fixtureScript(final PersonaWithBuilderScript<BuilderScriptAbstract<T>> persona) {
        return runPersona(persona);
    }
    
    /**
     * @deprecated renamed to {@link #run(FixtureScript...)}
     */
    @Programmatic
    public void runFixtureScript(final FixtureScript... fixtureScriptList) {
        run(fixtureScriptList);
    }

    /**
     * @deprecated renamed to {@link #runBuilder(BuilderScriptAbstract)}
     */
    @Programmatic
    public <T> T runBuilderScript(final BuilderScriptAbstract<T> builderScript) {
        return runBuilder(builderScript);
    }




}
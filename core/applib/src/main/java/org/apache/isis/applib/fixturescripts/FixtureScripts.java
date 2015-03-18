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
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryService;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryService2;
import org.apache.isis.applib.services.memento.MementoService;
import org.apache.isis.applib.services.memento.MementoService.Memento;
import org.apache.isis.applib.util.ObjectContracts;

public abstract class FixtureScripts extends AbstractService {

    //region > nonPersistedObjectsStrategy, multipleExecutionStrategy enums

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
     */
    public enum MultipleExecutionStrategy {
        /**
         * Any given fixture script (or more precisely, any fixture script instance for a particular fixture script
         * class) can only be run once.
         *
         * <p>
         *     This strategy represents the original design of fixture scripts service.  Specifically,it allows an
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
        IGNORE,
        /**
         * Allow fixture scripts to run as requested, even if that another instance of the fixture script's class
         * has already been invoked.
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

        public boolean isIgnore() {
            return this == IGNORE;
        }
        public boolean isExecute() {
            return this == EXECUTE;
        }
    }

    //endregion

    //region > constructors

    /**
     * Defaults to {@link org.apache.isis.applib.fixturescripts.FixtureScripts.NonPersistedObjectsStrategy#PERSIST persist}
     * strategy (if non-persisted objects are {@link #newFixtureResult(FixtureScript, String, Object, boolean) added} to a {@link org.apache.isis.applib.fixturescripts.FixtureResultList}),
     * defaults {@link #getMultipleExecutionStrategy()} to {@link org.apache.isis.applib.fixturescripts.FixtureScripts.MultipleExecutionStrategy#IGNORE ignore}
     * if multiple instances of the same fixture script class are encountered.
     *
     * @param packagePrefix - to search for fixture script implementations, eg "com.mycompany"
     */
    public FixtureScripts(final String packagePrefix) {
        this(packagePrefix, NonPersistedObjectsStrategy.PERSIST, MultipleExecutionStrategy.IGNORE);
    }

    /**
     * Defaults to {@link org.apache.isis.applib.fixturescripts.FixtureScripts.NonPersistedObjectsStrategy#PERSIST persist}
     * strategy (if non-persisted objects are {@link #newFixtureResult(FixtureScript, String, Object, boolean) added} to a {@link org.apache.isis.applib.fixturescripts.FixtureResultList}).
     *
     * @param packagePrefix - to search for fixture script implementations, eg "com.mycompany"
     * @param multipleExecutionStrategy - whether more than one instance of the same fixture script class can be run multiple times
     */
    public FixtureScripts(
            final String packagePrefix,
            final MultipleExecutionStrategy multipleExecutionStrategy) {
        this(packagePrefix, NonPersistedObjectsStrategy.PERSIST, multipleExecutionStrategy);
    }

    /**
     * Defaults {@link #getMultipleExecutionStrategy()} to {@link org.apache.isis.applib.fixturescripts.FixtureScripts.MultipleExecutionStrategy#IGNORE ignore}
     * if multiple instances of the same fixture script class are encountered.
     *
     * @param packagePrefix  - to search for fixture script implementations, eg "com.mycompany"
     * @param nonPersistedObjectsStrategy - how to handle any non-persisted objects that are {@link #newFixtureResult(FixtureScript, String, Object, boolean) added} to a {@link org.apache.isis.applib.fixturescripts.FixtureResultList}.
     */
    public FixtureScripts(
            final String packagePrefix, final NonPersistedObjectsStrategy nonPersistedObjectsStrategy) {
        this(packagePrefix, nonPersistedObjectsStrategy, MultipleExecutionStrategy.IGNORE);
    }

    /**
     * @param packagePrefix  - to search for fixture script implementations, eg "com.mycompany"
     * @param nonPersistedObjectsStrategy - how to handle any non-persisted objects that are {@link #newFixtureResult(FixtureScript, String, Object, boolean) added} to a {@link org.apache.isis.applib.fixturescripts.FixtureResultList}.
     * @param multipleExecutionStrategy - whether more than one instance of the same fixture script class can be run multiple times
     */
    public FixtureScripts(
            final String packagePrefix,
            final NonPersistedObjectsStrategy nonPersistedObjectsStrategy,
            final MultipleExecutionStrategy multipleExecutionStrategy) {
        this.packagePrefix = packagePrefix;
        this.nonPersistedObjectsStrategy = nonPersistedObjectsStrategy;
        this.multipleExecutionStrategy = multipleExecutionStrategy;
    }

    //endregion

    //region > packagePrefix, nonPersistedObjectsStrategy, multipleExecutionStrategy

    private final String packagePrefix;

    @Programmatic
    public String getPackagePrefix() {
        return packagePrefix;
    }

    private final NonPersistedObjectsStrategy nonPersistedObjectsStrategy;
    private final FixtureScripts.MultipleExecutionStrategy multipleExecutionStrategy;

    @Programmatic
    public NonPersistedObjectsStrategy getNonPersistedObjectsStrategy() {
        return nonPersistedObjectsStrategy;
    }

    @Programmatic
    public MultipleExecutionStrategy getMultipleExecutionStrategy() {
        return multipleExecutionStrategy;
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
    private List<FixtureScript> getFixtureScriptList() {
        if(fixtureScriptList == null) {
            fixtureScriptList = findAndInstantiateFixtureScripts();
        }
        return fixtureScriptList;
    }

    private List<FixtureScript> findAndInstantiateFixtureScripts() {
        final List<FixtureScript> fixtureScripts = Lists.newArrayList();
        final Set<Class<? extends FixtureScript>> classes =
                findSubTypesOfClasses();
        for (final Class<? extends FixtureScript> fixtureScriptCls : classes) {
            final String packageName = fixtureScriptCls.getPackage().getName();
            if(!packageName.startsWith(packagePrefix)) {
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
                return ObjectContracts.compare(o1, o2, "friendlyName,qualifiedName");
            }
        });
        return fixtureScripts;
    }

    private Set<Class<? extends FixtureScript>> findSubTypesOfClasses() {
        if(classDiscoveryService instanceof ClassDiscoveryService2) {
            final ClassDiscoveryService2 classDiscoveryService2 = (ClassDiscoveryService2) classDiscoveryService;
            return classDiscoveryService2.findSubTypesOfClasses(FixtureScript.class, packagePrefix);
        } else {
            return classDiscoveryService.findSubTypesOfClasses(FixtureScript.class);
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
        getContainer().injectServicesInto(fixtureScript);

        return fixtureScript.run(parameters);
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
    public String disableRunFixtureScript(final FixtureScript fixtureScript, final String parameters) {
        return getFixtureScriptList().isEmpty()? "No fixture scripts found under package '" + packagePrefix + "'": null;
    }
    public String validateRunFixtureScript(final FixtureScript fixtureScript, final String parameters) {
        return fixtureScript.validateRun(parameters);
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
    /**
     * Optional hook.
     */
    protected FixtureScript findFixtureScriptFor(final Class<? extends FixtureScript> fixtureScriptClass) {
        final List<FixtureScript> fixtureScripts = getFixtureScriptList();
        for (final FixtureScript fs : fixtureScripts) {
            if(fixtureScriptClass.isAssignableFrom(fs.getClass())) {
                return fs;
            }
        }
        return null;
    }

    /**
     * Optional hook.
     */
    protected FixtureScript.ExecutionContext newExecutionContext(final String parameters) {
        final ExecutionParameters executionParameters =
                executionParametersService != null
                        ? executionParametersService.newExecutionParameters(parameters)
                        : new ExecutionParameters(parameters);
        return FixtureScript.ExecutionContext.create(executionParameters, this);
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
            switch(nonPersistedObjectsStrategy) {
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
    private MementoService mementoService;
    
    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private ClassDiscoveryService classDiscoveryService;

    @javax.inject.Inject
    private ExecutionParametersService executionParametersService;

    //endregion

}
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
import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryService;
import org.apache.isis.applib.services.memento.MementoService;
import org.apache.isis.applib.services.memento.MementoService.Memento;
import org.apache.isis.applib.util.ObjectContracts;

public abstract class FixtureScripts extends AbstractService {

    /**
     * How to handle objects that are to be
     * {@link FixtureScripts#newFixtureResult(FixtureScript, String, Object, boolean) added}
     * into a {@link org.apache.isis.applib.fixturescripts.FixtureResult} but which are not yet persisted.
     */
    public enum NonPersistedObjectsStrategy {
        PERSIST,
        IGNORE
    }

    private final String packagePrefix;
    private final NonPersistedObjectsStrategy nonPersistedObjectsStrategy;

    /**
     * Defaults to {@link org.apache.isis.applib.fixturescripts.FixtureScripts.NonPersistedObjectsStrategy#PERSIST persist}
     * strategy if non-persisted objects are {@link #newFixtureResult(FixtureScript, String, Object, boolean) added} to a {@link org.apache.isis.applib.fixturescripts.FixtureResultList}.
     *
     * @param packagePrefix - to search for fixture script implementations, eg "com.mycompany"
     */
    public FixtureScripts(String packagePrefix) {
        this(packagePrefix, NonPersistedObjectsStrategy.PERSIST);
    }

    /**
     * @param packagePrefix  - to search for fixture script implementations, eg "com.mycompany"
     * @param nonPersistedObjectsStrategy - how to handle any non-persisted objects that are {@link #newFixtureResult(FixtureScript, String, Object, boolean) added} to a {@link org.apache.isis.applib.fixturescripts.FixtureResultList}.
     */
    public FixtureScripts(String packagePrefix, NonPersistedObjectsStrategy nonPersistedObjectsStrategy) {
        this.packagePrefix = packagePrefix;
        this.nonPersistedObjectsStrategy = nonPersistedObjectsStrategy;
    }

    @Programmatic
    public NonPersistedObjectsStrategy getNonPersistedObjectsStrategy() {
        return nonPersistedObjectsStrategy;
    }

    // //////////////////////////////////////

    private final List<FixtureScript> fixtureScriptList = Lists.newArrayList();
    
    @PostConstruct
    public void init() {
        findAndInstantiateFixtureScripts(fixtureScriptList);
    }

    private void findAndInstantiateFixtureScripts(List<FixtureScript> fixtureScriptList) {
        final Set<Class<? extends FixtureScript>> classes = classDiscoveryService.findSubTypesOfClasses(FixtureScript.class);
        for (final Class<? extends FixtureScript> fixtureScriptCls : classes) {
            final String packageName = fixtureScriptCls.getPackage().getName();
            if(!packageName.startsWith(packagePrefix)) {
                continue;
            } 
            final FixtureScript fs = newFixtureScript(fixtureScriptCls);
            if(fs != null) {
                fixtureScriptList.add(fs);
            }
        }
        Collections.sort(fixtureScriptList, new Comparator<FixtureScript>() {
            @Override
            public int compare(FixtureScript o1, FixtureScript o2) {
                return ObjectContracts.compare(o1, o2, "friendlyName,qualifiedName");
            }
        }); 
    }

    private FixtureScript newFixtureScript(Class<? extends FixtureScript> fixtureScriptCls) {
        try {
            final Constructor<? extends FixtureScript> constructor = fixtureScriptCls.getConstructor();
            final FixtureScript template = constructor.newInstance();
            if(!template.isDiscoverable()) {
                return null;
            }
            return getContainer().newViewModelInstance(fixtureScriptCls, mementoFor(template));
        } catch(Exception ex) {
            // ignore if does not have a no-arg constructor or cannot be instantiated
            return null;
        }
    }

    /**
     * For subclasses to instantiate .
     * @param parameters
     */
    protected FixtureScript.ExecutionContext newExecutionContext(String parameters) {
        return new FixtureScript.ExecutionContext(parameters, this);
    }

    // //////////////////////////////////////
    
    /**
     * To make this action usable in the UI, override either {@link #choices0RunFixtureScript()} or 
     * {@link #autoComplete0RunFixtureScript(String)} with <tt>public</tt> visibility</tt>.
     */
    @Prototype
    @MemberOrder(sequence="10")
    public List<FixtureResult> runFixtureScript(
            final FixtureScript fixtureScript, 
            @Named("Parameters")
            @DescribedAs("Script-specific parameters (if any).  The format depends on the script implementation (eg key=value, CSV, JSON, XML etc)")
            @MultiLine(numberOfLines=10)
            @Optional
            final String parameters) {

        // if this method is called programmatically, the caller may have simply new'd up the fixture script
        // (rather than use container.newTransientInstance(...).  To allow this use case, we need to ensure that
        // domain services are injected into the fixture script.
        getContainer().injectServicesInto(fixtureScript);

        return fixtureScript.run(parameters);
    }
    public FixtureScript default0RunFixtureScript() {
        return fixtureScriptList.isEmpty() ? null: fixtureScriptList.get(0);
    }
    protected List<FixtureScript> choices0RunFixtureScript() {
        return fixtureScriptList;
    }
    protected List<FixtureScript> autoComplete0RunFixtureScript(final @MinLength(1) String arg) {
        return Lists.newArrayList(
                Collections2.filter(fixtureScriptList, new Predicate<FixtureScript>() {
                    @Override
                    public boolean apply(FixtureScript input) {
                        return contains(input.getFriendlyName()) || contains(input.getLocalName());
                    }
                    private boolean contains(String str) {
                        return str != null && str.contains(arg);
                    }
                }));
    }
    public String disableRunFixtureScript(final FixtureScript fixtureScript, final String parameters) {
        return fixtureScriptList.isEmpty()? "No fixture scripts found under package '" + packagePrefix + "'": null;
    }
    public String validateRunFixtureScript(final FixtureScript fixtureScript, final String parameters) {
        return fixtureScript.validateRun(parameters);
    }

    protected FixtureScript findFixtureScriptFor(String qualifiedName) {
        List<FixtureScript> fixtureScripts = fixtureScriptList;
        for (FixtureScript fs : fixtureScripts) {
            if(fs.getQualifiedName().contains(qualifiedName)) {
                return fs;
            }
        }
        return null;
    }
    protected FixtureScript findFixtureScriptFor(Class<? extends FixtureScript> fixtureScriptClass) {
        List<FixtureScript> fixtureScripts = fixtureScriptList;
        for (FixtureScript fs : fixtureScripts) {
            if(fixtureScriptClass.isAssignableFrom(fs.getClass())) {
                return fs;
            }
        }
        return null;
    }


    // //////////////////////////////////////

    String mementoFor(final FixtureScript fs) {
        return mementoService.create()
                .set("path", fs.getParentPath())
                .asString();
    }
    void initOf(final String mementoStr, final FixtureScript fs) {
        Memento memento = mementoService.parse(mementoStr);
        fs.setParentPath(memento.get("path", String.class));
    }

    // //////////////////////////////////////

    FixtureResult newFixtureResult(FixtureScript script, String subkey, Object object, boolean firstTime) {
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
        fixtureResult.setKey(script.pathWith(subkey));
        fixtureResult.setObject(object);
        return fixtureResult;
    }

    // //////////////////////////////////////
    
    @javax.inject.Inject
    private MementoService mementoService;
    
    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private ClassDiscoveryService classDiscoveryService;

}
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

import com.google.common.collect.Lists;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryService;
import org.apache.isis.applib.services.memento.MementoService;
import org.apache.isis.applib.services.memento.MementoService.Memento;
import org.apache.isis.applib.util.ObjectContracts;

public abstract class FixtureScripts extends AbstractService {

    private final String packagePrefix;
    
    /**
     * @param packagePrefix - to search for fixture script implementations, eg "com.mycompany"
     */
    public FixtureScripts(String packagePrefix) {
        this.packagePrefix = packagePrefix;
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
            return container.newViewModelInstance(fixtureScriptCls, mementoFor(template));
        } catch(Exception ex) {
            // ignore if does not have a no-arg constructor or cannot be instantiated
            return null;
        }
    }
    
    // //////////////////////////////////////
    
    @Prototype
    @MemberOrder(sequence="10")
    public List<FixtureResult> runFixtureScript(
            final FixtureScript fixtureScript, 
            @Named("Parameters")
            @DescribedAs("Script-specific parameters (if any).  The format depends on the script implementation (eg key=value, CSV, JSON, XML etc)")
            @MultiLine(numberOfLines=10)
            @Optional
            final String parameters) {
        return fixtureScript.run(parameters);
    }
    public List<FixtureScript> choices0RunFixtureScript() {
        return fixtureScriptList;
    }
    public String disableRunFixtureScript(final FixtureScript fixtureScript, final String parameters) {
        return fixtureScriptList.isEmpty()? "No fixture scripts found under package '" + packagePrefix + "'": null;
    }
    public String validateRunFixtureScript(final FixtureScript fixtureScript, final String parameters) {
        return fixtureScript.validateRun(parameters);
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

    String mementoFor(FixtureResult fr) {
        return mementoService.create()
                .set("key", fr.getKey())
                .set("object", bookmarkService.bookmarkFor(fr.getObject()))
                .asString();
    }
    void initOf(String mementoStr, FixtureResult fr) {
        Memento memento = mementoService.parse(mementoStr);
        fr.setKey(memento.get("key", String.class));
        fr.setObject(bookmarkService.lookup(memento.get("object", Bookmark.class)));
    }

    FixtureResult newFixtureResult(FixtureScript script, String subkey, Object object) {
        String mementoFor = mementoFor(script, subkey, object);
        return container.newViewModelInstance(FixtureResult.class, mementoFor);
    }

    private String mementoFor(FixtureScript script, String subkey, Object object) {
        final FixtureResult template = new FixtureResult();
        template.setKey(script.pathWith(subkey));
        template.setObject(object);
        return mementoFor(template);
    }

    // //////////////////////////////////////
    
    @javax.inject.Inject
    private MementoService mementoService;
    
    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private DomainObjectContainer container;

    @javax.inject.Inject
    private ClassDiscoveryService classDiscoveryService;

}
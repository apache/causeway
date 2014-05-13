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

import java.util.List;

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
         * {@link FixtureScripts#runFixtureScript(FixtureScript) run fixture script} action
         */
        DISCOVERABLE,
        /**
         * The fixture script is non-discoverable and so will <i>not</i> be listed by the 
         * {@link FixtureScripts#runFixtureScript(FixtureScript) run fixture script} action
         */
        NON_DISCOVERABLE
    }

    // //////////////////////////////////////

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
     * {@link #setLocalName(String) overridden} if {@link CompositeFixtureScript#add(String, FixtureScript) reused} within a {@link CompositeFixtureScript composite fixture script}.
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
     * By default {@link CompositeFixtureScript}s are {@link Discoverability#DISCOVERABLE discoverable}, while
     * {@link SimpleFixtureScript}s are {@link Discoverability#NON_DISCOVERABLE not}.  This can be overridden in the
     * constructor, however.
     */
    @Hidden
    public boolean isDiscoverable() {
        return discoverability == Discoverability.DISCOVERABLE;
    }
    public void setDiscoverability(Discoverability discoverability) {
        this.discoverability = discoverability;
    }
    
    // //////////////////////////////////////

    @Programmatic
    public final List<FixtureResult> run(final String parameters) {
        FixtureResultList fixtureResults = new FixtureResultList(fixtureScripts);
        doRun(parameters, fixtureResults);
        return fixtureResults.getResults();
    }

    @Programmatic
    protected abstract void doRun(final String parameters, final FixtureResultList fixtureResults);

    /**
     * Optional hook to validate parameters.
     */
    @Programmatic
    public String validateRun(final String parameters) {
        return null;
    }

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

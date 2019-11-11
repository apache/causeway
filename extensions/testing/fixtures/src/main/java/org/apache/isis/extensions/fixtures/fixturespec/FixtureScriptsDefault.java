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
package org.apache.isis.extensions.fixtures.fixturespec;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.extensions.fixtures.events.FixturesInstalledEvent;
import org.apache.isis.extensions.fixtures.events.FixturesInstallingEvent;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureResult;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;

import lombok.val;

/**
 * Default instance of {@link FixtureScripts}, instantiated automatically by the framework if no custom user-defined instance was
 * registered.
 *
 * <p>
 *     The original design (pre 1.9.0) was to subclass {@link FixtureScripts} and specify the package prefix (and optionally other
 *     settings) to search for {@link FixtureScript} subtypes.  The inherited functionality from the superclass then knew how to
 *     find and execute fixture scripts.
 * </p>
 * <p>
 *     The new (1.9.0+) design separates these responsibilities.  Rather than subclassing {@link FixtureScripts}, you can instead
 *     rely on the framework to instantiate <i>this</i> default, fallback, implementation of {@link FixtureScripts}.  You can then
 *     optionally (though typically) specify the package prefix and other settings by implementing the new {@link FixtureScriptsSpecificationProvider}
 *     service.
 * </p>
 *
 * <p>
 *     Note that this class is deliberately <i>not</i> annotated with {@link org.apache.isis.applib.annotation.DomainService}; rather it is
 *     automatically registered programmatically if no other instance of {@link FixtureScripts} is found.
 * </p>
 */
@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isisApplib.FixtureScriptsDefault"
        )
@DomainServiceLayout(
        named="Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
        )
public class FixtureScriptsDefault extends FixtureScripts {

    // -- constructor, init
    /**
     * The package prefix to search for fixture scripts.  This default value will result in
     * no fixture scripts being found.  However, normally it will be overridden.
     */
    public static final String PACKAGE_PREFIX = FixtureScriptsDefault.class.getPackage().getName();

    public FixtureScriptsDefault() {
        super(FixtureScriptsSpecification.builder(PACKAGE_PREFIX)
                .build());
    }


    @Override
    @PostConstruct
    public void init() {
        if(fixtureScriptsSpecificationProvider == null) {
            fixtureScriptsSpecificationProvider = 
                    serviceRegistry.select(FixtureScriptsSpecificationProvider.class);
        }
        if(fixtureScriptsSpecificationProvider.isEmpty()) {
            return;
        }
        setSpecification(fixtureScriptsSpecificationProvider
                .getFirst().get().getSpecification());
    }



    // -- runFixtureScript (using choices as the drop-down policy)
    @Action(
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa="fa fa-chevron-right"
            )
    @MemberOrder(sequence="10")
    @Override
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
            return super.runFixtureScript(fixtureScript, parameters);
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

    @Override
    public String disableRunFixtureScript() {
        return super.disableRunFixtureScript();
    }

    @Override
    public FixtureScript default0RunFixtureScript() {
        Class<? extends FixtureScript> defaultScript = getSpecification().getRunScriptDefaultScriptClass();
        if(defaultScript == null) {
            return null;
        }
        return findFixtureScriptFor(defaultScript);
    }

    /**
     * Promote to public visibility.
     */
    @Override
    public List<FixtureScript> choices0RunFixtureScript() {
        return super.choices0RunFixtureScript();
    }

    @Override
    public String validateRunFixtureScript(final FixtureScript fixtureScript, final String parameters) {
        return super.validateRunFixtureScript(fixtureScript, parameters);
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
        return super.validateRunFixtureScript(fixtureScript, parameters);
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
        return fixtureScriptsSpecificationProvider.isEmpty() || getSpecification().getRunScriptDropDownPolicy() != requiredPolicy;
    }

    private Can<FixtureScriptsSpecificationProvider> fixtureScriptsSpecificationProvider;

    // -- DEPS

    @Inject ServiceRegistry serviceRegistry;
    @Inject EventBusService eventBusService;


}
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.html.domain;

import java.util.List;

import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.SemanticsOf;

@Named(SampleMenu.LOGICAL_TYPE_NAME)
@DomainService
@DomainServiceLayout(named = "Web Components", menuBar = DomainServiceLayout.MenuBar.PRIMARY)
public class SampleMenu {

    public static final String LOGICAL_TYPE_NAME = "causeway.webcomponents.sample.SampleMenu";
    public static final String DISABLED_REASON = "Available to administrators only.";
    public static final String GREETING_VALIDATION_REASON = "A name is required.";

    private final SampleObjectRepository repository;

    public SampleMenu(final SampleObjectRepository repository) {
        this.repository = repository;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(named = "Welcome Message", describedAs = "Returns a deterministic scalar result.")
    public String welcomeMessage() {
        return "Welcome to Causeway web components.";
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(named = "Personal Greeting", describedAs = "Demonstrates a parameterized service action.")
    public String greet(@Parameter(maxLength = 40) final String name) {
        return "Hello, " + name + "!";
    }

    public String default0Greet() {
        return "Ada";
    }

    public List<String> choices0Greet() {
        return List.of("Ada", "Grace", "Linus");
    }

    public String validate0Greet(final String name) {
        return name == null || name.isBlank() ? GREETING_VALIDATION_REASON : null;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(named = "Find Sample", describedAs = "Returns the deterministic sample object.")
    public SampleObject findSample() {
        return repository.findById(SampleObject.SAMPLE_ID);
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(named = "Related Objects", describedAs = "Returns a deterministic object collection.")
    public List<SampleRelatedObject> findRelatedObjects() {
        return repository.findById(SampleObject.SAMPLE_ID).getRelatedObjects();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(named = "Status Message", describedAs = "Returns an enum-derived scalar result.")
    public String statusMessage(final SampleStatus status) {
        return "Selected status: " + status.name();
    }

    public SampleStatus default0StatusMessage() {
        return SampleStatus.ACTIVE;
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    @ActionLayout(named = "Clear Sample Notes", describedAs = "Demonstrates a void mutating service action.")
    public void clearSampleNotes() {
        repository.findById(SampleObject.SAMPLE_ID).setNotes(null);
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(named = "Disabled Action")
    public String disabledAction() {
        return "Unavailable";
    }

    public String disableDisabledAction() {
        return DISABLED_REASON;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(named = "Hidden Action")
    public String hiddenAction() {
        return "Hidden";
    }

    public boolean hideHiddenAction() {
        return true;
    }
}

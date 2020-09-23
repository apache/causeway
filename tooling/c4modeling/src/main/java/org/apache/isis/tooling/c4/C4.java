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
package org.apache.isis.tooling.c4;

import java.util.Optional;

import javax.annotation.Nullable;

import com.structurizr.Workspace;
import com.structurizr.io.plantuml.AbstractPlantUMLWriter;
import com.structurizr.io.plantuml.StructurizrPlantUMLWriter;
import com.structurizr.model.Element;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.model.Tags;
import com.structurizr.view.Shape;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.View;
import com.structurizr.view.ViewSet;

import org.apache.isis.commons.internal.base._Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class C4 {

    @Getter private final Workspace workspace;
    private final AbstractPlantUMLWriter plantUMLWriter;

    /**
     * Creates a new workspace.
     *
     * @param name          the name of the workspace
     * @param description   a short description
     */
    public static C4 of(@NonNull String name, @Nullable String description) {
        val plantUMLWriter = new StructurizrPlantUMLWriter();
        val c4 = new C4(new Workspace(name, _Strings.nullToEmpty(description)), plantUMLWriter);
        c4.applyDefaultStyles();
        return c4;
    }
    
    public String getWorkspaceName() {
        return workspace.getName();
    }
    
    /**
     * @return the software architecture model
     */
    public Model getModel() {
        return workspace.getModel();
    }

    /**
     * @return set of views onto the software architecture model
     */
    public ViewSet getViewSet() {
        return workspace.getViews();
    }

    /**
     * @return a single {@code view} as a PlantUML diagram definition
     */
    public String toPlantUML(View view) {
        return plantUMLWriter.toString(view);
    }
    
    // -- SIMPLE FACTORIES
    
    public Person person(@NonNull String name, @Nullable String description) {
        return getModel().addPerson(name, _Strings.nullToEmpty(description));
    }
    
    public SoftwareSystem softwareSystem(@NonNull String name, @Nullable String description) {
        return getModel().addSoftwareSystem(name, _Strings.nullToEmpty(description));
    }
    
    public SystemContextView systemContextView(@NonNull SoftwareSystem softwareSystem, @NonNull String key, @Nullable String description) {
        return getViewSet().createSystemContextView(softwareSystem, key, _Strings.nullToEmpty(description));
    }

    // -- EXPERIMENTAL

    public static void setTypeOverride(Element element, String typeOverride) {
        element.addProperty("typeOverride", typeOverride);
    }

    public static Optional<String> getTypeOverride(Element element) {
        return Optional.ofNullable(element.getProperties().get("typeOverride"));
    }

    // -- HELPER

    private void applyDefaultStyles() {
        val styles = getViewSet().getConfiguration().getStyles();

        styles.addElementStyle(Tags.ELEMENT).color("#fffffe");
        //styles.addElementStyle(Tags.PERSON).background("#08427b");
        styles.addElementStyle(Tags.CONTAINER).background("#438dd5");


        //        styles.addElementStyle(Tags.SOFTWARE_SYSTEM)
        //        .color("#ffffff")    
        //        .background("#1168bd");
        styles.addElementStyle(Tags.PERSON).background("#08427b").color("#ffffff").shape(Shape.Person);
    }





}

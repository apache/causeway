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
package org.apache.isis.tooling.c4.test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import com.structurizr.Workspace;
import com.structurizr.io.plantuml.PlantUMLWriter;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemContextView;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.tooling.c4.C4;

import lombok.val;

class C4Test {


    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    /**
     * see https://www.baeldung.com/structurizr
     */
    @Test
    void testStructurizr_native() throws IOException {
        
        // First, we need to create a Workspace and a Model:
        
        val workspace = new Workspace("Payment Gateway", "Payment Gateway");
        val model = workspace.getModel();
        
        // We also define a user and two software systems within that model:
        
        Person user = model.addPerson("Merchant", "Merchant");
        SoftwareSystem paymentTerminal = model
                .addSoftwareSystem("Payment Terminal", "Payment Terminal");
        user.uses(paymentTerminal, "Makes payment");
        SoftwareSystem fraudDetector = model
                .addSoftwareSystem("Fraud Detector", "Fraud Detector");
        paymentTerminal.uses(fraudDetector, "Obtains fraud score");
        
        // Now that our system is defined, we can create a view
        // Here we created a view that includes all software systems and persons. 
        
        val viewSet = workspace.getViews();
        
        SystemContextView contextView = viewSet
                .createSystemContextView(paymentTerminal, "context", "Payment Gateway Diagram");
        contextView.addAllSoftwareSystems();
        contextView.addAllPeople();
        
        // Now the view needs to be rendered.
        
        val stringWriter = new StringWriter();
        val plantUMLWriter = new PlantUMLWriter();
        plantUMLWriter.write(workspace, stringWriter);
        
        _Text.assertTextEquals(
                _Text.readLinesFromResource(this.getClass(), "baeldung-example-v1.puml", StandardCharsets.UTF_8), 
                stringWriter.toString());
    }
    
    /**
     * see https://www.baeldung.com/structurizr
     */
    @Test
    void testStructurizr_usingFactory() throws IOException {
        
        val c4 = C4.of("Payment Gateway", "Payment Gateway");
        
        // We also define a user and two software systems within that model:
        
        Person user = c4.person("Merchant", "Merchant");
        SoftwareSystem paymentTerminal = c4.softwareSystem("Payment Terminal", "Payment Terminal");
        SoftwareSystem fraudDetector = c4.softwareSystem("Fraud Detector", "Fraud Detector");
        
        user.uses(paymentTerminal, "Makes payment");        
        paymentTerminal.uses(fraudDetector, "Obtains fraud score");
        
        // Now that our system is defined, we can create a view
        // Here we created a view that includes all software systems and persons. 
        
        SystemContextView contextView = c4.systemContextView(paymentTerminal, "context", "Payment Gateway Diagram");
        contextView.addAllSoftwareSystems();
        contextView.addAllPeople();
        
        // Now the view needs to be rendered.
        
        val plantUmlSource = c4.toPlantUML(contextView);
        
        // System.out.println(plantUmlSource); //debug
        
        _Text.assertTextEquals(
                _Text.readLinesFromResource(this.getClass(), "baeldung-example-v2.puml", StandardCharsets.UTF_8), 
                plantUmlSource);
        
    }
    

}

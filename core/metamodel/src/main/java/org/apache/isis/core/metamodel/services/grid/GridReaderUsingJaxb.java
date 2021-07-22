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
package org.apache.isis.core.metamodel.services.grid;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.applib.services.grid.GridSystemService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.collections._Arrays;

import lombok.RequiredArgsConstructor;

/**
 *
 * @since 2.0
 *
 */
@Service
@Named("isis.metamodel.GridReaderUsingJaxb")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GridReaderUsingJaxb {

    private final JaxbService jaxbService;
    private final ServiceRegistry serviceRegistry;

    private JAXBContext jaxbContext;

    @PostConstruct
    public void init(){

        final Class<?>[] pageImplementations =
            serviceRegistry.select(GridSystemService.class)
            .stream()
            .map(GridSystemService::gridImplementation)
            .collect(_Arrays.toArray(Class.class));

        try {
            jaxbContext = JAXBContext.newInstance(pageImplementations);
        } catch (JAXBException e) {
            // leave as null
        }
    }

    public Grid loadGrid(final String xml) {
        initForTesting(); // a no-op if already initialized via @PostConstruct
        return (Grid) jaxbService.fromXml(jaxbContext, xml);
    }

    // -- HELPER

    // JUnit support
    private void initForTesting() {
        if(jaxbContext==null) {
            try {
                jaxbContext = JAXBContext.newInstance(BS3Grid.class);
            } catch (JAXBException e) {
                // leave as null
            }
        }
    }

}

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
package org.apache.isis.extensions.docgen.help.topics.domainobjects;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.metamodel.objgraph.ObjectGraph;
import org.apache.isis.extensions.docgen.help.IsisModuleExtDocgenHelp;

import lombok.val;

@Component
@Named(IsisModuleExtDocgenHelp.NAMESPACE + ".DomainEntityDiagramPage")
public class DomainEntityDiagramPage extends EntityDiagramPageAbstract {

    @Inject
    public DomainEntityDiagramPage(final MetaModelService metaModelService) {
        super(metaModelService);
    }

    @Override
    public String getTitle() {
        return "Domain Entity Diagram";
    }

    @Override
    protected String diagramTitle() {
        return "Entity Relations";
    }

    @Override
    protected boolean accept(final BeanSort beanSort, final LogicalType logicalType) {
        if(!beanSort.isEntity()) return false;
        val ns = "" + logicalType.getNamespace();
        return !ns.equals("isis")
                && !ns.startsWith("isis.");
    }

    @Override
    protected String renderObjectGraph(final ObjectGraph objectGraph) {
        return super.renderObjectGraphUsingPlantuml(objectGraph);
    }

}


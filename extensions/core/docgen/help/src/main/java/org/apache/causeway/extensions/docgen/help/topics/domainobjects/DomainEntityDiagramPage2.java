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
package org.apache.causeway.extensions.docgen.help.topics.domainobjects;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.metamodel.objgraph.ObjectGraph;
import org.apache.causeway.extensions.docgen.help.CausewayModuleExtDocgenHelp;

@Component
@Named(CausewayModuleExtDocgenHelp.NAMESPACE + ".DomainEntityDiagramPage2")
public class DomainEntityDiagramPage2 extends EntityDiagramPageAbstract {

    @Inject
    public DomainEntityDiagramPage2(final MetaModelService metaModelService) {
        super(metaModelService);
    }

    @Override
    public String getTitle() {
        return "Domain Entity Diagram (interactive)";
    }

    @Override
    protected String diagramTitle() {
        return "Entity Relations";
    }

    @Override
    protected boolean accept(final BeanSort beanSort, final LogicalType logicalType) {
        if(!beanSort.isEntity()) return false;
        var ns = "" + logicalType.getNamespace();
        return !ns.equals("causeway")
                && !ns.startsWith("causeway.");
    }

    @Override
    protected String renderObjectGraph(final ObjectGraph objectGraph) {
        return super.renderObjectGraphUsingD3js(objectGraph);
    }

}


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

import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.docgen.help.CausewayModuleExtDocgenHelp;

import lombok.val;

@Component
@Named(CausewayModuleExtDocgenHelp.NAMESPACE + ".DomainEntityDiagramPage")
public class DomainEntityDiagramPage extends EntityDiagramPageAbstract {

    @Inject
    public DomainEntityDiagramPage(final SpecificationLoader specLoader, final CausewayBeanTypeRegistry beanTypeRegistry) {
        super(specLoader, beanTypeRegistry);
    }

    @Override
    public String getTitle() {
        return "Domain Entity Diagram";
    }

    protected boolean accept(final ObjectSpecification objSpec) {
        val ns = "" + objSpec.getLogicalType().getNamespace();
        return !ns.equals("causeway")
                && !ns.startsWith("causeway.");
    }

}


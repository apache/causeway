/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */

package org.apache.isis.progmodels.dflt;

import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.object.objectspecid.classname.ObjectSpecIdFacetDerivedFromClassNameFactory;
import org.apache.isis.core.metamodel.progmodel.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;

public final class ProgrammingModelForObjectSpecIdFacet extends ProgrammingModelAbstract {

    public ProgrammingModelForObjectSpecIdFacet(final IsisConfiguration configuration) {
        this(DeprecatedPolicy.parse(configuration));
    }

    public ProgrammingModelForObjectSpecIdFacet(final DeprecatedPolicy deprecatedPolicy) {
        super(deprecatedPolicy);

        addFactory(new ObjectSpecIdFacetDerivedFromClassNameFactory());
    }

    @Override
    public List<ObjectSpecificationPostProcessor> getPostProcessors() {
        return Collections.emptyList();
    }

}

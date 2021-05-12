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
package org.apache.isis.core.metamodel.inspect;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.metamodel.inspect.model.ActionNode;
import org.apache.isis.core.metamodel.inspect.model.CollectionNode;
import org.apache.isis.core.metamodel.inspect.model.FacetAttrNode;
import org.apache.isis.core.metamodel.inspect.model.FacetGroupNode;
import org.apache.isis.core.metamodel.inspect.model.FacetNode;
import org.apache.isis.core.metamodel.inspect.model.ParameterNode;
import org.apache.isis.core.metamodel.inspect.model.PropertyNode;
import org.apache.isis.core.metamodel.inspect.model.TypeNode;

@Configuration
@Import({
        Object_inspectMetamodel.class,
        ActionNode.class,
        CollectionNode.class,
        FacetAttrNode.class,
        FacetGroupNode.class,
        FacetNode.class,
        ParameterNode.class,
        PropertyNode.class,
        TypeNode.class,

})
public class IsisModuleCoreMetamodelInspection {

}

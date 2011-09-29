/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkReprBuilder;
import org.apache.isis.viewer.json.viewer.representations.ReprBuilderAbstract;

public class DomainTypeReprBuilder extends ReprBuilderAbstract<DomainTypeReprBuilder> {

    public static DomainTypeReprBuilder newBuilder(ResourceContext representationContext) {
        return new DomainTypeReprBuilder(representationContext);
    }

    public static LinkReprBuilder newLinkToBuilder(ResourceContext resourceContext, String rel, ObjectSpecification objectSpec) {
        String typeFullName = objectSpec.getFullIdentifier();
        String url = "domainTypes/" + typeFullName;
        return LinkReprBuilder.newBuilder(resourceContext, rel, url);
    }

    public DomainTypeReprBuilder(ResourceContext resourceContext) {
        super(resourceContext);
    }


    public JsonRepresentation build() {
        return representation;
    }


}
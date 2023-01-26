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
package org.apache.causeway.extensions.docgen.topics.domainobjects;

import java.util.List;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _DiagramUtils {

    String plantumlBlock(final String diagramSource) {
        return "[plantuml]\n"
                + "--\n"
                + diagramSource
                + "--\n";
    }

    String object(final String name, final List<String> fields) {
        val sb = new StringBuilder();

        sb.append("object " + name).append('\n');

        fields.forEach(field->{
            sb.append(name + " : " + field).append('\n');
        });

        return sb.toString();
    }

    String object(final ObjectSpecification objSpec) {

        val props = objSpec.streamProperties(MixedIn.EXCLUDED)
                .collect(Can.toCan());
        val fields = props.map(prop->prop.getId())
                .toList();

        return object(objSpec.getLogicalType().getLogicalTypeSimpleName(), fields);
    }


}

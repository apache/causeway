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
package org.apache.causeway.core.metamodel.inspect.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.applib.value.Markup;

sealed interface MMNode 
permits
    MemberNode,
    FacetGroupNode,
    FacetNode,
    ParameterNode,
    TypeNode {

    Stream<MMNode> streamChildNodes();

    String title();
    String iconName();
    
    void putDetails(Details details);
    
    /**
     * The detail part of the master/detail view.
     */
    default Markup details() {
        var details = new Details();
        putDetails(details);
        return details.toMarkup();
    }
    
    record Details(Map<String, String> map) {
        Details() {
            this(new LinkedHashMap<>());
        }
        
        Details put(String key, String value) {
            map.put(key, value);
            return this;
        }
        
        Markup toMarkup() {
            var tableTemplate = 
            """
            <table class="table table-striped">
              <tbody>
                %s
              </tbody>
            </table>
            """;
            
            var rowTemplate = 
            """
                <tr>
                  <th scope="row">%s</th>
                  <td>%s</td>
                </tr>
            """;
            
            var rows = map.entrySet().stream()
                .map(entry->rowTemplate.formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
            
            return Markup.valueOf(tableTemplate.formatted(rows));
        }
    }
    
}

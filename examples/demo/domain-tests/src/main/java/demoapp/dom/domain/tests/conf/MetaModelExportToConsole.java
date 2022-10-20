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
package demoapp.dom.domain.tests.conf;

import org.apache.causeway.applib.services.metamodel.DomainModel;

import lombok.val;

public class MetaModelExportToConsole {

    //XXX this is just a blue-print example - (nomnoml syntax)
    //idea is to eventually create a SPI to allow for pluging into
    //the MM export menu to provide custom export formats
    public void export(final DomainModel domainModel) {

        domainModel.forEachLogicalType((logicalType, members)->{

            //[rum|tastiness: Int|swig()]
            val sb = new StringBuilder();

            sb
            .append("[")
            .append(logicalType);

            members.forEach(member->{

                sb
                .append("|")
                .append(member.getMemberName())
                .append(": ")
                .append(member.getType());

            });

            sb.append("]");

            System.err.printf("%s%n", sb.toString());
        });

    }

}

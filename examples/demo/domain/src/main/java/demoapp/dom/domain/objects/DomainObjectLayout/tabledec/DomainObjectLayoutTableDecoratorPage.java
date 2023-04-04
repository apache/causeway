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
package demoapp.dom.domain.objects.DomainObjectLayout.tabledec;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.*;

import org.apache.causeway.applib.annotation.*;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.DomainObjectLayoutTableDecoratorVm")
@DomainObject(
        nature=Nature.VIEW_MODEL)
@DomainObjectLayout(
        tableDecorator = TableDecorator.DatatablesNet.class)
public class DomainObjectLayoutTableDecoratorPage implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "DomainObjectLayout#tableDecorator";
    }

    @Action
    public List<DomainObjectLayoutTableDecoratorPage> standaloneTable() {
        return getSamples();
    }

    @Collection
    //@CollectionLayout(tableDecorator = TableDecorator.DatatablesNet.class) -> //TODO[CAUSEWAY-3311] see collection layout demo instead
    private List<DomainObjectLayoutTableDecoratorPage> samples;
    public List<DomainObjectLayoutTableDecoratorPage> getSamples() {
        if(samples==null) {
            samples = List.of(
                    new DomainObjectLayoutTableDecoratorPage(),
                    new DomainObjectLayoutTableDecoratorPage(),
                    new DomainObjectLayoutTableDecoratorPage(),
                    new DomainObjectLayoutTableDecoratorPage());
        }
        return samples;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @XmlElement(required = false)
    @Getter @Setter
    private String uuid = UUID.randomUUID().toString();

}
//end::class[]

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

import java.util.List;
import java.util.UUID;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.TableDecorator;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.DomainObjectLayoutTableDecoratorVm")
@DomainObject(
        nature=Nature.VIEW_MODEL)
@DomainObjectLayout(
        tableDecorator = TableDecorator.DatatablesNet.class)
public class DomainObjectLayoutTableDecoratorVm implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "DomainObjectLayout#tableDecorator";
    }

    @Action
    public List<DomainObjectLayoutTableDecoratorVm> standaloneTable() {
        return getSamples();
    }

    @Collection
    @CollectionLayout(tableDecorator = TableDecorator.DatatablesNet.class)
    private List<DomainObjectLayoutTableDecoratorVm> samples;
    public List<DomainObjectLayoutTableDecoratorVm> getSamples() {
        if(samples==null) {
            samples = List.of(
                    new DomainObjectLayoutTableDecoratorVm(),
                    new DomainObjectLayoutTableDecoratorVm(),
                    new DomainObjectLayoutTableDecoratorVm(),
                    new DomainObjectLayoutTableDecoratorVm());
        }
        return samples;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @XmlElement(required = false)
    @Getter @Setter
    private String uuid = UUID.randomUUID().toString();

}
//end::class[]

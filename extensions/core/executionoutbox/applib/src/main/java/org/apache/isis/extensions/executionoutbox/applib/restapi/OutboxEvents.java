/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.extensions.executionoutbox.applib.restapi;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.extensions.executionoutbox.applib.IsisModuleExtExecutionOutboxApplib;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Wrapper around a collection of {@link ExecutionOutboxEntry outbox entries}.
 *
 * <p>
 *     This class is used as the return value of {@link OutboxRestApi#pending()}.  The <i>outbox client</i> calls this
 *     with an HTTP <code>Accept</code> header set to {@link org.apache.isis.schema.ixn.v2.InteractionsDto} so that
 *     it is serialized into a list of {@link org.apache.isis.applib.services.iactn.Interaction}s for processing.
  * </p>
 *
 * @since 2.0 {@index}
 *
 * @see OutboxRestApi#pending()
 */
@XmlRootElement
@XmlType(
        propOrder = {
                "executions"
        }
)
@Named(OutboxEvents.LOGICAL_TYPE_NAME)
@DomainObject(nature = Nature.VIEW_MODEL)
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class OutboxEvents  {

    static final String LOGICAL_TYPE_NAME = IsisModuleExtExecutionOutboxApplib.NAMESPACE + ".OutboxEvents";

    @ObjectSupport public String title() {
        return String.format("%d executions", executions.size());
    }

    @Collection
    @CollectionLayout(defaultView = "table")
    @XmlElementWrapper()
    @XmlElement(name="event")
    @Getter @Setter
    private List<ExecutionOutboxEntry> executions = new ArrayList<>();

}

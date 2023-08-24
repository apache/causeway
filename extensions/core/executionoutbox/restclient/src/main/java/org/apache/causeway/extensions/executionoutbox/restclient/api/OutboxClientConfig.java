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
package org.apache.causeway.extensions.executionoutbox.restclient.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@XmlRootElement(name="outbox-client-config")
@XmlAccessorType(XmlAccessType.FIELD)
@Data @Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxClientConfig {

    @XmlElement(name="pendingUri")
    private String pendingUri = "services/causeway.ext.executionOutbox.OutboxRestApi/actions/pending/invoke";

    @XmlElement(name="deleteUri")
    private String deleteUri = "services/causeway.ext.executionOutbox.OutboxRestApi/actions/delete/invoke";

    @XmlElement(name="deleteManyUri")
    private String deleteManyUri = "services/causeway.ext.executionOutbox.OutboxRestApi/actions/deleteMany/invoke";

}

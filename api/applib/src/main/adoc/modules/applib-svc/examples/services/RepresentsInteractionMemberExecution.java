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
package org.apache.isis.applib.services;

import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.schema.ixn.v2.InteractionDto;

/**
 * Extends {@link HasUniqueId}, where the {@link HasUniqueId#getUniqueId()} is interpreted as an
 * interaction (cf {@link InteractionDto}) that has at least one member execution (cf
 * {@link org.apache.isis.schema.ixn.v1.MemberExecutionDto}) and may (by way of {@link WrapperFactory}) contain
 * several.
 *
 * <p>
 *     Examples could include SPI services that persist published events and status messages.
 * </p>
 */
public interface RepresentsInteractionMemberExecution extends HasUniqueId {

    int getSequence();
}

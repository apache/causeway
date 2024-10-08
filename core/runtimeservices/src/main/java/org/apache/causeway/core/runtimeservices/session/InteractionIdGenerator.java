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

package org.apache.causeway.core.runtimeservices.session;

import java.util.UUID;

import org.apache.causeway.applib.services.iactn.Interaction;

/**
 * Every {@link Interaction} has an {@link Interaction#getInteractionId() interactionId}; this service is delegaterd
 * to in order to obtain a unique Id for each interaction.
 *
 * @since 2.0 {@index}
 */
public interface InteractionIdGenerator {

    UUID interactionId();

}

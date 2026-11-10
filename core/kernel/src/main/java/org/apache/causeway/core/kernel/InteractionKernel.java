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
package org.apache.causeway.core.kernel;

import java.util.List;
import java.util.UUID;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.core.metamodel.object.ManagedObject;

public interface InteractionKernel {

    // -- CTX

    record InteractionContext(
            UUID correlationId,
            UserMemento principal,
            InteractionConstraint constraint) {
    }

    // -- IN

    enum RepresentationProfile {
        RAW
    }
    interface KernelCommandDto {}

    record KernelQuery(
            Bookmark interactionTarget,
            RepresentationProfile profile) {
    }

    record KernelCommand(
            Bookmark interactionTarget,
            List<Bookmark> arguments,
            RepresentationProfile profile) {
    }

    // -- OUT

    interface RepresentationModel {}
    interface KernelMessage {}

    enum InteractionStatus {
        SUCCESS,
        DENIED,
        INVALID,
        FAILED}

    record ManagedObjectWrapper(
            ManagedObject mo) implements RepresentationModel {
    }

    record KernelResult(
            UUID correlationId,
            Bookmark interactionTarget,
            InteractionStatus status,
            RepresentationModel representation,
            List<KernelMessage> messages) {
    }

    // -- HANDLE

    KernelResult query(
            KernelQuery query,
            InteractionContext context);

    KernelResult execute(
            KernelCommand command,
            InteractionContext context);

}

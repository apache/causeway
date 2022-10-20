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
package org.apache.causeway.applib.mixins.system;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.services.iactn.Interaction;

/**
 * Allows domain objects that represent or are associated with a system
 * {@link Interaction} to act as a mixee in order that other modules can
 * contribute behaviour.
 *
 * @since 2.0 {@index}
 */
public interface HasInteractionId {

    @Property(
            editing = Editing.DISABLED,
            maxLength = InteractionId.MAX_LENGTH
    )
    @Parameter(
            maxLength = InteractionId.MAX_LENGTH
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InteractionId {
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
        int MAX_LENGTH = 36;
    }


    /**
     * A unique identifier (a GUID).
     */
    @InteractionId
    UUID getInteractionId();

}

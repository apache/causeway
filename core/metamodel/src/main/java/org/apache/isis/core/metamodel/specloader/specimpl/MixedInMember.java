/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.specloader.specimpl;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

/**
 * Interface indicating an a contributed association or action.
 */
public interface MixedInMember extends ObjectMember {

    /**
     * If the mixin uses a method of precisely this name, then the corresponding {@link ObjectMember#getName() name} and
     * {@link ObjectMember#getId() id} will instead be inferred from the mixin class' {@link ObjectSpecification#getShortIdentifier() name}.
     *
     * <p>
     *     This makes it easier to avoid silly spelling mistakes in supporting methods, with the name of the member
     *     in essence specified in only just one place, namely the mixin class' name.
     * </p>
     *
     * <p>
     *     Remarks: originally intended to use a single (or perhaps two) underscore, however these may not be valid
     *     identifiers after Java 8.
     * </p>
     */
    public static final String DEFAULT_MEMBER_NAME = "$$";

    /**
     * The id if it was originally {@link #DEFAULT_MEMBER_NAME the default member name}.
     */
    String getOriginalId();

    // not actually required, as of yet
    // boolean isMixinOf(ObjectAction mixinAction);

    // not actually required, as of yet
    // ObjectSpecification getMixin();

}


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
package org.apache.isis.applib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import lombok.experimental.UtilityClass;

/**
 * Annotation container class.
 * @see Domain.Include
 * @see Domain.Exclude
 */
@UtilityClass
public class Domain {

    /**
     * Indicates that a field or method must contribute to the metamodel.
     * <p>
     * For <i>mixins</i> is also allowed to be placed on the mixin's main method.
     * <p>
     * By placing the {@link Domain.Include} annotation on a method or field,
     * a contract with the meta-model is enforced,
     * such that this class-member must be recognized by the meta-model
     * and cannot be ignored.
     * Meta-model validation will fail when this contract is violated.
     * <p>
     * Acts as the semantic counterpart to {@link Domain.Exclude}.
     * @see Domain.Exclude
     * @since 2.x {@index}
     */
    @Inherited
    @Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.ANNOTATION_TYPE
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Include {
    }

    /**
     * Indicates that a field, method or type should be ignored by the meta-model
     * introspection.
     * <p>
     * Acts as the semantic counterpart to {@link Domain.Include}.
     * @see Domain.Include
     *
     * @since 2.x {@index}
     */
    @Inherited
    @Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.TYPE,
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Exclude {
    }

}

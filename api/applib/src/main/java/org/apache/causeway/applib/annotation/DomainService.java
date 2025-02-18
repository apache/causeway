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
package org.apache.causeway.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.bookmark.Bookmark;

/**
 * Indicates that the class should be automatically recognized as a domain service.
 *
 * <p>
 * Also indicates whether the domain service acts as a repository for an entity, and menu ordering UI hints.
 * </p>
 *
 * @implNote Meta annotation {@link Service} allows for the Spring framework to pick up (discover) the
 * annotated type.
 * For more details see {@code org.apache.causeway.core.config.beans.CausewayBeanFactoryPostProcessorForSpring}.
 *
 * @see DomainObject
 * @see DomainServiceLayout
 *
 * @since 1.x {@index}
 */
@Named()
@Inherited
@Target({
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Service @Singleton
public @interface DomainService {

    /**
     * Alternative logical type name(s) for the annotated type.
     * @see Bookmark
     * @see Named
     */
    String[] aliased() default {};
}

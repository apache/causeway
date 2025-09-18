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

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.commons.net.DataUri;

/**
 * Indicates that a method is a supporting-method that provides UI hints (title, iconName, layout, cssClass)
 * to its <i>Object</i>.
 * <p>
 * May only be placed on <i>domain object</i> methods, not <i>mixins</i> nor <i>domain services</i>.
 * <p>
 * By placing the {@link ObjectSupport} annotation on a method, a contract with the meta-model is enforced,
 * such that this method must be recognized by the meta-model and cannot be ignored.
 * <p>
 * It is complementary to {@link MemberSupport} and {@link ObjectLifecycle},
 * and in some sense acts as the semantic counterpart to {@link Programmatic}.
 *
 * @since 2.0 {@index}
 * @see ObjectLifecycle
 * @see MemberSupport
 * @see Programmatic
 */
@Inherited
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Domain.Include // meta annotation, in support of meta-model validation
public @interface ObjectSupport {

    /**
     * Rendering context for object icons.
     * An object's icon is either rendered within the object detail page header (along its title)
     * or within a table row.
     */
    public enum IconWhere {
        OBJECT_HEADER,
        TABLE_ROW //TODO also TREE_NODE and SELECT_DROPDOWN
    }

    public sealed interface IconResource extends Serializable
    permits ClassPathIconResource, FontAwesomeIconResource, EmbeddedIconResource {

    }

    public record ClassPathIconResource(
        String suffix) implements IconResource {
        public static ClassPathIconResource emptySuffix() {
            return new ClassPathIconResource("");
        }
    }

    public record FontAwesomeIconResource(
        FontAwesomeLayers faLayers) implements IconResource {
    }

    public record EmbeddedIconResource(
        DataUri dataUri) implements IconResource {
    }

}

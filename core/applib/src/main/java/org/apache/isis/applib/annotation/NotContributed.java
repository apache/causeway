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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the a (repository) action should not be contributed, either as
 * an object action, or as an association (property/collection), or as either.
 * <p/>
 * <p/>
 * It may still be appear in the repository menu (unless it has been annotated
 * as {@link NotInServiceMenu}).
 * <p/>
 * <p/>
 * If annotated with {@link Hidden}, then also implies that the
 * menu should not be contributed.
 * <p/>
 * <p/>
 * Has no meaning for actions on regular entities.
 *
 * @deprecated - to not contribute at all, move action to a service whose {@link org.apache.isis.applib.annotation.DomainService#nature() nature} is {@link org.apache.isis.applib.annotation.NatureOfService#VIEW_MENU_ONLY} or {@link org.apache.isis.applib.annotation.NatureOfService#DOMAIN}; to contribute only as an action or as an association, ensure action is in a service whose nature is {@link org.apache.isis.applib.annotation.NatureOfService#VIEW} or {@link org.apache.isis.applib.annotation.NatureOfService#VIEW_CONTRIBUTIONS_ONLY}, and use {@link ActionLayout#contributed()} to specify whether the contribution should be  {@link Contributed#AS_ACTION as action} or {@link Contributed#AS_ASSOCIATION as association}.
 */
@Deprecated
@Inherited
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotContributed {

    /**
     * @deprecated
     */
    @Deprecated
    public enum As {
        ACTION,
        ASSOCIATION,
        EITHER;

        /**
         * @deprecated
         */
        @Deprecated
        public static As from(final Contributed contributed) {
            if(contributed == null) { return null; }
            switch (contributed) {
                case AS_ACTION: return As.ASSOCIATION;
                case AS_ASSOCIATION: return As.ACTION;
                case AS_NEITHER: return As.EITHER;
                case AS_BOTH: return null;
            }
            return null;
        }

    }

    /**
     * @deprecated
     * @return
     */
    @Deprecated
    As value() default As.EITHER;

}

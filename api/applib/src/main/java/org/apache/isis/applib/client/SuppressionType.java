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
package org.apache.isis.applib.client;

import java.util.EnumSet;
import java.util.List;

import org.apache.isis.commons.internal.base._NullSafe;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

/**
 * 
 * @since 2.0
 */
// tag::refguide[]
public enum SuppressionType {

    // end::refguide[]
    /**
     * suppress '$$RO', RO Spec representation
     */
    // tag::refguide[]
    RO,

    // end::refguide[]
    /**
     * suppress '$$href', hyperlink to the representation
     */
    // tag::refguide[]
    HREF,

    // end::refguide[]
    /**
     suppress '$$instanceId', instance id of the domain object
     */
    // tag::refguide[]
    ID,

    // end::refguide[]
    /**
     * suppress '$$domainType', object spec of the domain object
     */
    // tag::refguide[]
    DOMAIN_TYPE,

    // end::refguide[]
    /**
     * suppress '$$title', title of the domain object
     */
    // tag::refguide[]
    TITLE,

    // end::refguide[]
    /**
     * suppress all '$$...' entries
     */
    // tag::refguide[]
    ALL

    // end::refguide[]
    ;

    public static EnumSet<SuppressionType> setOf(SuppressionType ... types){
        final EnumSet<SuppressionType> set = EnumSet.noneOf(SuppressionType.class);
        stream(types).forEach(set::add);
        return set;
    }

    public static class ParseUtil {

        public static EnumSet<SuppressionType> parse(List<String> parameterList) {
            final EnumSet<SuppressionType> set = EnumSet.noneOf(SuppressionType.class);
            parameterList.stream()
            .map(SuppressionType.ParseUtil::parseOrElseNull)
            .filter(_NullSafe::isPresent)
            .forEach(set::add);
            if(set.contains(ALL)) {
                return EnumSet.allOf(SuppressionType.class);
            }
            return set;
        }

        private static SuppressionType parseOrElseNull(String literal) {

            // honor pre v2 behavior
            if("true".equalsIgnoreCase(literal)) {
                return SuppressionType.RO;
            }

            try {
                return SuppressionType.valueOf(literal.toUpperCase());
            } catch (IllegalArgumentException  e) {
                return null;
            }
        }
    }
    // tag::refguide[]

}
// end::refguide[]

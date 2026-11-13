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
package org.apache.causeway.viewer.graphql.model.types;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.experimental.UtilityClass;

/**
 * Closed classification of the value types supplied by Causeway's metamodel.
 *
 * <p>The corresponding integration test deliberately fails when the framework adds a value-semantics
 * provider without extending this inventory.</p>
 */
@UtilityClass
public class BuiltInValueTypeClassifications {

    public enum Classification {
        REVERSIBLE,
        STRUCTURED,
        PROTECTED,
        OUTPUT_ONLY,
        UNSUPPORTED
    }

    private static final Map<String, Classification> BY_CLASS_NAME = Map.ofEntries(
            reversible("java.lang.Boolean"),
            reversible("java.lang.Byte"),
            reversible("java.lang.Character"),
            reversible("java.lang.Double"),
            reversible("java.lang.Float"),
            reversible("java.lang.Integer"),
            reversible("java.lang.Long"),
            reversible("java.lang.Short"),
            reversible("java.lang.String"),
            reversible("java.math.BigDecimal"),
            reversible("java.math.BigInteger"),
            reversible("java.net.URL"),
            reversible("java.sql.Date"),
            reversible("java.sql.Time"),
            reversible("java.sql.Timestamp"),
            reversible("java.time.LocalDate"),
            reversible("java.time.LocalDateTime"),
            reversible("java.time.LocalTime"),
            reversible("java.time.OffsetDateTime"),
            reversible("java.time.OffsetTime"),
            reversible("java.time.ZonedDateTime"),
            reversible("java.util.Date"),
            reversible("java.util.Locale"),
            reversible("java.util.UUID"),
            reversible("org.apache.causeway.applib.services.appfeat.ApplicationFeatureId"),
            reversible("org.apache.causeway.applib.services.bookmark.Bookmark"),
            structured("org.apache.causeway.applib.value.Blob"),
            structured("org.apache.causeway.applib.value.Clob"),
            structured("org.apache.causeway.applib.value.LocalResourcePath"),
            protectedValue("org.apache.causeway.applib.value.Password"),
            outputOnly("org.apache.causeway.applib.value.Markup"),
            unsupported("java.awt.image.BufferedImage"),
            unsupported("org.apache.causeway.applib.Identifier"),
            unsupported("org.apache.causeway.applib.graph.tree.TreeNode"),
            unsupported("org.apache.causeway.applib.graph.tree.TreePath"),
            unsupported("org.apache.causeway.schema.chg.v2.ChangesDto"),
            unsupported("org.apache.causeway.schema.cmd.v2.CommandDto"),
            unsupported("org.apache.causeway.schema.common.v2.OidDto"),
            unsupported("org.apache.causeway.schema.ixn.v2.InteractionDto"));

    public static Optional<Classification> classificationFor(final Class<?> javaType) {
        return javaType.isEnum()
                ? Optional.of(Classification.REVERSIBLE)
                : Optional.ofNullable(BY_CLASS_NAME.get(javaType.getName()));
    }

    public static Set<String> classNames() {
        return BY_CLASS_NAME.keySet();
    }

    private static Map.Entry<String, Classification> reversible(final String className) {
        return Map.entry(className, Classification.REVERSIBLE);
    }

    private static Map.Entry<String, Classification> structured(final String className) {
        return Map.entry(className, Classification.STRUCTURED);
    }

    private static Map.Entry<String, Classification> protectedValue(final String className) {
        return Map.entry(className, Classification.PROTECTED);
    }

    private static Map.Entry<String, Classification> outputOnly(final String className) {
        return Map.entry(className, Classification.OUTPUT_ONLY);
    }

    private static Map.Entry<String, Classification> unsupported(final String className) {
        return Map.entry(className, Classification.UNSUPPORTED);
    }
}

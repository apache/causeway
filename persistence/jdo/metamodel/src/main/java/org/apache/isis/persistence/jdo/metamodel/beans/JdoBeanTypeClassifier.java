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
package org.apache.isis.persistence.jdo.metamodel.beans;

import java.util.Locale;

import javax.annotation.Nullable;
import javax.jdo.annotations.EmbeddedOnly;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.beans.IsisBeanTypeClassifier;
import org.apache.isis.core.config.util.LogicalTypeNameUtil;
import org.apache.isis.core.metamodel.facets.Annotations;

import static org.apache.isis.commons.internal.reflection._Annotations.findNearestAnnotation;

import lombok.val;

/**
 * ServiceLoader plugin, classifies PersistenceCapable types into BeanSort.ENTITY.
 * @since 2.0
 */
public class JdoBeanTypeClassifier implements IsisBeanTypeClassifier {

    @Override
    public BeanClassification classify(
            final Class<?> type,
            final @Nullable BeanClassificationContext context) {

        val persistenceCapableAnnot = findNearestAnnotation(type, javax.jdo.annotations.PersistenceCapable.class);
        if(persistenceCapableAnnot.isPresent()) {

            val embeddedOnlyAttribute = persistenceCapableAnnot.get().embeddedOnly();
            // Whether objects of this type can only be embedded,
            // hence have no ID that binds them to the persistence layer
            final boolean embeddedOnly = Boolean.valueOf(embeddedOnlyAttribute)
                    || Annotations.getAnnotation(type, EmbeddedOnly.class)!=null;
            if(embeddedOnly) {
                return null; // don't categorize as entity ... fall through in the caller's logic
            }

            String logicalTypeName = null;

            val aDomainObject = findNearestAnnotation(type, DomainObject.class).orElse(null);
            if(aDomainObject!=null) {
                logicalTypeName = LogicalTypeNameUtil.logicalTypeName(aDomainObject);
            }

            // don't trample over the @DomainObject(logicalTypeName=..) if present
            if(_Strings.isEmpty(logicalTypeName)) {
                val schema = persistenceCapableAnnot.get().schema();
                if(_Strings.isNotEmpty(schema)) {

                    val table = persistenceCapableAnnot.get().table();

                    logicalTypeName = String.format("%s.%s", schema.toLowerCase(Locale.ROOT),
                            _Strings.isNotEmpty(table)
                                ? table
                                : type.getSimpleName());
                }
            }


            if(_Strings.isNotEmpty(logicalTypeName)) {
                BeanClassification.selfManaged(
                        BeanSort.ENTITY, logicalTypeName);
            }
            return BeanClassification.selfManaged(BeanSort.ENTITY);
        }

        return null; // we don't feel responsible to classify given type
    }


}

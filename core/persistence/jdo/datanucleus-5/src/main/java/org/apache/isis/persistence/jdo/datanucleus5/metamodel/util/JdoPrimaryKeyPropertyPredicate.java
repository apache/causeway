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
package org.apache.isis.persistence.jdo.datanucleus5.metamodel.util;


import java.util.function.Predicate;

import javax.jdo.annotations.PrimaryKey;

import org.apache.isis.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.prop.primarykey.JdoPrimaryKeyFacet;

/**
 * Use to locate the property annotated with {@link PrimaryKey}.
 * <p>
 * In addition, must also be a {@link OneToOneAssociation}, and can be
 * {@link PropertyAccessorFacet read}.
 * <p>
 * Note that it is NOT necessary for there to be a facet to
 * {@link PropertySetterFacet set} the property.
 */
public final class JdoPrimaryKeyPropertyPredicate implements Predicate<ObjectAssociation> {
    @Override
    public boolean test(final ObjectAssociation noa) {
        return noa.isOneToOneAssociation() &&
                noa.containsFacet(JdoPrimaryKeyFacet.class) &&
                noa.containsFacet(PropertyOrCollectionAccessorFacet.class);
    }
}

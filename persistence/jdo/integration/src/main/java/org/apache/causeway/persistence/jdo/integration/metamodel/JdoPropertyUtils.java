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
package org.apache.causeway.persistence.jdo.integration.metamodel;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.jdo.annotations.PrimaryKey;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

public final class JdoPropertyUtils {

    private JdoPropertyUtils() {
    }

    /**
     * Searches for the property annotated with {@link PrimaryKey}.
     * <p>
     * Returns the {@link OneToOneAssociation} if there is precisely one; else
     * <tt>null</tt>.
     *
     * @see JdoPrimaryKeyPropertyPredicate
     */
    public static OneToOneAssociation getPrimaryKeyPropertyFor(final ObjectSpecification objectSpec) {
        return getPropertyFor(objectSpec, "@PrimaryKey", new JdoPrimaryKeyPropertyPredicate());
    }

    public static boolean hasPrimaryKeyProperty(final ObjectSpecification objectSpec) {
        return getPrimaryKeyPropertyFor(objectSpec) != null;
    }

    private static OneToOneAssociation getPropertyFor(
            final ObjectSpecification objSpec,
            final String annotationName,
            final Predicate<ObjectAssociation> predicate) {

        if (objSpec == null || !objSpec.containsFacet(JdoPersistenceCapableFacet.class)) {
            return null;
        }


        final List<ObjectAssociation> propertyList = objSpec
                .streamAssociations(MixedIn.EXCLUDED)
                .filter(predicate)
                .limit(2)
                .collect(Collectors.toList());

        if (propertyList.size() == 0) {
            return JdoPropertyUtils.getPropertyFor(objSpec.superclass(), annotationName, predicate);
        }
        if (propertyList.size() > 1) {
            throw new IllegalStateException(MessageFormat.format("Shouldn''t have more than one property annotated with {0} (''{1}'')", annotationName, objSpec.getFullIdentifier()));
        }
        return (OneToOneAssociation) propertyList.get(0);
    }

}

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
package org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.swagger.Visibility;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.val;
import lombok.experimental.UtilityClass;

import io.swagger.models.Response;

@UtilityClass
final class _Util {

    boolean isVisibleForPublic(final ObjectAction objectAction) {

        final ObjectSpecification specification = objectAction.getReturnType();
        return ( isVisibleForPublic(specification) || isTypeOfVisibleForPublic(objectAction) )
                && isVisibleForPublic(objectAction.getParameterTypes());
    }

    private boolean isTypeOfVisibleForPublic(final ObjectAction objectAction) {
        final TypeOfFacet typeOfFacet = objectAction.getFacet(TypeOfFacet.class);
        if (typeOfFacet == null) {
            return false;
        }
        return isVisibleForPublic(typeOfFacet.valueSpec());
    }

    private boolean isVisibleForPublic(final Can<ObjectSpecification> parameterTypes) {

        final boolean atLeastOneParamNotVisible =
                parameterTypes.stream()
                    .map(_Util::isNotVisibleForPublic)
                    .findAny()
                    .isPresent();

        return !atLeastOneParamNotVisible;
    }

    boolean isVisibleForPublic(final ObjectAssociation objectAssociation) {
        final ObjectSpecification specification = objectAssociation.getElementType();
        return isVisibleForPublic(specification);
    }

    boolean isNotVisibleForPublic(final ObjectSpecification specification) {
        return ! isVisibleForPublic(specification);
    }

    boolean isVisibleForPublic(final ObjectSpecification specification) {
        if (specification == null) {
            return true;
        }
        if(specification.isViewModel()) {
            return true;
        }
        if(specification.isValue()) {
            return true;
        }
        if(specification.isParentedOrFreeCollection()) {
            TypeOfFacet typeOfFacet = specification.getFacet(TypeOfFacet.class);
            if(typeOfFacet != null) {
                ObjectSpecification elementSpec = typeOfFacet.valueSpec();
                return isVisibleForPublic(elementSpec);
            }
        }

        final Class<?> correspondingClass = specification.getCorrespondingClass();
        return  Collection.class.isAssignableFrom(correspondingClass) ||
                correspondingClass.isArray() ||
                correspondingClass == void.class ||
                correspondingClass == Void.class;
    }

    Predicate<ObjectAssociation> associationsWith(final Visibility visibility) {
        return new Predicate<ObjectAssociation>() {
            @Override
            public boolean test(final ObjectAssociation objectAssociation) {
                return !visibility.isPublic() || isVisibleForPublic(objectAssociation);
            }
        };
    }

    List<OneToOneAssociation> propertiesOf(
            final ObjectSpecification objectSpecification,
            final Visibility visibility) {
        return associationsOf(objectSpecification, ObjectAssociation.Predicates.PROPERTIES, visibility);
    }

    List<OneToManyAssociation> collectionsOf(
            final ObjectSpecification objectSpecification,
            final Visibility visibility) {
        return associationsOf(objectSpecification, ObjectAssociation.Predicates.COLLECTIONS, visibility);
    }

    private <T extends ObjectAssociation> List<T> associationsOf(
            final ObjectSpecification objectSpecification,
            final Predicate<ObjectAssociation> associationPredicate, final Visibility visibility) {

        return objectSpecification.streamAssociations(MixedIn.INCLUDED)
                .filter(associationPredicate.and(associationsWith(visibility)))
                .map(x->_Casts.<T>uncheckedCast(x))
                .collect(Collectors.toList());
    }

    List<ObjectAction> actionsOf(
            final ObjectSpecification objectSpec,
            final Visibility visibility,
            final ClassExcluder classExcluder) {
        val actionTypes = actionScopesFor(visibility);

        return objectSpec.streamActions(actionTypes, MixedIn.INCLUDED)
                .filter(objectAction->
                    !classExcluder.exclude(objectAction)
                    && !visibility.isPublic()
                    || isVisibleForPublic(objectAction) )
                .collect(Collectors.toList());
    }

    String roSpec(final String section) {
        return "RO Spec v1.0, section " + section;
    }

    Response withCachingHeaders(final Response response, final Caching caching) {
        caching.withHeaders(response);

        return response;
    }

    ImmutableEnumSet<ActionScope> actionScopesFor(final Visibility visibility) {
        switch (visibility) {
        case PUBLIC:
        case PRIVATE:
            return ActionScope.PRODUCTION_ONLY;
        case PRIVATE_WITH_PROTOTYPING:
            return ActionScope.ANY;
        }
        throw _Exceptions.unmatchedCase(visibility);
    }
}

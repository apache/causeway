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
package org.apache.isis.core.metamodel.services.swagger.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import io.swagger.models.Response;

public final class Util {

    private Util(){}

    static boolean isVisibleForPublic(final ObjectAction objectAction) {
        final ObjectSpecification specification = objectAction.getReturnType();
        boolean visible = isVisibleForPublic(specification);
        if(!visible) {
            final TypeOfFacet typeOfFacet = objectAction.getFacet(TypeOfFacet.class);
            if(typeOfFacet != null) {
                ObjectSpecification elementSpec = typeOfFacet.valueSpec();
                if(!isVisibleForPublic(elementSpec)) {
                    return false;
                }
            }
        }
        List<ObjectSpecification> parameterTypes = objectAction.getParameterTypes();
        for (ObjectSpecification parameterType : parameterTypes) {
            boolean paramVisible = isVisibleForPublic(parameterType);
            if(!paramVisible) {
                return false;
            }
        }
        return true;
    }

    static boolean isVisibleForPublic(final ObjectAssociation objectAssociation) {
        final ObjectSpecification specification = objectAssociation.getSpecification();
        return isVisibleForPublic(specification);
    }

    static boolean isVisibleForPublic(final ObjectSpecification specification) {
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

    static Predicate<ObjectAssociation> associationsWith(final SwaggerService.Visibility visibility) {
        return new Predicate<ObjectAssociation>() {
            @Override
            public boolean test(final ObjectAssociation objectAssociation) {
                return !visibility.isPublic() || isVisibleForPublic(objectAssociation);
            }
        };
    }

    static List<OneToOneAssociation> propertiesOf(
            final ObjectSpecification objectSpecification,
            final SwaggerService.Visibility visibility) {
        return associationsOf(objectSpecification, ObjectAssociation.Predicates.PROPERTIES, visibility);
    }

    static List<OneToManyAssociation> collectionsOf(
            final ObjectSpecification objectSpecification,
            final SwaggerService.Visibility visibility) {
        return associationsOf(objectSpecification, ObjectAssociation.Predicates.COLLECTIONS, visibility);
    }

    private static <T extends ObjectAssociation> List<T> associationsOf(
            final ObjectSpecification objectSpecification,
            final Predicate<ObjectAssociation> associationPredicate, final SwaggerService.Visibility visibility) {
        
        final List<ObjectAssociation> list =
                objectSpecification.streamAssociations(Contributed.INCLUDED)
                .filter(associationPredicate.and(associationsWith(visibility)))
                .collect(Collectors.toList());

        return _Casts.uncheckedCast(list);
    }

    static List<ObjectAction> actionsOf(
            final ObjectSpecification objectSpec,
            final SwaggerService.Visibility visibility,
            final ClassExcluder classExcluder) {
        final List<ActionType> actionTypes = actionTypesFor(visibility);

        return objectSpec.streamObjectActions(actionTypes, Contributed.INCLUDED)
                .filter(objectAction->
                    !classExcluder.exclude(objectAction) &&
                            !visibility.isPublic() || isVisibleForPublic(objectAction)
                )
                .collect(Collectors.toList());
    }

    static String roSpec(final String section) {
        return "RO Spec v1.0, section " + section;
    }

    static Response withCachingHeaders(final Response response, final Caching caching) {
        caching.withHeaders(response);

        return response;
    }

    static List<ActionType> actionTypesFor(final SwaggerService.Visibility visibility) {
        switch (visibility) {
        case PUBLIC:
            return Arrays.asList(ActionType.USER);
        case PRIVATE:
            return Arrays.asList(ActionType.USER);
        case PRIVATE_WITH_PROTOTYPING:
            return Arrays.asList(ActionType.USER, ActionType.PROTOTYPE);
        }
        throw new IllegalArgumentException("Unrecognized type '" + visibility + "'");
    }
}

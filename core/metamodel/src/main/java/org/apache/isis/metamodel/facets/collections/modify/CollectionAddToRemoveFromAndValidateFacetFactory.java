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

package org.apache.isis.metamodel.facets.collections.modify;

import java.lang.reflect.Method;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.metamodel.exceptions.MetaModelException;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.facets.MethodPrefixConstants;
import org.apache.isis.metamodel.facets.collections.validate.CollectionValidateAddToFacetViaMethod;
import org.apache.isis.metamodel.facets.collections.validate.CollectionValidateRemoveFromFacetViaMethod;
import org.apache.isis.metamodel.methodutils.MethodScope;

/**
 * TODO: should probably split out into two {@link FacetFactory}s, one for
 * <tt>addTo()</tt>/<tt>removeFrom()</tt> and one for <tt>validateAddTo()</tt>/
 * <tt>validateRemoveFrom()</tt>.
 */
public class CollectionAddToRemoveFromAndValidateFacetFactory extends MethodPrefixBasedFacetFactoryAbstract  {

    private static final String[] PREFIXES = {};


    public CollectionAddToRemoveFromAndValidateFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final Class<?> collectionType = attachAddToFacetAndRemoveFromFacet(processMethodContext);
        attachValidateAddToAndRemoveFromFacetIfMethodsFound(processMethodContext, collectionType);
    }

    private Class<?> attachAddToFacetAndRemoveFromFacet(final ProcessMethodContext processMethodContext) {

        final Method accessorMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseName(accessorMethod.getName());

        final Class<?> cls = processMethodContext.getCls();

        // add
        final Method addToMethod = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, MethodPrefixConstants.ADD_TO_PREFIX + capitalizedName, void.class);
        processMethodContext.removeMethod(addToMethod);

        // remove
        final Method removeFromMethod = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, MethodPrefixConstants.REMOVE_FROM_PREFIX + capitalizedName, void.class);
        processMethodContext.removeMethod(removeFromMethod);

        // add facets
        final FacetHolder collection = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(createAddToFacet(addToMethod, accessorMethod, collection));
        FacetUtil.addFacet(createRemoveFromFacet(removeFromMethod, accessorMethod, collection));

        // infer typ
        final Class<?> addToType = ((addToMethod == null || addToMethod.getParameterTypes().length != 1) ? null : addToMethod.getParameterTypes()[0]);
        final Class<?> removeFromType = ((removeFromMethod == null || removeFromMethod.getParameterTypes().length != 1) ? null : removeFromMethod.getParameterTypes()[0]);

        return inferTypeOfIfPossible(accessorMethod, addToType, removeFromType, collection);
    }

    /**
     * TODO need to distinguish between Java collections, arrays and other
     * collections!
     */
    private CollectionAddToFacet createAddToFacet(final Method addToMethodIfAny, final Method accessorMethod, final FacetHolder holder) {
        if (addToMethodIfAny != null) {
            return new CollectionAddToFacetViaMethod(addToMethodIfAny, holder);
        } else {
            return new CollectionAddToFacetViaAccessor(accessorMethod, holder);
        }
    }

    /**
     * TODO need to distinguish between Java collections, arrays and other
     * collections!
     */
    private CollectionRemoveFromFacet createRemoveFromFacet(final Method removeFromMethodIfAny, final Method accessorMethod, final FacetHolder holder) {
        if (removeFromMethodIfAny != null) {
            return new CollectionRemoveFromFacetViaMethod(removeFromMethodIfAny, holder);
        } else {
            return new CollectionRemoveFromFacetViaAccessor(accessorMethod, holder);
        }
    }

    private Class<?> inferTypeOfIfPossible(final Method getMethod, final Class<?> addType, final Class<?> removeType, final FacetHolder collection) {

        if (addType != null && removeType != null && addType != removeType) {
            throw new MetaModelException("The addTo/removeFrom methods for " + getMethod.getDeclaringClass() + " must " + "both deal with same type of object: " + addType + "; " + removeType);
        }

        final Class<?> type = addType != null ? addType : removeType;
        if (type != null) {
            FacetUtil.addFacet(new TypeOfFacetInferredFromSupportingMethods(type, collection));
        }
        return type;
    }

    private void attachValidateAddToAndRemoveFromFacetIfMethodsFound(final ProcessMethodContext processMethodContext, final Class<?> collectionType) {
        attachValidateAddToFacetIfValidateAddToMethodIsFound(processMethodContext, collectionType);
        attachValidateRemoveFacetIfValidateRemoveFromMethodIsFound(processMethodContext, collectionType);
    }

    private void attachValidateAddToFacetIfValidateAddToMethodIsFound(final ProcessMethodContext processMethodContext, final Class<?> collectionType) {

        final Method getMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseName(getMethod.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Class<?>[] paramTypes = MethodFinderUtils.paramTypesOrNull(collectionType);
        Method validateAddToMethod = MethodFinderUtils.findMethod(
                cls, MethodScope.OBJECT,
                MethodPrefixConstants.VALIDATE_ADD_TO_PREFIX + capitalizedName,
                new Class<?>[]{String.class, TranslatableString.class},
                paramTypes);
        if (validateAddToMethod == null) {
            validateAddToMethod = MethodFinderUtils.findMethod(
                    cls, MethodScope.OBJECT,
                    MethodPrefixConstants.VALIDATE_ADD_TO_PREFIX_2 + capitalizedName,
                    new Class<?>[]{String.class, TranslatableString.class},
                    MethodFinderUtils.paramTypesOrNull(collectionType));
        }
        if (validateAddToMethod == null) {
            return;
        }
        processMethodContext.removeMethod(validateAddToMethod);

        final IdentifiedHolder facetHolder = processMethodContext.getFacetHolder();
        final TranslationService translationService = getTranslationService();
        // sadness: same as in TranslationFactory
        final String translationContext = facetHolder.getIdentifier().toClassAndNameIdentityString();

        final CollectionValidateAddToFacetViaMethod facet = new CollectionValidateAddToFacetViaMethod(validateAddToMethod, translationService, translationContext, facetHolder);
        FacetUtil.addFacet(facet);
    }

    private void attachValidateRemoveFacetIfValidateRemoveFromMethodIsFound(final ProcessMethodContext processMethodContext, final Class<?> collectionType) {

        final Method getMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseName(getMethod.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Class<?>[] paramTypes = MethodFinderUtils.paramTypesOrNull(collectionType);
        Method validateRemoveFromMethod = MethodFinderUtils.findMethod(
                cls, MethodScope.OBJECT,
                MethodPrefixConstants.VALIDATE_REMOVE_FROM_PREFIX + capitalizedName,
                new Class<?>[]{String.class, TranslatableString.class},
                paramTypes);
        if (validateRemoveFromMethod == null) {
            validateRemoveFromMethod = MethodFinderUtils.findMethod(
                    cls, MethodScope.OBJECT,
                    MethodPrefixConstants.VALIDATE_REMOVE_FROM_PREFIX_2 + capitalizedName,
                    new Class<?>[]{String.class, TranslatableString.class},
                    MethodFinderUtils.paramTypesOrNull(collectionType));
        }
        if (validateRemoveFromMethod == null) {
            return;
        }
        processMethodContext.removeMethod(validateRemoveFromMethod);

        final IdentifiedHolder facetHolder = processMethodContext.getFacetHolder();
        final TranslationService translationService = getTranslationService();
        // sadness: same as in TranslationFactory
        final String translationContext = facetHolder.getIdentifier().toClassAndNameIdentityString();

        final CollectionValidateRemoveFromFacetViaMethod facet = new CollectionValidateRemoveFromFacetViaMethod(validateRemoveFromMethod, translationService, translationContext, facetHolder);
        FacetUtil.addFacet(facet);
    }

}

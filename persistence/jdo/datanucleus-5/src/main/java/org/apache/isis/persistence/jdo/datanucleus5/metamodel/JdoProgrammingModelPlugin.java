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
package org.apache.isis.persistence.jdo.datanucleus5.metamodel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.jdo.annotations.IdentityType;

import org.springframework.stereotype.Component;

import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.collections._Multimaps;
import org.apache.isis.core.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.ignore.datanucleus.RemoveDatanucleusPersistableTypesFacetFactory;
import org.apache.isis.metamodel.facets.object.ignore.datanucleus.RemoveDnPrefixedMethodsFacetFactory;
import org.apache.isis.metamodel.facets.object.ignore.jdo.RemoveJdoEnhancementTypesFacetFactory;
import org.apache.isis.metamodel.facets.object.ignore.jdo.RemoveJdoPrefixedMethodsFacetFactory;
import org.apache.isis.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.progmodel.ProgrammingModel.Marker;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.object.datastoreidentity.JdoDatastoreIdentityAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.object.discriminator.JdoDiscriminatorAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.object.persistencecapable.JdoPersistenceCapableAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.object.query.JdoQueryAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.object.version.JdoVersionAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.prop.column.BigDecimalDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.prop.column.MandatoryFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.prop.column.MaxLengthDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.prop.notpersistent.JdoNotPersistentAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.prop.primarykey.JdoPrimaryKeyAnnotationFacetFactory;

import static org.apache.isis.core.commons.internal.base._NullSafe.stream;

import lombok.val;

@Component
public class JdoProgrammingModelPlugin implements MetaModelRefiner {
    
    @Inject private IsisConfiguration config;
    private ProgrammingModel pm;

    @Override
    public void refineProgrammingModel(ProgrammingModel pm) {
        
        this.pm = pm;

        val step1 = ProgrammingModel.FacetProcessingOrder.C2_AFTER_METHOD_REMOVING;
        
        // come what may, we have to ignore the PersistenceCapable supertype.
        pm.addFactory(step1, RemoveJdoEnhancementTypesFacetFactory.class, Marker.JDO);
        // so we may as well also just ignore any 'jdo' prefixed methods here also.
        pm.addFactory(step1, RemoveJdoPrefixedMethodsFacetFactory.class, Marker.JDO);
        // Datanucleus
        pm.addFactory(step1, RemoveDatanucleusPersistableTypesFacetFactory.class, Marker.JDO);
        pm.addFactory(step1, RemoveDnPrefixedMethodsFacetFactory.class, Marker.JDO);

        
        val step2 = ProgrammingModel.FacetProcessingOrder.A2_AFTER_FALLBACK_DEFAULTS;

        pm.addFactory(step2, JdoPersistenceCapableAnnotationFacetFactory.class, Marker.JDO);
        pm.addFactory(step2, JdoDatastoreIdentityAnnotationFacetFactory.class, Marker.JDO);

        pm.addFactory(step2, JdoPrimaryKeyAnnotationFacetFactory.class, Marker.JDO);
        pm.addFactory(step2, JdoNotPersistentAnnotationFacetFactory.class, Marker.JDO);
        pm.addFactory(step2, JdoDiscriminatorAnnotationFacetFactory.class, Marker.JDO);
        pm.addFactory(step2, JdoVersionAnnotationFacetFactory.class, Marker.JDO);

        pm.addFactory(step2, JdoQueryAnnotationFacetFactory.class, Marker.JDO);

        pm.addFactory(step2, BigDecimalDerivedFromJdoColumnAnnotationFacetFactory.class, Marker.JDO);
        pm.addFactory(step2, MaxLengthDerivedFromJdoColumnAnnotationFacetFactory.class, Marker.JDO);
        // must appear after JdoPrimaryKeyAnnotationFacetFactory (above)
        // and also MandatoryFacetOnPropertyMandatoryAnnotationFactory
        // and also PropertyAnnotationFactory
        pm.addFactory(step2, MandatoryFromJdoColumnAnnotationFacetFactory.class, Marker.JDO);


        // -- validators
        
        val validator = config.getReflector().getValidator();
        
        addValidatorToEnsureIdentityType();
        addValidatorToCheckForUnsupportedAnnotations();
        
        if(validator.isEnsureUniqueObjectTypes()) {
            addValidatorToEnsureUniqueObjectIds();
        }
        
        if(validator.isCheckModuleExtent()) {
            addValidatorToCheckModuleExtent();
        }

    }

    private void addValidatorToEnsureIdentityType() {

        pm.addValidator((objSpec, validation) -> {

            final JdoPersistenceCapableFacet jpcf = objSpec.getFacet(JdoPersistenceCapableFacet.class);
            if(jpcf == null) {
                return true;
            }
            final IdentityType identityType = jpcf.getIdentityType();
            if(identityType == IdentityType.APPLICATION) {
                // ok

            } else if(identityType == IdentityType.NONDURABLE) {
                // ok; for use with DN view objects (http://www.datanucleus.org/products/accessplatform_3_2/datastores/rdbms_views.html)

            } else if(identityType == IdentityType.DATASTORE || identityType == IdentityType.UNSPECIFIED) {

                // TODO: ensure that DATASTORE has recognised @DatastoreIdentity attribute

            } else {
                // in fact, at the time of writing there are no others, so this is theoretical in case there is
                // a future change to the JDO spec
                validation.onFailure(
                        objSpec,
                        objSpec.getIdentifier(),
                        "%s: is annotated with @PersistenceCapable but with an unrecognized identityType (%s)",
                        objSpec.getFullIdentifier(),
                        identityType);
            }

            return true;
        }, Marker.JDO);

    }

    private void addValidatorToCheckForUnsupportedAnnotations() {

        pm.addValidator((objSpec, validation) -> {
            if (objSpec.containsNonFallbackFacet(ParentedCollectionFacet.class) && !objSpec.containsNonFallbackFacet(CollectionFacet.class)) {
                validation.onFailure(
                        objSpec,
                        objSpec.getIdentifier(),
                        "%s: DataNucleus object store currently does not supported Aggregated or EmbeddedOnly annotations",
                        objSpec.getFullIdentifier());
            }
            return true;
        }, Marker.JDO);

    }

    private void addValidatorToEnsureUniqueObjectIds() {

        final ListMultimap<ObjectSpecId, ObjectSpecification> collidingSpecsById = 
                _Multimaps.newConcurrentListMultimap();

        final MetaModelValidatorVisiting.SummarizingVisitor ensureUniqueObjectIds = 
                new MetaModelValidatorVisiting.SummarizingVisitor(){

            @Override
            public boolean visit(ObjectSpecification objSpec, MetaModelValidator validator) {
                val specId = objSpec.getSpecId();
                collidingSpecsById.putElement(specId, objSpec);
                return true;
            }

            @Override
            public void summarize(MetaModelValidator validator) {
                for (val specId : collidingSpecsById.keySet()) {
                    val collidingSpecs = collidingSpecsById.get(specId);
                    val isCollision = collidingSpecs.size()>1;
                    if(isCollision) {
                        val csv = asCsv(collidingSpecs);
                        
                        collidingSpecs.forEach(spec->{
                            validator.onFailure(
                                    spec,
                                    spec.getIdentifier(),
                                    "Object type '%s' mapped to multiple classes: %s", 
                                    specId.asString(), 
                                    csv);    
                        });
                        
                        
                    }
                }
                // so can be revalidated again if necessary.
                collidingSpecsById.clear();
            }

            private String asCsv(final List<ObjectSpecification> specList) {
                return stream(specList)
                        .map(ObjectSpecification::getFullIdentifier)
                        .collect(Collectors.joining(","));
            }

        };

        pm.addValidator(ensureUniqueObjectIds);
    }

    private void addValidatorToCheckModuleExtent() {

        final Map<String, List<String>> domainObjectClassNamesByPackage = _Maps.newConcurrentHashMap();

        MetaModelValidatorVisiting.SummarizingVisitor visitor = new MetaModelValidatorVisiting.SummarizingVisitor(){

            @Override
            public boolean visit(ObjectSpecification objSpec, MetaModelValidator validator) {
                Class<?> correspondingClass = objSpec.getCorrespondingClass();
                if(correspondingClass == null) {
                    return true;
                }
                Package aPackage = correspondingClass.getPackage();
                if(aPackage == null) {
                    return true;
                }
                final String packageName = aPackage.getName();

                if (objSpec.isValue() || objSpec.isAbstract() || objSpec.isMixin() ||
                        objSpec.isParentedOrFreeCollection() ||
                        objSpec.getFullIdentifier().startsWith("java") ||
                        objSpec.getFullIdentifier().startsWith("org.joda") ||
                        objSpec.getFullIdentifier().startsWith("org.apache.isis")) {
                    // ignore
                } else {
                    List<String> classNames = domainObjectClassNamesByPackage.get(packageName);
                    if (classNames == null) {
                        classNames = _Lists.newArrayList();
                        domainObjectClassNamesByPackage.put(packageName, classNames);
                    }
                    classNames.add(objSpec.getFullIdentifier());
                }
                return true;
            }

            @Override
            public void summarize(final MetaModelValidator validator) {
                //FIXME[2112] module (legacy) specific, remove?
                //                final Set<String> modulePackageNames = modulePackageNamesFrom(appManifest);
                //
                //                final Set<String> domainObjectPackageNames = domainObjectClassNamesByPackage.keySet();
                //                for (final String pkg : domainObjectPackageNames) {
                //                    List<String> domainObjectClassNames = domainObjectClassNamesByPackage.get(pkg);
                //                    boolean withinSomeModule = isWithinSomeModule(modulePackageNames, pkg);
                //                    if(!withinSomeModule) {
                //                        String csv = stream(domainObjectClassNames)
                //                                .collect(Collectors.joining(","));
                //                        validationFailures.add(
                //                                "Domain objects discovered in package '%s' are not in the set of modules obtained from "
                //                                        + "the AppManifest's top-level module '%s'.  Classes are: %s",
                //                                        pkg, topLevelModule.getClass().getName(), csv);
                //                    }
                //                }
            }
            //FIXME[2112] module (legacy) specific, remove?
            //            private Set<String> modulePackageNamesFrom(final AppManifest appManifest) {
            //                final List<Class<?>> modules = appManifest.getModules();
            //                return modules.stream()
            //                        .map(aClass->aClass.getPackage().getName())
            //                        .collect(Collectors.toCollection(HashSet::new));
            //            }
            //
            //            private boolean isWithinSomeModule(final Set<String> modulePackageNames, final String pkg) {
            //                for (final String modulePackageName : modulePackageNames) {
            //                    if(pkg.startsWith(modulePackageName)) {
            //                        return true;
            //                    }
            //                }
            //                return false;
            //            }
        };

        pm.addValidator(visitor);
    }


}

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
package org.apache.isis.core.metamodel.specloader.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.AppManifest2;
import org.apache.isis.applib.Module;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class MetaModelValidatorToCheckModuleExtent extends MetaModelValidatorComposite {

    private static final String ISIS_REFLECTOR_CHECK_MODULE_EXTENT_KEY = "isis.reflector.validator.checkModuleExtent";
    private static final boolean ISIS_REFLECTOR_CHECK_MODULE_EXTENT_DEFAULT = true;

    public MetaModelValidatorToCheckModuleExtent() {
        addValidatorToCheckModuleExtent();
    }

    @Override
    public void validate(final ValidationFailures validationFailures) {
        boolean check = specificationLoader.getConfiguration()
                .getBoolean(ISIS_REFLECTOR_CHECK_MODULE_EXTENT_KEY,
                        ISIS_REFLECTOR_CHECK_MODULE_EXTENT_DEFAULT);
        if(!check) {
            return;
        }

        AppManifest2 appManifest = specificationLoader.getServicesInjector()
                .lookupService(MetaModelService.class).getAppManifest2();
        if(appManifest == null) {
            return;
        }

        super.validate(validationFailures);
    }

    private void addValidatorToCheckModuleExtent() {

        final Map<String, List<String>> domainObjectClassNamesByPackage = _Maps.newTreeMap();

        MetaModelValidatorVisiting.SummarizingVisitor visitor = new MetaModelValidatorVisiting.SummarizingVisitor(){

            @Override
            public boolean visit(ObjectSpecification objSpec, ValidationFailures validationFailures) {
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
            public void summarize(final ValidationFailures validationFailures) {

                AppManifest2 appManifest = specificationLoader.getServicesInjector()
                        .lookupService(MetaModelService.class).getAppManifest2();
                if(appManifest == null) {
                    return;
                }
                Module topLevelModule = appManifest.getModule();
                if(topLevelModule == null) {
                    // shouldn't happen
                    return;
                }

                final Set<String> modulePackageNames = modulePackageNamesFrom(appManifest);

                final Set<String> domainObjectPackageNames = domainObjectClassNamesByPackage.keySet();
                for (final String pkg : domainObjectPackageNames) {
                    List<String> domainObjectClassNames = domainObjectClassNamesByPackage.get(pkg);
                    boolean withinSomeModule = isWithinSomeModule(modulePackageNames, pkg);
                    if(!withinSomeModule) {
                        String csv = Joiner.on(",").join(domainObjectClassNames);
                        validationFailures.add(
                                "Domain objects discovered in package '%s' are not in the set of modules obtained from "
                                        + "the AppManifest's top-level module '%s'.  Classes are: %s",
                                        pkg, topLevelModule.getClass().getName(), csv);
                    }
                }
            }

            private Set<String> modulePackageNamesFrom(final AppManifest appManifest) {
                final List<Class<?>> modules = appManifest.getModules();
                return modules.stream()
                        .map(aClass->aClass.getPackage().getName())
                        .collect(Collectors.toCollection(HashSet::new));
            }

            private boolean isWithinSomeModule(final Set<String> modulePackageNames, final String pkg) {
                for (final String modulePackageName : modulePackageNames) {
                    if(pkg.startsWith(modulePackageName)) {
                        return true;
                    }
                }
                return false;
            }
        };

        add(new MetaModelValidatorVisiting(visitor));
    }
}

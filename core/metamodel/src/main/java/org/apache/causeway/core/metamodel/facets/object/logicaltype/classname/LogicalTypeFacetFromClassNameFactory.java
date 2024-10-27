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
package org.apache.causeway.core.metamodel.facets.object.logicaltype.classname;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MessageTemplate;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.causeway.core.metamodel.object.MmSpecUtils;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

public class LogicalTypeFacetFromClassNameFactory
extends FacetFactoryAbstract
implements
        ObjectTypeFacetFactory,
        MetaModelRefiner {

    @Inject
    public LogicalTypeFacetFromClassNameFactory(
            final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    // -- JUNIT SUPPORT

    public static LogicalTypeFacetFromClassNameFactory forTesting(
            final MetaModelContext mmc) {
        return new LogicalTypeFacetFromClassNameFactory(mmc);
    }

    @Override
    public void process(final ProcessObjectTypeContext processClassContext) {
        // no-op.
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        // no-op.
    }

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {
        var shouldCheck = getConfiguration().getCore().getMetaModel().getValidator().isExplicitLogicalTypeNames();
        if(!shouldCheck) return;

        programmingModel.addValidatorSkipManagedBeans(objectSpec-> {
            if(skip(objectSpec)) return;

            var logicalType = objectSpec.getLogicalType();

            if(logicalType.getClassName().equals(logicalType.getLogicalTypeName())
                    && !_ClassCache.getInstance().isAnnotatedWithNamed(objectSpec.getCorrespondingClass())) {
                ValidationFailure.raise(objectSpec, MessageTemplate.LOGICAL_TYPE_NAME_IS_NOT_EXPLICIT
                        .builder()
                        .addVariable("type", objectSpec.getFullIdentifier())
                        .addVariable("beanSort", objectSpec.getBeanSort().name())
                        .addVariable("configProperty", "causeway.core.meta-model.validator.explicit-logical-type-names")
                        .buildMessage());
            }
        });
    }

    // -- HELPER

    private boolean skip(final ObjectSpecification objectSpec) {
        if (objectSpec.isAbstract()
                || MmSpecUtils.isJavaRecord(objectSpec)
                || objectSpec.isValue()
                || objectSpec.isMixin()
                || MmSpecUtils.isFixtureScript(objectSpec)) return true;
        if (objectSpec.isEntity()) return false;
        if (objectSpec.isViewModel()) {
            // with
            // skip JAXB DTOs
            return objectSpec.getCorrespondingClass().getAnnotation(XmlType.class) != null;
        }
        if (objectSpec.isInjectable()) {
            // only check if its a domain service (that is potentially contributing to UI or Web-API(s).
            if(!objectSpec.isDomainService()) return true;

            // skip if domain service has only programmatic methods
            return objectSpec.streamAnyActions(MixedIn.INCLUDED).findAny().isEmpty();

        }
        return true; //skip validation
    }

}

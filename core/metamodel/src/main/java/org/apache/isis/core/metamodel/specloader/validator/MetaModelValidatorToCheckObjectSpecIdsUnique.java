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

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class MetaModelValidatorToCheckObjectSpecIdsUnique extends MetaModelValidatorComposite {

    public static final String ISIS_REFLECTOR_ENSURE_UNIQUE_OBJECT_IDS_KEY = "isis.reflector.validator.ensureUniqueObjectTypes";
    public static final boolean ISIS_REFLECTOR_ENSURE_UNIQUE_OBJECT_IDS_DEFAULT = true;

    public MetaModelValidatorToCheckObjectSpecIdsUnique() {
        addValidatorToEnsureUniqueObjectIds();
    }

    @Override
    public void validate(final ValidationFailures validationFailures) {
        boolean check = getConfiguration()
                .getBoolean(ISIS_REFLECTOR_ENSURE_UNIQUE_OBJECT_IDS_KEY,
                        ISIS_REFLECTOR_ENSURE_UNIQUE_OBJECT_IDS_DEFAULT);
        if(!check) {
            return;
        }
        super.validate(validationFailures);
    }

    private void addValidatorToEnsureUniqueObjectIds() {

        final Map<ObjectSpecId, List<ObjectSpecification>> specsById = _Maps.newHashMap();

        MetaModelValidatorVisiting.SummarizingVisitor ensureUniqueObjectIds = new MetaModelValidatorVisiting.SummarizingVisitor(){

            @Override
            public boolean visit(ObjectSpecification objSpec, ValidationFailures validationFailures) {
                ObjectSpecId specId = objSpec.getSpecId();
                List<ObjectSpecification> objectSpecifications = specsById.get(specId);
                if(objectSpecifications == null) {
                    objectSpecifications = _Lists.newArrayList();
                    specsById.put(specId, objectSpecifications);
                }
                objectSpecifications.add(objSpec);
                return true;
            }

            @Override
            public void summarize(final ValidationFailures validationFailures) {
                for (final ObjectSpecId specId : specsById.keySet()) {
                    final List<ObjectSpecification> specList = specsById.get(specId);
                    int numSpecs = specList.size();
                    if(numSpecs > 1) {
                        String csv = asCsv(specList);
                        validationFailures.add(
                                "Object type '%s' mapped to multiple classes: %s", specId.asString(), csv);
                    }
                }
            }

            private String asCsv(final List<ObjectSpecification> specList) {
                return stream(specList)
                        .map(ObjectSpecification.Functions.FULL_IDENTIFIER)
                        .collect(Collectors.joining(","));
            }

        };


        add(new MetaModelValidatorVisiting(ensureUniqueObjectIds));
    }
}

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
package org.apache.isis.core.metamodel.methods;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.NonNull;
import lombok.val;

class _OrphanedSupportingMethodValidator {

    static void validate(
            final @NonNull ObjectSpecificationAbstract spec,
            final @NonNull Set<Method> supportMethods,
            final @NonNull Set<Method> memberMethods,
            final @NonNull Set<Method> alreadyReported) {

        if(spec.isAbstract()
                || spec.getBeanSort().isManagedBeanNotContributing()
                || spec.isValue()
                || spec.getIntrospectionPolicy()
                    .getMemberAnnotationPolicy()
                    .isMemberAnnotationsRequired()) {
            return; // ignore
        }

        val potentialOrphans = spec.getPotentialOrphans();
        if(potentialOrphans.isEmpty()) {
            return; // nothing to do
        }

        // find reasons why these are not recognized
        potentialOrphans.stream()
        .filter(Predicate.not(alreadyReported::contains))
        .filter(Predicate.not(memberMethods::contains))
        .filter(Predicate.not(supportMethods::contains))
        .forEach(orphanedMethod->{

            val methodIdentifier = Identifier
                    .methodIdentifier(spec.getFeatureIdentifier().getLogicalType(), orphanedMethod);

            ValidationFailure.raise(
                    spec,
                    ProgrammingModelConstants.Validation.ORPHANED_METHOD
                    .getMessage(methodIdentifier));
        });

        potentialOrphans.clear(); // no longer needed

    }

}

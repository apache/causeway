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

import java.util.function.Predicate;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderDefault;

import lombok.NonNull;
import lombok.val;

public class MetaModelValidatorVisiting 
extends MetaModelValidatorAbstract {

    @Override
    public String toString() {
        return "MetaModelValidatorVisiting{" +
                "visitor=" + visitor +
                '}';
    }

    // -- INTERFACES
    
    @FunctionalInterface
    public interface Visitor {
        /**
         * @return <tt>true</tt> continue visiting specs.
         */
        boolean visit(ObjectSpecification spec);
    }
    
    public interface SummarizingVisitor extends Visitor {
        void summarize();
    }
    
    // -- IMPLEMENTATION

    public static MetaModelValidatorVisiting of(
            final @NonNull Visitor visitor,
            final @NonNull Predicate<ObjectSpecification> includeIf) {
        return new MetaModelValidatorVisiting(visitor, includeIf);
    }


    @NonNull private final Visitor visitor;
    @NonNull private final Predicate<ObjectSpecification> includeIf;

    private MetaModelValidatorVisiting(
            final @NonNull Visitor visitor,
            final @NonNull Predicate<ObjectSpecification> includeIf
            ) {
        this.visitor = visitor;
        this.includeIf = includeIf;
    }

    @Override
    public void collectFailuresInto(@NonNull ValidationFailures validationFailures) {
        validateAll();
        summarize();
        super.collectFailuresInto(validationFailures);
    }

    private void validateAll() {

        val specLoader = (SpecificationLoaderDefault)super.getMetaModelContext().getSpecificationLoader();
        
        val isActionExplicit = getConfiguration().getApplib().getAnnotation().getAction().isExplicit();
        
        specLoader.forEach(spec->{
            
            if(!isActionExplicit
                    && spec.getBeanSort().isUnknown()) {
                    return; // in support of @Action not being forced, we need to relax 
            }
            
            if(!includeIf.test(spec)) {
                return;
            }
            
            visitor.visit(spec);
        });
    }

    private void summarize() {
        if(visitor instanceof SummarizingVisitor) {
            ((SummarizingVisitor) visitor).summarize();
        }
    }

}

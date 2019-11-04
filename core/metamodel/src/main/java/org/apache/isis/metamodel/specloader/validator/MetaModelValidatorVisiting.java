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

package org.apache.isis.metamodel.specloader.validator;

import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoaderDefault;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "of")
public class MetaModelValidatorVisiting extends MetaModelValidatorAbstract {

    // -- INTERFACES
    
    @FunctionalInterface
    public interface Visitor {
        /**
         * @return <tt>true</tt> continue visiting specs.
         */
        boolean visit(ObjectSpecification objectSpec, MetaModelValidator validator);
    }
    
    public interface SummarizingVisitor extends Visitor {
        void summarize(MetaModelValidator validator);
    }
    
    // -- IMPLEMENTATION
    
    @NonNull private final Visitor visitor;
    
    @Override
    public void collectFailuresInto(@NonNull ValidationFailures validationFailures) {
        validateAll();
        summarize();
        super.collectFailuresInto(validationFailures);
    }

    private void validateAll() {

        val specLoader = (SpecificationLoaderDefault)super.getMetaModelContext().getSpecificationLoader();
        
        specLoader.forEach(spec->{
            
            if(spec.isManagedBean() || spec.getBeanSort().isUnknown()) {
                return; // exclude managed beans from validation
            }
            
            visitor.visit(spec, this);            
        });
        
    }

    private void summarize() {
        if(visitor instanceof SummarizingVisitor) {
            SummarizingVisitor summarizingVisitor = (SummarizingVisitor) visitor;
            summarizingVisitor.summarize(this);
        }
    }

}

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

import java.util.Collection;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public final class MetaModelValidatorVisiting extends MetaModelValidatorAbstract {

    public interface Visitor {
        /**
         * @return <tt>true</tt> continue visiting specs.
         */
        boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures);
    }
    public interface SummarizingVisitor extends Visitor {
        void summarize(ValidationFailures validationFailures);
    }
    
    private final Visitor visitor;
    public MetaModelValidatorVisiting(final Visitor visitor) {
        this.visitor = visitor;
    }
        
    @Override
    public final void validate(ValidationFailures validationFailures) {
        final Collection<ObjectSpecification> objectSpecs = specificationLoader.allSpecifications();
        for (final ObjectSpecification objSpec : objectSpecs) {
            if(!visitor.visit(objSpec, validationFailures)) {
                break;
            }
        }
        if(visitor instanceof SummarizingVisitor) {
            SummarizingVisitor summarizingVisitor = (SummarizingVisitor) visitor;
            summarizingVisitor.summarize(validationFailures);
        }
    }
    
}

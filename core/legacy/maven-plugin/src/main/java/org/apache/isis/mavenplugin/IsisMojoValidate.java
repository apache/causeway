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
package org.apache.isis.mavenplugin;

import java.util.Collection;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.runtime.system.context.IsisContext;

@Mojo(
        name = "validate",
        defaultPhase = LifecyclePhase.TEST,
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresDependencyCollection = ResolutionScope.COMPILE
        )
public class IsisMojoValidate extends IsisMojoAbstract {

    private final MetaModelProcessor metaModelProcessor;

    protected IsisMojoValidate() {
        this.metaModelProcessor = new ValidateMetaModelProcessor();
    }

    @Override
    protected void doExecute(
            final ContextForMojo context)
                    throws MojoFailureException {
        
        metaModelProcessor.process(context, IsisContext.getSpecificationLoader());
    }

    static class ValidateMetaModelProcessor implements MetaModelProcessor {


        @Override
        public void process(
                final Context context, 
                final SpecificationLoader specificationLoader)
                        throws MojoFailureException {

            final ValidationFailures validationFailures = specificationLoader.validate();

            final Collection<ObjectSpecification> objectSpecifications = specificationLoader.currentSpecifications();
            for (ObjectSpecification objectSpecification : objectSpecifications) {
                context.getLog().debug("loaded: " + objectSpecification.getFullIdentifier());
            }
            if (validationFailures.occurred()) {
                context.throwFailureException(validationFailures.getNumberOfMessages() + " problems found.", validationFailures.getMessages());
            }
        }
    }

}
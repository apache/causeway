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


package org.apache.isis.runtime.transaction.facetdecorator;

import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecoratorAbstract;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionClearFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.metamodel.facets.properties.modify.PropertySetterFacet;


public abstract class TransactionFacetDecoratorAbstract  extends FacetDecoratorAbstract implements TransactionFacetDecorator {

    private IsisConfiguration configuration;

    public TransactionFacetDecoratorAbstract(IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    protected IsisConfiguration getConfiguration() {
        return configuration;
    }

    public Class<? extends Facet>[] getFacetTypes() {
        return new Class[] { 
                ActionInvocationFacet.class,
                PropertyClearFacet.class, 
                PropertySetterFacet.class,  
                CollectionAddToFacet.class, 
                CollectionRemoveFromFacet.class, 
                CollectionClearFacet.class 
            };
    }
}


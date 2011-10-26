/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.Rel;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.MemberType;

import com.google.common.base.Strings;

public abstract class AbstractTypeFeatureReprBuilder<R extends ReprRendererAbstract<R, SpecAndFeature<T>>, T extends ObjectFeature> extends ReprRendererAbstract<R, SpecAndFeature<T>> {

    protected ObjectSpecification objectSpecification;
    protected T objectFeature;

    protected ObjectSpecification parentSpec;

    public AbstractTypeFeatureReprBuilder(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    public ObjectSpecification getObjectSpecification() {
        return objectSpecification;
    }
    
    public T getObjectFeature() {
        return objectFeature;
    }

    @Override
    public R with(SpecAndFeature<T> specAndFeature) {
        objectSpecification = specAndFeature.getObjectSpecification();
        objectFeature = specAndFeature.getObjectFeature();
        
        return cast(this);
    }

    public R withParent(ObjectSpecification parentSpec) {
        this.parentSpec = parentSpec;
        return cast(this);
    }

    public JsonRepresentation render() {
        
        addLinkSelfIfRequired();
        addLinkToParentIfProvided();
        
        addPropertiesSpecificToFeature();
        
        addLinksSpecificToFeature();
        putExtensionsSpecificToFeature();

        return representation;
    }

    
    /**
     * Optional hook method.
     */
    protected void addPropertiesSpecificToFeature() {
    }

    /**
     * Mandatory hook method.
     */
    protected abstract void addLinkSelfIfRequired();

    /**
     * Mandatory hook method.
     */
    protected abstract void addLinkToParentIfProvided();

    /**
     * Optional hook method.
     */
    protected void addLinksSpecificToFeature() {
    }

    /**
     * Mandatory hook method.
     */
    protected abstract void putExtensionsSpecificToFeature();

    
    protected void putExtensionsName() {
        String friendlyName = getObjectFeature().getName();
        getExtensions().mapPut("friendlyName", friendlyName);
    }

    protected void putExtensionsDescriptionIfAvailable() {
        String description = getObjectFeature().getDescription();
        if(!Strings.isNullOrEmpty(description)) {
            getExtensions().mapPut("description", description);
        }
    }


}
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


package org.apache.isis.metamodel.examples.facets.namefile;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.metamodel.spec.identifier.Identified;


public class NameFileFacetFactory implements FacetFactory {

    private final NameFileParser nameFileParser;
    
    public NameFileFacetFactory() {
        nameFileParser = new NameFileParser();
        try {
            nameFileParser.parse();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public ObjectFeatureType[] getFeatureTypes() {
        return ObjectFeatureType.EVERYTHING_BUT_PARAMETERS;
    }

    
    /**
     * Simply attaches a {@link NameFileFacet}.
     */
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        return FacetUtil.addFacet(create(cls, holder));
    }

    private NameFileFacet create(final Class<?> cls, FacetHolder holder) {
        String memberNameInFile = nameFileParser.getName(cls);
        return memberNameInFile!=null?new NameFileFacet(holder, memberNameInFile): null;
    }
    
    /**
     * Simply attaches a {@link NameFileFacet}.
     */
    public boolean process(final Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
    	if (!(holder instanceof Identified)) {
    		return false;
    	}
		Identified identifiedHolder = (Identified) holder;
        Class<?> declaringClass = method.getDeclaringClass();
        String memberName = identifiedHolder.getIdentifier().getMemberName();
        return FacetUtil.addFacet(create(declaringClass, memberName, holder));
    }

    private NameFileFacet create(final Class<?> declaringClass, final String memberName, FacetHolder holder) {
        String memberNameInFile = nameFileParser.getMemberName(declaringClass, memberName);
        return memberNameInFile!=null?new NameFileFacet(holder, memberNameInFile): null;
    }


    public boolean processParams(final Method method, final int paramNum, final FacetHolder holder) {
        // nothing to do
        return false;
    }


}

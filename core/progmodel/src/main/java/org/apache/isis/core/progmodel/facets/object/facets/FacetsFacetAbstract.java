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


package org.apache.isis.core.progmodel.facets.object.facets;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceFactory;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetAbstract;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetHolder;


public abstract class FacetsFacetAbstract extends FacetAbstract implements FacetsFacet {

    public static Class<? extends Facet> type() {
        return FacetsFacet.class;
    }

    private final Class<? extends FacetFactory>[] facetFactories;

	public FacetsFacetAbstract(final String[] names, final Class<?>[] classes, final FacetHolder holder) {
        super(type(), holder, false);
        final List<Class<? extends FacetFactory>> facetFactories = new ArrayList<Class<? extends FacetFactory>>();
        for (int i = 0; i < names.length; i++) {
            final Class<? extends FacetFactory> facetFactory = facetFactoryOrNull(names[i]);
            if (facetFactory != null) {
                facetFactories.add(facetFactory);
            }
        }
        for (int i = 0; i < classes.length; i++) {
            final Class<? extends FacetFactory> facetFactory = facetFactoryOrNull(classes[i]);
            if (facetFactory != null) {
                facetFactories.add(facetFactory);
            }
        }
        this.facetFactories = asArray(facetFactories);
    }

	@SuppressWarnings("unchecked")
	private Class<? extends FacetFactory>[] asArray(
			final List<Class<? extends FacetFactory>> facetFactories) {
		return (Class<? extends FacetFactory>[]) facetFactories.toArray(new Class[] {});
	}

    public Class<? extends FacetFactory>[] facetFactories() {
        return facetFactories;
    }

    private Class<? extends FacetFactory> facetFactoryOrNull(final String classCandidateName) {
        if (classCandidateName == null) {
            return null;
        }
        Class<?> classCandidate = null;
        try {
        	classCandidate = InstanceFactory.loadClass(classCandidateName);
            return facetFactoryOrNull(classCandidate);
        } catch (final IsisException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
	private Class<? extends FacetFactory> facetFactoryOrNull(final Class classCandidate) {
        if (classCandidate == null) {
            return null;
        }
        return FacetFactory.class.isAssignableFrom(classCandidate) ? classCandidate : null;
    }

}

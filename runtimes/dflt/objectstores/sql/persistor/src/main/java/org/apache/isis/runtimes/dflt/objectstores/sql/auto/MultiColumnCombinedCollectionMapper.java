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


/**
 * 
 */
package org.apache.isis.runtimes.dflt.objectstores.sql.auto;

import org.apache.isis.runtimes.dflt.objectstores.sql.FieldMappingLookup;
import org.apache.isis.runtimes.dflt.objectstores.sql.ObjectMappingLookup;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationImpl;

/**
 * Used to map 1-to-many collections by creating, in the child table, 1 column per parent collection.
 * The column is named by combining the final part of the parent class name and the collection variable name. 
 * 
 * @author Kevin
 */
public class MultiColumnCombinedCollectionMapper extends
		CombinedCollectionMapper {

	public MultiColumnCombinedCollectionMapper(
			ObjectAssociation objectAssociation,
			String parameterBase, FieldMappingLookup lookup,
			ObjectMappingLookup objectMapperLookup) {
		super(objectAssociation, parameterBase, lookup, objectMapperLookup);
	}

	@Override
    protected String determineColumnName(ObjectAssociation objectAssociation){
	    if (objectAssociation instanceof OneToManyAssociationImpl){
	    	OneToManyAssociationImpl fkAssoc = (OneToManyAssociationImpl) objectAssociation;
	    	FacetedMethod peer = fkAssoc.getFacetedMethod();
	    	String fullClassName = peer.getIdentifier().getClassName();
	    	int lastPos = fullClassName.lastIndexOf('.');
	    	return fullClassName.substring(lastPos+1)+"_"+fkAssoc.getId();
	    } else {
	    	return  objectAssociation.getSpecification().getShortIdentifier();
	    }
	}
}

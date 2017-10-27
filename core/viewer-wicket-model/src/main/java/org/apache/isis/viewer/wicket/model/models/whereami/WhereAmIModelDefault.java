/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.isis.viewer.wicket.model.models.whereami;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.isis.core.metamodel.util.pchain.ParentChain;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

class WhereAmIModelDefault implements WhereAmIModel {

	private final List<Object> chainOfParents = new ArrayList<>();
	private final EntityModel endOfChain;
	
	public WhereAmIModelDefault(EntityModel endOfChain) {
		this.endOfChain = endOfChain;
		
		final Object startPojo = endOfChain.getObject().getObject();

		ParentChain.caching()
		.streamReversedParentChainOf(startPojo)
		.forEach(chainOfParents::add);
	}
	
	@Override
	public EntityModel getEndOfChain() {
		return endOfChain;
	}
	
	@Override
	public boolean isShowWhereAmI() {
		return !chainOfParents.isEmpty();
	}

	@Override
	public Stream<EntityModel> streamParentChain() {
		return chainOfParents.stream()
		.map(this::toEntityModel);
	}
	
	// -- HELPER

	private EntityModel toEntityModel(Object domainObject) {
		return new EntityModel(
				endOfChain.getPersistenceSession()
				.adapterFor(domainObject)	);
	}
	
}

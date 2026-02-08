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
package org.apache.causeway.core.metamodel.spec;

import java.util.Optional;

import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;

/**
 * Some times types end up in the meta-model unexpected. 
 * The {@link IntrospectionTrigger} helps find the cause for why a specific type got introspected.
 * 
 * @since 4.0
 */
public record IntrospectionTrigger(
		IntrospectionCause introspectionCause,
		Optional<ObjectSpecification> triggerSpec) {
	
	public enum IntrospectionCause {
		/**
		 * originating directly from {@link CausewayBeanTypeRegistry}
		 */
		BEAN_CANDIDATE,
		TYPE_HIERARCHY,
		PROPERTY,
		ACTION_RETURN,
		ACTION_PARAM
	}
	
	private final static IntrospectionTrigger BEAN_CANDIDATE = new IntrospectionTrigger(
			IntrospectionCause.BEAN_CANDIDATE, Optional.empty());
	
	public static IntrospectionTrigger beanCandidate() {
		return BEAN_CANDIDATE;
	}
	
	/**
	 * @deprecated refactoring helper
	 */
	@Deprecated
	public static IntrospectionTrigger dummy() {
		return BEAN_CANDIDATE;
	}

}

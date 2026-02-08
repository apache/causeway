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
import java.util.function.Function;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;

/**
 * Some times types end up in the meta-model unexpected. 
 * The {@link IntrospectionTrigger} helps find the cause for why a specific type got introspected.
 * 
 * @since 4.0
 */
public record IntrospectionTrigger(
		IntrospectionCause introspectionCause,
		Optional<Class<?>> triggerClass) {
	
	public enum IntrospectionCause {
		/**
		 * originating directly from {@link CausewayBeanTypeRegistry}
		 */
		BEAN_CANDIDATE,
		/**
		 * lookup of class directly or via {@link LogicalType}
		 */
		CLASS_LOOKUP,
		/**
		 * action result (introspection of the action method return)
		 */
		ACTION_RETURN,
		/**
		 * association (introspection of the getter method return)
		 */
		GETTER,
		
		MIXIN,
		/**
		 * class directly reloaded (includes purging of caches)
		 */
		RELOAD,
		/**
		 * used during reloading, to purge the class-hierarchy from any caches 
		 */
		TEMPORARY,
		
		TYPE_HIERARCHY,
		/**
		 * originating from {@link CausewayBeanTypeRegistry} categorized as domain service
		 */
		SERVICE,
		/**
		 * originating from {@link CausewayBeanTypeRegistry} categorized as domain entity
		 */
		ENTITY,
		/**
		 * originating from {@link CausewayBeanTypeRegistry} categorized as view-model
		 */
		VIEWMODEL,
		/**
		 * originating from registered {@link ValueSemanticsProvider}
		 */
		VALUE_TYPE,
		
		@Deprecated // not used yet - why?
		ACTION_PARAM
	}
	
	private final static IntrospectionTrigger BEAN_CANDIDATE = new IntrospectionTrigger(
			IntrospectionCause.BEAN_CANDIDATE, Optional.empty());
	private final static IntrospectionTrigger TEMPORARY = new IntrospectionTrigger(
			IntrospectionCause.TEMPORARY, Optional.empty());

	// -- FACTORIES
	
	public static IntrospectionTrigger beanCandidate() {
		return BEAN_CANDIDATE;
	}
	
	public static IntrospectionTrigger typeHierarchy(@NonNull Class<?> cls) {
		return new IntrospectionTrigger(IntrospectionCause.TYPE_HIERARCHY, Optional.of(cls));
	}
	
	public static IntrospectionTrigger valueType(@NonNull Class<?> cls) {
		return new IntrospectionTrigger(IntrospectionCause.VALUE_TYPE, Optional.of(cls));
	}
	
	public static IntrospectionTrigger lookup(@NonNull Class<?> cls) {
		return new IntrospectionTrigger(IntrospectionCause.CLASS_LOOKUP, Optional.of(cls));
	}
	public static IntrospectionTrigger lookup(@NonNull String logicalTypeNameNotUsed) {
		return new IntrospectionTrigger(IntrospectionCause.CLASS_LOOKUP, Optional.empty());
	}
	
	public static IntrospectionTrigger actionReturn(Class<?> cls) {
		return new IntrospectionTrigger(IntrospectionCause.ACTION_RETURN, Optional.of(cls));
	}
	
	public static IntrospectionTrigger getter(Class<?> cls) {
		return new IntrospectionTrigger(IntrospectionCause.GETTER, Optional.of(cls));
	}
	
	public static IntrospectionTrigger mixin(Class<?> mixinType) {
		return new IntrospectionTrigger(IntrospectionCause.MIXIN, Optional.of(mixinType));
	}
	
	public static IntrospectionTrigger service(Class<?> cls) {
		return new IntrospectionTrigger(IntrospectionCause.SERVICE, Optional.of(cls));
	}
	
	public static IntrospectionTrigger entity(Class<?> cls) {
		return new IntrospectionTrigger(IntrospectionCause.SERVICE, Optional.of(cls));
	}
	
	public static IntrospectionTrigger viewmodel(Class<?> cls) {
		return new IntrospectionTrigger(IntrospectionCause.SERVICE, Optional.of(cls));
	}
	
	public static IntrospectionTrigger temporary(Class<?> domainTypeNotUsed) {
		return TEMPORARY;
	}
	
	public static IntrospectionTrigger reload(Class<?> cls) {
		return new IntrospectionTrigger(IntrospectionCause.RELOAD, Optional.of(cls));
	}
	
	/**
	 * @deprecated refactoring helper
	 */
	@Deprecated
	public static Function<Class<?>, IntrospectionTrigger> dummy() {
		return null;
	}

}

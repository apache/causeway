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
package org.apache.causeway.applib.services.appfeat;

/**
 * The various viewer implementations will individually honor any filters registered with Spring,
 * based on a matching qualifier ('graphql', 'restful', etc.). 
 *
 * <p>All filters that match a qualifier are consulted until any one rejects the {@link ApplicationFeature}.
 *    
 * <p>In no filters match a qualifier, all {@link ApplicationFeature} are accepted.
 *
 * @since 4.0 {@index}
 */
@FunctionalInterface
public interface ApplicationFeatureFilter {

	public final static String GRAPHQL_VIEWER = "graphql";
	public final static String RESTFUL_VIEWER = "restful";
	public final static String WICKET_VIEWER = "wicket";

	/**
	 * Whether to include given {@link ApplicationFeature}.
	 */
	boolean filter(ApplicationFeature feature);
	
}

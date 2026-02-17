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
package org.apache.causeway.extensions.secman.applib.permission.dom;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;

import lombok.experimental.UtilityClass;

@UtilityClass
class _WeavingWorkaround {
	
	/**
	 * This former inlined code snipped was found to break static-weaving, when running the build on JDK 21 or 25.
	 * Only JDK 17 worked.
	 */
    String sort(ApplicationPermission perm) {
    	var e = perm.getFeatureSort() != ApplicationFeatureSort.MEMBER
    			? perm.getFeatureSort()
                : perm.getMemberSort().orElse(null);
    	return e != null ? e.name(): null;
    }

}

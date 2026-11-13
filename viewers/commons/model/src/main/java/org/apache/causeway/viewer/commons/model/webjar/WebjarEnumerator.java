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
package org.apache.causeway.viewer.commons.model.webjar;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.springframework.util.StringUtils;

/**
 * Utility to scan webjar version strings dynamically from class-path under {@code META-INF/resources/webjars}.
 *
 * <p>Removes the need to hard code version strings for webjar resource lookups.
 *
 * @since 3.6.0, 4.0
 */
public record WebjarEnumerator() {

    private final static _Lazy<Map<String, WebjarResource>> WEBJARS = _Lazy.threadSafe(WebjarEnumerator::scanClassPath);

    public static Optional<WebjarResource> lookup(final String path) {
        return Optional.ofNullable(WEBJARS.get().get(path));
    }

    public static WebjarResource lookupElseFail(final String path) {
        return lookup(path).orElseThrow(()->_Exceptions
                .noSuchElement("no webjar found on class-path under META-INF/resources/webjars matching sub-path '%s'", path));
    }

    // datatables/2.3.6
    // npm/bootstrap-select/1.14.0-beta3
    // npm/inputmask/5.0.9
    // jquery-ui/1.14.1
    public record WebjarResource(
    	/**
    	 * default: META-INF/resources/webjars
    	 */
		String location,
		/**
		 * path part after location and before version
		 */
        String path,
        String version) {

		boolean isValid() {
			return StringUtils.hasText(path)
					&& StringUtils.hasText(version);
		}
    }

    // -- HELPER

    private static Map<String, WebjarResource> scanClassPath() {
    	var acceptedPaths = List.of(
    			"META-INF/resources/webjars"
    			// adds additional lookup path, for non standard conforming Vega webjars, could be removed in the future
    			,"META-INF/resources/_static"
    			);

    	var map = new ResourceProcessor(acceptedPaths)
    			.processAll();
//debug
//		map.forEach((k, v)->{
//        	System.out.println(k + ": " + v);
//        });

        return map;
    }

}

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
package org.apache.isis.viewer.wicket.ui.util;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverConfig;
import de.agilecoders.wicket.jquery.IKey;

public class ExtendedPopoverConfig extends PopoverConfig {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * Overflow constraint boundary of the popover. 
     * Accepts the values of 'viewport', 'window', 'scrollParent', or an HTMLElement reference (JavaScript only). 
     * For more information refer to Popper.js's preventOverflow docs.
     */
    private static final IKey<String> Boundary = newKey("boundary", PopoverBoundary.scrollParent.name());
    
    public enum PopoverBoundary {
    	viewport, window, scrollParent
    }
    
    public ExtendedPopoverConfig withBoundary(PopoverBoundary boundary) {
    	return withBoundary(boundary.name());
    }
    
    public ExtendedPopoverConfig withBoundary(String boundary) {
    	put(Boundary, boundary);
    	return this;
    }
}

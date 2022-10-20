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
package org.apache.causeway.applib.layout.grid.bootstrap;

/**
 * @since 1.x {@index}
 */
public abstract class BSClearFix extends BSRowContent {

    private static final long serialVersionUID = 1L;

    public enum CssDisplay {
    	NONE,
        BLOCK,
        INLINE,
        INLINE_BLOCK,
        GRID,
        TABLE,
        TABLE_CELL,
        TABLE_ROW,
        FLEX,
        INLINE_FLEX;

        public String toCssClassFragment() {
            return name().toLowerCase().replace('_', '-');
        }
    }

	protected String getDisplayFragment(final CssDisplay displayValue, final Size breakpoint) {
		switch(breakpoint) {
		case XS:
			return String.format("d-%s", displayValue.toCssClassFragment());
		default:
			return String.format("d-%s-%s", breakpoint.toCssClassFragment(), displayValue.toCssClassFragment());
		}
	}

    public abstract String toCssClass();
}

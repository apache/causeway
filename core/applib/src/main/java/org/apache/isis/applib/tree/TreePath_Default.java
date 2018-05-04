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
package org.apache.isis.applib.tree;

import java.util.Objects;

/**
 * Package private mixin for TreePath.
 */
class TreePath_Default implements TreePath {

	private static final long serialVersionUID = 530511373409525896L;
	private final int[] canonicalPath;

	TreePath_Default(int[] canonicalPath) {
		Objects.requireNonNull(canonicalPath, "canonicalPath is required");
		if(canonicalPath.length<1) {
			throw new IllegalArgumentException("canonicalPath must not be empty");
		}
		this.canonicalPath = canonicalPath;
	}

	@Override
	public TreePath append(int indexWithinSiblings) {
		int[] newCanonicalPath = new int[canonicalPath.length+1];
		System.arraycopy(canonicalPath, 0, newCanonicalPath, 0, canonicalPath.length);
		newCanonicalPath[canonicalPath.length] = indexWithinSiblings;
		return new TreePath_Default(newCanonicalPath);
	}
	
}

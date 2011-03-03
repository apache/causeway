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

package org.apache.isis.alternatives.objectstore.sql;

public class IntegerPrimaryKey implements PrimaryKey {
	private static final long serialVersionUID = 1L;
	private final int primaryKey;

	public IntegerPrimaryKey(final int primaryKey) {
		this.primaryKey = primaryKey;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof IntegerPrimaryKey) {
			IntegerPrimaryKey o = ((IntegerPrimaryKey) obj);
			return primaryKey == o.primaryKey;
		}
		return false;
	}

	@Override
	public String stringValue() {
		return "" + primaryKey;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 37 * hash + primaryKey;
		return hash;
	}

	@Override
	public String toString() {
		return "" + primaryKey;
	}

	public int intValue() {
		return primaryKey;
	}

	@Override
	public Object naturalValue() {
		return intValue();
	}

}

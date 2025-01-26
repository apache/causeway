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
package org.apache.causeway.extensions.tabular.pdf.factory.internal;

/**
 * Data container for HTML ordered list elements.
 */
class HTMLListNode {

	/**
	 * <p>
	 * Element's current ordering number (e.g third element in the current list)
	 * </p>
	 */
	private int orderingNumber;

	/**
	 * <p>
	 * Element's whole ordering number value (e.g 1.1.2.1)
	 * </p>
	 */
	private String value;

	public int getOrderingNumber() {
		return orderingNumber;
	}

	public void setOrderingNumber(final int orderingNumber) {
		this.orderingNumber = orderingNumber;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public HTMLListNode(final int orderingNumber, final String value) {
		this.orderingNumber = orderingNumber;
		this.value = value;
	}

}

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

package org.apache.isis.applib.util;

import org.apache.isis.applib.internal.exceptions._Exceptions;
import org.apache.isis.applib.util.ObjectContracts.ObjectContract;
import org.apache.isis.applib.util.ObjectContracts.ToStringEvaluator;

/**
 * Package private default implementation for ObjectContract.
 *  
 * @since 2.0.0
 */
class ObjectContract_Impl<T> implements ObjectContract<T> {

	@Override
	public int compare(Object obj, Object other) {
		// TODO Auto-generated method stub
		_Exceptions.throwNotImplemented();
		return 0;
	}

	@Override
	public boolean equals(Object obj, Object other) {
		// TODO Auto-generated method stub
		_Exceptions.throwNotImplemented();
		return false;
	}

	@Override
	public int hashCode(Object obj) {
		// TODO Auto-generated method stub
		_Exceptions.throwNotImplemented();
		return 0;
	}

	@Override
	public String toString(Object obj) {
		// TODO Auto-generated method stub
		_Exceptions.throwNotImplemented();
		return null;
	}

	@Override
	public ObjectContract<T> withToStringEvaluators(ToStringEvaluator... evaluators) {
		// TODO Auto-generated method stub
		_Exceptions.throwNotImplemented();
		return null;
	}

}

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


package org.apache.isis.defaults.bytecode.future;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import net.sf.cglib.proxy.Enhancer;

import org.apache.isis.core.commons.futures.FutureFactory;
import org.apache.isis.core.commons.futures.FutureResultFactory;
import org.apache.isis.core.commons.lang.ArrayUtils;
import org.apache.isis.defaults.bytecode.future.internal.EvaluatingMethodInterceptor;

public class FutureFactoryCglib implements FutureFactory {

	/**
	 * Cache of Enhancers, lazy populated.
	 */
	private final Map<FutureResultFactory<?>, Enhancer> enhancerByResultFactory = 
		new HashMap<FutureResultFactory<?>, Enhancer>();
	
	public FutureFactoryCglib() {
		// nothing to do
	}

	public <T> T createFuture(FutureResultFactory<T> resultFactory) {
		return newInstance(resultFactory);
	}

	@SuppressWarnings("unchecked")
	private <T> T newInstance(FutureResultFactory<T> resultFactory) {
		Enhancer enhancer = lookupOrCreateEnhancerFor(resultFactory);
		return (T) enhancer.create();
	}
	
	private <T> Enhancer lookupOrCreateEnhancerFor(FutureResultFactory<T> resultFactory) {
		
		Enhancer enhancer = enhancerByResultFactory.get(resultFactory);
		Class<T> cls = resultFactory.getResultClass();
		if (enhancer == null) {
			enhancer = new Enhancer();
			enhancer.setSuperclass(cls);
			enhancer.setInterfaces(ArrayUtils.combine(
					cls.getInterfaces(),
					new Class<?>[] { Future.class }));
			enhancer.setCallback(new EvaluatingMethodInterceptor<T>(resultFactory));
			enhancerByResultFactory.put(resultFactory, enhancer);
		}
		return enhancer;
	}

}

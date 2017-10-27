/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
package org.apache.isis.core.metamodel.util.pchain;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Parent;
import org.apache.isis.core.commons.reflection.Reflect;

/**
 * Represents a unidirectional linked ordered set of Pojos (chain), where the chain 
 * starts at startNode. Each subsequent node is linked via de-referencing a 
 * singular field (or no-arg method) that is annotated with {@code @Parent}.
 * <br/>
 * 
 * startNode --@Parent--&gt; node2 --@Parent--&gt; node3 ...
 * 
 * @author ahuber@apache.org
 *
 */
public interface ParentChain {
	
	static ParentChain simple() {
		return new SimpleParentChain();
	}
	
	static ParentChain caching() {
		return new CachingParentChain();
	}
	
	public Object parentOf(Object node);
	
	static boolean providesParent(Method m) {
		if(!Reflect.isNoArg(m))
			return false;
		if(!Reflect.isPublic(m))
			return false;
		if(Reflect.isVoid(m)) 
			return false;
		if(Reflect.isPrimitive(m.getReturnType())) 
			return false;
		
		if(m.getName().equals("parent"))
			return true;
		
		if(m.isAnnotationPresent(Parent.class))
			return true;
		
		return false;
	}

	default Stream<Object> streamParentChainOf(Object startNode){
		final Set<Object> chain = new LinkedHashSet<>();
		
		chain.add(startNode);
		
		Object next = startNode;
		
		while((next = parentOf(next))!=null) {
			final boolean doContinue = chain.add(next);
			if(!doContinue)
				break;
		}
		
		return chain.stream().skip(1);
	}
	
	default Stream<Object> streamReversedParentChainOf(Object startNode){
		final LinkedList<Object> reverseChain = new LinkedList<Object>();
		
		streamParentChainOf(startNode)
		.forEach(reverseChain::addFirst);
		
		return reverseChain.stream();
	}
	
	
}

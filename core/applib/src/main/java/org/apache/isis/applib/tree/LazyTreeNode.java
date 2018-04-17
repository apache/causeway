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
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.internal.base._Lazy;

@Value(semanticsProviderName="org.apache.isis.core.metamodel.facets.value.treenode.TreeNodeValueSemanticsProvider")
public class LazyTreeNode<T> implements TreeNode<T> {
	
	private final T value;
	private final Class<? extends TreeAdapter<T>> treeAdapterClass;
	private final _Lazy<TreeAdapter<T>> treeAdapter = _Lazy.of(this::newTreeAdapter);
	
	public static <T> TreeNode<T> of(T value, Class<? extends TreeAdapter<T>> treeAdapterClass) {
		return new LazyTreeNode<T>(value, treeAdapterClass);
	}
	
	protected LazyTreeNode(T value, Class<? extends TreeAdapter<T>> treeAdapterClass) {
		this.value = Objects.requireNonNull(value);
		this.treeAdapterClass = Objects.requireNonNull(treeAdapterClass);
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public TreeNode<T> getParentIfAny() {
		return treeAdapter().parentOf(getValue())
				.map(this::toTreeNode)
				.orElse(null)
				;
	}

	@Override
	public int getChildCount() {
		return treeAdapter().childCountOf(value);
	}

	@Override
	public Stream<TreeNode<T>> streamChildren() {
		if(isLeaf()) {
			return Stream.empty();
		}
		return treeAdapter().childrenOf(value)
				.map(this::toTreeNode)
				;
	}

	@Override
	public Class<? extends TreeAdapter<T>> getTreeAdapterClass() {
		return treeAdapterClass;
	}
	
	// -- HELPER
	
	private TreeAdapter<T> newTreeAdapter() {
		try {
			return treeAdapterClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException(
					String.format("failed to instantiate TreeAdapter '%s'", treeAdapterClass.getName()), e);
		}
	}

	private TreeAdapter<T> treeAdapter() {
		return treeAdapter.get();
	}
	
	private TreeNode<T> toTreeNode(T value){
		return of(value, getTreeAdapterClass());
	}

	
}

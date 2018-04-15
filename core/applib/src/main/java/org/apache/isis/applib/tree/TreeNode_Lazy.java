package org.apache.isis.applib.tree;

import java.util.Objects;
import java.util.stream.Stream;

class TreeNode_Lazy<T> implements TreeNode<T> {
	
	private final T value;
	private final TreeAdapter<T> treeAdapter;
	
	static <T> TreeNode_Lazy<T> of(T value, TreeAdapter<T> treeAdapter) {
		Objects.requireNonNull(value);
		Objects.requireNonNull(treeAdapter);
		return new TreeNode_Lazy<T>(value, treeAdapter);
	}

	private TreeNode_Lazy(T value, TreeAdapter<T> treeAdapter) {
		this.value = value;
		this.treeAdapter = treeAdapter;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public TreeNode<T> getParentIfAny() {
		return treeAdapter.parentOf(getValue())
				.map(this::toTreeNode)
				.orElse(null)
				;
	}

	@Override
	public int getChildCount() {
		return treeAdapter.childCountOf(value);
	}

	@Override
	public Stream<TreeNode<T>> streamChildren() {
		if(isLeaf()) {
			return Stream.empty();
		}
		return treeAdapter.childrenOf(value)
				.map(this::toTreeNode)
				;
	}

	// -- HELPER
	
	private TreeNode<T> toTreeNode(T value){
		return of(value, treeAdapter);
	}


}

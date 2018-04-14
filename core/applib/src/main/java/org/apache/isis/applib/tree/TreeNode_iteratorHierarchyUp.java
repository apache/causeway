package org.apache.isis.applib.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;

class TreeNode_iteratorHierarchyUp<T> implements Iterator<TreeNode<T>> {

	private TreeNode<T> next;

	TreeNode_iteratorHierarchyUp(TreeNode<T> treeNode) {
		next = treeNode;
	}

	@Override
	public boolean hasNext() {
		return next!=null;
	}

	@Override
	public TreeNode<T> next() {
		if(next==null) {
			throw new NoSuchElementException("Iterator has run out of elements.");
		}
		final TreeNode<T> result = next; 
		next = fetchNext(next);		
		return result;
	}
	
	// -- HELPER

	private TreeNode<T> fetchNext(TreeNode<T> current) {
		return current.getParent().orElse(null);
	}

}

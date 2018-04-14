package org.apache.isis.applib.tree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

class TreeNode_iteratorBreadthFirst<T> implements Iterator<TreeNode<T>> {

	private Deque<TreeNode<T>> deque = new ArrayDeque<>();
	private TreeNode<T> next;

	TreeNode_iteratorBreadthFirst(TreeNode<T> treeNode) {
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
		if(!current.isLeaf()) {
			current.streamChildren()
			.forEach(deque::offerLast);
		}
		return deque.pollFirst();
	}

}

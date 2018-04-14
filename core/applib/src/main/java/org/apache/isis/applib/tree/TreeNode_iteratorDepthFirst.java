package org.apache.isis.applib.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

class TreeNode_iteratorDepthFirst<T> implements Iterator<TreeNode<T>> {

	private Stack<TreeNode<T>> stack = new Stack<>();
	private TreeNode<T> next;

	TreeNode_iteratorDepthFirst(TreeNode<T> treeNode) {
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
			pushChildrenToStackInReverseOrder(current);
		}
		return stack.isEmpty() ? null : stack.pop();
	}

	private Stack<TreeNode<T>> fifo = new Stack<>(); // declared as field only to reduce heap pollution
	
	private void pushChildrenToStackInReverseOrder(TreeNode<T> node) {
		
		node.streamChildren()
		.forEach(fifo::push);
		
		while(!fifo.isEmpty()) {
			stack.push(fifo.pop());
		}
	}
	

}

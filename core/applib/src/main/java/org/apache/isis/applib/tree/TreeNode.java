package org.apache.isis.applib.tree;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.isis.applib.annotation.ViewModel;

@ViewModel
public interface TreeNode<T> {
	
	// -- VALUE
	
	public T getValue();
	
	// -- PARENT
	
	public TreeNode<T> getParentIfAny();
	
	// -- CHILDREN
	
	public int getChildCount();

	public Stream<TreeNode<T>> streamChildren();
	
	// -- BASIC PREDICATES
	
	public default boolean isRoot() {
		return getParentIfAny() == null;
	}
	
	public default boolean isLeaf() {
		return getChildCount() == 0;
	}

	// -- CONSTRUCTION
	
	public static <T> TreeNode<T> of(T node, TreeAdapter<T> treeAdapter) {
		return TreeNode_Lazy.of(node, treeAdapter);
	}
	
	// -- PARENT NODE ITERATION
	
	public default Iterator<TreeNode<T>> iteratorHierarchyUp(){
		return new TreeNode_iteratorHierarchyUp<>(this);
	}
	
	// -- PARENT NODE STREAMING
	
	public default Stream<TreeNode<T>> streamHierarchyUp(){
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(iteratorHierarchyUp(), Spliterator.ORDERED), 
				false); // not parallel
	}
	
	// -- CHILD NODE ITERATION
	
	public default Iterator<TreeNode<T>> iteratorDepthFirst(){
		return new TreeNode_iteratorDepthFirst<>(this);
	}
	
	public default Iterator<TreeNode<T>> iteratorBreadthFirst(){
		return new TreeNode_iteratorBreadthFirst<>(this);
	}
	
	// -- CHILD NODE STREAMING
	
	public default Stream<TreeNode<T>> streamDepthFirst(){
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(iteratorDepthFirst(), Spliterator.ORDERED), 
				false); // not parallel
	}
	
	public default Stream<TreeNode<T>> streamBreadthFirst(){
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(iteratorBreadthFirst(), Spliterator.ORDERED), 
				false); // not parallel
	}
	
}

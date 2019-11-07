<span class="version-reference">(since 2.0.0-M1)</span>

This is a lazy tree. Tree nodes are either domain-objects or domain-views, which are generated on the fly by a specified [TreeAdapter](${SOURCES_ISIS}/org/apache/isis/applib/tree/TreeAdapter.java). 

```java
public interface TreeAdapter<T> {

	public Optional<T> parentOf(T value);
	
	public int childCountOf(T value);
	
	public Stream<T> childrenOf(T value);
	
}
```

We create a tree by starting with it's root node and providing a `FileSystemTreeAdapter` that implements `TreeAdapter`.

```java
public TreeNode<FileNode> createTree() {
	val root = FileNodeFactory.defaultRoot();
	val tree = TreeNode.lazy(root, FileSystemTreeAdapter.class);
	tree.expand(TreePath.of(0)); // expand the root node
	return tree;
}
```
					
See the tree demo [sources](${SOURCES_DEMO}/domainapp/dom/tree).

Also see [open issues](${ISSUES_DEMO}?utf8=✓&q=is%3Aissue+is%3Aopen+tree) with trees.
package org.apache.isis.viewer.wicket.model.models.tree;

import org.apache.isis.applib.tree.TreeNode;
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;

public class TreeViewModel extends ModelAbstract<TreeNode<Object>> {

	private static final long serialVersionUID = 1L;
	
	private TreeNode<Object> primaryNode;
	
	
	public TreeNode<Object> getPrimaryNode() {
		return primaryNode;
	}

	public void setPrimaryNode(TreeNode<Object> primaryNode) {
		this.primaryNode = primaryNode;
	}

	@Override
	protected TreeNode<Object> load() {
		return primaryNode;
	}
	
	
}

package demoapp.dom.annotations.PropertyLayout.navigable;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.graph.tree.TreeNode;

import lombok.RequiredArgsConstructor;

import demoapp.dom.annotations.PropertyLayout.hidden.PropertyLayoutHiddenVm;

//tag::class[]
@Action(
    semantics = SemanticsOf.SAFE,
    associateWith = "tree"
)
@ActionLayout(cssClassFa="fa-tree", position = ActionLayout.Position.PANEL)
@RequiredArgsConstructor
public class FileNodeVm_returnsTree {

    private final FileNodeVm fileNodeVm;

    public TreeNode<FileNodeVm> act(){
        return fileTreeNodeService.sessionTree();
    }

    @Inject
    FileTreeNodeService fileTreeNodeService;
}
//end::class[]

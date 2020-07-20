package demoapp.dom.annotLayout.PropertyLayout.navigable;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.graph.tree.TreeNode;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(
    semantics = SemanticsOf.SAFE,
    associateWith = "tree"
)
@ActionLayout(cssClassFa="fa-sitemap", position = ActionLayout.Position.PANEL)
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

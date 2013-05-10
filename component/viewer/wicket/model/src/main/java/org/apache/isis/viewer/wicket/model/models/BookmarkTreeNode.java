package org.apache.isis.viewer.wicket.model.models;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public class BookmarkTreeNode implements Serializable {
    
        private static final long serialVersionUID = 1L;
        
        public static final Function<? super BookmarkTreeNode, ? extends PageParameters> AS_PAGE_PARAMETERS = new Function<BookmarkTreeNode, PageParameters>() {
            public PageParameters apply(BookmarkTreeNode node) {
                return node.pageParameters;
            }
        };
        final PageParameters pageParameters;
        private final List<PageParameters> children = Lists.newArrayList();
        
        public BookmarkTreeNode(BookmarkableModel<?> node) {
            this.pageParameters = node.asPageParameters();
        }
        public PageParameters getPageParameters() {
            return pageParameters;
        }
        public List<PageParameters> getChildren() {
            return children;
        }
        public BookmarkTreeNode addChild(BookmarkableModel<?> childModel) {
            return null;
        }
//        public TreeNode exists(BookmarkableModel<?> candidateModel) {
//            
//        }
    }
package org.apache.isis.viewer.wicket.model.links;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.model.LoadableDetachableModel;


public class ListOfLinksModel extends LoadableDetachableModel<List<LinkAndLabel>> {

    private static final long serialVersionUID = 1L;
    
    private final List<LinkAndLabel> links;

    public ListOfLinksModel(List<LinkAndLabel> links) {
        // copy, in case supplied list is a non-serializable guava list using lazy evaluation;
        this.links = Lists.newArrayList(links); 
    }

    @Override
    protected List<LinkAndLabel> load() {
        return links;
    }

}

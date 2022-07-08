package org.apache.isis.viewer.graphql.viewer.source;

import lombok.Data;

import java.util.List;

@Data
public class GQLGenericStructure {

    private final ObjectTypeConstructionHelper constructionHelper;

    public List<String> properties(){
        return constructionHelper.oneToOneAssociationNames();
    }

    public List<String> collections(){
        return constructionHelper.oneToManyAssociationNames();
    }

    public List<String> actions(){
        return constructionHelper.allActionNames();
    }

    public String layoutXml(){
        // TODO: implement
     return "<xml>todo: implement .... </xml>";
    }

}

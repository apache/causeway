package org.apache.isis.viewer.graphql.viewer.source;

import lombok.Data;

import java.util.List;

@Data
public class GQLMetaStructure {

    private final ObjectTypeConstructionHelper constructionHelper;

    public List<String> properties(){
        return constructionHelper.oneToOneAssociationNames();
    }

    public List<String> collections(){
        return constructionHelper.oneToManyAssociationNames();
    }

    public List<String> safeActions(){
        return constructionHelper.safeActionsNames();
    }

    public List<String> idempotentActions(){
        return constructionHelper.idempotentActionNames();
    }
    public List<String> nonIdempotentActions(){
        return constructionHelper.nonIdempotentActionNames();
    }

    public String layoutXml(){
        // TODO: implement
     return "<xml>todo: implement .... </xml>";
    }

}

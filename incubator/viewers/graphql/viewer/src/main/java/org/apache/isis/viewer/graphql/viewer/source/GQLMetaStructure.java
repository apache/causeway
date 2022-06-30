package org.apache.isis.viewer.graphql.viewer.source;

import lombok.Data;

import java.util.List;

@Data
public class GQLMetaStructure {

    private final ObjectTypeDataCollector dataCollector;

    public List<String> properties(){
        return dataCollector.properties();
    }

    public List<String> collections(){
        return dataCollector.collections();
    }

    public List<String> safeActions(){
        return dataCollector.safeActions();
    }

    public List<String> idempotentActions(){
        return dataCollector.idempotentActions();
    }
    public List<String> nonIdempotentActions(){
        return dataCollector.nonIdempotentActionNames();
    }

    public String layoutXml(){
        // TODO: implement
     return "<xml>todo: implement .... </xml>";
    }

}

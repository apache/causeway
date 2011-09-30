package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.applib.RepresentationType;

public abstract class ReprRendererFactoryAbstract implements RendererFactory {

    private final RepresentationType representationType;

    public ReprRendererFactoryAbstract(RepresentationType representationType) {
        this.representationType = representationType;
    }
    
    @Override
    public RepresentationType getRepresentationType() {
        return representationType;
    }


    
}

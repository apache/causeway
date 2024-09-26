package org.apache.causeway.persistence.querydsl.metamodel.refiner;

import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;

import org.apache.causeway.persistence.querydsl.metamodel.facets.AutoCompleteGeneratedQueryFacetFactory;

import org.springframework.stereotype.Component;

@Component
public class AutoCompleteGeneratedQueryMetaModelRefiner implements MetaModelRefiner {

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addFactory(ProgrammingModel.FacetProcessingOrder.E1_MEMBER_MODELLING,
                new AutoCompleteGeneratedQueryFacetFactory(programmingModel.getMetaModelContext()));
    }

    // TODO Add validator?
}

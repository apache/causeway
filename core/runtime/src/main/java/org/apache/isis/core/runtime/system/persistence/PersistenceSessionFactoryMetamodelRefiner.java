package org.apache.isis.core.runtime.system.persistence;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.auditable.AuditableAnnotationInJdoApplibFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.auditable.AuditableMarkerInterfaceInJdoApplibFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.datastoreidentity.JdoDatastoreIdentityAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.discriminator.JdoDiscriminatorAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.query.JdoQueryAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.version.JdoVersionAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.column.BigDecimalDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.column.MandatoryFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.column.MaxLengthDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.notpersistent.JdoNotPersistentAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.primarykey.JdoPrimaryKeyAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.specloader.validator.JdoMetaModelValidator;

public class PersistenceSessionFactoryMetamodelRefiner implements MetaModelRefiner {

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel, IsisConfiguration configuration) {
        programmingModel.addFactory(
                JdoPersistenceCapableAnnotationFacetFactory.class, ProgrammingModel.Position.BEGINNING);
        programmingModel.addFactory(JdoDatastoreIdentityAnnotationFacetFactory.class);

        programmingModel.addFactory(JdoPrimaryKeyAnnotationFacetFactory.class);
        programmingModel.addFactory(JdoNotPersistentAnnotationFacetFactory.class);
        programmingModel.addFactory(JdoDiscriminatorAnnotationFacetFactory.class);
        programmingModel.addFactory(JdoVersionAnnotationFacetFactory.class);

        programmingModel.addFactory(JdoQueryAnnotationFacetFactory.class);

        programmingModel.addFactory(BigDecimalDerivedFromJdoColumnAnnotationFacetFactory.class);
        programmingModel.addFactory(MaxLengthDerivedFromJdoColumnAnnotationFacetFactory.class);
        // must appear after JdoPrimaryKeyAnnotationFacetFactory (above)
        // and also MandatoryFacetOnPropertyMandatoryAnnotationFactory
        // and also PropertyAnnotationFactory
        programmingModel.addFactory(MandatoryFromJdoColumnAnnotationFacetFactory.class);

        programmingModel.addFactory(AuditableAnnotationInJdoApplibFacetFactory.class);
        programmingModel.addFactory(AuditableMarkerInterfaceInJdoApplibFacetFactory.class);
    }

    @Override
    public void refineMetaModelValidator(
            MetaModelValidatorComposite metaModelValidator,
            IsisConfiguration configuration) {
        metaModelValidator.add(new JdoMetaModelValidator());
    }
}

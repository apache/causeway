package net.sf.isiscontrib.bdd.fitnesse.internal.fixtures;

import java.util.Set;

import net.sf.isiscontrib.bdd.fitnesse.internal.AbstractSubsetFixture;

import com.google.common.collect.Sets;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.session.IsisSessionFactory;
import org.apache.isis.viewer.bdd.common.AliasRegistry;

public class CheckSpecificationsLoadedForFitNesse extends AbstractSubsetFixture {

    private static Set<ObjectSpecification> allSpecNames() {
        final Set<ObjectSpecification> specs = Sets.newTreeSet(ObjectSpecification.COMPARATOR_FULLY_QUALIFIED_CLASS_NAME);

        final SpecificationLoader specificationLoader = getSessionFactory().getSpecificationLoader();
        for (final ObjectSpecification ObjectSpecification : specificationLoader.allSpecifications()) {
            specs.add(ObjectSpecification);
        }
        return specs;
    }

    public CheckSpecificationsLoadedForFitNesse(final AliasRegistry aliasesRegistry) {
        super(aliasesRegistry, CheckSpecificationsLoadedForFitNesse.allSpecNames());
    }

    // ///////////////////////////////////////////////////////////////
    // from context
    // ///////////////////////////////////////////////////////////////

    private static IsisSessionFactory getSessionFactory() {
        return IsisContext.getSessionFactory();
    }

}

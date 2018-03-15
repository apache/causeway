package org.apache.isis.progmodels.plugins;

import org.apache.isis.core.metamodel.facets.value.date.DateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.datetime.DateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.time.TimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.timestamp.TimeStampValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelPlugin;

public class ProgrammingModelIsisTimePlugin implements ProgrammingModelPlugin {

	@Override
	public void plugin(FactoryCollector collector) {
		
		collector.addFactory(new DateValueFacetUsingSemanticsProviderFactory(), FacetFactoryCategory.VALUE);
		collector.addFactory(new DateTimeValueFacetUsingSemanticsProviderFactory(), FacetFactoryCategory.VALUE);
        
		collector.addFactory(new TimeStampValueFacetUsingSemanticsProviderFactory(), FacetFactoryCategory.VALUE);
		collector.addFactory(new TimeValueFacetUsingSemanticsProviderFactory(), FacetFactoryCategory.VALUE);

	}

}

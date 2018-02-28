package org.apache.isis.core.metamodel.services.swagger.plugins;

import org.apache.isis.core.metamodel.services.swagger.internal.ValuePropertyPlugin;

import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;

public class IsisTimeValuePropertyPlugin implements ValuePropertyPlugin {

	@Override
	public void plugin(ValuePropertyCollector collector) {

		collector.addValueProperty(org.apache.isis.applib.value.DateTime.class, DateTimeProperty::new);
		collector.addValueProperty(org.apache.isis.applib.value.Date.class, DateProperty::new);
		
	}

}

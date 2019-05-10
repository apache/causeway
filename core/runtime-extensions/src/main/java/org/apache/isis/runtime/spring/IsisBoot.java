package org.apache.isis.runtime.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.AppConfig;
import org.apache.isis.core.metamodel.IsisMetamodelModule;
import org.apache.isis.core.runtime.IsisRuntimeModule;
import org.apache.isis.core.runtime.services.IsisRuntimeServicesModule;
import org.apache.isis.core.wrapper.IsisWrapperModule;

@Configuration 
@ComponentScan(
		basePackageClasses= {
				IsisApplibModule.class,
				IsisMetamodelModule.class,
				IsisRuntimeModule.class,
				IsisRuntimeServicesModule.class,
				IsisWrapperModule.class},
		includeFilters= {
				@Filter(type = FilterType.CUSTOM, classes= {BeanScanInterceptorForSpring.class})
		})
public class IsisBoot implements ApplicationContextAware {
	
	@Autowired AppConfig appConfig;

	@Override
	public void setApplicationContext(ApplicationContext springContext) throws BeansException {
	    
	    System.out.println("!!!!!!!!!!!!!!!!!!! finalizing config");
	    appConfig.isisConfiguration();
	    System.out.println("!!!!!!!!!!!!!!!!!!! finalized config");
	    
	    _Context.putSingleton(ApplicationContext.class, springContext);
	}

	
}

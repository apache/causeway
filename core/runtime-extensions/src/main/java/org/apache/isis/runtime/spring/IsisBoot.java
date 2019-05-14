package org.apache.isis.runtime.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.ConfigurableEnvironment;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.spring._Spring;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.IsisMetamodelModule;
import org.apache.isis.core.runtime.IsisRuntimeModule;
import org.apache.isis.core.runtime.services.IsisRuntimeServicesModule;
import org.apache.isis.core.wrapper.IsisWrapperModule;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class IsisBoot implements ApplicationContextAware {
	
    @Autowired
    private ConfigurableEnvironment configurableEnvironment;
    
	@Override
	public void setApplicationContext(ApplicationContext springContext) throws BeansException {
	    _Context.putSingleton(ApplicationContext.class, springContext);
	    _Config.putAll(_Spring.copySpringEnvironmentToMap(configurableEnvironment));
	    log.info("Spring's context was passed over to Isis");
	}

	
}

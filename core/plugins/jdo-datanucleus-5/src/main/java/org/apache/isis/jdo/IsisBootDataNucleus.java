package org.apache.isis.jdo;

import org.apache.isis.config.beans.IsisBeanScanInterceptorForSpring;
import org.apache.isis.jdo.jdosupport.IsisJdoSupportDN5;
import org.apache.isis.jdo.jdosupport.mixins.Persistable_datanucleusIdLong;
import org.apache.isis.jdo.metrics.MetricsServiceDefault;
import org.apache.isis.jdo.transaction.IsisPlatformTransactionManagerForJdo;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
	//TimestampService.class, //FIXME initializes too early 
	MetricsServiceDefault.class,
	IsisJdoSupportDN5.class,
	IsisPlatformTransactionManagerForJdo.class,
})
@ComponentScan(
		basePackageClasses= {
				// bring in the mixins
				Persistable_datanucleusIdLong.class,
		},
		includeFilters= {
				@ComponentScan.Filter(type = FilterType.CUSTOM, classes= {IsisBeanScanInterceptorForSpring.class})
		})
public class IsisBootDataNucleus {

}

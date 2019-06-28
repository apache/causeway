package org.apache.isis.jdo;

import org.apache.isis.jdo.jdosupport.IsisJdoSupportDN5;
import org.apache.isis.jdo.metrics.MetricsServiceDefault;
import org.apache.isis.jdo.transaction.IsisPlatformTransactionManagerForJdo;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
	//TimestampService.class, //FIXME initializes too early 
	MetricsServiceDefault.class,
	IsisJdoSupportDN5.class,
	IsisPlatformTransactionManagerForJdo.class,
})
public class IsisBootDataNucleus {

}

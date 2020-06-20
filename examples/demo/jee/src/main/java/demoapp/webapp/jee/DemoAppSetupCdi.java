package demoapp.webapp.jee;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.springframework.boot.SpringApplication;

import lombok.extern.log4j.Log4j2;

@Singleton @Startup // CDI managed
@Log4j2
public class DemoAppSetupCdi {

    @PostConstruct
    private void init() {
        log.info("about to init ...");
        
        System.out.println("ABOUT TO INIT");
        
        SpringApplication.run(new Class[] { DemoAppJee.class }, new String[] {});
        
        //new SpringApplicationBuilder(DemoAppJee.class).run();
    }
    
}

package domainapp.dom.scalars;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.factory.FactoryService;

import lombok.val;

@DomainService(nature=NatureOfService.VIEW_MENU_ONLY)
@DomainObjectLayout(named="Scalar Demo")
public class ScalarMenu {
    
    @Inject private FactoryService factoryService;

    @Action
    @ActionLayout(cssClassFa="fa-font")
    public TextDemo text(){
        val demo = factoryService.instantiate(TextDemo.class);
        demo.initDefaults();  
        return demo;
    }
    
    @Action
    @ActionLayout(cssClassFa="fa-clock-o")
    public TemporalDemo temporals(){
        val demo = factoryService.instantiate(TemporalDemo.class);
        demo.initDefaults();  
        return demo;
    }
    
}

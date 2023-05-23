package demoapp.dom._infra.docgen;

import org.apache.causeway.extensions.docgen.menu.DocumentationMenu;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class SuppressDocumentation {

    @EventListener(DocumentationMenu.help.ActionDomainEvent.class)
    public void on(DocumentationMenu.help.ActionDomainEvent ev) {
        ev.hide();
    }
}

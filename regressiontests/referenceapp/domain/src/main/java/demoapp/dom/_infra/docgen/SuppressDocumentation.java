package demoapp.dom._infra.docgen;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.extensions.docgen.help.menu.DocumentationMenu;

@Service
public class SuppressDocumentation {

    @EventListener(DocumentationMenu.help.ActionDomainEvent.class)
    public void on(final DocumentationMenu.help.ActionDomainEvent ev) {
        ev.hide();
    }
}

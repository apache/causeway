#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package app;

import com.google.inject.Singleton;

import org.apache.isis.viewer.wicket.viewer.registries.components.ComponentFactoryRegistrarDefault;

@Singleton
public class ComponentFactoryRegistrarForQuickStart extends ComponentFactoryRegistrarDefault {

    @Override
    public void addComponentFactories(ComponentFactoryList componentFactories) {
        super.addComponentFactories(componentFactories);
        // currently no replacements
    }
}

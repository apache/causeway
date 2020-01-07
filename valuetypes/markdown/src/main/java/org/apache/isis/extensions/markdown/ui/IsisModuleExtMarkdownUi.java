package org.apache.isis.extensions.markdown.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.markdown.applib.IsisModuleExtMarkdownApplib;
import org.apache.isis.extensions.markdown.ui.components.MarkdownPanelFactoriesForWicket;

@Configuration
@Import({
    IsisModuleExtMarkdownApplib.class,
    MarkdownPanelFactoriesForWicket.Parented.class,
    MarkdownPanelFactoriesForWicket.Standalone.class,
})
public class IsisModuleExtMarkdownUi {
}

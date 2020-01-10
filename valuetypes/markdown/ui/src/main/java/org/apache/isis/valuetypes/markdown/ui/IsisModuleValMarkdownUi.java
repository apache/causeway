package org.apache.isis.valuetypes.markdown.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.valuetypes.markdown.applib.IsisModuleValMarkdownApplib;
import org.apache.isis.valuetypes.markdown.ui.components.MarkdownPanelFactoriesForWicket;

@Configuration
@Import({
    IsisModuleValMarkdownApplib.class,
    MarkdownPanelFactoriesForWicket.Parented.class,
    MarkdownPanelFactoriesForWicket.Standalone.class,
})
public class IsisModuleValMarkdownUi {
}

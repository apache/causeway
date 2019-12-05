package org.apache.isis.extensions.markdown.ui;

import org.apache.isis.extensions.markdown.applib.IsisModuleExtMarkdownApplib;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({IsisModuleExtMarkdownApplib.class})
public class IsisModuleExtMarkdownUi {
}

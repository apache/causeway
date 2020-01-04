package org.apache.isis.extensions.markdown.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.markdown.applib.IsisModuleExtMarkdownApplib;

@Configuration
@Import({IsisModuleExtMarkdownApplib.class})
public class IsisModuleExtMarkdownUi {
}

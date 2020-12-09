package org.apache.isis.tooling.model4adoc.ast;

import org.asciidoctor.ast.ListItem;

import org.apache.isis.commons.internal.base._Strings;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
public class SimpleListItem extends SimpleStructuralNode implements ListItem {

    @Getter @Setter private String source;
    @Getter @Setter private String text;
    @Getter @Setter private String marker;
    
    @Override
    public boolean hasText() {
        return _Strings.isNotEmpty(getText());
    }

}

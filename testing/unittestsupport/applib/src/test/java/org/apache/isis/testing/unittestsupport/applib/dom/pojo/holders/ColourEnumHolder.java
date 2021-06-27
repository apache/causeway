package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class ColourEnumHolder extends HolderAbstract<ColourEnumHolder> {
    private ColourEnum colourEnum;
    public void setColourEnum(ColourEnum colourEnum) {
        bump();
        this.colourEnum = broken ? null : colourEnum;
    }
}

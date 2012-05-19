package org.apache.isis.tck.dom.poly;


public class StringableEntityWithOwnDerivedProperty implements Stringable {
    
    // {{ String
    private String string;

    @Override
    public String getString() {
        return string;
    }

    public void setString(final String string) {
        this.string = string;
    }
    // }}

    public String getSpecial() {
        return "special";
    }

}
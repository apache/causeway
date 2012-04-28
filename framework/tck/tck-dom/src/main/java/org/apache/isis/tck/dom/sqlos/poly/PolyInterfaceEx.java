package org.apache.isis.tck.dom.sqlos.poly;


public class PolyInterfaceEx implements PolyInterface {
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
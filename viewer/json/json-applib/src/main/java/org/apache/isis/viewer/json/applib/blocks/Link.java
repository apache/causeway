package org.apache.isis.viewer.json.applib.blocks;

public class Link {
    
    private String rel;
    private String href;
    private Method method;
    
    public String getRel() {
        return rel;
    }
    public void setRel(String rel) {
        this.rel = rel;
    }
    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }
    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }
    @Override
    public String toString() {
        return "Link [rel=" + rel + ", href=" + href + ", method=" + method + "]";
    }

    
}

package org.apache.isis.viewer.json.applib.blocks;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

public class Link {
    
    private String rel;
    private String href;
    private Method method = Method.GET;
    
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
    
    public <T> Response follow(ClientExecutor executor) throws Exception {
        ClientRequest restEasyRequest = executor.createRequest(href);
        restEasyRequest.setHttpMethod(method.name());
        restEasyRequest.accept(MediaType.APPLICATION_JSON_TYPE);
        @SuppressWarnings("unchecked")
        ClientResponse<T> restEasyResponse = executor.execute(restEasyRequest);
        return restEasyResponse;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((href == null) ? 0 : href.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((rel == null) ? 0 : rel.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Link other = (Link) obj;
        if (href == null) {
            if (other.href != null)
                return false;
        } else if (!href.equals(other.href))
            return false;
        if (method != other.method)
            return false;
        if (rel == null) {
            if (other.rel != null)
                return false;
        } else if (!rel.equals(other.rel))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "Link [rel=" + rel + ", href=" + href + ", method=" + method + "]";
    }
    

    
}

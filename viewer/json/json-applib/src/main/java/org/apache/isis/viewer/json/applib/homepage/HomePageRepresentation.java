package org.apache.isis.viewer.json.applib.homepage;

import org.apache.isis.viewer.json.applib.blocks.Link;

public class HomePageRepresentation {

    private Link user;
    private Link services;
    public Link getUser() {
        return user;
    }
    public void setUser(Link user) {
        this.user = user;
    }
    public Link getServices() {
        return services;
    }
    public void setServices(Link services) {
        this.services = services;
    }
    
    @Override
    public String toString() {
        return "HomePage [user=" + user + ", services=" + services + "]";
    }
    
}

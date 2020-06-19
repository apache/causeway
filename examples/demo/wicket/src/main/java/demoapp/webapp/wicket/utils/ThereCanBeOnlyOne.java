package demoapp.webapp.wicket.utils;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import lombok.val;

public class ThereCanBeOnlyOne {

    public static void remoteShutdownOthersIfAny() {
        try {
            invokeRemoteShutdown();
        } catch (Exception e) {
            // ignore them all
        }
    }
    
    private static void invokeRemoteShutdown() throws IOException {
        
        val targetHost = new HttpHost("localhost", 8080, "http");
        val credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials("sven", "pass"));
        
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        val httpget = new HttpGet("/restful/services/demo.LineBreaker/actions/shutdown/invoke");
        
        try(val httpClient = HttpClientBuilder.create().build()){
            try(val response = httpClient.execute(targetHost, httpget, context)) {
                response.getEntity();
            } 
        }
        
    }
    
}

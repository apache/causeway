package org.apache.isis.commons.handlers;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import org.apache.isis.commons.handler.ChainOfResponsibility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;

class ChainOfResponsibilityTest {

    @Test
    void threeHandlers_shouldProperlyTakeResponsibilityInOrder() {
        
        val aToUpperCase = new ChainOfResponsibility.Handler<String, String>() {

            @Override
            public boolean isHandling(String request) {
                return request.startsWith("a");
            }

            @Override
            public String handle(String request) {
                return request.toUpperCase();
            }
            
        };

        val bToUpperCase = new ChainOfResponsibility.Handler<String, String>() {

            @Override
            public boolean isHandling(String request) {
                return request.startsWith("b");
            }

            @Override
            public String handle(String request) {
                return request.toUpperCase();
            }
            
        };

        val finallyNoop = new ChainOfResponsibility.Handler<String, String>() {

            @Override
            public boolean isHandling(String request) {
                return true;
            }

            @Override
            public String handle(String request) {
                return request;
            }
            
        };
        
        val chainOfResponsibility = ChainOfResponsibility.of(
                Arrays.asList(aToUpperCase, bToUpperCase, finallyNoop)); 
        
        
        assertEquals("ASTRING", chainOfResponsibility.handle("aString").orElse(null)); // handled by first handler
        assertEquals("BSTRING", chainOfResponsibility.handle("bString").orElse(null)); // handled by second handler
        assertEquals("cString", chainOfResponsibility.handle("cString").orElse(null)); // handled by third handler
        
    }

}

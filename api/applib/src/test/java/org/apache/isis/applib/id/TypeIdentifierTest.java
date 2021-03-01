package org.apache.isis.applib.id;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.applib.SomeDomainClass;
import org.apache.isis.commons.internal.testing._SerializationTester;

import lombok.val;

class TypeIdentifierTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void eager() {
        
        val original = TypeIdentifier.fqcn(SomeDomainClass.class);
        
        _SerializationTester.assertEqualsOnRoundtrip(original);
        
        assertEquals(
                original.getLogicalTypeName(),
                SomeDomainClass.class.getName());
        
        assertEquals(
                _SerializationTester.roundtrip(original).getLogicalTypeName(), 
                original.getLogicalTypeName());
    }
    
    @Test
    void lazy() {
        
        val original = TypeIdentifier.lazy(SomeDomainClass.class, ()->"hello");
        
        _SerializationTester.assertEqualsOnRoundtrip(original);
        
        assertEquals(
                original.getLogicalTypeName(),
                "hello");
        
        assertEquals(
                _SerializationTester.roundtrip(original).getLogicalTypeName(), 
                original.getLogicalTypeName());
    }


}

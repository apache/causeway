/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.testdomain.model.interaction;

import java.util.stream.IntStream;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.testdomain.model.interaction.InteractionDemo_negotiate.Params.NumberRange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;

@Action
@RequiredArgsConstructor
public class InteractionDemo_negotiate {

    @SuppressWarnings("unused")
    private final InteractionDemo holder;

    @Value @Accessors(fluent = true)
    public static class Params {
        
        @Getter @RequiredArgsConstructor
        public static enum NumberRange {
            POSITITVE(new int[] {1, 2, 3, 4}),
            NEGATIVE(new int[] {-1, -2, -3, -4}),
            EVEN(new int[] {-4, -2, 0, 2, 4}),
            ODD(new int[] {-3, -1, 1, 3});
            private final int[] numbers;
        }
        
        NumberRange rangeA;
        int a;
        
        NumberRange rangeB;
        int b;
        
        NumberRange rangeC;
        int c;
    }

    // for the purpose of testing we constrain parameters a, b, c by their ranges rangeA, rangeB, rangeC
    // and let the picked set {a, b, c} only be valid if a+b+c==0
    
    @MemberSupport
    public int act(
            NumberRange rangeA,
            int a,
            NumberRange rangeB,
            int b,
            NumberRange rangeC,
            int c) {
        
        return a + b + c;
    }

    // constraint considering all parameters
    @MemberSupport 
    public String validate(Params p) {
        final int sum = p.a + p.b + p.c;
        if(sum == 0) {
            return null; 
        }
        return String.format("invalid, sum must be zero, got %d", sum);
    }
    
    // -- defaults 

    @MemberSupport public NumberRange defaultRangeA(Params p) { return NumberRange.POSITITVE; }
    @MemberSupport public NumberRange defaultRangeB(Params p) { return NumberRange.NEGATIVE; }
    @MemberSupport public NumberRange defaultRangeC(Params p) { return NumberRange.ODD; }
    
    @MemberSupport public int defaultA(Params p) { return firstOf(p.rangeA()); }
    @MemberSupport public int defaultB(Params p) { return firstOf(p.rangeB()); }
    @MemberSupport public int defaultC(Params p) { return firstOf(p.rangeC()); }

    // -- choices
    
    @MemberSupport public int[] choicesA(Params p) { return p.rangeA().numbers(); }
    @MemberSupport public int[] choicesB(Params p) { return p.rangeB().numbers(); }
    @MemberSupport public int[] autoCompleteC(Params p, String search) { return searchWithin(p.rangeC(), search); }
    
    // -- parameter specific validation
    
    @MemberSupport public String validateA(Params p) { return verifyContains(p.a(), p.rangeA(), p); }
    @MemberSupport public String validateB(Params p) { return verifyContains(p.b(), p.rangeB(), p); }
    @MemberSupport public String validateC(Params p) { return verifyContains(p.c(), p.rangeC(), p); }
    
    // -- HELPER
    
    private int firstOf(NumberRange range) {
        return range!=null
                ? range.numbers()[0]
                : -99;
    }
    
    private String verifyContains(int x, NumberRange range, Params p) {
        if(IntStream.of(range.numbers()).anyMatch(e->e==x)) {
            return null;
        }
        val paramSet = _Lists.of(p.a, p.b, p.c);
        return String.format("invalid, element not contained in %s got %d, param set %s", range.name(), x, paramSet);
    }
    
    private int[] searchWithin(NumberRange range, String search) {
        if(_Strings.isEmpty(search)) {
            return new int[0];
        }
        return IntStream.of(range.numbers())
        .filter(e->(""+e).contains(search))
        .toArray();
    }
    

}

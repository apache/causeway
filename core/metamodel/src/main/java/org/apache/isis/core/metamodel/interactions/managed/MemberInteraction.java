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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.Optional;
import java.util.function.Function;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._Either;

import lombok.NonNull;
import lombok.val;

public abstract class MemberInteraction<T extends ManagedMember, H extends MemberInteraction<T, ?>> {

    public static enum AccessIntent {
        ACCESS, MUTATE;

        public boolean isMutate() {
            return this == MUTATE;
        }
    }
    
    @NonNull protected _Either<T, InteractionVeto> chain;

    protected MemberInteraction(@NonNull _Either<T, InteractionVeto> chain) {
        this.chain = chain;
    }
    
    public H checkVisibility(@NonNull final Where where) {
        chain = chain.leftRemap(property->{
            val visibilityVeto = property.checkVisibility(where);
            return visibilityVeto.isPresent()
                ? _Either.right(visibilityVeto.get()) 
                : _Either.left(property); 
        });
        return _Casts.uncheckedCast(this);
    }
    
    public H checkUsability(@NonNull final Where where) {
        chain = chain.leftRemap(property->{
            val usablitiyVeto = property.checkUsability(where);
            return usablitiyVeto.isPresent()
                ? _Either.right(usablitiyVeto.get()) 
                : _Either.left(property); 
        });
        return _Casts.uncheckedCast(this);
    }
    
    /**
     * Only check usability if intent is {@code MUTATE}. 
     * @param where
     * @param intent
     * @return self
     */
    public H checkUsability(@NonNull final Where where, @NonNull final AccessIntent intent) {
        if(intent.isMutate()) {
            return checkUsability(where);
        }
        return _Casts.uncheckedCast(this);
    }

    public <X extends Throwable> 
    H validateElseThrow(Function<InteractionVeto, ? extends X> onFailure) throws X {
        val veto = chain.rightIfAny();
        if (veto == null) {
            return _Casts.uncheckedCast(this);
        } else {
            throw onFailure.apply(veto);
        }
    }

    @Deprecated // use more specialized methods 
    public <X extends Throwable> 
    T getOrElseThrow(Function<InteractionVeto, ? extends X> onFailure) throws X {
        val value = chain.leftIfAny();
        if (value != null) {
            return value;
        } else {
            throw onFailure.apply(chain.rightIfAny());
        }
    }
    
    @Deprecated 
    public Optional<T> get() {
        return chain.left();
    }
    
    
}

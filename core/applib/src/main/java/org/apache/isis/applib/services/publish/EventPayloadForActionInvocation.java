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

package org.apache.isis.applib.services.publish;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * No longer in use.
 *
 * @deprecated
 */
@Deprecated
public class EventPayloadForActionInvocation<T> implements EventPayload {
    
    private final Identifier actionIdentifier;
    private final T target;
    private final List<? extends Object> arguments;
    private final Object result;
    private ObjectStringifier stringifier;

    public EventPayloadForActionInvocation(final Identifier actionIdentifier, final T target, final List<? extends Object> arguments, final Object result) {
        this.target = target;
        this.actionIdentifier = actionIdentifier;
        this.arguments = arguments != null? arguments: Collections.emptyList();
        this.result = result;
    }

    /**
     * Injected by Isis runtime immediately after instantiation.
     */
    @Deprecated
    @Programmatic
    public void withStringifier(ObjectStringifier stringifier) {
        this.stringifier = stringifier;
    }

    @Deprecated
    @Programmatic
    public List<? extends Object> getArguments() {
        return arguments;
    }

    @Deprecated
    @Programmatic
    public T getTarget() {
        return target;
    }

    @Deprecated
    @Programmatic
    public String getActionName() {
        return actionIdentifier.toFullIdentityString();
    }

    @Deprecated
    @Programmatic
    public Object getArg0() {
        return getArg(0);
    }
    @Deprecated
    @Programmatic
    public boolean hideArg0() {
        return hideArg(0);
    }

    @Deprecated
    @Programmatic
    public Object getArg1() {
        return getArg(1);
    }
    @Deprecated
    @Programmatic
    public boolean hideArg1() {
        return hideArg(1);
    }

    @Deprecated
    @Programmatic
    public Object getArg2() {
        return getArg(2);
    }
    @Deprecated
    @Programmatic
    public boolean hideArg2() {
        return hideArg(2);
    }

    @Deprecated
    @Programmatic
    public Object getArg3() {
        return getArg(3);
    }
    @Deprecated
    @Programmatic
    public boolean hideArg3() {
        return hideArg(3);
    }

    @Deprecated
    @Programmatic
    public Object getArg4() {
        return getArg(4);
    }
    @Deprecated
    @Programmatic
    public boolean hideArg4() {
        return hideArg(4);
    }

    @Deprecated
    @Programmatic
    public Object getArg5() {
        return getArg(5);
    }
    @Deprecated
    @Programmatic
    public boolean hideArg5() {
        return hideArg(5);
    }

    @Deprecated
    @Programmatic
    public Object getArg6() {
        return getArg(6);
    }
    @Deprecated
    @Programmatic
    public boolean hideArg6() {
        return hideArg(6);
    }

    @Deprecated
    @Programmatic
    public Object getArg7() {
        return getArg(7);
    }
    @Deprecated
    @Programmatic
    public boolean hideArg7() {
        return hideArg(7);
    }

    @Deprecated
    @Programmatic
    public Object getArg8() {
        return getArg(8);
    }
    @Deprecated
    @Programmatic
    public boolean hideArg8() {
        return hideArg(8);
    }

    @Deprecated
    @Programmatic
    public Object getArg9() {
        return getArg(9);
    }
    @Deprecated
    @Programmatic
    public boolean hideArg9() {
        return hideArg(9);
    }

    @Deprecated
    @Programmatic
    public Object getResult() {
        return result;
    }

    private Object getArg(int paramNum) {
        return arguments.size()>paramNum?arguments.get(paramNum):null;
    }
    private boolean hideArg(int paramNum) {
        return arguments.size()<=paramNum;
    }
    
    @Override
    public String toString() {
        if(stringifier == null) {
            throw new IllegalStateException("ObjectStringifier has not been injected");
        }

        final StringBuilder buf = new StringBuilder();
        buf.append(getActionName());
        buf.append("\n    target=").append(stringifier.toString(target));
        buf.append("\n      args=[");
        for (Object arg : arguments) {
            buf.append("\n           ").append(stringifier.toString(arg));
        }
        buf.append("\n      ]");
        final String stringifiedResult = stringifier.toString(result);
        buf.append("\n    result=").append(stringifiedResult != null ? stringifiedResult : "void");
        return buf.toString();
    }

}
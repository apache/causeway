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
 *
 */
package org.apache.causeway.persistence.querydsl.testsupport;

import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.querydsl.core.types.*;

/**
 * For testing purposes.
 */
public final class PredicatePrettifier implements Visitor<String, Templates> {

    static ThreadLocal<AtomicInteger> num = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    private List<String> visits = new ArrayList<>();

    @Override
    public String visit(Constant<?> e, Templates templates) {
        String str = e.getConstant().toString();
        return str;
    }

    @Override
    public String visit(FactoryExpression<?> e, Templates templates) {
        final StringBuilder builder = new StringBuilder();
        appendStr(builder.append("new ").append(e.getType().getSimpleName()), "(");
        boolean first = true;
        for (Expression<?> arg : e.getArgs()) {
            if (!first) {
                appendStr(builder, ", ");
            }
            incrementIndent();
            String str = arg.accept(this, templates);
            appendStr(builder, str);
            decrementIndent();
            first = false;
        }
        appendStr(builder, ")");
        String str = builder.toString();
        return str;
    }

    @Override
    public String visit(Operation<?> o, Templates templates) {
        final Template template = templates.getTemplate(o.getOperator());
        if (template != null) {
            final int precedence = templates.getPrecedence(o.getOperator());
            final StringBuilder builder = new StringBuilder();
            for (Template.Element element : template.getElements()) {
                final Object rv = element.convert(o.getArgs());
                if (rv instanceof Expression) {
                    if (precedence > -1 && rv instanceof Operation) {
                        if (precedence < templates.getPrecedence(((Operation<?>) rv).getOperator())) {
                            incrementIndent();
                            appendStrNl(builder, "(");
                            appendStrNl(builder, "");
                            if (o.getOperator() == Ops.OR) {
                                appendStrNl(builder, "");
                            }
                            incrementIndent();
                            String str = ((Expression<?>) rv).accept(this, templates);
                            appendStr(builder, str);
                            decrementIndent();
                            appendStrNl(builder, ")");
                            decrementIndent();
                            continue;
                        }
                    }
                    incrementIndent();
                    String str = ((Expression<?>) rv).accept(this, templates);
                    builder.append(str);
                    decrementIndent();
                } else {
                    builder.append(rv.toString());
                }
            }
            String str = builder.toString();
            visits.add(0, str);
            return str;
        } else {
            String str = "unknown, name=" + o.getOperator().name() + " args= " + o.getArgs();
            return str;
        }
    }

    private static void decrementIndent() {
        num.get().decrementAndGet();
    }

    private static void incrementIndent() {
        num.get().incrementAndGet();
    }

    @Override
    public String visit(ParamExpression<?> param, Templates templates) {
        String str = "{" + param.getName() + "}";
        return str;
    }

    @Override
    public String visit(Path<?> p, Templates templates) {
        final Path<?> parent = p.getMetadata().getParent();
        final Object elem = p.getMetadata().getElement();
        if (parent != null) {
            Template pattern = templates.getTemplate(p.getMetadata().getPathType());
            if (pattern != null) {
                final List<?> args = Arrays.asList(parent, elem);
                final StringBuilder builder = new StringBuilder();
                for (Template.Element element : pattern.getElements()) {
                    Object rv = element.convert(args);
                    if (rv instanceof Expression) {
                        incrementIndent();
                        String str = ((Expression<?>) rv).accept(this, templates);
                        builder.append(str);
                        decrementIndent();
                    } else {
                        builder.append(rv.toString());
                    }
                }
                String str = builder.toString();
                visits.add(num + " path: " + str);
                return str;
            } else {
                throw new IllegalArgumentException("No pattern for " + p.getMetadata().getPathType());
            }
        } else {
            String str = elem.toString();
            visits.add(num + " path: " + str);
            return str;
        }
    }

    @Override
    public String visit(SubQueryExpression<?> expr, Templates templates) {
        String str = expr.getMetadata().toString();
        return str;
    }

    @Override
    public String visit(TemplateExpression<?> expr, Templates templates) {
        final StringBuilder builder = new StringBuilder();
        for (Template.Element element : expr.getTemplate().getElements()) {
            Object rv = element.convert(expr.getArgs());
            if (rv instanceof Expression) {
                incrementIndent();
                String str = ((Expression<?>) rv).accept(this, templates);
                appendStr(builder, str);
                decrementIndent();
            } else {
                String str = rv.toString();
                appendStr(builder, str);
            }
        }
        String str = builder.toString();
        return str;
    }

    public String prettied() {
        return visits.get(0);
    }

    private void appendStr(StringBuilder builder, String str) {
        builder.append(spaces());
        builder.append(str);
    }

    private void appendStrNl(StringBuilder builder, String str) {
        builder.append("\n");
        appendStr(builder, str);
    }

    static String spaces() {
        return spaces(num.get().get());
    }

    private static String spaces(int num) {
        return Stream.generate(() -> " ").limit(num).collect(Collectors.joining());
    }

    public static class IndentableTemplates extends Templates {

        @SneakyThrows
        public IndentableTemplates() {

            Field templateFactoryField = Templates.class.getDeclaredField("templateFactory");
            templateFactoryField.setAccessible(true);
            templateFactoryField.set(this, new MyTemplateFactory());

            add(Ops.OR, "{0} \n|| {1}", Precedence.OR);
    //        templates.put(op, templateFactory.create(pattern));
    //        precedence.put(op, pre);
        }

        public static class IndentedOr extends Template.Element {
            private static final long serialVersionUID = 1L;

            @Override
            public Object convert(List<?> args) {
                return "\n" + PredicatePrettifier.spaces() + " || ";
            }

            @Override
            public boolean isString() {
                return true;
            }

//            private static String spaces(int num) {
//                return Stream.generate(() -> " ").limit(num).collect(Collectors.joining());
//            }

        }

        public static class MyTemplateFactory extends TemplateFactory {

            public MyTemplateFactory() {
                super('\\');
            }

            @SneakyThrows
            @Override
            public Template create(String template) {
                if(!template.equals("{0} \n|| {1}")) {
                    throw new IllegalArgumentException(template);
                }
                Template.Element byIndex0 = new Template.ByIndex(0);
                Template.Element staticText = new IndentedOr();
                Template.Element byIndex1 = new Template.ByIndex(1);
                Constructor<Template> constructor = Template.class.getDeclaredConstructor(String.class, List.class);
                constructor.setAccessible(true);
                Template rv = constructor.newInstance(template, Arrays.asList(byIndex0, staticText, byIndex1));
                return rv;
            }
        }
    }
}

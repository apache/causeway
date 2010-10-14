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


package org.apache.isis.app.cart.fixtures;

import org.apache.isis.app.cart.Product;
import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.applib.value.Money;


public class ProductFixture extends AbstractFixture {

    @Override
    public void install() {
        createProduct(
                "RESTful Web Services",
                "\"Every developer working with the Web needs to read this book.\" - David Heinemeier Hansson, creator of the Rails framework. \"RESTful Web Services\" finally provides a practical roadmap for constructing services that embrace the Web, instead of trying to route around it.\" - Adam Trachtenberg, PHP author and \"EBay Web Services Evangelist\". You\'ve built web sites that can be used by humans. But can you also build web sites that are usable by machines? That's where the future lies, and that's what \"RESTful Web Services\" shows you how to do. The World Wide Web is the most popular distributed application in history, and Web services and mashups have turned it into a powerful distributed computing platform. But today's web service technologies have lost sight of the simplicity that made the Web successful. They don't work like the Web, and they're missing out on its advantages. This book puts the \"Web\" back into web services. It shows how you can connect to the programmable web with the technologies you already use every day. The key is REST, the architectural style that drives the Web. This book: emphasizes the power of basic Web technologies - the HTTP application protocol, the URI naming standard, and the XML markup language; introduces the Resource-Oriented Architecture (ROA), a common-sense set of rules for designing RESTful web services; Shows how a RESTful design is simpler, more versatile, and more scalable than a design based on Remote Procedure Calls (RPC); and includes real-world examples of RESTful web services, like Amazon's Simple Storage Service and the Atom Publishing Protocol; discusses web service clients for popular programming languages. It also shows you how to implement RESTful services in three popular frameworks - Ruby on Rails, Restlet (for Java), and Django (for Python), and focuses on practical issues such as: how to design and implement RESTful web services and clients. This is the first book that applies the REST design philosophy to real web services. It sets down the best practices you need to make your design a success, and the techniques you need to turn your design into working code. You can harness the power of the Web for programmable applications: you just have to work with the Web instead of against it. This book shows you how.",
                14.99, "restful.jpg");
        createProduct(
                "Beautiful Code",
                "How do the experts solve difficult problems in software development? In this unique and insightful book, leading computer scientists offer case studies that reveal how they found unusual, carefully designed solutions to high-profile projects. You will be able to look over the shoulder of major coding and design experts to see problems through their eyes. This is not simply another design patterns book, or another software engineering treatise on the right and wrong way to do things. The authors think aloud as they work through their project's architecture, the tradeoffs made in its construction, and when it was important to break rules. \"Beautiful Code\" is an opportunity for master coders to tell their story.",
                22.56, "beautiful-code.jpg");
        createProduct(
                "Professional Wikis: Collaboration on the Web",
                "Professional Wikis explains how Wikis are emerging as the new means of content delivery and information sharing on the web. They have proven to be invaluable to web and application developers who employ them in collaborative development efforts. The book goes from the theory behind why wikis are successful and how user generated content delivers value to a business, to actual implementation. The book is rounded-out by advanced sections that cover scaling and social issues that are involved in real-world wiki management. It also explores the API of commercial wikis and integrating them with corporate infrastructure and expanding to application add-ons and database integration.",
                14.98, "professional-wikis.jpg");
    }

    private void createProduct(String title, String description, double price, String imageUrl) {
        Product product = newTransientInstance(Product.class);
        product.setTitle(title);
        product.setDescription(description);
        product.setImageUrl(imageUrl);
        product.setPrice(new Money(price, "USD"));
        persist(product);
    }

}

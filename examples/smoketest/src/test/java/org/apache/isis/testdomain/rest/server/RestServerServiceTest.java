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
package org.apache.isis.testdomain.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.client.ResponseDigest;
import org.apache.isis.applib.client.RestfulClient;
import org.apache.isis.applib.client.SuppressionType;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule;
import org.apache.isis.viewer.restfulobjects.IsisBootWebRestfulObjects;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import lombok.val;

@SpringBootTest(
		classes = {RestServerService.class},
		properties = {
				"logging.config=log4j2-test.xml",
		},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
	JdoTestDomainModule.class,
	IsisBootWebRestfulObjects.class
})
class RestServerServiceTest {

	@LocalServerPort int port;
	@Inject RestServerService restServerService;

	@Test
	void test() throws InterruptedException {
		
		//Thread.sleep(10000000000L);
		
		assertNotNull(restServerService.getPort());
		assertTrue(restServerService.getPort()>0);

		RestfulClient client = restServerService.newClient();

		Builder request = client.request(
				"services/testdomain.InventoryRepository/actions/recommendedBookOfTheWeek/invoke", 
				SuppressionType.ALL);

		Entity<String> args = client.arguments()
				.build();

		Response response = request.post(args);

		ResponseDigest<Book> digest = client.digest(response, Book.class);

		if(digest.isSuccess()) {
		
			val bookOfTheWeek = digest.get();
			System.out.println("result: "+ bookOfTheWeek);
			
			
			assertNotNull(bookOfTheWeek);
			assertEquals("Book of the week", bookOfTheWeek.getName());
			

		} else {
			
			fail(digest.getFailureCause());
			
		}

	}

}

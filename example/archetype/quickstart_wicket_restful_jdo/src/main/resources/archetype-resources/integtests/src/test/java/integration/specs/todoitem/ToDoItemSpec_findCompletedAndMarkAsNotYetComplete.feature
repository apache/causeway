#
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
@ToDoItemsFixture
Feature: Find completed ToDoItem and mark as not yet complete

  # the scenario is listed twice here just to demonstrate that it
  # can be run either at @unit-level scope (using mocks) or
  # at @integration-level scope (against the running system).
  
  @integration
  Scenario: Todo items can be uncompleted
    Given a completed item
    When  I mark the item as not yet complete
    Then  the item is listed as incomplete 

 
  @unit
  Scenario: Todo items can be uncompleted
    Given a completed ToDo item
    When  I mark the item as not yet complete
    Then  the item is listed as incomplete 

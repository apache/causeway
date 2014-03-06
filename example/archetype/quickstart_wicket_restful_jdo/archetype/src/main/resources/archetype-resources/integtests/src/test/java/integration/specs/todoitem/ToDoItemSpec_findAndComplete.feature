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
Feature: Find And Complete ToDo Items

  # the scenario is listed twice here just to demonstrate that it
  # can be run either at @unit-level scope (using mocks) or
  # at @integration-level scope (against the running system).
  
  @unit
  Scenario: Todo items once completed are no longer listed
    Given there are a number of incomplete ToDo items
    When  I choose the first of the incomplete items
    And   mark the item as complete
    Then  the item is no longer listed as incomplete 


  @integration
  Scenario: Todo items once completed are no longer listed
    Given there are a number of incomplete ToDo items
    When  I choose the first of the incomplete items
    And   mark the item as complete
    Then  the item is no longer listed as incomplete 

    
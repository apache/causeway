Feature: Find And Complete ToDo Items

  # can either run at unit-level scope or integration-level scope
  
  #@integration
  @unit
  Scenario: Todo items once completed are no longer listed
    Given there are a number of incomplete ToDo items
    When  I choose the first one
    And   mark it as complete
    Then  the item is no longer listed as incomplete 



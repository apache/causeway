RO based Regression Testing (Tellurium;-)

Given  

* RO App uses all/most features of the FW,
* RO App tests are included in the FW coverage, and
* Recording caught all/most functions of the App

When

* Recordings are replayed 

Then

* FW test coverage should increase 

.Record Phase
[plantuml,file="test1-overview.png"]
--
@startuml

:USER:
USER -> FE : action

component kroviz <<RO FrontEnd>> as FE {
    database EventLog as EL
    FE -> EL : Remote and UI \nevents are logged
}

component Demo <<RO App>> as APP
FE <-> APP : request / response

component "Apache Isis" <<Naked Objects FW>> as FW
APP -> FW : uses

@enduml
--
.Export Function
[plantuml,file="test2-overview.png" ]
--
@startuml

title
Sample Content of EventLog
|= url |= [request data] |= state |= response |
| http://localhost:8080/restful | uid/pw | SUCCESS | {jsonStr} |
| http://localhost:8080/restful/menubars | n/a | ERROR | {another js} |
end title

database "Event Log" as EL
file "Replay Events" as RE
EL -> RE : export

@enduml
--
.Replay Phase
[plantuml,file="test3-overview.png" ]
--
@startuml

file "Replay Events" as RE
component "Demo \nRegression \nTest" as DRT
RE <- DRT : reads

component Demo <<RO App>> as APP
DRT -> APP : invokes RO API calls

component "Apache Isis" <<Naked Objects FW>> as FW
APP -> FW : uses

note bottom of DRT
Event Log Entries are replayed during test.
Expected/actual responses are compared.

Events in state: 
* SUCCESS -> expected == actual
* ERROR -> expected != actual 
end note

note bottom of FW
additional
coverage
end note

note as NREG
Regressions can be
caused in either
end note
APP .. NREG
NREG .. FW

@enduml
--




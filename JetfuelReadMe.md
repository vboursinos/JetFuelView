JetFuel
==================
To Run Jetfuel Main Run JetFuel:web [bootRun]
comment log jars from utils/build.gradle
To Run JetFuel explorer  Run JetFuel:utils [runDataExplorerUAT]
To build JetFuel jar Run JetFuel:utils [jar]


To run JetFuel Main ( Run Gradle :web, application, bootrun)
JetFuel Main ( Run Gradle :web, application, bootrun)
To build jetfuel data explorer jar  ( Run Gradle :web, application, bootrun)

Todo List
==================
Jet Fuel Data Explorer

Features
Show all fields and then only fields available for record - in the datachooser
Disable the non selectable option when queue is selected. 


Bugs

OutOfFocus cells in vertical view does not highlight full column
/n in table view spoils tables.
Tree view deletes all children, This is an issue when we have a map with lots of children and does not support list of maps ( might not be required)
In jetFuel selector when we want to enter a window it does not allow to to enter and start and end date. its fine on a mac


Properties
Show delta switch
Highlight outOffocus
Test area show sow key in message. 


Phase 2
Edit Views - remove and recreate tab
Move to json publish format
create About amps page . show version and soem data
Save layouts
copy paste does not work on Mac
Add convertor for fix, nfix
Test nested json
Search dat in table views


Old

Only load the webservice poller when subscribed
History option for webservice
Tomcat not shutting down correctly

App
start and stop nicely
Build to jar

Views
SystemView - Canvas to show nodes
            Each amps server you can right click and open
                    amps admin page
                    ssh , ftp to box
                    Show connected clients
                    Connect to external links
                    view amps config file
                    Data Viewer
ComponentView - Html5 table to show connected componments  - with filters
Event View - Real time feed to show clients logging on and off from each server - with filters
StatsView - Show some stats from web service inc history
Startup page - Create, edit, delete and  save enviroment
                To create env chose name ( UAT), colour (red for prod), connections url and admin port. Check admin port works

Canvas tutorial
http://www.html5canvastutorials.com/tutorials/html5-canvas-rectangles/
http://gojs.net/latest/samples/customContextMenu.html
http://icons.iconarchive.com/ -- icons
https://github.com/andy-goryachev/FxDock





=========================================


JetFuel Execute

Any component can publish a function
Any component can call a function 
Make sync call
Subscribe for updates and cancel subscription
Call from any process and get only its response
Have an update functions that can change any record. So updateField(topic,recordid, key, value)
Check if you can make recordid a list and key value pair a map. This way we can move update several fields for sevral records
Register as source so it the component dies , undo all. eg when trader does a quotes on and then disconnect,  then switch off all his quotes
Make selection of users in "subscriptions" narrowable
Copy/paste does not work on windows.

Amps
One Topic called Functions
One topic called Functions_Bus
One action to delete records from functions if the publisher goes down

Publisher
For every function a publihser publishes it 
Will create a function hash. eg calcPrice(long yield) will have hash of calcPrice_1. long =1, String =2, double = 3 and boolean =4
Check the function hash does nto exists on Functions topic, if it does throw an error
Subscribe to funcion hash on Functions_bus
pubish a message on Functions with connectionname so the action can delete this if the pubisher dies.


Caller
Gnererate a session UUID  subscribe to this on Functions_Bus
subscribe to Functions
Check the functions exists else report error
generate a caller UUid for function call create a json message with session uuid, calleruuid, date time, connectionname, function name and parameters
When you get a reply for caller uuid forward to the correct listerner.

For subscribe keep hold of caller uuid and call functions to unsubscribe. 


To get history of function
Query Functions_bus for connectionname and datatime and function name and get the callerId
do a bookmark subscription for callerid

Being able to replay function commands 

If amps disconnects republish and res subscribe everything
Dynamically add and remove functions as they come

+++++++++

To build app from utils
run the following
clean
BuildApp
ZipBuild

=====
Select bug
Select RFQ
select filter add fitler ef /ID=0
In Field Select "ALL" then "SELECT"
Enter "state" in fields text field
select a few fields
Press show. ( fields dont get added)
===
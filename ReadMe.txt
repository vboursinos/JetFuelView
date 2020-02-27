Thank you for downloading JetFuelView

To Start using JetFuelView
* Set JAVA_HOME variable to a java 8
* Enable amps stats in your amps config.  eg add  <SOWStatsInterval>5s</SOWStatsInterval> at the root level
* Add a config file in the config folder. Use provided example as a template. Please note servers should be the json connectionurl, adminport should be the web port and the environment is DEV, UAT, PREPROD, PROD etc..
* Run one of the following from the bin directory
    StartJetFuelView to see a system view
    StartJetFuelExplorer to connect to any amps instance
    StartJetFuelExplorerDirect to connect to a single amps instance

Note if your amps has no authentication then just pass in any username and password this is ignored safely

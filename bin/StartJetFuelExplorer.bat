title JetFuelView Launcher

set CLASSPATH=./config/*;./libs/*;

"%JAVA_HOME%\bin\java" -Dlog4j.configurationFile=log4j2-JetFuel.xml -XX:MaxGCPauseMillis=10 -XX:SurvivorRatio=4 -XX:+UseConcMarkSweepGC -cp %CLASSPATH% headfront.jetfuelview.JetFuelView JetFuelExplorer
set CLASSPATH=../libs/*;


set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_92
set AMPS_CONNECTION=tcp://192.168.56.101:8001/amps/json
set AMPS_ADMIN_PORT=8199
set AMPS_ENV=UAT

"%JAVA_HOME%\bin\java" -Dlog4j.configurationFile=log4j2-JetFuel.xml -XX:MaxGCPauseMillis=10 -XX:SurvivorRatio=4 -XX:+UseConcMarkSweepGC -cp %CLASSPATH% headfront.dataexplorer.DataExplorer %AMPS_CONNECTION% %AMPS_ADMIN_PORT% %AMPS_ENV%
#!/bin/sh
./mvnw "-Dexec.args=-Xmx2g -classpath %classpath com.github.braully.graph.UtilCProjects" -Dexec.executable=java -Dexec.classpathScope=runtime -DskipTests=true -Dexec.vmArgs=-Xmx2g -Dexec.appArgs= org.codehaus.mojo:exec-maven-plugin:1.5.0:exec

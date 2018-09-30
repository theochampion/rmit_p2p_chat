javac  `find . -name *.java`
jar -cfvm client.jar client_manifest.txt `find . -name *.class`
find . -name *.class -delete
java -jar client.jar
# javac ./app/*.java ./model/*.java ./network/*.java ./view/*.java ./file/*.java ./dht/*.java
# jar -cfvm ClientApp.jar client_manifest.txt ./app/*.class ./model/*.class ./network/*.class ./view/*.class ./file/*.class ./dht/*.class
# java -jar ClientApp.jar
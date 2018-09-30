javac  `find . -name *.java`
jar -cfvm server.jar server_manifest.txt `find . -name *.class`
find . -name *.class -delete
java -jar server.jar
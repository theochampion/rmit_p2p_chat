@echo on
javac .\app\*.java .\model\*.java .\network\*.java .\view\*.java .\file\*.java .\dht\*.java
jar -cfvm PresentationApp.jar pres_manifest.txt .\app\*.class .\model\*.class .\network\*.class .\view\*.class .\file\*.class .\dht\*.class
java -jar PresentationApp.jar
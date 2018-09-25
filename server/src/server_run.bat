@echo on
javac .\app\*.java .\model\*.java .\network\*.java .\view\*.java .\file\*.java
jar -cfvm ServerApp.jar server_manifest.txt .\app\*.class .\model\*.class .\network\*.class .\view\*.class .\file\*.class
java -jar ServerApp.jar
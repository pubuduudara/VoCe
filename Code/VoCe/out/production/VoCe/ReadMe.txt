
Run VOIP :
	javac VOIP.java
	java VOIP <MULTICAST IP> <PORT>


Run Testcases:

Compile:

	Edit permisson of junit-4.11.jar and hamcrest-core-1.3.jar

	sudo chmod +x junit-4.11.jar hamcrest-core-1.3.jar
	
	
	javac -cp junit-4.11.jar:hamcrest-core-1.3.jar  TestRunner.java TestVoipClassMethods.java TestPacketHandler.java VoipDataPacket.java VOIP.java PacketHandler.java ErrorDetails.java
	
	To  run Tests :

	java -cp .:junit-4.11.jar:hamcrest-core-1.3.jar TestRunner

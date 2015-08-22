JCC = javac

JAVA = java

RM = rm

default:	fileServer.class	FileConnectionHandler.class		fileClient.class	


fileServer.class: fileServer.java
	$(JCC) fileServer.java

FileConnectionHandler.class: FileConnectionHandler.java
	$(JCC) FileConnectionHandler.java

fileClient.class: fileClient.java
	$(JCC) fileClient.java

runServer:
	$(JAVA)  -Djava.security.policy=policy masterObject
	
runClient:
	$(JAVA) fileClient

clean: 
	$(RM) *.class 

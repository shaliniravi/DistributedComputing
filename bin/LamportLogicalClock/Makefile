JCC = javac

JAVA = java

RM = rm

default:	ProcessConnectionHandler.class	ProcessThread.class	MasterConnectionHandler.class	MasterThread.class	SocketTest.class


ProcessConnectionHandler.class: ProcessConnectionHandler.java
	$(JCC) ProcessConnectionHandler.java

ProcessThread.class: ProcessThread.java
	$(JCC) ProcessThread.java

MasterConnectionHandler.class: MasterConnectionHandler.java
	$(JCC) MasterConnectionHandler.java

MasterThread.class: MasterThread.java
	$(JCC) MasterThread.java

SocketTest.class: SocketTest.java
	$(JCC) SocketTest.java

run:
	$(JAVA) SocketTest

clean: 
	$(RM) *.class 

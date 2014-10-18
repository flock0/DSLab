Reflect about your solution!

Summary:
- TODO

- Sync über node in
-- AliveListener.refreshNode()
-- NodePurgeTask.purgeInactiveNodes()
So kann niemals der Zustand eintreten, dass ein Node nur bei manchen Operator-Listen drinnen ist.
Bsp: refreshNodes: removeFromActiveNodes() und addToActiveNodes nur halbat.
Dann purgeInactiveNodes: removeFromActiveNodes() entfernt die, die vorher halbat hinzugefügt wurden.
Dann refreshNodes: addToActiveNodes wird fertig gemacht.
Ergebnis: Node ist in Operator-Listen nur halbat drinnen.



Interrupts mehrstufig: 
CloudController stößt shutdown an
ClientListener schließt den ServerSocket, dann jeden ClientSocket, wodurch die Threads im ThreadPool interrupted werden
Danach kann der ThreadPool heruntergefahren werden


analog beim Node mit dessen ComputationRequestListenern

------------------------------------------
nextNodeToTry wird nicht synchronisiert. D.h. Node-Findung nicht synchron. Es kann sein, dass usage nicht immer perfekt ist. Auf Dauer ist es jedoch perfekt.
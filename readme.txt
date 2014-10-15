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
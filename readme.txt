Ein paar Worte zu den wichtigsten Komponenten:

-  Node:
  - Die Alive-Nachrichten werden durch einen Timer regelmäßig versandt.
  - Berechnungen werden an einen fixed ThreadPool delegiert. 
  
- CloudController:
  - Ein eigener Thread kümmert sich um das Empfangen der Alive-Nachrichten und um das Management der Node-Liste.
  - Ein Timer setzt Nodes offline, falls der Timeout überschritten ist.
  - Clients werden an einen cached ThreadPool delegiert.
  - In ConcurrentSkipListSets sind die Nodes der Usage nach sortiert.
  - Zum Herunterfahren reicht es nicht, alle Threads zu interrupten. Es müssen alle offenen Sockets geschlossen werden.
  
- Synchronisierung im Controller:
  - Geschieht entweder auf Node- oder auf HashMap-Ebene.
  - Node-Objekte sind synchronisiert, falls eine Alive-Nachricht eintrifft oder eine Zeitüberschreitungen eingetreten ist. (So kann niemals der Zustand eintreten, dass ein Node nur bei manchen Operator-Listen bei den aktiven Nodes drinnen ist.)
  - Die HashMap der aktiven Nodes ist ebenfalls an einigen Stellen synchronisiert, damit Änderungen durch Zeitüberschreitungen oder neue Nodes konsistent gespeichert sind.

- Secure Channel:
  - Die Klasse SecureChannelSetup stellt Methoden für Client und Controller bereit, um die Authentifizierung mit RSA durchzuführen. Danach wird von RSA auf AES gewechselt.
  - Die AES- und Base64-Funktionalität wurde mittels Decorator Pattern umgesetzt.
  - Der !login-Command wird nun nicht mehr unterstützt.
  - Solange der Client nicht authentifiziert ist, sind keine Commands (außer !authenticate) möglich.
  - Solange der Client nicht authentifiziert ist, ist die Verbindung unverschlüsselt. Nach einem !authenticate ist sie - wie gefordert - verschlüsselt. Kommt dann ein !logout, wird die AES-Verschlüsselung wieder entfernt.
 
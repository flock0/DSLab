Ein paar Worte zu den wichtigsten Komponenten:

-  Node:
  - Die Alive-Nachrichten werden durch einen Timer regelmaessig versandt.
  - Berechnungen werden an einen fixed ThreadPool delegiert. 
  
- CloudController:
  - Ein eigener Thread kuemmert sich um das Empfangen der Alive-Nachrichten und um das Management der Node-Liste.
  - Ein Timer setzt Nodes offline, falls der Timeout ueberschritten ist.
  - Clients werden an einen cached ThreadPool delegiert.
  - In ConcurrentSkipListSets sind die Nodes der Usage nach sortiert.
  - Zum Herunterfahren reicht es nicht, alle Threads zu interrupten. Es muessen alle offenen Sockets geschlossen werden.
  
- Synchronisierung im Controller:
  - Geschieht entweder auf Node- oder auf HashMap-Ebene.
  - Node-Objekte sind synchronisiert, falls eine Alive-Nachricht eintrifft oder eine Zeitueberschreitungen eingetreten ist. (So kann niemals der Zustand eintreten, dass ein Node nur bei manchen Operator-Listen bei den aktiven Nodes drinnen ist.)
  - Die HashMap der aktiven Nodes ist ebenfalls an einigen Stellen synchronisiert, damit Aenderungen durch Zeitueberschreitungen oder neue Nodes konsistent gespeichert sind.

- Secure Channel:
  - Die Klasse SecureChannelSetup stellt Methoden fuer Client und Controller bereit, um die Authentifizierung mit RSA durchzufuehren. Danach wird von RSA auf AES gewechselt.
  - Die AES- und Base64-Funktionalitaet wurde mittels Decorator Pattern umgesetzt.
  - Der !login-Command wird nun nicht mehr unterstuetzt.
  - Solange der Client nicht authentifiziert ist, sind keine Commands (ausser !authenticate) moeglich.
  - Solange der Client nicht authentifiziert ist, ist die Verbindung unverschluesselt. Nach einem !authenticate ist sie - wie gefordert - verschluesselt. Kommt dann ein !logout, wird die AES-Verschluesselung wieder entfernt.
 
- Two-phase-commit:
  - Nodes und Cloud Controller koennen offline sein, heruntergefahren werden oder abstuerzen. In diesem Fall wartet ein Node, 
    welcher der Cloud beitreten moechte, jeweils 10 Sek. (Cloud Controller) bzw. 5 Sek. (Node) lang auf eine Antwort. Falls 
    innerhalb der Wartezeit keine Antwort zurueck kommt, gilt der Versuch, der Cloud beizutreten als fehlgeschlagen. Nach 3 
    Versuchen wird eine Fehlermeldung ausgegeben und die Node heruntergefahren.
  - Resourcen werden nur aktualisiert, wenn ein neuer Node der Cloud betritt. Werden Nodes heruntergefahren oder stuerzen
    sie ab, werden die Resourcen nicht automatisch aktualisiert. Dadurch entstehen zwar ungenutzte Ressourcen, jedoch muss
    der Cloud Controller sich nicht um Faelle wie verzoegerte Alive-Messages kuemmern, sich alle Ressourcenwerte der Nodes
    merken und im Fall, dass Nodes heruntergefahren werden oder abstuerzen, neue Ressourcenwerte berechnen und ausschicken.
  - Im Falle von verzoegerten Alive-Messages hat der Cloud Controller einen aktiven Node als offline markiert. In diesem Fall
    muesste entweder der Cloud Controller alle Nodes auf verzoegerte Alive-Messages abpruefen oder die Nodes sich selbst neu
    starten, um erneut das two-phase-commit auszufuehren. Wir haben uns fuer letztere Variante entschieden.
    
 - Message Integrity:
  - Wenn eine Nachricht waehrend des Nachrichtentransfers zwischen Cloud Controller und Node veraendert wurde, werden dem Benutzer
    gar keine credits abgezogen, auch wenn zuvor schon Teilberechnungen erfolgreich durchgefuehrt wurden.
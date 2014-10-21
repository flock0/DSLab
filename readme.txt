Ein paar Worte zu den einzelnen Komponenten:
- Kommandozeile:
  - Zur Ein- und Ausgabe beim Client, Controller und Node wird die Shell-Klasse verwendet. Diese ist um einen ExceptionHandler erweitert, um die unschönen Fehlermeldungen zu verbergen.

-  Node:
  - Die Alive-Nachrichten werden durch einen Timer regelmäßig versandt.
  - In einem eigenen Thread lauscht ein Socket nach eingehenden Anfragen und delegiert sie an einen (fixed) ThreadPool. 
  - Ein passendes Berechnungsobjekt wird durch eine Factory erzeugt.
  
- CloudController:
  - Ein eigener Thread kümmert sich um das Empfangen der Alive-Nachrichten und um das Management der Node-Liste.
  - Mittels Timer werden Zeitüberschreitungen der Alive-Nachrichten erkannt und die Node-Liste entsprechend angepasst.
  - In einem eigenen Thread lauscht ein Socket nach Clients und delegiert sie an einen (cached) ThreadPool.
  - Bei einer !compute-Anfrage wird das sortierte Set der Nodes nacheinander durchlaufen bis ein funktionierender Node gefunden wurde. Dies wird für jede Operation wiederholt, bis die Berechnung abgeschlossen ist.
  - Unerlaubte Duplikate in einer ConcurrentSkipListSet werden durch eine UUID umgangen.
  - Zum Herunterfahren reicht es nicht, alle Threads zu interrupten. Es müssen alle offenen Sockets geschlossen werden.
  
- Synchronisierung im Controller:
  - Node-Objekte sind synchronisiert, falls eine Alive-Nachricht eintrifft oder eine Zeitüberschreitungen eingetreten ist. (So kann niemals der Zustand eintreten, dass ein Node nur bei manchen Operator-Listen bei den aktiven Nodes drinnen ist.)
  - Die HashMap der aktiven Nodes ist ebenfalls an einigen Stellen synchronisiert, damit Änderungen durch Zeitüberschreitungen oder neue Nodes konsistent gespeichert sind.
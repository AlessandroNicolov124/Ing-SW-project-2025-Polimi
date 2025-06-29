# ing-sw-2025-Nicolov-Errigo-Roggi-Galimberti

In order to run the project jdk 23.0.2 must be used

To modify sequence diagram visit the following link: https://sequencediagram.org/
paste in the diagram you want to modify and then export the file once done.

The group implemented the following features: Regole Complete + TUI + GUI + RMI + Socket + 1FA.
Among the advanced functionalities we managed to implement multiple matches feature and the trial flight feature.


# Relazione sul Progetto INGSW

## INTRODUZIONE

Il presente documento descrive il progetto software realizzato per il corso di Ingegneria del Software, che consiste nello sviluppo di una versione digitale distribuita del gioco da tavolo Galaxy Trucker. Il progetto è stato sviluppato in Java, con architettura client-server e supporto sia per interfaccia testuale (TUI) che grafica (GUI).

## Obiettivi di progetto

L’obiettivo principale è la realizzazione di un sistema che riproduca fedelmente le regole e le dinamiche di Galaxy Trucker, garantendo:
- Aderenza alle regole ufficiali del gioco
- Architettura modulare e scalabile, facilmente estendibile
- Esperienza utente accessibile tramite TUI e GUI
- Affidabilità nella gestione della comunicazione di rete tra client e server

## Deliverables

Il progetto fornisce:
- Diagrammi UML (classi, sequenza, package) che illustrano la struttura e le interazioni principali
- Codice sorgente Java documentato con JavaDoc
- Implementazione funzionante del gioco, con modalità TUI e GUI
- Documentazione dettagliata del protocollo di comunicazione client-server (Socket e RMI)
- Suite di test unitari per i componenti principali
- Documenti di peer review e manuale utente

---

## ANALISI DEI REQUISITI

### Requisiti Game-Specific

L’implementazione rispetta le regole di Galaxy Trucker, con:
- **Volo Standard**: tutte le meccaniche di base sono implementate, inclusi eventi, gestione delle navi e risoluzione dei danni.
- **Volo di Prova**: modalità opzionale con regole semplificate.
- **Assemblaggio delle Navi**: verifica automatica della correttezza del posizionamento dei componenti e penalità per errori.
- **Carte Avventura**: implementazione delle carte avventura
- **Visibilità**: ogni giocatore può visualizzare lo stato delle navi avversarie in ogni fase.

### Requisiti Game-Agnostic

- **Architettura**: pattern Model-View-Controller (MVC) per separazione delle responsabilità.
- **Tecnologia**: sistema distribuito client-server, realizzato in Java SE.
- **Server**: gestione di una partita per istanza, supporto sia Socket TCP/IP che RMI, gestione di più client contemporanei.
- **Client**: interfaccia JavaFX (GUI) e TUI, selezionabili all’avvio; scelta della tecnologia di comunicazione.
- **Internazionalizzazione**: codice e documentazione in inglese.

### Gestione Delle Partite

**Creazione e Avvio**
- Creazione di nuove partite
- Il primo giocatore sceglie il nome della partita
- Avvio permesso al raggiungimento del numero minimo di giocatori

**Gestione delle Disconnessioni**
- Terminazione della partita in caso di disconnessione
- Notifica a tutti i giocatori dell'evento

---

## FUNZIONALITA’ AVANZATE

- **Volo di Prova**: modalità alternativa con plance e regole semplificate.
- **Partite Multiple**: il server può gestire più partite contemporaneamente (funzionalità predisposta, parzialmente implementata).
- **Persistenza**: salvataggio periodico dello stato partita (funzionalità prevista, non completamente implementata).
- **Resilienza**: meccanismi di riconnessione e gestione timeout (in parte implementati).

---

## PROGETTAZIONE DEL SISTEMA

### Architettura MVC

- **Model**: contiene la logica di gioco, lo stato delle navi, la gestione delle carte e la validazione delle mosse.
- **View**: implementa sia la TUI che la GUI, mostrando lo stato aggiornato e ricevendo input dagli utenti.
- **Controller**: gestisce la logica di coordinamento tra Model e View, inoltra i comandi e si occupa della comunicazione client-server.

### Protocollo di Comunicazione

- **Tecnologie**: supporto sia per Socket TCP/IP che per Java RMI, con possibilità di client eterogenei nella stessa partita.
- **Messaggi**: protocollo a messaggi strutturati per autenticazione, comandi di gioco, aggiornamenti di stato e notifiche di errore/evento.
- **Serializzazione**: utilizzo di oggetti serializzabili per il trasferimento dei dati tra client e server.

---

## IMPLEMENTAZIONE

### Struttura del Progetto

- **AdventureCards**: classi per ogni carta evento, con logica specifica (es. BigMeteorSwarm).
- **Player**: rappresenta lo stato e le azioni di ciascun giocatore, inclusa la plancia nave e i componenti.
- **Game**: gestisce il ciclo di gioco, i turni, la risoluzione degli eventi e la sincronizzazione tra client.
- **Componenti**: classi per i moduli nave (cannoni, scudi, motori, ecc.), con controlli di validità e gestione danni.
- **Utility**: funzioni di supporto per randomizzazione, input/output, validazione.

### Principali Funzionalità Implementate

- **Gestione degli eventi**: ogni turno può essere influenzato da carte avventura che introducono ostacoli o opportunità.
- **Sistema di difesa**: i giocatori possono posizionare e orientare cannoni e scudi per difendersi da minacce come meteoriti.
- **Gestione danni**: se una minaccia non viene neutralizzata, i componenti dell’astronave possono essere distrutti.
- **Interfaccia utente**: output testuale che mostra lo stato della plancia, le minacce in arrivo e le azioni disponibili.

### Qualità del Codice

- Struttura modulare
- Utilizzo di design pattern (MVC, Observer, Factory).
- Convenzioni di naming e documentazione JavaDoc.
- Presenza di commenti esplicativi.

### Testing

- Test unitari per Model, Controller e principali eventi.
- Test di integrazione client-server.
- Copertura dei casi limite e gestione degli errori.

---

## CONCLUSIONI

**Requisiti Implementati**  
Regole complete, TUI, GUI, RMI, Socket, principali funzionalità avanzate.

Il progetto Galaxy Trucker ha permesso di affrontare tutte le principali sfide dell’ingegneria del software: progettazione di sistemi distribuiti, implementazione di interfacce utente, gestione della persistenza e della comunicazione di rete.  
L’adozione di pattern consolidati e la cura nella qualità del codice hanno garantito un sistema robusto, manutenibile e conforme alle specifiche.  
L’architettura modulare consente inoltre di estendere facilmente il sistema con nuove funzionalità e modalità di gioco.


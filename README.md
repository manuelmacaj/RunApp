# RunApp – Il tuo compagno per le corse
## Cos'è RunApp?

Applicazione per il monitoraggio delle tue sessioni di corsa. Gli utenti registrati hanno la 
possibilità di salvare le proprie corse e di visualizzare nel dettaglio le informazioni annesse. 
La sessione viene salvata se l’utente percorre almeno 100m. 

Abbiamo utilizzato il servizio Firebase come architettura per il salvataggio e gestione dei dati. <br/><br/>

## Servizi di Firebase utilizzati
- Firebase Authentication: gestione credenziali utenti (Provider: e-mail e password)
- Firebase Firestore database: collezione di utenti e di sessioni corsa 
- Firebase Storage: servizio per memorizzare le foto profilo degli utenti 
- Firebase Crashlytics: servizio per monitoraggio dei crash che possono capitare agli utenti durante l’utilizzo
- Firebase Analytics: servizio per il monitoraggio degli utenti. 

## Funzionalità applicazione:

- Registrazione tramite e-mail e password
- Login tramite e-mail e password
- Sezione attività che mostra le sessioni di corsa sostenute raggruppate in una lista
- Monitoraggio della sessione di corsa (calcolo km, calcolo andatura media, cronometro, salvataggio delle coordinate)
- Sezione profilo utente
- Salvataggio delle sessioni un una collezione dedicata (visibile solo all&#39;utente che l&#39;ha sostenuto)
- Visualizzazione della posizione corrente su Google Maps
- Possibilità di modificare le proprie informazioni (nome e cognome, genere, e-mail, password)
- Possibilità di effettuare il Logout
- Lingue supportate: inglese e italiano
- Possibilità di mostrare in dettaglio una specifica sessione sostenuta con una mappa che mostra il tracciato percorso.
- Possibilità di impostare l&#39;immagine profilo.

## Liste di permessi che l&#39;app chiederà

- Permesso di accesso alla posizione corrente (senza tale permesso, l&#39;app non potrà avviare una sessione corsa)
- Permesso di accesso alla galleria dell&#39;utente per impostare un&#39;immagine di profilo.

## Altre informazioni

- L&#39;app necessita di Google Play Service per l&#39;uso di Firebase e Google Maps.
- Il tema non è dinamico (solo modalità light mode).
- Il GPS deve essere necessariamente attivo, altrimenti non sarà possibile monitorare la corsa correttamente.
- Per gli utenti Xiaomi sarà necessario andare nelle impostazioni -\&gt; app -\&gt; RunApp -\&gt; risparmio batteria e disattivare le restrizioni della MIUI.
- Durante una sessione, l&#39;utente può navigare in altre applicazioni ed essere monitorato in background.
- L&#39;utente deve ricordarsi le credenziali d&#39;accesso (non è presente un recupero password o recupero email).





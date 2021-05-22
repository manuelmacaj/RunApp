package com.manuelmacaj.bottomnavigation.Global

import com.manuelmacaj.bottomnavigation.Model.Utente

/*
Questa classe mi permette di dichiarare degli oggetti che sono globali all'interno dell'applicazione
 */

class Global {

    companion object{
        var utenteLoggato: Utente? = null // utenteLoggato è una variabile che salva i dati dell'utente connesso in quel momento nell'app
    }
}
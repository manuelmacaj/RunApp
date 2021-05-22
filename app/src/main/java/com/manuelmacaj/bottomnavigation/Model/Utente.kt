package com.manuelmacaj.bottomnavigation.Model

class Utente(var idUtente: String, var nomeCognomeUtente: String, var emailUtente: String) { //costruttore della classe utente

    fun toStringUtente(): String { //metodo toStringUtente
        return "ID utente: ${idUtente}, nome e cognome: ${nomeCognomeUtente}, email utente: ${emailUtente}"
    }
}
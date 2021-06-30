package com.manuelmacaj.bottomnavigation.Model

class Utente(
    var idUtente: String,
    var nomeCognomeUtente: String,
    var emailUtente: String,
    var dataNascita: String,
    var genere: String,
    var pathImageProfile: String
) { //costruttore della classe Utente

    fun toStringUtente(): String { //metodo toStringUtente
        return "ID utente: ${idUtente}, nome e cognome: ${nomeCognomeUtente}, email utente: ${emailUtente}, data di nascita: ${dataNascita}, genere: $genere,  URI immagine: $pathImageProfile"
    }
}
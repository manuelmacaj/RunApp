package com.manuelmacaj.bottomnavigation.Model

class Utente { //costruttore della classe utente

    private var idUtente: String = ""
        get() = field
        set(value) {
            field = value
        }

    private var nomeCognomeUtente: String = ""
        get() = field
        set(value) {
            field = value
        }

    private var emailUtente: String = ""
        get() = field
        set(value) {
            field = value
        }

    private var passwordUser: String = ""
        get() = field
        set(value) {
            field = value
        }

    constructor(idUtente: String, nomeCognomeUtente: String, emailUtente: String, passwordUser: String) {
        this.idUtente = idUtente
        this.nomeCognomeUtente = nomeCognomeUtente
        this.emailUtente = emailUtente
        this.passwordUser = passwordUser

    }

    override fun toString(): String {
        return "ID utente: ${idUtente}, nome e cognome: ${nomeCognomeUtente}, email utente: ${emailUtente}"
    }

}
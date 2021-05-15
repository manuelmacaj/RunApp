package com.manuelmacaj.bottomnavigation.Model

class Corsa {

    private var polylineString: String
        get() = field
        set(value) {
            field = value
        }

    private var tempo: String
        get() = field
        set(value) {
            field = value
        }

    private var km: String
        get() = field
        set(value) {
            field = value
        }
    private var dataOrarioPartenza: String
        get() = field
        set(value) {
            field = value
        }

    constructor(polylineEncode: String, time: String, chilometers: String, date: String) {
        polylineString = polylineEncode
        tempo = time
        km = chilometers
        dataOrarioPartenza = date
    }
}
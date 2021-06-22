package com.manuelmacaj.bottomnavigation.Model

class Corsa(
    var polylineString: String,
    var tempo: String,
    var km: String,
    var DataOrarioPartenza: String,
    var andaturaMedia: String
) { //Costruttore classe Corsa
    fun toStringCorsa(): String { //metodo tostringCorsa
        return "PolylineEncode:  ${polylineString}, time: ${tempo}, km: ${km}, date: ${DataOrarioPartenza}, averagePace: $andaturaMedia}"
    }
}
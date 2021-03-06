package com.manuelmacaj.bottomnavigation.Model

import java.util.*

class Corsa(
    var polylineString: String,
    var tempo: String,
    var km: String,
    var DataOrarioPartenza: String,
    var andaturaMedia: String
) { //Costruttore della classe Corsa
    fun toStringCorsa(): String { //metodo tostringCorsa
        return "PolylineEncode:  ${polylineString}, time: ${tempo}, km: ${km}, date: ${DataOrarioPartenza}, averagePace: $andaturaMedia}"
    }
}
package it.polito.kgame

import java.text.FieldPosition

data class User(
    var id: String? = null,
    var mail: String? = null,
    var username: String? = null,
    var familyCode: String? = null,
    var pawnCode: Int? = null,
    var objective: Double? = null,
    var profileImg: String? = null,
    var position: Int? = 0,
    var goodWeightStreak: Int? = 0
    )

package it.polito.kgame

data class Family(
    var code: String? = null,
    var name: String? = null,
    var components: List<User>? = null,
    var lastMatchMillis: Long? = null,
    var matchState: String = "NONE",            // NONE (there is no match at all),
                                                // ACTIVE (someone started the match, but it is not started yet, all players must join),
                                                // STARTED (all players joined and the match is Started and is going),
                                                // ENDED (the match is ended, players may start an other one)
    var playersInGame: Int = 0,
    var lastWinnerMail: String? = null
)

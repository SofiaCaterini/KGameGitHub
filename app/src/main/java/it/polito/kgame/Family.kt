package it.polito.kgame

data class Family(
    var code: String? = null,
    var name: String? = null,
    var components: List<User>? = null
)

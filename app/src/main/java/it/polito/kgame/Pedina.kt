package it.polito.kgame

class Pedina (id: Int?) {

    fun Pedina() {
        //empty constructor needed
    }

    fun Pedina(id:Int?) : Int? {
        var idpedina : Int? = null
        when (id) {
            0 -> {
                idpedina= R.drawable.dog //imageviewpedina.setImageResource(idpedina)
            }
            1 -> {
                idpedina=R.drawable.lion
            }
            2 -> {
                idpedina=R.drawable.owl
            }
            R.drawable.dog -> {
                idpedina = 0
            }
            R.drawable.lion -> {
                idpedina = 1
            }
            R.drawable.owl -> {
                idpedina = 2
            }
        }
        return idpedina
}

}
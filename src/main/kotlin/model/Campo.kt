package model

enum class CampoEvento { ABERTURA, MARCACAO, DESMARCACAO, EXPLOSAO, REINICIALIZACAO }

data class Campo(val linha: Int, val coluna: Int) {
    private val vizinhos = ArrayList<Campo>()
    private val callbacks = ArrayList<(Campo, CampoEvento) -> Unit>()

    var marcado: Boolean = false
    var aberto: Boolean = false
    var minado: Boolean = false

    // Read only
    val desmarcado: Boolean get() = !marcado
    val fechado: Boolean get() = !aberto
    val seguro: Boolean get() = !minado
    val objetivoAlcancado: Boolean get() = seguro && aberto || minado && marcado
    // Filter only neighbors were 'minado' is true, this value is shown in the game field
    val qtdeVizinhosMinados: Int get() = vizinhos.filter { it.minado }.size
    // map() returns a boolean array of safe/unsafe fields then reduce gets only the safe ones
    // if all 'resultado' are true, then return true, returns false otherwise
    val vizinhancaSegura: Boolean
        get() = vizinhos.map { it.seguro }.reduce { resultado, seguro -> resultado && seguro }

    fun addVizinho(vizinho: Campo) {
        vizinhos.add(vizinho)
    }

    fun onEvento(callbaback: (Campo, CampoEvento) -> Unit) {
        callbacks.add(callbaback)
    }

    fun abrir() {
        if (fechado) {
            aberto = true
            if (minado) {
                callbacks.forEach { it(this, CampoEvento.EXPLOSAO) }
            } else {
                callbacks.forEach { it(this, CampoEvento.ABERTURA)}
                // If safe then we open the neighbors
                vizinhos.filter { it.fechado && it.seguro && vizinhancaSegura }.forEach { it.abrir() }
            }
        }
    }

    fun alterarMarcacao() {
        if (fechado) {
            marcado = !marcado
            val evento = if (marcado) CampoEvento.MARCACAO else CampoEvento.DESMARCACAO
            callbacks.forEach { it(this, evento) }
        }
    }

    fun minar() {
        minado = true
    }

    fun reiniciar() {
        aberto = false
        minado = false
        marcado = false
        callbacks.forEach { it(this, CampoEvento.REINICIALIZACAO) }
    }
}

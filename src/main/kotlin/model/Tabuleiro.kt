package model

import java.util.*
import kotlin.collections.ArrayList

enum class TabuleiroEvento { VITORIA, DERROTA }

class Tabuleiro(val qtdeLinhas: Int, val qtdeColunas: Int, private val qtdeMinas: Int) {
    private val campos = ArrayList<ArrayList<Campo>>()
    private val callbacks = ArrayList<(TabuleiroEvento) -> Unit>()

    init {
        gerarCampos()
        associarVizinhos()
        sortearMinas()
    }

    private fun gerarCampos() {
        for (linha in 0 until qtdeLinhas) {
            campos.add(ArrayList())
            for (coluna in 0 until qtdeColunas) {
                val novoCampo = Campo(linha, coluna)
                novoCampo.onEvento(this::verificarDerrotaOuVitoria)
                campos[linha].add(novoCampo)
            }
        }
    }

    private fun associarVizinhos() {
        forEachCampo { associarVizinhos(it) }
    }

    private fun associarVizinhos(campo: Campo) {
        val (linha, coluna) = campo // Using destructuring
        val linhas = arrayOf(linha - 1, linha, linha + 1)
        val colunas = arrayOf(coluna - 1, coluna, coluna + 1)

        linhas.forEach { l ->
            colunas.forEach { c ->
                // if getOrNull(c) returns a valid 'campos' it means we probably have a neighbor
                val atual = campos.getOrNull(l)?.getOrNull(c)
                // if 'campo' different than 'atual/it', then add as neighbor
                atual?.takeIf { campo != it }?.let { campo.addVizinho(it) }
            }
        }
    }

    private fun sortearMinas() {
        val gerador = Random()

        var linhaSorteada = -1
        var colunaSorteada = -1
        var qtdeMinasAtual = 0

        while (qtdeMinasAtual < this.qtdeMinas) {
            linhaSorteada = gerador.nextInt(qtdeLinhas)
            colunaSorteada = gerador.nextInt(qtdeColunas)

            val campoSorteado = campos[linhaSorteada][colunaSorteada]
            if (campoSorteado.seguro) {
                campoSorteado.minar()
                qtdeMinasAtual++
            }
        }
    }

    // We start considering the player has won, if any 'campo' returns false, then we return false
    private fun objetivoAlcancado(): Boolean {
        var jogadorGanhou = true
        forEachCampo { if (!it.objetivoAlcancado) jogadorGanhou = false }
        return jogadorGanhou
    }

    // This method will be invoked every time an event happens in 'campo', whenever a 'campo' is
    // 'aberto', 'marcado' or 'explosao' we need to check if there was a win or loose.
    private fun verificarDerrotaOuVitoria(campo: Campo, evento: CampoEvento) {
        if (evento == CampoEvento.EXPLOSAO) {
            callbacks.forEach { it(TabuleiroEvento.DERROTA) }
        } else if (objetivoAlcancado()) {
            callbacks.forEach { it(TabuleiroEvento.VITORIA) }
        }
    }

    // This method allows to go through all 'campos' in 'tabuleiro'
    // The callback can be a clear event, for example
    // The callback is called 'linha' times
    fun forEachCampo(callback: (Campo) -> Unit) {
        campos.forEach { linha -> linha.forEach(callback) }
    }

    fun onEvento(callback: (TabuleiroEvento) -> Unit) {
        callbacks.add(callback)
    }

    fun reiniciar() {
        forEachCampo { it.reiniciar() }
        sortearMinas()
    }
}
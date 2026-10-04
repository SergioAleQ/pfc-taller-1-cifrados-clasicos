package taller

import scala.annotation.tailrec

/**
 * Taller 1 — cifrados clásicos con recursión.
 *
 * Solo se cifran las 26 letras minúsculas del alfabeto inglés; cualquier otro
 * carácter se copia sin cambio.
 */
class CifradosClasicos {

  type Mensaje = String
  type Clave = String

  // Una frecuencia asocia cada letra con las veces que aparece.
  type Frecuencias = List[(Char, Int)]

  val letras = 26
  val primera = 'a'.toInt

  def esMinuscula(c: Char): Boolean = c >= 'a' && c <= 'z'

  // Punto 1 -------------------------------------------------------------------

  /** Cifra cada carácter y concatena el resultado con el cifrado del resto. */
  def cesar(mensaje: Mensaje, desplazamiento: Int): Mensaje = {
    if (mensaje.isEmpty) ""
    else {
      val caracter = mensaje.head
      val desplazamientoReducido = Math.floorMod(desplazamiento, letras)
      val caracterCifrado =
        if (esMinuscula(caracter)) {
          val posicionOriginal = caracter.toInt - primera
          val posicionCifrada = (posicionOriginal + desplazamientoReducido) % letras
          (primera + posicionCifrada).toChar
        } else caracter

      caracterCifrado.toString + cesar(mensaje.tail, desplazamientoReducido)
    }
  }

  // Punto 2 -------------------------------------------------------------------

  /**
   * El mismo César como proceso iterativo: espacio constante.
   * Cuando la función esté escrita, anótela con @tailrec: el compilador
   * comprueba que la llamada recursiva sea lo último que hace.
   */
  @tailrec
  final def cesarCola(m: Mensaje, k: Int, acc: Mensaje = ""): Mensaje = {
    if (m.isEmpty) acc
    else {
      val c = m.head
      if (esMinuscula(c)) {
        val cCifrado = (((c.toInt - primera + k) % letras + letras) % letras + primera).toChar
        cesarCola(m.tail, k, acc + cCifrado)
      }
      else cesarCola(m.tail, k, acc + c)
    }
  }

  // Punto 3 -------------------------------------------------------------------

  /**
   * Cuenta las letras minúsculas del mensaje, de mayor a menor frecuencia y,
   * en empate, en orden alfabético. El recorrido es recursivo de cola.
   */
  def frecuencias(m: Mensaje): Frecuencias = {

    def actConteo(c: Char, lista: Frecuencias): Frecuencias = {
      if (lista.isEmpty) List((c, 1))
      else if (lista.head._1 == c) (c, lista.head._2 + 1) :: lista.tail
      else lista.head :: actConteo(c, lista.tail)
    }

    @tailrec
    def leer(texto: Mensaje, lista: Frecuencias): Frecuencias = {
      if (texto.isEmpty) lista
      else {
        val c = texto.head
        if (esMinuscula(c)) leer(texto.tail, actConteo(c, lista))
        else leer(texto.tail, lista)
      }
    }

    val conteo = leer(m, List.empty[(Char, Int)])
    conteo.sortWith { (par1, par2) =>
      val (char1, frec1) = par1
      val (char2, frec2) = par2

      if (frec1 != frec2) frec1 > frec2
      else char1 < char2
    }
  }

  // Punto 4 -------------------------------------------------------------------

  /**
   * Supone que la letra más frecuente del mensaje cifrado es la 'e' del
   * original y devuelve la distancia entre las dos. Sin letras, cero.
   */
  def desplazamientoProbable(m: Mensaje): Int = {
    val listaFrecuencias = frecuencias(m)
    if (listaFrecuencias.isEmpty) {
      0
    }
    else {
      val tuplaMasFrecuente = listaFrecuencias.head
      val letra = tuplaMasFrecuente._1
      (letra - 'e' + 26) % 26
    }
  }

  def romperCesar(m: Mensaje): Mensaje = {
    val probable = desplazamientoProbable(m)
    cesarCola(m, -probable)
  }



  // Punto 5 -------------------------------------------------------------------

  /**
   * Cuántos mensajes de longitud n se forman con a letras sin dos iguales
   * seguidas.
   */
  def combinaciones(n: Int, a: Int): BigInt = {
    if (n == 0) BigInt(1)
    else if (n == 1) BigInt(a)
    else BigInt(a - 1) * combinaciones(n - 1, a)

  }

  /**
   * Vigenère: cada letra se corre según la letra de la clave que le toca. Lo
   * que no es letra minúscula se copia y no consume clave.
   */
  def vigenere(m: Mensaje, clave: Clave): Mensaje = {
    if (clave.isEmpty) m
    else {
      @tailrec
      def aux(mensajeRestante: List[Char], iClave: Int, acc: List[Char]): List[Char] = {
        mensajeRestante match {
          case Nil => acc.reverse

          case c :: tail =>
            if (c >= 'a' && c <= 'z') {

              val despl = clave(iClave % clave.length) - 'a'
              val nuevaLetra = ('a' + (c - 'a' + despl) % 26).toChar

              aux(tail, iClave + 1, nuevaLetra :: acc)
            } else {
              aux(tail, iClave, c :: acc)
            }
        }
      }

      aux(m.toList, 0, Nil).mkString
    }
  }
}

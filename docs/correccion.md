# Informe de corrección

## Especificación y supuestos

Se argumenta la corrección de las funciones implementadas en
`CifradosClasicos.scala`. Para los cifrados, se consideran minúsculas del
alfabeto inglés, con posiciones de $0$ a $25$; cualquier otro carácter se
copia sin cambio. Los desplazamientos se interpretan módulo $26$.

En `desplazamientoProbable` y `romperCesar`, la conclusión depende de la
suposición estadística del programa: una letra más frecuente del texto cifrado
corresponde a la `e` del texto original. Para `combinaciones`, el dominio es
$n \geq 0$ y $a \geq 1$. Para Vigenère, se supone una clave no vacía formada
por letras minúsculas; la clave vacía se trata explícitamente.

## Punto 1: `cesar`

Sea $C_k(c)$ el carácter obtenido al desplazar $c$ en $k$ posiciones módulo
$26$ si $c$ es minúscula, y $c$ mismo en caso contrario. La especificación
para un mensaje $m$ es aplicar $C_k$ a cada carácter, conservando el orden.
Demostraremos por inducción estructural sobre el mensaje que
`cesar(m, k)` cumple esta especificación.

**Caso base.** Si $m = ""$, el código devuelve `""`. Aplicar el desplazamiento
carácter por carácter a una cadena vacía también da `""`; por tanto, la
propiedad se cumple.

**Paso inductivo.** Sea $m = c :: r$, donde $c$ es el primer carácter y $r$ el
resto. Como hipótesis inductiva, supongamos que `cesar(r, k)` cifra
correctamente todos los caracteres de $r$. El código calcula la versión
reducida del desplazamiento módulo $26$, transforma $c$ según $C_k$ (o lo
conserva si no es minúscula) y concatena ese resultado con `cesar(r, k)`.
Por la hipótesis inductiva, el sufijo está cifrado correctamente; el primer
carácter también lo está, y el orden no cambia. Así, `cesar(c :: r, k)` cumple
la especificación.

Por inducción estructural, `cesar` cifra correctamente cualquier mensaje
finito.

## Punto 2: `cesarCola`

La función auxiliar recibe un acumulador. Para todo mensaje restante $m$,
desplazamiento $k$ y acumulador $acc$, el invariante es

$$
\texttt{cesarCola}(m,k,acc) = acc \mathbin{+\!\!+} \texttt{cesar}(m,k),
$$

donde $+\!\!+$ denota concatenación de cadenas.

**Caso base.** Si $m = ""$, la función devuelve `acc`. Como
`cesar("", k) = ""`, el lado derecho del invariante es
$acc \mathbin{+\!\!+} "" = acc$. Se cumple.

**Paso inductivo.** Sea $m = c :: r$. La función calcula el carácter
cifrado $C_k(c)$ y hace una llamada de cola con el mensaje $r$ y el acumulador
$acc \mathbin{+\!\!+} C_k(c)$. Para caracteres fuera de `a`–`z`, $C_k(c)=c$.
Por la hipótesis inductiva, esa llamada devuelve

$$
(acc \mathbin{+\!\!+} C_k(c)) \mathbin{+\!\!+} \texttt{cesar}(r,k).
$$

Por asociatividad de la concatenación, esto es
$acc \mathbin{+\!\!+} \texttt{cesar}(c :: r,k)$, que es el invariante. Cada
llamada consume un carácter, así que eventualmente llega al caso base. Con el
acumulador inicial vacío, `cesarCola(m, k)` produce el mismo cifrado que
`cesar(m, k)`.

## Punto 3: `frecuencias`

La especificación es devolver cada letra minúscula presente en el mensaje una
sola vez, asociada a su cantidad de apariciones, ordenada por frecuencia
descendente y, en caso de empate, por orden alfabético.

Primero, `actConteo(c, lista)` conserva las cantidades de todas las letras de
`lista` excepto que agrega una aparición de $c$: si $c$ ya está, incrementa su
cantidad; si no está, agrega $(c,1)$. Esto se demuestra por inducción sobre la
lista. Para la lista vacía, el resultado es `List((c, 1))`. Para una lista no
vacía, si su cabeza es $c$, el código incrementa esa cantidad y deja intacta
la cola; si la cabeza es distinta, la conserva y aplica el mismo razonamiento
inductivo a la cola.

Para `leer(texto, lista)`, el invariante es que la lista resultante representa
las cantidades de las letras del acumulado más las minúsculas del texto aún
por leer.

**Caso base.** Cuando `texto` está vacío, devuelve `lista`. No queda ninguna
letra pendiente y el conteo acumulado es el conteo requerido.

**Paso inductivo.** Sea `texto = c :: resto`. Si $c$ es minúscula,
`actConteo` agrega exactamente una aparición de $c$ al acumulado; de lo
contrario, no cambia el conteo, pues los caracteres que no son minúsculas no
forman parte de la especificación. Después se procesa `resto`. Por hipótesis
inductiva, la llamada recursiva cuenta correctamente ese sufijo. Por tanto, al
terminar, el conteo contiene exactamente las apariciones de las minúsculas del
mensaje original.

Finalmente, `sortWith` compara primero las cantidades en orden descendente y,
si son iguales, los caracteres en orden ascendente. La ordenación cambia el
orden de las tuplas, pero no sus cantidades. Así, `frecuencias` cumple la
especificación.

## Punto 4: `desplazamientoProbable`

Si el mensaje no contiene minúsculas, `frecuencias` devuelve una lista vacía y
la función devuelve $0$, tal como define el caso sin letras. Si sí contiene,
por la corrección de `frecuencias`, su primera tupla tiene una letra con la
mayor frecuencia. Bajo la suposición estadística descrita arriba, esa letra
corresponde a la `e` original después de aplicar el desplazamiento. La función
calcula cuántos lugares se avanzó en el alfabeto desde `e` hasta la letra más
frecuente; al llegar a `z`, el conteo continúa desde `a`. Por ejemplo, si la
letra más frecuente es `h`, el desplazamiento probable es 3, porque de `e` a
`h` hay tres lugares.

En el código, `(letra - 'e' + 26) % 26` hace ese cálculo usando los valores
numéricos de las letras y deja el resultado entre 0 y 25. Por ello devuelve el
desplazamiento correcto cuando se cumple la suposición estadística; sin ella,
el resultado es solo una estimación.

## Punto 4: `romperCesar`

La función obtiene $k$ mediante `desplazamientoProbable` y aplica
`cesarCola(m, -k)`. Si el texto se cifró con desplazamiento $k$ y la
estimación lo recupera correctamente, cada letra minúscula se desplaza primero
$k$ posiciones hacia adelante y luego $k$ hacia atrás. La suma de
desplazamientos es $k + (-k) \equiv 0 \pmod{26}$, por lo que se recupera la
letra original. Los caracteres no minúsculos se conservan en ambas funciones.
Por tanto, `romperCesar` recupera el mensaje bajo la suposición indicada; en
otro caso devuelve el resultado de descifrar con el desplazamiento estimado.

## Punto 5: `combinaciones`

Sea $P(n)$ la afirmación de que `combinaciones(n, a)` cuenta las cadenas de
longitud $n$ formadas con un alfabeto de $a$ letras, sin letras iguales
consecutivas.

**Caso base $n=0$.** Existe exactamente una cadena de longitud cero: la cadena
vacía. El código devuelve $1$.

**Caso base $n=1$.** Cada una de las $a$ letras puede formar una cadena de
longitud uno. El código devuelve $a$.

**Paso inductivo.** Sea $n \geq 2$ y supongamos que
`combinaciones(n - 1, a)` cuenta correctamente los prefijos válidos de
longitud $n-1$. Para cada uno, la última letra de una extensión válida puede
ser cualquiera de las $a-1$ letras distintas de la última letra del prefijo.
Así, el total es

$$
\texttt{combinaciones}(n,a)
=(a-1)\cdot\texttt{combinaciones}(n-1,a).
$$

Esta es precisamente la recurrencia que implementa el código. Junto con los
dos casos base, demuestra por inducción que la función cuenta correctamente
las cadenas para $n \geq 0$ y $a \geq 1$.

## Punto 5: `vigenere`

Si la clave está vacía, la función devuelve el mensaje sin cambios. Esto
coincide con la regla implementada para ese caso. Supongamos ahora que la clave
tiene longitud positiva. `aux` recorre la lista de caracteres del mensaje y
mantiene un índice de clave y un acumulador en orden inverso.

El invariante después de procesar un prefijo $p$ es:

1. El índice `iClave` es la cantidad de minúsculas de $p$; los otros caracteres
   no consumen clave.
2. `acc.reverse` es el resultado Vigenère de $p$, copiando sin cambio los
   caracteres que no son minúsculas.

**Caso base.** Cuando no quedan caracteres, el prefijo procesado es el mensaje
completo. La función devuelve `acc.reverse`, que por el invariante contiene
el resultado completo en el orden original.

**Paso inductivo.** Sea el siguiente carácter $c$.

- Si $c$ es minúscula, se usa la letra de clave en la posición
  `iClave % clave.length`, se desplaza $c$ según esa letra y se incrementa
  `iClave`. Anteponer el carácter cifrado a `acc` hace que, al invertir el
  acumulador, quede después del resultado del prefijo. El invariante se
  conserva.
- Si $c$ no es minúscula, se copia sin cambio y se mantiene `iClave`, pues ese
  carácter no consume clave. Al anteponerlo a `acc`, también queda en la
  posición correcta tras invertir. El invariante se conserva.

En cada llamada se elimina un carácter del mensaje restante, así que la
recursión termina. Por el invariante en el caso base, `vigenere` devuelve el
cifrado correcto según las reglas anteriores.

# Resumen de Estado del Proyecto - POOBSoccer (Head Soccer)

## 1. Arquitectura y Paquete `domain`
El proyecto sigue el modelo de diseño orientado a objetos puro, desacoplado de la interfaz gráfica (`presentation`).

### Archivos y Estado Actual:

- **Geometría y Colisiones Base:**
  - `Figure.java`: Clase abstracta base (`x`, `y`). Métodos abstractos `detectCollisionWithCircle` y `detectCollisionWithRectangle`.
  - `Circle.java`: Detección geométrica círculo-círculo (distancia euclidiana) y círculo-rectángulo (reutiliza el de `Rectangle`). Coordenadas centradas en $(x, y)$.
  - `Rectangle.java`: Detección de intersección caja-caja y caja-círculo verificando los 4 límites simultáneos con `&&`. Coordenadas en esquina superior izquierda.
  - `Collidable.java`: Interfaz con métodos `default` de reacción: `collideWithBall`, `collideWithHead`, `collideWithBody`, `collideWithFoot`.

- **Entidades Físicas:**
  - `Ball.java`: Físicas del balón implementando `Collidable`. Tiene `horizontalSpeed`, `verticalSpeed`, constantes `GRAVITY` y `FRICCION`. Métodos:
    - `collideWithFoot(Foot)`: Impulso en ángulo $(\Delta x / d, \Delta y / d) \times power$.
    - `collideWithHead(Head)`: Rebote suave / cabezazo.
    - `collideWithGB(Rectangle)`: Rebote versátil para travesaño y cuerpo. Si pega en caras planas invierte la componente correspondiente; si pega en esquinas calcula ángulo con hipotenusa.
    - `collideWithEdges()`: Rebote contra techo ($0$), suelo (`Field.HEIGHT`), pared izq ($0$) y pared der (`Field.WIDTH`).
    - `move()`: Desplaza $(x, y)$, aplica gravedad en el aire y fricción horizontal en el suelo.
  - `Head.java`: `Circle`. Registra patadas recibidas (`kicksRecieved`), bandera `struck` para evitar rebotes múltiples en un solo tic, y efecto de cuello atrás/adelante (`isStruck`, `isNotStruck`). Al recibir 5 patadas activa `isStunned = true`.
  - `Foot.java`: `Circle`. Estados `isKicking` y `powerKicking`. Métodos `kicking(Side)` y `stopKicking(Side)` orientados según el lado del jugador.
  - `Body.java`: `Rectangle`. Implementa `Collidable`.

- **Jugadores y Estadísticas:**
  - `Side.java`: `enum` con `LEFT` y `RIGHT`.
  - `Country.java`: `enum` (`COLOMBIA`, `KOREA`, `CANADA`, `BRAZIL`, `ARGENTINA`, `CAMEROON`).
  - `PlayerStats.java`: Valores de velocidad, salto, fuerza, dash, survival, kick.
  - `Player.java`: Agrupa `Head`, `Body`, `Foot`, `PlayerStats`, `Side`, `Country`. Acciones:
    - `moveLeft()`, `moveRight()`, `dashLeft()`, `dashRight()`, `jump()`, `kick()`. Todas bloqueadas si `head.isStunned()`.
    - `moveVertical()`: Física de salto/caída con gravedad hasta el suelo o hasta estar apoyado (`onTop`).
    - `collideWithPlayer(Player)`: Empuje horizontal mutuo según estadísticas de velocidad. Resbala al jugador de arriba si cae sobre la cabeza del rival y activa `onTop`.
    - `collideWithEdges()`: Límites de pantalla para no salirse.
    - `collideWithGoal(Goal)`: Frena caída en travesaño (`onTop`) o choque de cabeza.
    - Puntos y goles con `getPoints()` / `setPoints()`.
  - `MachinePlayer.java` (`extends Player`): IA con método `act(Ball, Player)`. Calcula `targetX` según su lado para ubicarse detrás del balón, patea si está a rango y salta si el balón viene por arriba.

- **Cancha, Porterías y Reglas:**
  - `Goal.java`: `Rectangle` del travesaño. `detectGoal(Ball)` valida que el balón esté dentro del ancho del arco y por debajo del travesaño.
  - `Field.java`: Constantes `WIDTH = 350`, `HEIGHT = 200`, y referencias a `leftGoal` y `rightGoal`.
  - `ScoreBoard.java`: Puntos de ambos jugadores, `isTie()`, y `getWinner()`.
  - `MatchTimer.java`: Cuenta regresiva de 60 segundos (`decrementTime()`, `isTimeUp()`).
  - `GameStatus.java`: `enum` con `PLAYING`, `PAUSED`, `GOLDEN_GOAL`, `FINISHED`.

- **Poderes:**
  - `Power.java`: Clase abstracta con métodos `applyPower(Ball, Player)`, `stopPower(Player)`, `affectBall(Ball, Side)`.
  - `FreezePower.java`: Sube el balón aéreo y lo manda en picada hacia el arco. Si impacta al rival, aplica `setStunned(true)` (bloque de hielo).

---

## 2. Lo que queda pendiente para retomar:
1. **Poderes restantes:** Implementar `ReductionPower` (encoger tamaño) y `ConfusionPower` (invertir controles).
2. **Afilar `MachinePlayer`:** Agregar activación de poder cuando esté listo y comportamiento defensivo si el balón queda detrás de ella.
3. **Controlador principal `HeadSoccer.java`:** Orquestar el método `tic()` que une el timer, movimientos continuos (`move`, `moveVertical`), llamadas a colisiones y chequeo de goles / reinicio de posiciones.
4. **Capa `presentation` (GUI):** Ventana Swing (`JFrame`/`JPanel`), `KeyListener` para controles y `Timer` para invocar `headSoccer.tic()` y redibujar.

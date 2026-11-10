# POOBSoccer — Hoja de ruta del dominio

Punto de partida: commit `fix1` (`86b5ffa`).
Objetivo: dejar el paquete `domain` completo, sin bugs conocidos y con un diseño que no haya que rehacer.

> Este documento reemplaza el orden de `PLAN_DOMINIO.md`. Aquel plan sigue sirviendo como referencia de qué pide el PDF.

---

## Reglas de trabajo

1. **Una fase a la vez, en orden.** Cada fase deja el proyecto compilando y probado antes de pasar a la siguiente.
2. **Bug + diseño juntos.** Cuando un bug vive en código que se va a rediseñar, se arregla *con* el rediseño, nunca antes. Por eso algunos bugs aparecen en fases tardías: arreglarlos antes sería trabajo perdido.
3. **Un commit por paso** (`fase 2.3: efectos de estado en Player`). Push al terminar cada fase.
4. **La GUI solo se toca para que compile.** Si una fase cambia la API del dominio, se hace el ajuste mínimo en `presentation` y nada más. Las mejoras de la vista van después de este documento.
5. **Cada fase termina con sus pruebas JUnit en verde.** La sección "Listo cuando" de cada fase dice qué probar.

Los IDs `B#` son bugs y los `D#` son mejoras de diseño. Ambos están listados al final.

---

## Por qué este orden

```
Fase 0  Repo
Fase 1  Tipos base (Side, Vector2, Physics, Contact) ─┐  todo lo demás se escribe
                                                      │  encima de estos tipos
Fase 2  Player: ancla única + efectos + Country ──────┤  cambia el constructor y la
                                                      │  forma de mover UNA sola vez
Fase 3  Física del balón: un solo rebote ─────────────┤  necesita Contact y el pie
                                                      │  relativo al ancla
Fase 4  Vistas de solo lectura + input ───────────────┤  la COM y la GUI se adaptan
                                                      │  UNA sola vez a las vistas
Fase 5  Poderes ──────────────────────────────────────┤  necesita efectos (2) y
                                                      │  contacto con el balón (3)
Fase 6  Match abstracto + marcador + eventos ─────────┤  esqueleto del partido
Fase 7  Modos de partido ─────────────────────────────┤  se cuelgan del esqueleto (6)
Fase 8  Modalidad + fábrica + fachada HeadSoccer ─────┤  arma todo lo anterior
Fase 9  COM ajustada ─────────────────────────────────┘  usa vistas (4), Side (1),
                                                         modos (7)
Fase 10 Cierre
```

Qué se evita con este orden:
- Arreglar `resetPosition`, el knockback de la cabeza y `onTop` (B12-B15) **antes** del ancla única los obligaría a reescribirlos en la Fase 2. Por eso van en la Fase 2.
- Arreglar la patada en el piso (B3) **antes** de la colisión única la obligaría a reescribirse en la Fase 3.
- Arreglar el poder (B1, B2) **antes** de los efectos de estado obligaría a rehacer `FreezePower`. Por eso va en la Fase 5, después de los efectos (Fase 2).
- Cambiar la COM **antes** de las vistas de solo lectura obligaría a cambiarla dos veces.

---

## Fase 0 — Repo sano (≈ 0.5 h)

| Paso | Tarea | Resuelve |
|---|---|---|
| 0.1 | Crear `.gitignore` con `bin/` y ejecutar `git rm -r --cached ProyectoHeadSoccer/bin` | B17 |
| 0.2 | Volver a subir el PDF del enunciado (`POOBSoccer_2026-2.pdf`, sin el ` (1)` en el nombre) | B18 |
| 0.3 | Configurar JUnit 5 en el proyecto de Eclipse y crear la carpeta `test/domain` | B19 |

**Listo cuando:** un clon limpio compila y corre una prueba trivial en verde.

---

## Fase 1 — Tipos base (≈ 2.5 – 3.5 h)

Estas clases las usan todas las fases siguientes. Hacerlas primero evita reescribir código después.

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 1.1 | `Side.java` | Agregar métodos: `dir()` (`+1` LEFT, `-1` RIGHT: hacia dónde ataca), `opposite()`, `startX()` (posición inicial del jugador). Reemplazar los 13 ternarios `side == Side.LEFT ? … : …` del dominio | D1 |
| 1.2 | `Vector2.java` (nuevo) | Clase inmutable con `x`, `y`, `plus`, `minus`, `scale`, `dot`, `length`, `normalized()` (si la longitud es 0, devuelve `(0, -1)`). Constantes `ZERO` y `UP` | D2 |
| 1.3 | `Physics.java` | Agregar `TICK_MS = 1000 / FPS` (la GUI lo usará en su `Timer`). Mover aquí las constantes de superficies: `HEAD_RESTITUTION`, `BODY_RESTITUTION`, `BAR_RESTITUTION`, `WALL_RESTITUTION`, `HEAD_IMPULSE` | B5, D3 |
| 1.4 | `Contact.java` (nuevo) | Resultado de una colisión: `normal` (`Vector2`, sale de la figura hacia el balón) y `penetration` (`double`). Inmutable | D4 |
| 1.5 | `Figure.java` | Nuevo método abstracto `Contact contactWith(Circle c)` que devuelve el contacto o `null` si no se tocan | D4 |
| 1.6 | `Circle.java` | Implementar `contactWith`: normal = (centro del otro − centro propio) normalizada; penetración = radios − distancia | D4 |
| 1.7 | `Rectangle.java` | Implementar `contactWith` con el punto más cercano que ya tienen en `detectCollisionWithCircle`. Si el centro del círculo está dentro del rectángulo, usar la cara más cercana como normal | D4 |
| 1.8 | `Circle` / `Rectangle` | `detectCollisionWithCircle` pasa a ser `return contactWith(c) != null;` (sin duplicar cálculos) | D4 |

**Listo cuando:** pruebas de `Vector2` (normalizar el vector cero no da NaN) y de `contactWith` (de lado, de frente, esquina sin contacto, círculo dentro del rectángulo).

---

## Fase 2 — Player: ancla única, efectos y Country (≈ 6 – 8 h)

La fase más importante. Cambia el constructor de `Player` y la forma de moverlo **una sola vez**, y arregla los bugs que viven en ese código.

### 2A. Country como fábrica

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 2.1 | `PlayerStats.java` | Atributos `final` (velocidad, salto, fuerza), validación 1-5 **en el constructor** (lanza `HeadSoccerException`), borrar los setters. Mantener `speedPx()`, `jumpPx()` y `kickPx()` | D5, B20 |
| 2.2 | `Country.java` | Cada país lleva sus stats y su poder: `CANADA(new PlayerStats(3, 2, 4), FreezePower::new)`. Métodos `defaultStats()` y `createPower()`. Por ahora todos los países pueden usar `FreezePower`; en la Fase 5 se asignan los poderes nuevos | D6 |

### 2B. Ancla única y partes relativas

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 2.3 | `Player.java` | Nuevo atributo `anchor` (`Vector2`: punto en el piso, al centro del jugador). Constructor: `Player(String name, Country country, Side side)` (las stats y el poder salen del país) | D7 |
| 2.4 | `Head`, `Body`, `Foot` | Ya no guardan posición absoluta. Guardan su **desplazamiento** respecto al ancla y su tamaño base. Cada una tiene un método `shape(Vector2 anchor, Side side, double scale)` que devuelve su `Circle` o `Rectangle` ya posicionado. El pie usa otro desplazamiento mientras patea. La cabeza suma un desplazamiento temporal de knockback | D7, B12 |
| 2.5 | `Player.java` | `getHeadShape()`, `getBodyShape()` y `getFootShape()` calculan las figuras desde el ancla. Borrar `translate` x3: mover es `anchor = anchor.plus(...)` | D7 |
| 2.6 | `Player.java` | `resetPosition()`: `anchor = (side.startX(), Field.HEIGHT)`, velocidad 0, sin patada, sin knockback, intención anterior vacía. **No recrea objetos** | B15 |
| 2.7 | `Player.java` | Física vertical sobre el ancla: `verticalSpeed`, gravedad y piso en `anchor.y = Field.HEIGHT` | — |
| 2.8 | `Player.java` | `onTop` unificado: al inicio de cada tick se reinicia en `false`, y **solo** las colisiones con soporte (cabeza del rival, travesaño) lo ponen en `true`. Borrar `collideMyFootWithBall`. En `collideWithGoal`, quitar el `else onTop = false` | B13, B14 |
| 2.9 | `Player.java` | `collideWithPlayer`: el empuje usa `stats.speedPx()`, no `getSpeed()` | B16 |
| 2.10 | `Head.java` | El knockback vuelve solo: al recibir una patada se pone un desplazamiento temporal con N ticks, y en `tick()` se descuenta. Borrar `isStruck`/`isNotStruck`. Renombrar `kicksRecieved` → `kicksReceived` | B12, B25 |
| 2.11 | `Player`, `Head`, `Body`, `Foot` | Borrar los setters de partes, posición y estado (`setHead`, `setBody`, `setFoot`, `setCircle`, `setSide`, `setStats`, `setOnTop`, `setVerticalSpeed`, `setCountry`, `setName`, `setPower`). Lo que se fija al crear va en el constructor y es `final` | B20 |

### 2C. Efectos de estado

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 2.12 | `EffectType.java` (nuevo) | `enum EffectType { STUNNED, SHRUNK, CONFUSED }` | D8 |
| 2.13 | `Player.java` | `EnumMap<EffectType, Integer> effects` (ticks restantes). Métodos `addEffect(EffectType, double seconds)`, `hasEffect(EffectType)`, `effectTicks(EffectType)`. En `tick()` se descuentan y se quitan al llegar a 0 | D8 |
| 2.14 | `Head.java` | Quitar `isStunned`, `setStunned` y su contador. Las 5 patadas recibidas llaman `player.addEffect(STUNNED, 3)` (el `Player` revisa `head.kicksReceived()` después de las colisiones, o la cabeza lo informa con un `boolean`) | D8 |
| 2.15 | `Player.java` | La escala del jugador es `hasEffect(SHRUNK) ? 0.5 : 1.0` y se pasa a `shape(...)` | D8 |

### Ajuste mínimo de la GUI
`PhotoHeadSoccer` debe pasar a usar `getHeadShape()`, `getBodyShape()`, `getFootShape()` y `hasEffect(STUNNED)`.

**Listo cuando:**
- Moverse, saltar (más de 50 px) y aterrizar en el piso exacto.
- La cabeza recibe 5 patadas y el jugador queda aturdido 180 ticks; el knockback vuelve solo.
- `resetPosition` deja al jugador en `side.startX()` sin perder efectos.
- `PlayerStats(0, 3, 3)` lanza excepción.

---

## Fase 3 — Física del balón: un solo rebote (≈ 3 – 4 h)

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 3.1 | `Ball.java` | Posición por su `Circle`; velocidad como `Vector2 velocity`. Borrar `horizontalSpeed`/`verticalSpeed` y sus setters (dejar `getVelocity()` y `setVelocity(Vector2)` solo para uso del dominio) | D2 |
| 3.2 | `Ball.java` | Un solo método `boolean bounceOff(Figure f, double restitution, Vector2 impulse)`: pide `f.contactWith(circle)`; si es `null` devuelve `false`. Si hay contacto: saca el balón la distancia de penetración en la dirección normal, refleja la velocidad (`v − (1 + e)(v·n)n`, solo si `v·n < 0`), suma el impulso y devuelve `true` | D4, B24 |
| 3.3 | `Ball.java` | Borrar `collideWithFoot`, `collideWithHead` y `collideWithGB`. `collideWithEdges` se queda (paredes del campo) pero usa `Physics.WALL_RESTITUTION` | D4 |
| 3.4 | `Collidable.java` | Borrar la interfaz (solo tenía métodos vacíos) y quitar los `implements Collidable` | B21 |
| 3.5 | `Foot.java` | Arco de la patada: desplazamiento al patear `(side.dir() × KICK_REACH_X, -KICK_REACH_Y)` con valores pequeños en Y (por ejemplo 20 y 8) como constantes, para que le pegue al balón en el piso | B3 |
| 3.6 | `Player.java` | `kickImpulse()`: si está pateando, `Vector2(side.dir(), -0.6).normalized().scale(stats.kickPx())`; si no, `Vector2.ZERO` (el pie quieto solo rebota) | B3 |
| 3.7 | `Match.java` | `resolveCollisions` usa `bounceOff` con la figura y el impulso de cada parte: travesaños, cuerpos, pies (`kickImpulse()`) y cabezas (impulso hacia arriba pequeño). Guardar qué jugador tocó el balón en este tick (lo usará la Fase 5) | D4 |

**Listo cuando:**
- Balón quieto en el piso delante del pie + patada → el balón sale hacia el arco rival y hacia arriba.
- El balón no atraviesa travesaños, paredes ni piso, y un toque no lo golpea varias veces en ticks seguidos.
- Balón quieto lejos de todos → sigue quieto después de 1 tick.

---

## Fase 4 — Vistas de solo lectura e input (≈ 3 – 4 h)

Las vistas van **antes** de tocar poderes, partido y COM, para que la COM y la GUI se adapten una sola vez.

### 4A. Vistas

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 4.1 | `BallView.java` (nuevo) | Interfaz con solo lectura: `getX()`, `getY()`, `getRadius()`, `getVelocity()`. `Ball` la implementa | B7, D9 |
| 4.2 | `PlayerView.java` (nuevo) | Interfaz: `getName()`, `getCountry()`, `getSide()`, `getAnchor()`, `getHeadShape()`, `getBodyShape()`, `getFootShape()`, `isKicking()`, `hasEffect(EffectType)`, `getPowerCharge()`, `isPowerArmed()`, `isOnGround()`. `Player` la implementa | B7, D9 |
| 4.3 | Figuras | Las figuras que devuelven las vistas son **nuevas en cada llamada** (se calculan desde el ancla), así que modificarlas no afecta al jugador | B7 |
| 4.4 | `ReaderMatch.java` | Cambiar a `PlayerView getPlayer(Side)`, `BallView getBall()`, `int getGoals(Side)`, `GameStatus getState()`. Borrar `getPlayer1/2`, `getField` (las medidas son constantes estáticas) y `getScoreBoard` | B7 |

### 4B. Input

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 4.5 | `Controller.java` | Firma: `Set<PlayerAction> decide(ReaderMatch match)`. Se mantiene el `default void receive(...)` | — |
| 4.6 | `HumanController.java` | `EnumSet` en vez de `HashSet`, y copia con `EnumSet.copyOf` al recibir y al devolver (cuidado con el `Set` vacío: usar `EnumSet.noneOf`) | B6 |
| 4.7 | `Player.java` | El jugador tiene su control (`private final Controller control`), recibido en el constructor. Métodos `receiveIntention(Set)` (delega en `control.receive`) y `think(ReaderMatch)` (devuelve `control.decide(...)`) | D10 |
| 4.8 | `Player.java` | `applyInput(Set<PlayerAction>)` sin `switch` y en este orden: (1) `null` → vacío; (2) si `STUNNED` → guardar como anterior y salir; (3) izquierda/derecha, intercambiadas si `CONFUSED`, sin movimiento si vienen las dos; (4) salto; (5) patada solo en flanco de subida; (6) poder solo en flanco de subida; (7) guardar como intención anterior. Quitar el `stopKick()` (la patada termina sola) y los parámetros `ball` y `rival` | B4, B8 |
| 4.9 | `Player.java` | Quitar los chequeos de aturdido repetidos en `moveLeft`, `moveRight`, `jump` y `kick` (ya están al inicio de `applyInput`) | B8 |

### Ajuste mínimo
- **COM:** que compile con las vistas (`match.getPlayer(side)`, `match.getPlayer(side.opposite())`). La mejora real va en la Fase 9.
- **GUI:** mandar las intenciones con `receiveIntention` (o con la fachada de la Fase 8, si prefieren esperar) y dibujar desde las vistas.

**Listo cuando:**
- Mantener KICK 2 s produce 1 patada.
- Con `STUNNED`, ninguna acción tiene efecto; con `CONFUSED`, LEFT mueve a la derecha.
- Modificar el `Set` que se le pasó al humano no cambia lo que decide.
- La COM no puede llamar `jump()` ni `setVelocity()` (no compila).

---

## Fase 5 — Poderes (≈ 3 – 4 h)

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 5.1 | `Power.java` | La barra **empieza en 0**. Estados: `charge`, `armed` y `shotTicks` (cuánto le queda al tiro con poder para impactar al rival). Borrar `active` | B1, D11 |
| 5.2 | `Power.java` | `tick()` carga la barra (o descuenta `shotTicks`). `arm()`: si está llena, `armed = true` y `charge = 0` | B1 |
| 5.3 | `Power.java` | `onBallTouched(Ball, Side)`: si `armed`, llama `affectBall(...)` (efecto del tiro), `armed = false`, `shotTicks = Physics.seconds(2)` | B1 |
| 5.4 | `Power.java` | `onBallHitRival(Player rival)`: si `shotTicks > 0`, llama `applyTo(rival)` y `shotTicks = 0` | B1 |
| 5.5 | `Power.java` | Métodos abstractos: `affectBall(Ball, Side)` y `applyTo(Player rival)`. Borrar `stopPower` (la duración la manejan los efectos) | D8, D11 |
| 5.6 | `FreezePower.java` | `applyTo`: `rival.addEffect(STUNNED, 3)`. `affectBall`: tiro con `side.dir()` | B1 |
| 5.7 | `ShrinkPower.java` (nuevo) | `applyTo`: `rival.addEffect(SHRUNK, 5)`. `affectBall`: tiro propio | B23 |
| 5.8 | `ConfusionPower.java` (nuevo) | `applyTo`: `rival.addEffect(CONFUSED, 5)`. `affectBall`: tiro propio | B23 |
| 5.9 | `Country.java` | Asignar el poder de cada país (por ejemplo CANADA → Freeze; dos países → Shrink; dos → Confusion) | B23 |
| 5.10 | `Match.java` | En `resolveCollisions`: si el jugador tocó el balón en este tick → `power.onBallTouched(...)`; si el balón toca cualquier parte del rival → `power.onBallHitRival(rival)` del que pateó. Quitar el código que **saltaba las colisiones del dueño** del poder | B2 |

**Listo cuando:**
- La barra empieza vacía y se llena en el tiempo definido.
- `POWER` con la barra incompleta no hace nada.
- El poder no afecta al balón hasta que el jugador lo toca, y el efecto solo cae si el balón golpea al rival.
- Cada efecto dura lo esperado y se quita solo.
- El dueño del poder siempre choca con el balón.

---

## Fase 6 — Match abstracto, marcador y eventos (≈ 3.5 – 4.5 h)

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 6.1 | `ScoreBoard.java` | `EnumMap<Side, Integer>` con los goles; un único `goalIn(Side goalSide)` que suma al lado contrario (autogol incluido); `getGoals(Side)`, `isTie()`, `getLeader()` devolviendo `Optional<Side>`. Borrar `golIn` y las referencias a jugadores | B11 |
| 6.2 | `Player.java` | Borrar `points`, `getPoints()` y `setPoints()` | B11 |
| 6.3 | `GameStatus.java` | Agregar `NOT_STARTED` y `GOAL_SCORED` | B10 |
| 6.4 | `MatchEvent.java` (nuevo) | `enum MatchEventType { GOAL, POWER_ARMED, POWER_HIT, PLAYER_STUNNED, GOLDEN_GOAL, MATCH_OVER }` + clase `MatchEvent(type, side)` | D12 |
| 6.5 | `Match.java` | Pasa a ser **`abstract`**. Los jugadores se guardan en un `EnumMap<Side, Player>`. Atributos `goalPauseTicks` y `List<MatchEvent> lastEvents` (se vacía al inicio de cada tick) | B10, D12, D13 |
| 6.6 | `Match.java` | `public final void tick()` (template method): (1) si `NOT_STARTED`, `PAUSED` o `FINISHED` → salir; (2) si `GOAL_SCORED` → descontar la pausa y, al terminar, `resetPositions()` y volver a jugar; (3) cada jugador: `applyInput(think(this))`; (4) `tick()` de jugadores y balón; (5) colisiones; (6) gol → `scoreBoard.goalIn`, `onGoal(scorer)`, evento, `GOAL_SCORED`; (7) `modeTick()`; (8) `if (isOver())` → `FINISHED` + evento | B10, D13 |
| 6.7 | `Match.java` | Huecos para los modos: `protected abstract void onGoal(Side scorer)`, `protected void modeTick() {}`, `protected abstract boolean isOver()`, `public abstract Optional<Side> getWinner()` | D13 |
| 6.8 | `Match.java` | `start()`, `pause()`, `resume()` con validación de estado. Borrar `setState` público y los getters/setters de controles | B22 |
| 6.9 | `Match.java` | `getLastEvents()` (lista de solo lectura) y agregarlo a `ReaderMatch` | D12 |

**Listo cuando:**
- Gol completo suma; balón sobre la línea no suma; autogol suma al rival.
- Después del gol hay una pausa de 1 s y luego se reinician las posiciones.
- En pausa, ningún tick cambia nada.
- Un tick con gol deja un evento `GOAL` del lado correcto.

---

## Fase 7 — Modos de partido (≈ 5 – 7 h)

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 7.1 | `TimeMatch.java` (nuevo) | `ticksLeft = Physics.seconds(60)`. `modeTick()`: descuenta mientras se juega; al llegar a 0, si hay empate → `GOLDEN_GOAL` + evento. `isOver()`: tiempo en 0 sin empate, o un gol durante `GOLDEN_GOAL`. `getSecondsLeft()` | B10 |
| 7.2 | `GoalsMatch.java` (nuevo) | Constructor con `target`; si no es 3, 5 o 7 → `HeadSoccerException(INVALID_GOAL_TARGET)`. `isOver()`: algún lado llegó al objetivo | B10 |
| 7.3 | `SurvivalMatch.java` (nuevo) | `EnumMap<Side, Integer> balls`. `onGoal(scorer)`: le quita un balón al rival. `isOver()`: algún lado en 0. `matchesWon` y `nextMatch()` (reinicia balones, marcador y posiciones; puede subir la dificultad de la COM). Validar PvsM → `HeadSoccerException(SURVIVAL_ONLY_PVSM)` | B10 |
| 7.4 | `MatchTimer.java` | Borrar (lo reemplaza `TimeMatch`) | — |
| 7.5 | `HeadSoccerException.java` | Constantes `INVALID_GOAL_TARGET`, `SURVIVAL_ONLY_PVSM`, `INVALID_STATE` | — |
| 7.6 | `ReaderMatch.java` | Agregar `int getSecondsLeft()` (`-1` si el modo no tiene tiempo) y `int getBallsLeft(Side)` (`-1` si no aplica), para que la vista no pregunte el tipo de partido | — |

> Antes de 7.3: confirmar con el profesor cómo funciona supervivencia (el PDF es ambiguo).

**Listo cuando:**
- `TimeMatch` termina a los 3600 ticks con ganador; con empate pasa a gol de oro y termina con el primer gol.
- `GoalsMatch(3)` termina al tercer gol; `GoalsMatch(4)` lanza excepción.
- `SurvivalMatch` termina al quedarse un lado sin balones, y rechaza PvsP y MvsM.

---

## Fase 8 — Modalidad, fábrica y fachada (≈ 2 – 3 h)

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 8.1 | `Modality.java` (nuevo) | `enum Modality { PVP, PVM, MVM }` | — |
| 8.2 | `MatchType.java` (nuevo) | `enum MatchType { TIME, GOALS, SURVIVAL }` | — |
| 8.3 | `MatchFactory.java` (nuevo) | `create(Modality, MatchType, int goalTarget, Country left, Country right)`: crea los controles según la modalidad (`HumanController` o `COMController(side)`), los jugadores con su control y el `Match` concreto. Valida las combinaciones (supervivencia solo en PvsM) | B22 |
| 8.4 | `HeadSoccer.java` | Fachada única para la vista: `newMatch(...)` (usa la fábrica), `tick()`, `pause()`, `resume()`, `receiveIntention(Side, Set<PlayerAction>)` y `getMatch()` devolviendo `ReaderMatch`. Borrar la configuración fija del constructor, `getTimer()`, `message`, `setState` y los getters de controles | B22 |

**Listo cuando:** se puede crear cualquier combinación válida desde una prueba, jugarla con `tick()` y mandar intenciones con `receiveIntention`, sin tocar ninguna clase interna.

---

## Fase 9 — COM ajustada (≈ 2.5 – 4 h)

| Paso | Archivo | Tarea | Resuelve |
|---|---|---|---|
| 9.1 | `COMController.java` | Usar solo `ReaderMatch` y las vistas; obtenerse con `match.getPlayer(side)` y al rival con `match.getPlayer(side.opposite())` | B7 |
| 9.2 | `COMController.java` | Reemplazar los ternarios por `side.dir()` y los números mágicos por constantes con nombre derivadas de los radios | D1 |
| 9.3 | `COMController.java` | `enum` de estados **privado y anidado**; quitar los setters públicos de estado y lado | B9 |
| 9.4 | `COMController.java` | Histéresis: cambiar de ATTACK a DEFEND solo si el balón está detrás por más de un margen, y volver solo si está delante por otro margen | B9 |
| 9.5 | `COMController.java` | DEFEND correcto: ubicarse **entre el balón y su arco** (no correr siempre a la pared) y no empujar el balón hacia su propio arco | B9 |
| 9.6 | `COMController.java` | Poder con intención: pedir POWER solo si está a punto de tocar el balón y el tiro va hacia el arco rival | — |
| 9.7 | Opcional | Predicción del punto de caída con una copia del balón; dificultad con un `Random` inyectable | — |

**Listo cuando:** en una simulación MvsM de 60 s, el marcador queda entre 0 y 15 por lado, ninguna COM se queda trabada contra una pared y no hay rachas de autogoles.

---

## Fase 10 — Cierre (≈ 2 – 3 h)

| Paso | Tarea |
|---|---|
| 10.1 | Revisión de nombres e idioma (todo en inglés en el código) |
| 10.2 | Borrar código muerto (`dashLeft`/`dashRight` si no se usan, imports, constantes sin uso) |
| 10.3 | Javadoc actualizado en las clases públicas |
| 10.4 | Diagrama de clases actualizado |
| 10.5 | Correr todas las pruebas y una simulación MvsM de 5 minutos sin excepciones |

---

## Resumen de tiempos

| Fase | Horas |
|---|---|
| 0 — Repo | ≈ 0.5 |
| 1 — Tipos base | 2.5 – 3.5 |
| 2 — Player (ancla, efectos, Country) | 6 – 8 |
| 3 — Física del balón | 3 – 4 |
| 4 — Vistas e input | 3 – 4 |
| 5 — Poderes | 3 – 4 |
| 6 — Match abstracto | 3.5 – 4.5 |
| 7 — Modos | 5 – 7 |
| 8 — Modalidad y fachada | 2 – 3 |
| 9 — COM | 2.5 – 4 |
| 10 — Cierre | 2 – 3 |
| **Total (incluye pruebas)** | **≈ 33 – 45 h** |

Con dos personas a 10-15 h por semana cada una: **≈ 1.5 a 2 semanas**.

**Reparto sugerido:** las fases 0-4 en pareja (todo depende de ellas). Después, en paralelo: una persona hace 5 → 9 y la otra 6 → 7 → 8. Se juntan para el cierre.

---

## Anexo A — Bugs (estado en `fix1`)

| ID | Bug | Fase |
|---|---|---|
| B1 | La barra empieza llena y el poder lanza el balón al activarse, aunque el jugador no lo toque ("control remoto"); en MvsM produce 0-18 en 60 s | 5 |
| B2 | Con el poder activo, el dueño atraviesa el balón hasta que el tiro toque al rival | 5 |
| B3 | La patada no le pega a un balón en el piso (el pie sube 25 px) | 3 |
| B4 | Mantener KICK patea muchas veces (10 en 2 s) | 4 |
| B5 | El `Timer` de la GUI (20 ms) no coincide con `Physics.FPS` (60); los tiempos duran un 20% más | 1 |
| B6 | `HumanController` guarda y devuelve la misma referencia del `Set` | 4 |
| B7 | `ReaderMatch` devuelve `Player` y `Ball` reales: la COM puede modificar el partido | 4 |
| B8 | `applyInput` sin chequeo central de aturdido; izquierda + derecha se cancelan por casualidad | 4 |
| B9 | COM sin histéresis, DEFEND corre siempre a la pared, estado y lado con setters públicos | 9 |
| B10 | `Match` sin modos, sin tiempo ni fin de partido | 6, 7 |
| B11 | `ScoreBoard` guarda los puntos en `Player`, con `golIn`/`goalIn` duplicados y `getWinner()` que devuelve `null` | 6 |
| B12 | El knockback de la cabeza nunca vuelve (`isNotStruck` no se llama) | 2 |
| B13 | `collideMyFootWithBall` pone `onTop` caminando contra el balón | 2 |
| B14 | `collideWithGoal` (x2) y `collideWithPlayer` se sobrescriben el `onTop` | 2 |
| B15 | `resetPosition` recrea cabeza, cuerpo y pie | 2 |
| B16 | `collideWithPlayer` empuja con `getSpeed()` (1-5) en vez de `speedPx()` | 2 |
| B17 | `bin/` versionado, sin `.gitignore` | 0 |
| B18 | El PDF del enunciado se borró del repo | 0 |
| B19 | No hay pruebas | 0 y todas |
| B20 | 42 setters públicos permiten romper el estado desde afuera | 2 |
| B21 | `Collidable` solo tiene métodos vacíos | 3 |
| B22 | `HeadSoccer` con configuración fija, `getTimer()` que devuelve `null`, `message` como texto y `setState` público | 6, 8 |
| B23 | Solo existe `FreezePower`; faltan Reducción y Confusión | 5 |
| B24 | El balón puede recibir varios golpes del mismo pie en ticks seguidos (no se separa) | 3 |
| B25 | Nombre `kicksRecieved` | 2 |

## Anexo B — Mejoras de diseño

| ID | Mejora | Fase |
|---|---|---|
| D1 | `Side` con `dir()`, `opposite()` y `startX()` | 1 |
| D2 | `Vector2` para velocidades y cálculos | 1, 3 |
| D3 | Constantes de superficies en `Physics` | 1 |
| D4 | `Contact` + un solo `bounceOff` para todo el balón | 1, 3 |
| D5 | `PlayerStats` inmutable con validación en el constructor | 2 |
| D6 | `Country` crea sus stats y su poder | 2 |
| D7 | `Player` con ancla única y partes relativas | 2 |
| D8 | Efectos de estado (`STUNNED`, `SHRUNK`, `CONFUSED`) con duración en ticks | 2, 5 |
| D9 | Vistas de solo lectura `PlayerView` y `BallView` | 4 |
| D10 | El control vive en `Player` | 4 |
| D11 | `Power` armado al tocar el balón, efecto al golpear al rival | 5 |
| D12 | Eventos del partido en vez de mensajes de texto | 6 |
| D13 | `Match` abstracto con template method | 6 |

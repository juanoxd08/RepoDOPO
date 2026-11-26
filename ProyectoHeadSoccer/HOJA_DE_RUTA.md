# DOPOSoccer 2026-2 — Hoja de ruta

Código de partida: commit **`fix12`** (`10f321e`).
Enunciado: `DOPOSoccer_2026-2.pdf`.

> **Estado:** el código está terminado: todos los bugs medidos están arreglados. Falta: **archivos de sonido**, **entregables** (pruebas de aceptación, ajustes de diagramas, planificación), revisiones cortas (los detalles del poder ya se arreglaron).

---

## Calendario

| Entrega | Qué pide el PDF | Fecha |
|---|---|---|
| **V3** | Lo anterior + lo propuesto para el ciclo 3, **pruebas de aceptación** | Do 22 nov |
| **V4** | Lo anterior + lo propuesto para el ciclo 4 | Parcial (S17) |
| Competencia | Todo completo | Vi 4 dic |

---

## Antes de seguir · ≈ 5 min

- [x] **Prueba del piso:** arreglo aplicado en `Ball.bounceOff` y la prueba `testFloorDisputeNeverPushesBallBelowFloor` de vuelta (`fix9`).
- [ ] **Siguen subidos archivos generados** (en `fix11` el log creció otras 619 líneas): `DOPOSoccer.log` y `profiles.dat`. Agregarlos a `.gitignore` y sacarlos del repositorio sin borrarlos del disco (`git rm --cached ProyectoHeadSoccer/DOPOSoccer.log ProyectoHeadSoccer/profiles.dat`). Si no, cada partido cambia esos archivos y genera conflictos entre los dos.

---

## Cambios del 27 nov (rama `claude/brave-newton-pne4dc`)

- **Aura y tiro rival, como dice el PDF (regla 5):** se quitó la protección del aura (BUG-17). Si el tiro de poder golpea a un rival con aura, este recibe el daño y el efecto, se avisa `onPowerHit` y, al tocar el balón, responde con su propio tiro. Pruebas ajustadas: `testPowerShotAgainstRivalWithAuraAppliesHitAndTriggersCounterShot` y `testPowerShotOnRivalWithAuraNotifiesPowerHit`.
- **Pantalla de resolución:** los botones decían 700×400 y 1400×800; ahora muestran las resoluciones reales (840×480, 1050×600 y 1260×720) con `FieldResolution.getLabel()`.

## Cambios en `fix12` (verificado)

- Compila; **85 de 85 pruebas pasan**, y siguen pasando en **10 corridas seguidas** (importante, por la patada aleatoria de abajo). Ningún comentario `//`.
- **BUG-17 arreglado (decisión del equipo):** si el rival tiene el aura activa, `Player.receiveHit` no le aplica **ni el daño ni el efecto**; al tocar el balón responde con su propio tiro. Medido: Camerún con aura recibe el tiro de Corea y queda con 100 de vida, sin efecto, y dispara su tiro. Como el PDF dice que el golpe aplica daño y efecto (regla 5), **mencionarlo como decisión de diseño en la sustentación**.
- **BUG-18 arreglado:** el balón en carga es un obstáculo fijo; `PlayerBody.pushOutOfChargingBall` saca al jugador hacia los lados (cabeza, pie y cuerpo), y no empuja al que está cargando. Medido en 22 casos (los dos lados, varias distancias, caminando y saltando): el rival **nunca** quedó dentro del balón ni lo cruzó. Prueba nueva: `testRivalCannotWalkThroughChargingBallFromEitherSide`.
- **Cambio nuevo, no estaba en la hoja:** la patada sale con un ángulo aleatorio (`KICK_LIFT_VARIANCE` = ±0.30, con `Math.random()`). Consecuencias:
  - El juego ya no es determinista: el mismo partido con las mismas teclas puede terminar distinto. Por eso la prueba de simetría de la patada ahora solo comprueba la dirección.
  - **Mala práctica:** `Math.random()` dentro del dominio no se puede controlar en las pruebas. Mejor un `Random` inyectable (como en `ClumsyMachine`), o quitar la variación si no se necesita.
- Siguen bien: BUG-13 (0 tiros que tocan al rival sin contar, en los 4 países) y las 4 máquinas.

**Ajustes de juego (después de `fix12`, en la rama `claude/brave-newton-pne4dc`):**
- [x] **Patada que puede salir más baja:** la inclinación ahora varía entre **24° y 55°** (antes entre 40° y 55°), con `KICK_LIFT_HIGHEST` = −1.45 y `KICK_LIFT_LOWEST` = −0.45 en `PlayerBody`. Las más bajas suben unos 27 px y entran al arco por debajo del travesaño. Se borró `Physics.KICK_LIFT_FACTOR`, que quedó sin uso.
- [x] **Salto un poco más alto:** `Physics.BASE_JUMP_IMPULSE` de 7 a **8** (de ~28 a ~36 px). Ahora la cabeza alcanza a tocar el travesaño desde abajo y rebota; se ajustó `testJumpingUnderCrossbarDoesNotTrapHeadInside` para contar penetración real (más de 0.5 px) y no el simple contacto. También `testLeftAndRightPhysicsSymmetry`, que ahora mide la velocidad vertical más alta del balón durante los 10 ticks.

**Detalles menores (arreglados después de `fix12`, en la rama `claude/brave-newton-pne4dc`):**
- [x] **`onPowerHit` ya no se avisa si el aura protegió al rival:** `Match.applyPowerHit` (un solo método para los dos jugadores, en lugar del código repetido) aplica el golpe y solo avisa si el rival no tenía aura. Prueba: `testPowerShotOnRivalWithAuraDoesNotNotifyPowerHit` (falla con el código de `fix12` y pasa con el arreglo).
- [x] **`Math.random()` reemplazado por un `Random` inyectable:** `PlayerBody(Side)` crea uno nuevo y `PlayerBody(Side, Random)` permite pasar uno con semilla fija. Prueba: `testSeededKickIsReproducibleAndMirrored` (con la misma semilla, la patada izquierda y la derecha salen exactamente espejadas).

## Cambios en `fix11` (verificado)

- Compila; **84 de 84 pruebas pasan** (antes 63), en 6 clases: dominio (38), máquina (6, nueva), persistencia (14), física (14), presentación (5, nueva) y estática/dinámica (7). Ningún comentario `//`.
- **BUG-16 arreglado:** `PlayerBody.triggerPowerShot` le pasa a `Power` un punto delante de la cabeza, del lado del arco rival (`executePowerShot(ball, side, holdX, holdY)`). Medido en los dos lados, tocando el balón con la nuca, encima y de frente: en los 6 casos el balón queda sostenido **26 px delante** del centro de la cabeza.
- **Siguen bien:** BUG-13 (0 tiros que tocan al rival sin contar, en los 4 países), BUG-14 (el rival con aura no roba el tiro) y BUG-15 (el tirador no se mueve durante la carga: 0 px).
- **Limpieza (2.5.1 y 2.5.2), casi completa:**
  - Código muerto borrado: `Head` quedó en 44 líneas (sin `collideWithFoot`, `isStruck`, `kicksRecieved` ni su propio aturdimiento), sin `get/setController` en `Match`, sin `set` en `Field`, sin `Ball.copy`, sin `GoalRule.winner` duplicado, sin los `set` de `Power`.
  - `Match` y `Power` con atributos `private`; `resetPositions`, `resolveCollisions` y `onGoal` privados; `setState` y `getScoreBoard` de paquete; `getHead`, `getBody`, `getFoot` y los `getCircle`/`getRectangle` internos ya no son públicos.
  - Una sola fuente de verdad para el aturdimiento (`Life`).
  - `PlayerStats` lanza `DOPOSoccerException`; nombres en inglés (`getKickDamage`, `getMaxLife`, `getKickMultiplier`); sin `golIn` ni `obstacule`.
  - Números mágicos con nombre (`GOAL_CELEBRATION_TICKS`, `WINNER_POINTS`, `LOSER_POINTS`, `KICKOFF_BALL_HEIGHT`, `CHARGE_LIFT_PX`).
  - Sin `import domain.*` (quedan solo `java.awt.*` y `javax.swing.*`, que es lo habitual en Swing). `Log` escribe en `System.err` en vez de `printStackTrace`.
- **Cobertura (JaCoCo):**

  | Paquete | Líneas (`fix10` → `fix11`) | Ramas (`fix10` → `fix11`) |
  |---|---|---|
  | `domain` | 80.7 % → **90.6 %** | 59.7 % → **70.4 %** |
  | `persistence` | 68.8 % → **84.4 %** | 60.0 % → **80.0 %** |
  | `presentation` | ~0 % → 13.3 % | ~0 % → 13.4 % (`Theme`, `Sound`, `MatchSetup` y `FieldResolution` en 98–100 %; el resto es Swing) |

  Solo quedan bajo 80 % de líneas `Head` (75 %, 6 de 8), `GoalRule` (75 %) y `Country` (78.6 %): son 2 o 3 líneas cada una.
- Probado abriendo el juego: menú, selección, partido con gol, pausa, fin con puntos y mejoras funcionan igual.

## Cambios en `fix10` (verificado)

- Compila; **63 de 63 pruebas pasan**; ningún comentario `//`.
- **Bugs arreglados** (comprobados con simulación):
  - **BUG-15:** mientras se carga su tiro, el jugador queda quieto (`Power.isChargingShot()` dentro de `Player.isDisabled()`).
  - **BUG-14 y BUG-11:** mientras el balón carga un tiro, nadie choca con él ni puede ejecutar otro tiro (`PlayerBody.collideWithBall` y `Power.executePowerShot` lo ignoran). El tiro del que cargó sale completo aunque el rival tenga aura.
  - **BUG-9:** si el balón queda apretado entre los dos jugadores, sale hacia arriba y nunca queda dentro de ninguno (`Match.escapeBallFromPinch`).
  - **BUG-7:** conducir el balón le da velocidad (1.5 veces el paso del jugador; `PlayerBody.moveHorizontally` y `DRIBBLE_SPEED_FACTOR`).
  - Pruebas nuevas: `testShooterStaysStillWhileChargingPowerShot`, `testRivalWithAuraCannotStealOrDisturbChargingPowerShot`, `testSimultaneousKickPinchPopsBallUpwardAndLeavesNoOverlap` y `testWalkingIntoStationaryBallImpartsDribbleSpeed`.
- **`COMController` borrado**; su prueba ahora usa `MachineProfile.create(...)`.
- **Máquinas:** la Defensiva cubre más cancha (`DEFEND_ZONE_RADIUS` 130 → 170) y la Experta ataca si el rival tiene **cualquier** efecto, no solo inconsciente.
- **Salto más bajo:** `Physics.BASE_JUMP_IMPULSE` = 7 (antes 12.8). La altura máxima del salto baja de ~82 a ~25 px. ⚠️ Confirmar que es intencional: un jugador ya no alcanza un balón apoyado sobre el travesaño (lo resuelve `rollOffCrossBar`, que lo hace rodar).
- **Animación por estado (1.3):** tono de color que pulsa sobre la cabeza, símbolo corto (`<<`/`>>`, `!!`, `Z z`, `?`, `***`) y barrita de duración; colores por país y por efecto en `Theme` con `EnumMap`. Probado con capturas: se ve bien.

## Cambios en `fix9` (verificado)

- Compila; **59 de 59 pruebas pasan**; ningún comentario `//`.
- **Bugs del poder arreglados** (paso 1.1c), comprobados con simulación en partidos reales:
  - **BUG-13:** el golpe se calcula antes de los rebotes. Tiros que tocan al rival sin contar: **0** en los 4 países (antes 36 a 74 de 243).
  - **BUG-8:** al golpear, el balón se desmarca y el tiro termina; un tiro que no golpea a nadie termina a los **2 s de vuelo** (`Power.MAX_FLIGHT_TICKS`).
  - **BUG-12:** el tiro sale al tocar el balón con la cabeza, el cuerpo o el pie. Con las dos auras, el golpeado recibe el daño y el efecto y **responde con su propio tiro**.
  - **BUG-10:** los balones falsos se borran al terminar el tiro (`Power.onShotFinished`, *hook* que redefine `IllusionPower`).
  - Pruebas nuevas: `testPowerShotExpiresAfterMaxFlightTicks`, `testPowerShotTriggersOnHeadContactWithoutKicking`, `testPowerShotAgainstRivalWithAuraAppliesHitAndTriggersCounterShot` y la del piso.
- **Reorganización de la GUI** (paso 1.1b): `MatchSetup`, `FieldRenderer`, `PlayerRenderer`, `BallRenderer`, `HudRenderer`, `OverlayRenderer`, `Theme` y teclado como `Map<Integer, PlayerAction>`. `PhotoHeadSoccer` bajó de 909 a 315 líneas.
- **Sonidos** (paso 1.4): `Sound` y `SoundPlayer` conectados a los 7 eventos, con opción de silenciar. **Faltan los archivos `.wav`** en `presentation/resources/sounds/`: hoy el juego queda en silencio y registra en el log los 7 que no encuentra.
- Probado abriendo el juego (pantalla virtual): menú, selección, cancha, partido con gol, pausa, fin con puntos y mejoras se ven bien.

## Cambios del 23 nov (commit `9597810`, verificado)

- Compila; **55 de 55 pruebas pasan**; no queda ningún comentario `//` en `src`.
- **`ClumsyMachine`** (Cristian): elige una acción al azar y la mantiene entre 12 y 29 ticks; usa el poder con un 10 % de probabilidad cuando la barra está llena.
- **`OffensiveMachine`** (Cristian): se pone detrás del balón respecto al arco rival, salta, patea si el balón queda adelante y usa el poder cerca del balón.
- **`ExpertMachine`** (Cristian): además defiende si viene un tiro de poder hacia su arco, y en muerte súbita defiende mientras el balón esté en su mitad.
- **Balón sobre el travesaño:** antes, la Ofensiva y la Experta se quedaban trabadas todo el partido (el balón quieto sobre el travesaño y la cabeza de la máquina tapándole la salida). Arreglo en dos partes:
  - `Match.rollOffCrossBar`: el balón apoyado sobre un travesaño rueda hacia la cancha.
  - `OffensiveMachine.isBallOnCrossBar`: la máquina retrocede 45 px para dejarlo caer.
- **Simulación** (5 partidos de 2 min por perfil, contra un humano que persigue el balón): Torpe 146–96, Ofensiva 217–41, Defensiva 15–0, Experta 95–15. Ninguna se traba.
- Retrospectiva V3 y 16 diagramas de secuencia (Astah) terminados.

## Estado en `fix8` (verificado)

- Compila; **55 de 55 pruebas pasan** (se quitó temporalmente la del piso, ver arriba).
- **Nuevo en `fix8`:**
  - **Datos para la GUI (todo el 1.0):** `PlayerView.getActiveEffect()` y `getEffectProgress()`; `BallView.getPowerShotCountry()`, `isPowerShotCharging()` y `getPowerShotChargeProgress()`; eventos `onBallKicked`, `onPowerActivated`, `onPowerShot`, `onPowerHit` y `onProfileSaveFailed`, con sus pruebas.
  - **GUI separada en paneles:** `MenuPanel`, `SelectPlayersPanel`, `SelectCampPanel`, `UpgradesPanel` e `ImageAssets`; imagen del balón y banderas de los 4 países.
  - Nombres de los jugadores, pantalla de mejoras, puntos totales al terminar, filtro de extensión al guardar y abrir.
  - `MatchConfig` rechaza el **mismo nombre en los dos lados**; el error al guardar el perfil ahora **se avisa con `onProfileSaveFailed`** en vez de lanzar `IllegalStateException`.
  - Figuras más pequeñas: balón de radio 8, cabeza 14, pie 8, cuerpo 18 × 16, arcos de 30 px de ancho con travesaño de 5 px.
- **Sigue igual:** `ClumsyMachine` y `OffensiveMachine` devuelven un conjunto vacío (la máquina no se mueve con esos perfiles, y la Experta tampoco ataca); `COMController` sigue existiendo; los bugs 7 a 10 no se tocaron.

---

## Prioridad 1 — Interfaz gráfica · ≈ 1 – 1.5 h

### 1.0 Datos del dominio que la GUI necesita · ✅ hecho en `fix8`
- [x] `PlayerView`: `getActiveEffect()` y `getEffectProgress()`.
- [x] `BallView`: `getPowerShotCountry()`, `isPowerShotCharging()` y `getPowerShotChargeProgress()`.
- [x] `MatchListener`: `onBallKicked`, `onPowerActivated`, `onPowerShot`, `onPowerHit` (y `onProfileSaveFailed`).
- [x] Pruebas de los eventos.

### 1.1 Lo que pide el PDF · casi terminado · ≈ 15 min
- [x] Nombres de los jugadores en la pantalla de selección.
- [x] Pantalla de mejoras con puntos, niveles, precio y botón "+".
- [x] Fin del partido con los puntos totales de cada jugador.
- [x] Filtro de extensión en el `JFileChooser`.
- [ ] Revisar: al **abrir** una partida, que aparezca en pausa con un mensaje para continuar.
- [x] Mostrar el mensaje de `onProfileSaveFailed` en un diálogo.

### 1.1b Reorganizar `PhotoHeadSoccer` · ✅ casi todo hecho en `fix9` · falta ≈ 30 min
`PhotoHeadSoccer` tiene 909 de las 2083 líneas de la GUI y hace cinco cosas: guarda la configuración del partido (y los otros paneles la usan como depósito), arma el `MatchConfig`, dibuja todo, maneja los clics sobre botones dibujados a mano y guarda/abre partidas. Si no se separa ahora, las caras, el giro del balón y las animaciones de los poderes terminan en la misma clase. **No cambia nada de lo que se ve en pantalla.**
- [x] **`MatchSetup`** (≈ 45 min): mover ahí los campos de selección (modalidad, países, nombres, tipo de partido, minutos/goles, resolución, perfil de la máquina) con sus `get`/`set`/`cycle...`, y un `toConfig()` que arme el `MatchConfig` con el `Builder`. `SelectPlayersPanel`, `SelectCampPanel` y `UpgradesPanel` reciben el `MatchSetup`, no `PhotoHeadSoccer`.
- [x] **Renderers** (≈ 1 – 1.5 h): cada uno con `draw(Graphics2D g, MatchView match, double escala)`:
  - `FieldRenderer`: cancha y arcos (aquí va el arco dibujado a mano de 1.2).
  - `PlayerRenderer`: jugadores (aquí van las caras de 1.2 y el aura/efectos de 1.3).
  - `BallRenderer`: balón (giro, estela, anillo de carga, balones falsos).
  - `HudRenderer`: marcador, tiempo, vida, barra de poder, banderas.
  - `OverlayRenderer`: pausa, gol, empate y fin del partido.
  - `PhotoHeadSoccer` queda como el panel del partido: llama a los renderers en orden y escucha los eventos.
- [ ] **`Theme`** (≈ 30 min): ya tiene los colores por país y por efecto; todavía quedan 84 `new Color(...)` y 40 `new Font(...)` en los demás archivos (paneles y renderers). Opcional: moverlos a `Theme`.
- [x] **Teclado como mapa** (≈ 30 min): un `Map<Integer, PlayerAction>` por jugador en lugar de la cadena de `if` de `processContinuousKeys`, y que cada pantalla maneje sus propias teclas en vez del `switch` de `HeadSoccerGUI.keyPressed`.
- [ ] *(Opcional, ≈ 45 min)* `MatchFileDialogs` para guardar y abrir, y los botones de pausa/fin como `JButton` reales en vez de comprobar a mano si el clic cayó en el rectángulo.
- [ ] Después de cada paso: abrir el juego y probar menú → selección → partido → pausa → guardar → abrir → fin → mejoras.

### 1.1c Bugs del poder · ✅ todos hechos (BUG-17 y BUG-18 en `fix12`)
- [x] **BUG-17 · Un jugador con aura no debe recibir el efecto del tiro rival.** *(Cambio de regla pedido por el equipo.)*
  - **Hoy:** si el tiro de poder golpea a un rival que tiene el aura activa, el rival recibe el **daño y el efecto** (por ejemplo, queda empujado o electrocutado) y después responde con su propio tiro.
  - **Comportamiento esperado:** si el rival tiene el aura activa, **el efecto no se le aplica**. Sigue respondiendo con su propio tiro al tocar el balón (BUG-12).
  - **Por decidir:** si el **daño** sí se aplica. Propuesta: sí se aplica el daño y no el efecto, así el golpe "cuenta" pero el aura lo protege. Como el PDF dice que *"si el tiro de poder golpea al rival, se aplican el daño y el efecto"* (regla 5), anotarlo como **decisión de diseño** y mencionarlo en la sustentación.
  - **Arreglo (≈ 20 min):** en `Match.resolveCollisions`, antes de `receiveHit`, preguntar si el rival tiene aura (`rival.hasAura()`). Si la tiene, aplicar solo el daño (`takeDamage`) y no el efecto. Es mejor dejar la decisión en `Player.receiveHit`, que ya conoce su aura: `if (!hasAura()) effect.apply(...)`.
  - **Pruebas:** actualizar `testPowerShotAgainstRivalWithAuraAppliesHitAndTriggersCounterShot` (hoy espera que el efecto sí se aplique) y agregar una que compruebe que, con aura, `getActiveEffect()` sigue en `NONE` después del golpe.
- [x] **BUG-18 · El rival atraviesa el balón mientras se carga un tiro.** *(Difícil.)*
  - **Medición (`fix11`):** Corea carga su tiro y el rival (Camerún) camina hacia el balón quieto en el aire: está **dentro del balón 13 de los 90 ticks** y termina del otro lado.
  - **Causa:** desde `fix10` (BUG-14 y BUG-11), `PlayerBody.collideWithBall` ignora por completo el balón mientras carga, para que nadie lo mueva ni robe el tiro. Por eso tampoco frena a los jugadores.
  - **Comportamiento esperado:** el balón en carga es un **obstáculo fijo**: no se mueve, pero los jugadores no lo pueden atravesar (chocan contra él como contra un poste).
  - **Arreglo propuesto (≈ 1 – 1.5 h):** en `PlayerBody.collideWithBall`, cuando `ball.isPowerShotCharging()`, en lugar de salir sin hacer nada, **sacar al jugador** del círculo del balón, igual que `resolveCircleAgainstBar` saca al jugador del travesaño:
    1. Calcular el contacto entre el círculo del balón y la cabeza, el cuerpo y el pie (`contactWith`).
    2. Mover **al jugador**, no al balón, en la dirección de la normal por la penetración (`translate`), preferiblemente solo en horizontal.
    3. No ejecutar ningún tiro ni dar impulso al balón.
  - **Por qué es difícil:**
    - El jugador tiene tres figuras (cabeza, cuerpo y pie), y empujarlo por una puede meterlo en otra cosa (el arco, el otro jugador, la pared).
    - Si el jugador salta y cae encima del balón, hay que decidir si se apoya en él o resbala; lo más simple es empujarlo solo en horizontal.
    - El orden importa: este empuje debe ir **después** de `collideWithPlayer` y antes de `collideWithEdges`, para no dejar a nadie fuera de la cancha.
    - El que carga el tiro está quieto y el balón queda 26 px delante de su cabeza: comprobar que no lo empuje a él.
  - **Pruebas:** el rival camina contra el balón en carga desde los dos lados; durante los 90 ticks `isHitByBall` debe ser falso (o la penetración casi cero) y el rival nunca debe terminar del otro lado.

- [x] **BUG-16 · Al ejecutar el poder, el balón puede quedar detrás del jugador.** Medición (`fix10`, los dos lados): si el jugador con aura toca el balón con la parte de atrás de la cabeza, el balón queda sostenido **16 px detrás** del centro de la cabeza; si lo toca justo encima, queda **dentro** de la cabeza. El tiro sale igual hacia el arco rival, pero arrancando desde atrás del jugador (y a través de su cuerpo).
  - **Causa:** `Power.executePowerShot` sostiene el balón en su posición de ese momento (`holdX = ball.getX()`, y lo sube 30 px), sin mirar dónde está el jugador.
  - **Comportamiento esperado:** al ejecutar el poder, el balón **siempre** queda **delante** del jugador, del lado del arco rival, a la altura de la cabeza.
  - **Arreglo:** que `PlayerBody` le pase a `Power` el punto de sostén delante de la cabeza: `holdX = cabeza.x + lado.dir() × (radioCabeza + radioBalón + 4)` y `holdY = cabeza.y`. Por ejemplo, `executePowerShot(ball, side, holdX, holdY)`. Esto arregla también el detalle de que el balón quedaba pegado a la cara (1.3).
  - **Prueba:** jugador con aura; dejar caer el balón detrás, encima y delante de la cabeza, para los dos lados. En los tres casos, `(balón.x − cabeza.x) × lado.dir() > radioCabeza` durante la carga.
- [x] BUG-13, BUG-8 y BUG-12 (detalle abajo, y medición en "Cambios en `fix9`").
- [x] **BUG-14 · Un rival con aura que toca el balón mientras se carga otro tiro le "roba" el tiro.** Medición: Corea carga su tiro; Nigeria, con aura, camina hacia el balón quieto en el aire, lo toca y ejecuta su propio tiro. Desde ahí los dos poderes sostienen el balón al mismo tiempo, el tiro de Corea se pierde y sale solo el de Nigeria. **Arreglo:** en `PlayerBody`, no ejecutar el tiro por contacto mientras `ball.isPowerShotCharging()` (o resolverlo junto con el BUG-11).
- [x] **BUG-15 · El que tira el poder sigue caminando durante la carga y atraviesa el balón.** Medición (Corea con aura, caminando a la derecha hacia el balón): al tocarlo, el balón sube y queda quieto en el aire 90 ticks (1.5 s), pero el jugador **sigue avanzando**: pasa a través del balón (13 de los 90 ticks está metido dentro de él) y termina 72 px **delante** del balón. Cuando el tiro sale, el balón parte desde atrás del jugador. **Comportamiento esperado:** mientras se carga su tiro, el jugador se queda quieto (no camina, no salta, no patea), como una animación de ejecución del poder. **Arreglo (≈ 20 min):** en `Power`, un método `isChargingShot()` (`ticksPerPower > 0`), y en `Player.isDisabled()` agregar `|| (power != null && power.isChargingShot())`; así `moveLeft`, `moveRight`, `jump` y `kick` quedan bloqueados solos. Prueba: con aura, caminar hacia el balón; durante la carga la posición del jugador no cambia. Para la GUI: `PlayerView` puede exponer `isChargingPower()` para dibujar la pose del poder (1.3).

Estos tres van antes de las imágenes y las animaciones: el BUG-8 se ve en la interfaz (el balón sigue dibujado con el efecto del poder), y el 12 y el 13 no cumplen el PDF. Arreglarlos primero evita animar un comportamiento que después cambia. Orden sugerido: **13** (el más corto) → **8** → **12** (necesita decidir el orden del golpe y el tiro de respuesta).

#### ✅ BUG-13 · A veces el tiro de poder toca al rival y no le hace nada
- **Medición:** en 243 tiros simulados por país, contra el rival a distintas distancias y saltando en distintos momentos, el balón **toca al rival y no cuenta como golpe** en 36 (Corea), 36 (Camerún), 74 (Nigeria) y 61 (EE. UU.). No es solo el efecto: tampoco hay daño ni `onPowerHit`.
- **Causa:** en `Match.resolveCollisions`, primero va `player2.collideWithBall(ball)`, que hace rebotar el balón y lo saca del cuerpo del rival, y **después** `powerHitOn(...)`, que pregunta `isHitByBall`. Para entonces el balón ya no lo toca.
- **Arreglo (probado en la simulación: los toques sin golpe bajan a 0 en los 4 países):** en `resolveCollisions`, calcular `hit1` y `hit2` **antes** de los `collideWithBall`, y aplicar `receiveHit` y los eventos como ahora.
- **Relacionado:** con el BUG-12 (el que tiene aura y es golpeado) hay que decidir el orden: primero el golpe y luego su tiro.

#### ✅ BUG-8 · El balón sigue con el efecto del poder después de terminar el tiro
- **Medición:** después de que el tiro golpea al rival, `ball.hasPowerShot()` sigue en `true` por lo menos 50 ticks después, con el balón ya quieto en el piso. La GUI lo sigue dibujando con el efecto. Solo se desmarca en `Match.resetPositions()`, es decir, con el siguiente gol.
- **Causa:** `Power.onHitRival` termina el tiro (`powerShotActive = false`), pero nadie llama `ball.setPowerShot(false)`.
- **Relacionado:** si el tiro no golpea al rival ni termina en gol, queda activo sin tiempo máximo y **el jugador no puede volver a usar el poder** hasta el siguiente gol.
- **Arreglo:** en `Match.resolveCollisions`, después de aplicar el golpe, `ball.setPowerShot(false)` y `finishPowerShot()` del tirador. Además, un tiempo máximo de vuelo en `Power` (por ejemplo, 2 s después de soltar el tiro) que haga lo mismo.

#### ✅ BUG-12 · El poder solo se ejecuta pateando, y con las dos auras el rival no responde
- **El PDF dice "toca", no "patea":** regla 4, *"Cuando el personaje con poder **toca** el balón, ejecuta su tiro de poder y el aura desaparece."* Hoy solo se ejecuta en `collideFootWithBall`, si el pie está pateando y el balón está al frente. Tocarlo con la cabeza, el cuerpo o el pie sin patear no hace nada.
- **Medición (las dos auras activas):** el tiro de Corea golpea a Camerún, que tiene el aura; Camerún recibe 40 de daño y el empuje, **y su aura sigue igual**: no ejecuta su tiro aunque el balón lo tocó.
- **Arreglo:** ejecutar el tiro cuando el balón toque cualquier parte del jugador con aura (cabeza, cuerpo o pie), no solo al patear.
- **Pregunta al monitor:** cuando el tiro del rival golpea a alguien que tiene aura, ¿primero recibe el daño y el efecto (regla 5) y después responde con su propio tiro (regla 4), o solo uno de los dos? Mientras tanto, proponer: se aplica el golpe y luego el que fue golpeado ejecuta su tiro.

### 1.2 Imágenes y dibujo · casi terminado · ≈ 15 – 30 min
- [x] Balón (`Ball.png`) y banderas.
- [x] **Giro del balón** (`BallRenderer`, con `g2d.rotate`).
- [x] **Arcos dibujados a mano** (`FieldRenderer`: red, travesaño y poste sobre `getGoalArea` y `getCrossBar`).
- [x] **Caras:** se dejan como están (cabeza de color con ojo, dibujada por código). Decisión del equipo.
- [ ] Revisar las 3 resoluciones: todo se escala desde las medidas del dominio, nunca con píxeles fijos. Con las figuras más pequeñas de `fix8`, revisar que se lea bien.
- [ ] `CREDITOS.txt` con la fuente de cada imagen.

### 1.3 Animación de los poderes · ✅ hecho en `fix10`
- [x] Aura que pulsa, anillo de carga del balón y balones falsos (`fix9`).
- [x] Tono de color que pulsa sobre la cabeza, símbolo corto por estado y barrita de duración (`PlayerRenderer.drawStatusEffect`).
- [x] Colores por país y por efecto en `Theme` (`EnumMap`).
- [ ] *(Opcional)* Barrita también para el aturdimiento: hoy muestra `***` pero no cuánto le queda (haría falta un `getStunProgress()` en `PlayerView`).
- [x] El balón en carga ya no queda pegado a la cara: se arregló con el BUG-16.
- [ ] *(Opcional)* Destello al golpear al rival (`onPowerHit`).

### 1.4 Sonidos · ✅ hecho (archivos y música de fondo)
- [x] Clase `SoundPlayer` en `presentation`: `javax.sound.sampled.Clip`, archivos **WAV**, cargados una sola vez en un `Map`.
- [x] Gol, aturdido, fin, patada, aura, tiro y golpe, cada uno con su evento (ya existen todos).
- [x] Opción de silenciar. Si un sonido falta o no hay audio, el juego sigue sin sonido (registrarlo en el log).
- [x] **Archivos de sonido agregados** en `src/presentation/resources/sounds/`: los 7 efectos (`goal`, `stunned`, `finish`, `kick`, `power_aura`, `power_shot`, `power_hit`) y la **música de fondo** (`soundtrack.wav`, 14 s en bucle). Todos WAV de 16 bits, 44.1 kHz. La música empieza al abrir el juego y la tecla **N** silencia todo (efectos y música) desde cualquier pantalla.
- [x] **Arreglado:** en un computador sin tarjeta de sonido, `AudioSystem.getClip()` lanza `IllegalArgumentException` y el juego no arrancaba; ahora `SoundPlayer` la atrapa, la registra en el log y el juego sigue sin sonido. Prueba nueva: `testEverySoundHasItsWavAndSoundtrackRespectsMute`.
- [ ] Si alguno de los sonidos es CC-BY, anotarlo en `CREDITOS.txt` con su autor y enlace.

**Listo cuando:** se puede configurar, jugar, mejorar estadísticas y guardar/abrir sin tocar código, y cada poder se distingue a simple vista.

---

## Prioridad 2 — Dominio · ✅ (solo opcionales)

### 2.1 Jugador contra máquina · ✅ hecho
- [x] **`OffensiveMachine`** (primero: la Experta delega en ella cuando va empatando o perdiendo): ubicarse detrás del balón respecto al arco rival, saltar si viene alto, patear si está al alcance, activar el poder cuando va a tocar el balón. Constantes con nombre, como en `DefensiveMachine`.
- [x] **`ClumsyMachine`**: acciones al azar con su `Random`, **mantenidas unos ticks** (si cambian en cada tick, solo tiembla).
- [x] Borrar **`COMController`** y pasar su prueba de `DOPOSoccerStaticDynamicTest` a los perfiles nuevos.
- [x] Con las figuras más pequeñas, revisar los alcances de `DefensiveMachine` (zona de defensa a 170 en `fix10`) (distancias pensadas para los tamaños anteriores).
- [x] La defensa de la Experta contra el tiro de poder ya funciona bien: desde `fix9` el balón se desmarca al terminar el tiro (BUG-8).

### 2.2 Pruebas de la máquina · ✅ `DOPOSoccerMachineTest` en `fix11`
- [ ] Defensiva: balón cerca de su arco → queda entre el balón y el arco; balón lejos → vuelve a su posición.
- [ ] Experta: va perdiendo → ofensiva; va ganando → defensiva; rival con aura → defensiva.
- [ ] Torpe con un `Random` de semilla fija → siempre las mismas acciones, y cada acción se mantiene al menos 12 ticks.
- [ ] Ofensiva: balón adelante → se mueve hacia él; balón sobre un travesaño → retrocede.
- [ ] Experta: tiro de poder hacia su arco → defensiva; muerte súbita con el balón en su mitad → defensiva.
- [ ] Partido: balón apoyado sobre un travesaño → rueda hacia la cancha.
- [ ] `MachineProfile.create(...)` devuelve la clase correcta para cada perfil.

### 2.3 Casos borde del progreso · ≈ 0.5 h
- [x] Mismo nombre en los dos lados (validado en `MatchConfig`).
- [x] Error al guardar el perfil al terminar → `onProfileSaveFailed`.
- [x] **Abrir una partida vieja ya no pisa los puntos nuevos:** al terminar se carga el perfil actual y se le suman solo los puntos del partido (ya estaba en `fix8`).
- [ ] Preguntas al monitor: si la **máquina acumula puntos**, si **terminar a la fuerza reparte puntos** y las **reglas de la Ilusión**.

### 2.4 Diseño (opcional) · ≈ 1 h
- [ ] `Field.WIDTH`/`HEIGHT` siguen como constantes globales en el dominio; quedaría mejor un objeto `Field` con medidas.
- [ ] Pruebas de `getFakeBalls()` y `getModality()` desde `MatchView`.
- [ ] `Match` creció bastante con los eventos; si pasa de ~300 líneas, sacar el disparo de eventos a un método o clase aparte.

---

### 2.5 Calidad del código · ✅ casi todo hecho en `fix11` (ver "Cambios en `fix11`")
Hacerlo en este orden: primero borrar lo que sobra, después corregir las malas prácticas y al final escribir las pruebas (así no se prueban métodos que se van a borrar o cambiar). Después de cada paso: todas las pruebas en verde.

#### 2.5.1 Código muerto · ✅
- [x] `Head`: `collideWithFoot`, `isStruck`, `isNotStruck`, `getKicksRecieved`, `setKicksRecieved`, `tick` y el atributo `kicksRecieved`. Por esto `Head` está en 22 %.
- [x] `Match`: `getController1`, `setController1`, `getController2`, `setController2`.
- [x] `Field`: `setLeftGoal`, `setRightGoal`.
- [x] `PlayerStats`: `setJump`, `setSpeed` (`setKick` y `setSurvival` solo los usa una prueba; se pueden reemplazar por el constructor).
- [x] `Power`: `setAuraActive`, `setPowerShotActive`, `getEffectType`.
- [x] `Ball.copy()`.
- [x] Constructores sin uso de `Head`, `Body`, `Foot` y `Circle` (revisar uno por uno antes de borrar).


#### 2.5.2 Malas prácticas · ✅ (opcional: clases largas)
- [x] **Atributos `protected` sin necesidad.** `Match` tiene 11 (`player1`, `ball`, `scoreBoard`, `state`, …) y no tiene subclases: pasarlos a `private`. En `Power`, dejar `private` lo que las subclases no usan (`charge`, `auraActive`, `ticksPerPower`, `holdX`, `holdY`, …) y darles acceso solo a lo necesario con métodos `protected`.
- [x] **Métodos públicos que cambian el partido desde afuera.** `Match.resetPositions`, `resolveCollisions`, `onGoal` y `setState` son `public`: cualquiera podría saltarse las reglas. Pasarlos a `private` (o de paquete, si una regla los necesita, como `setState` y `getScoreBoard` para `TimeRule`).
- [x] **Getters que entregan objetos internos que se pueden modificar.** `Ball.getCircle()`, `PlayerBody.getHead()`, `getBody()`, `getFoot()`, `Head.getCircle()`, `Body.getRectangle()`, `Goal.getRectangle()` y `Match.getScoreBoard()` son `public`: dejarlos de paquete (la GUI ya usa las vistas, que entregan copias).
- [x] **Dos fuentes de verdad para el aturdimiento:** `Life.stunned` y `Head.stunned`. Dejar solo el de `Life` y que la cabeza lo consulte.
- [x] **Excepción equivocada:** `PlayerStats.validate` lanza `IllegalArgumentException`; usar `DOPOSoccerException(INVALID_STAT)` como el resto del dominio.
- [x] **Nombres mezclados y con errores:** `getDanioPatada`, `getVidaMax`, `getMultiplicadorKick` y `golIn` (español en código en inglés), `kicksRecieved` (se escribe *received*; se borra con el código muerto) y el parámetro `obstacule` de `Ball.bounceOff`. Renombrar a `getKickDamage`, `getMaxLife`, `getKickMultiplier` y borrar el alias `golIn`.
- [x] **Números mágicos** en el dominio: los 85 ticks de celebración del gol, los 100 y 25 puntos, la altura de 40 px del saque y los 30 px que sube el balón al ejecutar el poder. Pasarlos a constantes con nombre (en `Physics` o en la clase que los usa).
- [x] **`import domain.*`** en 6 clases de `presentation` y en las 4 de pruebas: importar solo lo que se usa.
- [x] **`Log` usa `printStackTrace`** cuando no puede escribir el archivo del log. Es aceptable como último recurso, pero mejor escribir un mensaje corto en `System.err`.
- [ ] *(Opcional)* **Clases largas:** `Match` (hoy 573 líneas) y `Player` (493). Opcional: sacar el aviso de eventos de `Match` a métodos `notifyGoal`, `notifyPowerHit`, … o a una clase `MatchEvents`.
- [ ] Revisar con **SonarLint** o **PMD** después de estos cambios, por si queda algo más.

#### 2.5.3 Pruebas y cobertura · ✅ dominio 90.6 %, persistencia 84.4 %
Medida con JaCoCo sobre `fix10` (63 pruebas):

| Paquete | Líneas | Ramas |
|---|---|---|
| `domain` | 80.7 % | 59.7 % |
| `persistence` | 68.8 % | 60.0 % |
| `presentation` | ~0 % | — (normal: la GUI no se prueba con JUnit) |

**Pruebas nuevas (≈ 1 – 1.5 h)** para las clases más bajas:

| Clase | Líneas | Ramas | Qué probar |
|---|---|---|---|
| `DefensiveMachine` | 60 % | 12.5 % | Balón lejos → vuelve a su posición; balón cerca → se pone entre el balón y su arco, salta y patea. |
| `ClumsyMachine` | 66.7 % | 27.3 % | Con un `Random` de semilla fija: cada una de las 6 opciones; la acción se mantiene al menos 12 ticks; usa el poder con la barra llena. |
| `ExpertMachine` | 69 % | 37.5 % | Cada rama: tiro de poder hacia su arco, rival con aura, rival con efecto, muerte súbita, ganando, perdiendo. |
| `OffensiveMachine` | 70 % | 53.3 % | Balón adelante y atrás; salto; patada; poder; balón sobre el travesaño. |
| `MachineProfile` | 78.6 % | 0 % | `create(...)` devuelve la clase correcta para cada perfil (y `create(side, random)` para la Torpe). |
| `SerializedProfileRepository` | 63 % | 60 % | Archivo de perfiles corrupto → `OPEN_ERROR`; nombre vacío → `NULL_ARGUMENT`. |
| `SerializedMatchRepository` | 65.2 % | 50 % | Guardar en una ruta donde no se puede escribir → `SAVE_ERROR`; `null` → `NULL_ARGUMENT`. |
| `IllusionPower` | 65.2 % | 45 % | Tiro de EE. UU. completo: 7 falsos durante el vuelo, se borran al golpear. |
| `PushedEffect`, `ElectrocutedEffect`, `ConfusedEffect` | 0 – 50 % | — | Empujado: se mueve hacia su arco; electrocutado: velocidad 0.5 y no salta; confundido: izquierda y derecha invertidas. |
| `PlayerStats` | 69.4 % | 47.4 % | Subir cada una de las 4 estadísticas y el tope de 10. |

Las pruebas de la máquina de este paso son las mismas de 2.2: hacerlas una sola vez.

## Prioridad 3 — Entregables · ≈ 2 – 3 h
- [ ] **Pruebas de aceptación** (V3): jugar PvM contra cada perfil; partido por tiempo que termina en muerte súbita; guardar a mitad, cerrar y abrir; ganar, recibir 100 puntos y comprar una mejora.
- [x] **Diagramas de secuencia:** 16 terminados en Astah (faltan la 8 y la 9 del tiro de poder, que esperan los BUG-8, 11, 12 y 13).
- [ ] **Diagrama de clases:** `MachineController` y sus perfiles, `MatchRepository`, `ProfileRepository`, `ProfileView`, el paquete `persistence` y los paneles nuevos de la GUI.
- [x] **Retrospectiva** V3.
- [ ] **Planificación de versiones** actualizada.

---

### Nota · El rival puede empujar al que carga su tiro
- En `fix11`, el que carga su tiro queda quieto, pero si el rival camina contra él, lo empuja (en la medición, 124 px durante la carga). El balón sigue sostenido delante del punto donde se ejecutó el poder, así que el tiro sale bien, pero el tirador puede quedar lejos de él.
- El PDF no dice nada de esto. Si se quiere que el tirador sea "inamovible" durante la carga, en `Player.collideWithPlayer` no empujar a un jugador con `isChargingShot()`. Opcional.

## Prioridad 4 — Bugs · ✅ todos los medidos están arreglados

Los BUG-8, 12 y 13 se movieron al paso **1.1c**.

### ✅ BUG-7 · El balón no toma velocidad al conducirlo manteniendo D (arreglado en `fix10`)
- **Causa:** `Ball.bounceOff` solo usa la velocidad del balón; con el balón quieto, `v · n = 0` y solo se corrige la posición.
- **Arreglo:** velocidad relativa (`vn = (vBalón − vPie) · n`) o una velocidad mínima en la dirección del empujón cuando el pie que no patea empuja el balón en el piso.

### ✅ BUG-9 · Dos jugadores pegados, balón en medio, ambos patean: el balón traspasa a uno (arreglado en `fix10`)
- **Causa:** cada jugador resuelve su contacto por separado y en orden.
- **Arreglo:** si el balón toca a los dos en el mismo tick, que salga hacia arriba; y nunca dejarlo dentro de un cuerpo después de todas las correcciones.

### ✅ BUG-10 · Los balones falsos de la Ilusión no desaparecen si el tiro golpea al rival (arreglado en `fix9`)
- **Ojo:** ahora los falsos desaparecen **en cuanto** el tiro golpea o vence (máximo 2 s). El PDF dice que *"desaparecen al terminar el efecto"* (2.5 s). Preguntar al monitor si está bien así.
- **Causa:** se actualizan solo mientras el tiro está activo, y el golpe termina el tiro.
- **Arreglo:** que la duración de la ilusión no dependa de que el tiro siga activo (un *hook* que `updateShot` llame en todos los ticks).

### ✅ BUG-11 · El balón se comporta raro mientras se carga el tiro de poder (arreglado en `fix10`: en la carga el balón no choca con nadie)
- **Medición:** durante los 90 ticks (1.5 s) en que el balón queda quieto en el aire, el rival lo atraviesa caminando. En cada tick el choque mueve el balón unos píxeles y el siguiente tick `Power.updateShot` lo vuelve a poner en su sitio con velocidad 0, así que tiembla y no detiene a nadie. Si el rival lo patea, la patada no hace nada.
- **Causa:** el balón en carga se resuelve como un balón normal (el choque mueve **al balón**), pero `updateShot` lo teletransporta de vuelta en cada tick.
- **Arreglo:** mientras `ball.isPowerShotCharging()`, que el balón sea un obstáculo fijo: en `Player.collideWithBall`, en vez de `bounceOff`, sacar **al jugador** del círculo del balón (como `resolveCircleAgainstBar` con el arco) y no permitir patearlo. Otra opción, más simple: que en la carga el balón no choque con nadie.
- **Después del golpe:** el balón rebota contra la cabeza del rival con el impulso de cabeza (sale a −15 px/tick de vuelta hacia el tirador). Decidir si se quiere así o si el balón debe perder velocidad al golpear.

### Dash
- **No lo pide el PDF:** los movimientos son "mover, saltar, patear y activar el poder". Es opcional, y conviene dejarlo para después de los bugs y los entregables. Si se hace: una `PlayerAction.DASH` con su tecla, un impulso horizontal de pocos ticks y un tiempo de espera, todo dentro de `PlayerBody`.

---

## Resumen

| Orden | Qué | Horas |
|---|---|---|
| 0 | Sacar `DOPOSoccer.log` y `profiles.dat` del repositorio (y agregarlos a `.gitignore`) | 0.1 |
| 1 | **GUI:** revisar abrir partida en pausa, revisar las 3 resoluciones, `CREDITOS.txt` (con la fuente de los sonidos) | 0.5 – 1 |
| 2 | **Entregables:** pruebas de aceptación, ajustar diagramas (`AJUSTES_DIAGRAMAS_fix10.md` + S08, S09, estados y clases), planificación | 2 – 3 |
| | **Total** | **≈ 2.5 – 4 h** |
| — | *Opcionales:* tirador inamovible durante la carga, partir `Match`/`Player`, resto de `Theme`, barrita del aturdido, dash | — |

**Sugerencia de reparto:** Juan, las pruebas de aceptación y los diagramas; Cristian, `CREDITOS.txt` y las revisiones cortas.

## Reglas de trabajo
1. La GUI solo usa la fachada, las vistas, los eventos, los `enum` y `DOPOSoccerException`. Si falta un dato, se agrega a la vista; nunca se busca dentro del dominio.
2. La API pública (fachada, vistas, eventos y `MatchConfig`) no cambia sin avisar.
3. Un commit por paso; push frecuente; **todas las pruebas en verde antes del push**.
4. El dominio no importa nada de `presentation` ni de `persistence`.
5. No subir archivos generados al jugar (`*.log`, `*.dat`, partidas guardadas).

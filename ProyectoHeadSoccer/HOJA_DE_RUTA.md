# DOPOSoccer 2026-2 — Hoja de ruta

Enunciado vigente: `DOPOSoccer_2026-2.pdf`.
Código actual: commit **`fix2`** (`356753f`).

---

## Estado en `fix2`

**Hecho (verificado: compila y las 26 pruebas pasan):**
- Fachada `DOPOSoccer`, `MatchConfig` (Builder), `MatchView`/`PlayerView`/`BallView`, `MatchListener` (Observer), `MatchRule` con `TimeRule` y `GoalRule` (Strategy), `ScoreBoard` por lado, `Vector2`, `Contact` y `Ball.bounceOff`.
- La GUI ya habla con `DOPOSoccer`, usa `MatchListener` y `Physics.TICK_MS`; `FieldResolution` y `Screen` están en `presentation`.
- Fuera de `Player`, **nadie llama** `getHead()`, `getFoot()` ni `getBody()`.
- Se borraron `HeadSoccer`, `ReaderMatch` y `MatchTimer`.

**Problemas medidos (simulaciones sin GUI sobre `fix2`):**

| # | Problema | Medición | Causa |
|---|---|---|---|
| F1 | La patada no le pega a un balón en el piso | Balón quieto en el piso a 12-44 px del pie: velocidad (0, 0) en todos los casos. Balón rodando hacia el pie: **0 disparos de 30** | `Foot.kicking` sube el pie 25 px (de y = 188 a 163); el balón en el piso tiene centro en y = 190 → 27 px de distancia, se necesitan 22 |
| F2 | La patada a veces sale hacia atrás o hacia el piso | A media altura: vx negativo en 3 de 15 casos | El disparo usa la **normal** del contacto (`v = normal.scale(impulse)`), no la dirección de la patada |
| F3 | La patada "atraviesa" el balón | — | El pie se teletransporta 25 px en un tick; no recorre el camino |
| F4 | La cabeza se queda pegada al travesaño | Saltando bajo el travesaño: cabeza dentro del travesaño **120 de 120 ticks** | `collideWithGoal` solo pone `verticalSpeed = 0`; nunca saca al jugador |
| F5 | El pie se hunde en el travesaño | Parado encima: borde del pie en y = 105, el travesaño empieza en y = 100 | Igual que F4 |
| F6 | Se atraviesa el travesaño de lado | Caminando y saltando hacia el arco: **112 de 120 ticks** dentro | No hay corrección horizontal |
| F7 | `onTop` inestable | — | `collideWithPlayer` pone `onTop = false` en su `else` y pisa el `onTop = true` del travesaño |
| A1 | `Player` es una "clase Dios" | **589 líneas, 21 atributos, 61 métodos públicos (7 setters), usa 18 clases del dominio** | Mezcla identidad, física, colisiones, vida, efectos, poder, entrada, goles y puntos |
| A2 | `Power` y `Player` dependen el uno del otro | — | `Power.onHitRival` llama `rival.takeDamage()` y `rival.applyEffect()` |
| G1 | Los poderes se ven todos iguales | — | El aura es amarilla para todos, el balón con poder siempre brilla igual, el efecto se muestra como texto (`★ ELECTROCUTED ★`) y **los balones falsos de la Ilusión no se dibujan** |

---

## Para mañana (en este orden)

| Prioridad | Qué | Horas |
|---|---|---|
| **P1** | Física buena (patada y arcos) | 4 – 5 |
| **P2** | Bajar el acoplamiento de `Player` | 3 – 4 |
| **P3** | Animación de los poderes (*si alcanza*) | 1.5 – 2.5 |
| **Total** | | **8.5 – 11.5** |

> P1 y P2 se hacen **juntos**: la física nueva vive en la clase `PlayerBody` que también es el primer paso de P2. Así no se arregla dos veces.

---

### P1 — Física buena (≈ 4 – 5 h)

**P1.1 Crear `PlayerBody`** (clase nueva, el cuerpo físico del jugador)
- Mover de `Player`: `head`, `body`, `foot`, `verticalSpeed`, `onTop`, `translate`, `moveVertical`, la parte física de `jump`, `collideWithEdges`, `collideWithGoal`, `collideWithBall`, `collideMyFootWithBall`, el empuje de `collideWithPlayer`, `isHitByBall` y las figuras para dibujar.
- **Ancla única** dentro de `PlayerBody`: una posición (`Vector2`) y cabeza, cuerpo y pie relativos a ella. `resetPosition()` pasa a ser "poner el ancla en `side.startX()`", sin recrear objetos.
- `Player` delega: `player.collideWithBall(ball)` → `body.collideWithBall(ball, ...)`.

**P1.2 Patada (F1, F2, F3)**
- Constantes en `Foot` (o `Physics`): `KICK_REACH_X ≈ 20`, `KICK_LIFT_Y ≈ 6`, `KICK_SWEEP_TICKS ≈ 4`, `KICK_HOLD_TICKS ≈ 8`.
- **Barrido:** durante los primeros `KICK_SWEEP_TICKS`, el pie avanza `KICK_REACH_X / KICK_SWEEP_TICKS` por tick (y sube un poco); luego se mantiene y vuelve. Así el contacto se detecta en el camino.
- **Dirección del disparo:** si el balón está delante del pie (`(ball.x − foot.x) × side.dir() > −radio`), `velocidad = new Vector2(side.dir(), −0.45).normalized().scale(fuerza)`. Si está detrás, solo rebota (`bounceOff` con impulso 0).
- En `collideWithBall`, revisar **el pie antes que el cuerpo** cuando está pateando, para que el cuerpo no absorba el balón primero.
- El tiro de poder se dispara en ese mismo contacto (confirmar con el monitor si debe ser con cualquier toque, B-N3).

**P1.3 Travesaño sólido (F4, F5, F6)**
- Para la cabeza y el pie: `crossBar.contactWith(círculo)` → mover **todo el jugador** `normal × penetración`.
  - Si `normal.y < 0` (cayó encima) y va bajando: `onTop = true`, `verticalSpeed = 0`.
  - Si `normal.y > 0` (golpeó desde abajo): `verticalSpeed = max(verticalSpeed, 0)`.
  - Si la normal es horizontal: no hace falta nada más; la corrección de posición ya lo frena.
- Para el cuerpo (rectángulo contra rectángulo): sacarlo por el eje de **menor** solapamiento (un método `overlap` pequeño en `PlayerBody` o en `Rectangle`).

**P1.4 `onTop` estable (F7)**
- Al **inicio** de `Match.resolveCollisions`, `body.clearSupport()` para los dos jugadores; después, solo el travesaño y la cabeza del rival lo ponen en `true`.
- Quitar el `else { onTop = false; }` de `collideWithPlayer`.

**P1.5 Pruebas (las simulaciones como JUnit)**
- Balón quieto en el piso delante del pie + patada → `vx > 0` hacia el arco rival y `vy < 0`.
- Balón rodando hacia el pie: al menos 25 de 30 momentos de patada producen disparo.
- Balón detrás del pie + patada → no sale hacia el arco propio con fuerza de patada.
- Saltar bajo el travesaño: la cabeza no queda dentro más de 2 ticks seguidos.
- Parado sobre el travesaño: el borde del pie queda en `bar.top() ± 1`.
- Caminar contra el travesaño a la altura de la cabeza: nunca se solapan.
- Las 26 pruebas existentes siguen pasando.

**Listo cuando:** todas esas pruebas pasan y, en el juego, se puede disparar un balón que va por el piso.

---

### P2 — Bajar el acoplamiento de `Player` (≈ 3 – 4 h)

`Player` queda como **coordinador** que compone piezas pequeñas (composición en vez de una clase gigante):

| Paso | Clase | Qué sale de `Player` |
|---|---|---|
| P2.1 | `PlayerBody` | Hecho en P1: física, geometría y colisiones (≈ 200 líneas menos) |
| P2.2 | **`Life`** (nueva) | `vidaActual`, `vidaMax`, `stunTicksRemaining`, `isStunnedByZeroHealth`; métodos `takeDamage`, `tick`, `isStunned`, `reset` |
| P2.3 | **`EffectState`** (nueva) | `currentEffect`, `effectTicksRemaining`; métodos `apply(efecto, segundos)`, `tick`, `speedFactor()`, `canMove()`, `canJump()`, `canKick()`, `isConfused()`. Los `if (currentEffect == …)` de `Player` desaparecen. *(En V2 se puede convertir en Strategy cambiando solo esta clase.)* |
| P2.4 | `ScoreBoard` | Borrar `goals`, `addGoal`, `setGoals` de `Player`. `PlayerView.getGoals()` → la GUI usa `MatchView.getGoals(side)` |
| P2.5 | **`PlayerProfile`** (nueva) | `stats` y `points` (lo que el PDF pide mantener entre partidas) |
| P2.6 | Cortar el ciclo `Power` ↔ `Player` (A2) | `Power.onHitRival(...)` **devuelve** un objeto `PowerHit` (daño, efecto, duración) y `Match` lo aplica con `rival.receiveHit(hit)`. `executePowerShot` recibe lo que necesita (balón, lado, multiplicador de kick), no el `Player` |
| P2.7 | Limpieza | Borrar los 7 setters, el constructor vacío que crea `AuraAzulPower` y el `implements Collidable` |

**Listo cuando:** `Player` tiene **menos de 200 líneas y unos 8 atributos**, no conoce ninguna subclase de `Power`, y las pruebas siguen pasando.

---

### P3 — Animación de los poderes (≈ 1.5 – 2.5 h) · *si alcanza*

**Dominio (cambios pequeños en las vistas):**
- `BallView`: `Country getPowerShotCountry()` (o `null` si no lleva tiro), para que la GUI sepa **qué** tiro dibujar.
- `PlayerView`: `PlayerEffect getActiveEffect()` en vez de un `String`, y `double getEffectProgress()` (0..1) para dibujar cuánto le queda.
- `MatchView`: `List<BallView> getFakeBalls()` para la Ilusión. Esto también elimina los `instanceof IllusionPower`.

**Presentación (`PhotoHeadSoccer`):**
- Un **contador de frames** propio de la vista para animar: aura que pulsa (cambia de tamaño y transparencia con `Math.sin(frame × 0.2)`).
- **Aura del color de cada país** (azul para Corea, amarillo eléctrico para Camerún, verde para Nigeria, rojo y azul para EE. UU.).
- **Estela del balón con tiro**: la vista guarda las últimas 6-8 posiciones y las dibuja cada vez más transparentes.
- Por tiro: **Rayo** con zigzag alrededor del balón; **Tornado** con espiral; **Aura Azul** con estela larga y recta; **Ilusión** con los 7 balones falsos.
- Efecto sobre el rival con **ícono** en vez de texto (rayos, estrellas, flecha de empuje) y una barrita de duración.

---

### Documentación para la entrega (en paralelo, la hace el compañero)
Ver la lista de diagramas seguros más abajo, en "Diagramas que se pueden hacer ya".

- Diagramas de secuencia de la API estable.
- Diagrama de clases de la parte estable (con `Player` como una caja que se completa después de P2).
- Retrospectiva.

---

## Diagramas que se pueden hacer ya (no van a cambiar, o casi nada)

| Diagrama | Por qué es seguro |
|---|---|
| Crear partida: GUI → `MatchConfig.builder()…build()` → `DOPOSoccer.newMatch(config)` → `new Match(config)` → jugadores, controles, `MatchRule`, `ScoreBoard` | La fachada y el Builder ya están cerrados. Solo cambiará la flecha interna de "crear jugador" (un mensaje) |
| Ciclo de un tick (nivel alto): `Timer` → `setIntention` × 2 → `DOPOSoccer.tick` → `Match.tick` → `rule.onTick`/`isOver` → `decide` → `applyInput` → `tick` de jugadores y balón → `resolveCollisions` → gol | Poner `resolveCollisions` como **un solo mensaje**, sin abrirlo: lo de adentro es lo que cambia en P1 |
| Gol y fin de partido: `onGoal` → `ScoreBoard.goalIn` → `onGoalReceived` (+¼ de barra) → `MatchListener.onGoal` → `rule.isOver` → `finishMatch` → `onMatchFinished` | `Match`, `ScoreBoard`, `MatchRule` y `MatchListener` ya no cambian |
| Fin por tiempo y muerte súbita: `TimeRule.onTick` → tiempo en 0 con empate → `SUDDEN_DEATH` → primer gol → `finishMatch` | `TimeRule` está terminada |
| Pausa, reanudar y terminar: GUI → `togglePause`/`finish` → `Match.pause`/`resume`/`finish` | Estable |
| Entrada de teclado: `KeyListener` → teclas activas → `DOPOSoccer.setIntention` → `Match.setIntention` → `Controller.receive` | Estable |

**Esperar a después de P1/P2:** patada → daño → aturdido; tiro de poder → golpe al rival → efecto; colisiones por dentro.

---

## Después de V1

### V2 — Poderes, PvM y persistencia (Sa 21 nov)
- `EffectState` → **Strategy** (una clase por efecto).
- `Power` → **Template Method** (`PowerShot` con `launch` final).
- Perfiles de máquina: del `switch` de `COMController` a una clase por perfil (Strategy + Factory en `MachineProfile`), con `Random` inyectable.
- Persistencia: `MatchRepository` y `ProfileRepository` en el dominio, implementación con serialización en `persistence`; guardar y abrir desde la GUI.
- Log de errores con `java.util.logging`.
- Planificación de las 4 versiones (entregable).

### V3 — Progreso y experta (Do 22 nov)
- Mejoras de estadísticas con puntos (500 × 2^(nivel − 1)), progreso persistido, máquina experta completa, pruebas de aceptación.

### V4 — Pulido (parcial)
- Ilusión con reglas confirmadas, prueba de extensión (agregar un país o un perfil con una clase), Javadoc y diagramas finales.

### Pendientes menores
- ✅ Ya corregidos en `fix2`: la barra empieza vacía, Inconsciente ya no saca al jugador de la cancha, velocidad y salto con +10 % por punto, y `PlayerStats` valida 1-10 (lanza `IllegalArgumentException`; en P2.5 conviene que `PlayerProfile` use `DOPOSoccerException`).
- `.gitignore` con `bin/`.
- Decidir si `Country` se queda con 4 o con 8 países.

---

# Anexo — Contexto, diagnóstico y arquitectura

## 0. Contexto

### Feedback del monitor
1. **La COM todavía no tocaba.** La prioridad era una **interfaz decente** y un **buen backend PvP**.
2. **Acoplamiento alto.** Hay que bajarlo aplicando los **patrones de diseño** vistos en clase.

### Calendario de entregas (del PDF)

| Versión | Contenido | Fecha |
|---|---|---|
| **V1** | Presentación + dominio. Configuración del juego, nivel básico, **PvP**. Diagramas de secuencia, código, **pruebas de unidad**, **retrospectiva** | **Do 15 nov (evaluador, preliminar)** |
| **V2** | Persistencia (guardar y abrir), nivel medio, **PvM**. Planificación de las 4 versiones | Sa 21 nov (pares) |
| **V3** | Lo anterior + lo propuesto para el ciclo 3. **Pruebas de aceptación** | Do 22 nov (evaluador, inicial) |
| **V4** | Lo anterior + lo propuesto para el ciclo 4 | Parcial S17 (evaluador, final) |
| Competencia | Todo completo | Vi 4 dic |

⚠️ **V1 es en dos días.** Por eso esta hoja separa lo **indispensable** de lo que se hace **si alcanza**.

### Qué cambió respecto a POOBSoccer

| Antes | Ahora |
|---|---|
| PvP, PvM y MvM | **PvP y PvM** (no hay MvM) |
| Una COM | **4 perfiles de máquina**: Torpe, Ofensiva, Defensiva, Experta |
| Por tiempo, por goles y supervivencia | **Por tiempo** (minutos configurables, muerte súbita) y **por goles** (cantidad configurable). **No hay supervivencia** |
| Stats 1-5: velocidad, salto, fuerza | **Stats 1-10: speed, jump, kick, survival**, que suben con **puntos** |
| Aturdido por 5 patadas | **Vida**: patadas y tiros de poder hacen daño; con vida 0 → aturdido 3 s y la vida se reinicia |
| Poderes por país (congelar, encoger, confundir) | **4 personajes** con tiros de poder: Corea, Camerún, Nigeria, EE. UU. |
| Barra que se carga | Barra de 10 s, +¼ al recibir gol, **aura** sin límite hasta tocar el balón |
| — | **Puntos** al terminar (100 ganador, 25 perdedor) para **comprar mejoras** |
| — | **Cancha en 3 resoluciones** |
| — | **Guardar y abrir partidas** y **mantener el progreso** de los jugadores |
| — | **Log de errores** para programadores |

### Interpretaciones a confirmar con el monitor (no bloquean V1)
1. **"Nivel básico" y "nivel medio".** Esta hoja asume: básico = moverse, saltar, patear, goles, tipos de partido, vida y aturdido; medio = poderes, PvM y persistencia.
2. **Precio de las mejoras** (500, 1000, 2000): ¿se duplica **por estadística** o con **cada compra** del jugador? Se asume **por estadística** (precio = 500 × 2^(nivel − 1)).
3. **¿Cuándo termina un tiro de poder** que no toca al rival ni entra al arco? Se asume: cuando lo toca otro jugador o después de 2 s.
4. **Ilusión (EE. UU.)**: ¿los 7 balones falsos tienen física? ¿Qué pasa si el tiro no golpea al rival? Por eso va de último (V4).
5. **Identidad del jugador** para guardar el progreso: ¿por nombre? ¿La máquina acumula puntos?
6. **Las 3 resoluciones**: se asume que es **la misma cancha a distinta escala** (decisión de presentación, el dominio no cambia).

---

## 1. Diagnóstico de acoplamiento (por qué los regañaron)

Medido en `fix1`. En `fix2` ya se resolvió lo de la GUI y `Match` (usan la fachada y nadie llama `getHead()` desde afuera); lo que sigue pendiente es la tabla A1-A2 de arriba:

| Problema | Evidencia | Consecuencia |
|---|---|---|
| **La GUI conoce el interior del dominio** | `PhotoHeadSoccer` hace 23 llamadas del tipo `p.getHead().getCircle()`, `p.getPower().isActive()`, `game.getField().getLeftGoal()`, `game.getScoreBoard().getPoints()` | Cualquier cambio interno (por ejemplo, cómo se guarda la cabeza) rompe la vista |
| **La GUI manipula objetos internos** | `game.getController1().receive(...)` | La vista sabe que existen los controles |
| **`Match` conoce las partes del jugador** | 16 cadenas `getHead()`, `getFoot()` y `getBody()` en `Match.resolveCollisions` | El partido depende de cómo está armado un jugador (viola la Ley de Demeter) |
| **Los poderes manipulan al rival por dentro** | `FreezePower`: `rival.getHead().setStunned(true)` | Agregar un poder obliga a conocer `Head` |
| **Constantes globales por todas partes** | `Field.WIDTH`/`Field.HEIGHT` aparecen 26 veces en el dominio | Cabeza, cuerpo, pie, arco y balón dependen de la cancha |
| **Configuración fija** | El constructor de `HeadSoccer` crea Colombia vs. Corea, humano vs. COM | No hay forma de configurar un partido sin tocar código |
| **El marcador depende del jugador** | `ScoreBoard` guarda `Player` y le escribe `setPoints` | Dos clases editan el mismo dato |
| **Mensajes de texto en el dominio** | `HeadSoccer.message` y `getTimer()` que devuelve `null` | El dominio decide qué texto mostrar |

---

## 2. Arquitectura objetivo y patrones

### Capas y dependencias

```
presentation  ──────►  domain (fachada DOPOSoccer + vistas + eventos)
                          ▲
persistence  ─────────────┘   (implementa interfaces que define domain)
```

**Regla:** `presentation` solo importa la **fachada**, las **vistas de solo lectura**, los **enums** y la **excepción**. Nunca `Player`, `Ball`, `Match`, `Controller`, etc.

### Patrones y qué acoplamiento quitan

| Patrón | Dónde | Qué resuelve |
|---|---|---|
| **Facade** | `DOPOSoccer`: única puerta de entrada al dominio | La GUI deja de conocer `Match`, `Player`, `Controller`, `ScoreBoard`... |
| **Builder** | `MatchConfig.Builder`: modalidad, personajes, tipo de partido, minutos o goles, perfiles | Crear partidas configurables sin constructores de 8 parámetros y con validación centralizada |
| **Strategy** | `Controller` (humano o máquina) | El partido no distingue humano de máquina |
| **Strategy** | `MatchRule`: `TimeRule` y `GoalRule` | Las reglas de fin de partido se cambian sin tocar `Match` |
| **Strategy** | Perfiles de máquina: `ClumsyMachine`, `OffensiveMachine`, `DefensiveMachine`, `ExpertMachine` | Agregar un perfil no toca nada más (requisito de extensión) |
| **Template Method** | `PowerShot`: `launch()` fija los pasos; cada país define cómo sale el tiro, su daño y su efecto | Agregar un personaje = una clase |
| **Strategy / State** | `Effect`: `PushEffect`, `ElectrocutedEffect`, `UnconsciousEffect`, `ConfusionEffect` | El jugador pregunta `canJump()`, `speedFactor()`... sin `if` por cada poder |
| **Factory** | `Country` crea su `PowerShot`; `MachineProfile` crea su `Controller` | Un solo lugar sabe qué clase concreta corresponde a cada opción del menú |
| **Observer** | `MatchListener` con `onGoal`, `onStunned`, `onPowerShot`, `onMatchFinished` | El dominio avisa y la GUI decide qué mostrar; desaparece `message` |
| **Vistas de solo lectura** (interfaces) | `MatchView`, `PlayerView`, `BallView` | La GUI y la máquina solo pueden mirar, no modificar |
| **Repository** (DIP) | `MatchRepository` y `ProfileRepository` definidos en `domain`, implementados en `persistence` | El dominio no depende de archivos |

### Modelo del dominio (resumen)

```
DOPOSoccer (Facade)
 ├─ newMatch(MatchConfig) / tick() / setIntention(Side, Set<PlayerAction>)
 ├─ pause() / resume() / finish()
 ├─ getMatch() : MatchView
 ├─ addListener(MatchListener)
 └─ (V2) save / open      (V3) upgrade(Side, StatType)

Match ── MatchRule (Strategy)  ── TimeRule | GoalRule
  │  ── ScoreBoard (goles por Side)
  │  ── Ball (+ tiro de poder en vuelo)
  │  ── EnumMap<Side, Player>
  │  ── List<MatchListener> (Observer)

Player (en el partido)
  ├─ PlayerProfile  (persistente: nombre, Stats, puntos)
  ├─ Country        (personaje → PowerShot)
  ├─ Controller     (Strategy: HumanController | máquinas)
  ├─ Life, PowerBar, aura, Effect (Strategy), aturdido
  └─ cuerpo: cabeza, cuerpo y pie (internos, nadie los ve desde afuera)
```

**Separación clave:** el enunciado dice que *las estadísticas aplican a todos los personajes del jugador* y que hay que *mantener el progreso*. Por eso:
- **`PlayerProfile`** = la persona: nombre, estadísticas y puntos. **Persiste entre partidas.**
- **`Player`** = la participación en **un** partido: posición, vida, barra, aura, efecto y goles. Tiene un `PlayerProfile` y un `Country`.

### La idea que evita rehacer trabajo
**Primero se define la API pública (fachada, vistas y eventos); después se rediseña por dentro.**
Una vez que la GUI y las pruebas solo usan la fachada, cualquier cambio interno (ancla única, colisiones, poderes) **no las rompe**. Por eso el paso 1 de V1 es la fachada.

---


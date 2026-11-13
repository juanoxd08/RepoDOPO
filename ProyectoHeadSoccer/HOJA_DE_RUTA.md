# DOPOSoccer 2026-2 — Hoja de ruta

Punto de partida: commit `fix1` (`86b5ffa`).
Enunciado vigente: `DOPOSoccer_2026-2.pdf` (reemplaza al de POOBSoccer).

Esta hoja reemplaza a `PLAN_DOMINIO.md` y `HOJA_DE_RUTA_DOMINIO.md`, que quedaron obsoletos con el enunciado nuevo.

---

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

Medido en `fix1`:

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

## 3. V1 — PvP básico, desacoplado (hasta Do 15 nov)

**Alcance:** configuración (personajes, tipo de partido y su valor, resolución), PvP completo sin poderes, vida y daño por patada, aturdido, pausa, terminar, mensaje de ganador con puntos.
**Fuera de V1:** poderes, máquina, persistencia y mejoras de estadísticas (la barra de poder se muestra vacía o no se muestra).

### Paso 1.0 — Repo (≈ 0.5 h) · *indispensable*
- [ ] `.gitignore` con `bin/` y `git rm -r --cached ProyectoHeadSoccer/bin`.
- [ ] JUnit 5 configurado y carpeta `test/domain`.

### Paso 1.1 — API pública primero (≈ 3 – 4 h) · *indispensable*

| Archivo | Tarea |
|---|---|
| `HeadSoccerException` → **`DOPOSoccerException`** | Renombrar; constantes de mensajes (`INVALID_MATCH_CONFIG`, `INVALID_STAT`, `INVALID_STATE`, `NOT_ENOUGH_POINTS`...) |
| `HeadSoccer` → **`DOPOSoccer`** (Facade) | Métodos: `newMatch(MatchConfig)`, `tick()`, `setIntention(Side, Set<PlayerAction>)`, `pause()`, `resume()`, `finish()`, `getMatch()` (devuelve `MatchView`), `addListener(MatchListener)`. Borrar la configuración fija, `message`, `getTimer()`, `setState` y los getters de jugadores, balón, campo y controles |
| **`MatchConfig`** (Builder, nuevo) | Campos: modalidad, `Country` izquierda y derecha, `MatchType` (`TIME`/`GOALS`) y su valor (minutos o goles). `build()` valida y lanza `DOPOSoccerException` |
| **`Modality`**, **`MatchType`** (enums nuevos) | `PVP`, `PVM` · `TIME`, `GOALS` |
| **`MatchView`**, **`PlayerView`**, **`BallView`** (interfaces nuevas) | Solo lectura. `MatchView`: estado, segundos restantes (−1 si no aplica), muerte súbita, goles por lado, `getPlayer(Side)`, `getBall()`, ganador. `PlayerView`: país, lado, vida, vida máxima, barra (0..1), aura, efecto, aturdido, pateando y **figuras para dibujar** (cabeza, cuerpo y pie como copias). `BallView`: x, y, radio, si lleva tiro de poder |
| **`MatchListener`** (Observer, nuevo) | Métodos `default` vacíos: `onGoal(Side scorer)`, `onStunned(Side)`, `onMatchFinished(Side winner, int winnerPoints, int loserPoints)`; en V2 se agregan los de poder |
| `Country` | Dejar exactamente los 4 del Anexo 1: `SOUTH_KOREA`, `CAMEROON`, `NIGERIA`, `USA` (con nombre para mostrar) |

**Listo cuando:** existe una prueba que crea un partido PvP con el builder, llama `tick()` y lee el estado **solo** por `MatchView`.

### Paso 1.2 — Reglas del partido con Strategy (≈ 2.5 – 3 h) · *indispensable*

| Archivo | Tarea | Bug que cierra |
|---|---|---|
| `Side` | `dir()`, `opposite()`, `startX()` | Reemplaza 13 ternarios |
| `ScoreBoard` | `EnumMap<Side, Integer>`, `goalIn(Side goalSide)` (suma al contrario), `getGoals(Side)`, `isTie()`, `getLeader()` como `Optional<Side>`. Sin referencias a jugadores | Marcador acoplado a `Player`, `golIn` duplicado, `null` |
| `GameStatus` | `NOT_STARTED`, `PLAYING`, `GOAL_SCORED`, `SUDDEN_DEATH`, `PAUSED`, `FINISHED` | — |
| **`MatchRule`** (interfaz, Strategy) | `onTick(Match)`, `isOver(Match)`, `winner(Match)` | — |
| **`TimeRule`** | Minutos configurables, en ticks. Al acabar con empate → `SUDDEN_DEATH`; el primer gol gana | Partido sin fin |
| **`GoalRule`** | Gana quien llega a N goles | Partido sin fin |
| `Match` | Recibe `MatchRule`. `tick()` en orden fijo: intenciones → jugadores → balón → colisiones → gol → regla. Pausa de 1 s tras el gol, luego reinicio. Notifica a los listeners. Calcula los puntos (100 y 25) al terminar | — |
| `MatchTimer` | Borrar (lo reemplaza `TimeRule`) | — |
| `Physics` | `TICK_MS = 1000 / FPS` (la GUI lo usa para su `Timer`) | El tiempo de la GUI no coincidía |

### Paso 1.3 — Jugador encapsulado y bugs de PvP (≈ 5 – 6 h) · *indispensable*

Se hace **una sola vez** y queda detrás de la API: los cambios internos posteriores no rompen nada.

| Archivo | Tarea | Bug que cierra |
|---|---|---|
| `Player` | **Ancla única**: el jugador guarda una posición y cabeza, cuerpo y pie se calculan relativos a ella. Mover y reiniciar son una línea | `resetPosition` recreaba objetos; knockback que no volvía; partes que se desarman |
| `Player` | Métodos que esconden las partes: `touchBall(Ball)` (rebote con cuerpo, cabeza y pie), `kickReaches(Player rival)`, `collideWith(Player rival)`, `standOn(Goal)`. **`Match` ya no llama `getHead()`, `getFoot()` ni `getBody()`** | Acoplamiento `Match` → partes |
| `Player` | `onTop` unificado: se reinicia cada tick y solo lo ponen en `true` los soportes reales. Borrar `collideMyFootWithBall` | `onTop` pisado; "parado sobre el balón" |
| `Player` | `applyInput` sin `switch`: aturdido primero, izquierda/derecha resuelto, patada en **flanco de subida** (guarda la intención anterior). Quitar el `stopKick()` | KICK mantenido patea muchas veces |
| `Foot` | Arco de patada que barre adelante y poco hacia arriba (constantes), para que le pegue al balón en el piso | La patada no tocaba balones en el piso |
| `Ball` | Separar el balón de la figura después de cada contacto | Golpes repetidos en ticks seguidos |
| **`Stats`** + `StatType` (nuevos) | Niveles 1-10 para `SPEED`, `JUMP`, `KICK`, `SURVIVAL`. `kickMultiplier() = 1 + 0.1(k−1)`, `maxLife() = 100(1 + 0.1(s−1))`, velocidad y salto = base × (1 + 0.1(nivel−1)) | Stats del enunciado viejo |
| **`PlayerProfile`** (nuevo) | Nombre, `Stats` y puntos. En V1 se crea con todo en 1 y 0 puntos | — |
| `Player` | **Vida**: `receiveDamage(d)` (nunca menor que 0); en 0 → aturdido 3 s; al terminar, vida al máximo. **Daño por patada**: `10 × kickMultiplier`, una vez por patada, solo si el pie alcanza al rival; si toca el balón, solo lo impulsa | Aturdido viejo por 5 patadas |
| `Head` | Quitar `isStunned`, el conteo de patadas y `isStruck`/`isNotStruck` | — |
| `HumanController` | `EnumSet` y copia defensiva al recibir y al devolver | Referencia compartida |
| Setters | Borrar los que permiten romper el estado (`setHead`, `setFoot`, `setBody`, `setCircle`, `setSide`, `setOnTop`, `setVerticalSpeed`...) | Estado modificable desde afuera |
| Poderes | **Desconectar** `Power` y `FreezePower` del partido en V1 (no se borran todavía; se rehacen en V2) | Poder "control remoto" y jugador fantasma |

### Paso 1.4 — Interfaz decente usando solo la fachada (≈ 5 – 7 h) · *indispensable*

| Archivo | Tarea |
|---|---|
| `HeadSoccerGUI` → **`DOPOSoccerGUI`** | Ventana con `CardLayout`: **configuración → juego → resultado** |
| **`ConfigPanel`** (nuevo) | Modalidad (PvM deshabilitado en V1), personaje de cada jugador, tipo de partido (tiempo o goles) con su valor, **resolución** (3 opciones). Errores con `JOptionPane` y el mensaje de la `DOPOSoccerException` |
| `PhotoHeadSoccer` → **`FieldPanel`** | Dibuja **solo con `MatchView`**. Escala mundo → pantalla según la resolución elegida |
| **HUD** | Siempre visible: tiempo (o meta de goles), marcador y, por jugador, país, goles, **barra de vida** y barra de poder (vacía en V1) |
| Teclado | Traduce teclas a `Set<PlayerAction>` y llama `game.setIntention(side, set)`. Sin `instanceof` ni controles. `P` pausa y `Esc` termina (con confirmación) |
| `Timer` | `new Timer(Physics.TICK_MS, ...)` |
| Fin del partido | Implementa `MatchListener.onMatchFinished` y muestra el ganador y los puntos obtenidos |

**Listo cuando:** buscar `getHead`, `getFoot`, `getBody`, `getController`, `Player`, `Ball` y `Match` en `presentation` no da resultados.

### Paso 1.5 — Pruebas de unidad (≈ 3 h) · *indispensable*
- [ ] `MatchConfigTest`: configuración válida; goles ≤ 0 o minutos ≤ 0 lanzan excepción.
- [ ] `StatsTest`: tabla del Anexo 2 (vida máxima y multiplicador de kick para los niveles 1, 5 y 10); nivel 0 u 11 lanza excepción.
- [ ] `PlayerTest`: salto con altura mínima; patada en flanco; daño de patada = `10 × multiplicador`; vida en 0 → aturdido 3 s → vida al máximo; aturdido ignora el input.
- [ ] `BallTest`: balón quieto sigue quieto; no atraviesa piso, paredes ni travesaños; la patada en el piso lo mueve hacia el arco rival.
- [ ] `MatchTest`: gol completo suma; balón sobre la línea no suma; autogol suma al rival; pausa congela; reinicio tras el gol.
- [ ] `TimeRuleTest` y `GoalRuleTest`: fin por tiempo, muerte súbita, fin al llegar a N.
- [ ] `DOPOSoccerTest`: ejemplo del PDF (Kick 5 contra Survival 3 → 120 − 14 = 106).

### Paso 1.6 — Documentación de la entrega (≈ 2 – 3 h) · *indispensable*
- [ ] **Diagrama de clases** actualizado (sin `MachinePlayer`, `HeadSoccer` ni `MatchTimer`).
- [ ] **Diagramas de secuencia** (mínimo 4):
  1. Crear un partido desde la configuración: GUI → `MatchConfig.Builder` → `DOPOSoccer.newMatch` → `Match`.
  2. Un tick: `Timer` → `DOPOSoccer.tick` → `Match.tick` → controles → jugadores → balón → colisiones → regla.
  3. Patada que termina en gol: → `ScoreBoard.goalIn` → `MatchListener.onGoal` → reinicio.
  4. Patada que deja al rival en vida 0 → aturdido.
- [ ] **Retrospectiva**: qué se hizo, qué no, el feedback recibido (COM antes de tiempo, acoplamiento) y cómo se corrigió con patrones.

### Si alcanza en V1
- Cancha con colisiones unificadas (`Vector2`, `Contact`, un solo `bounceOff`). Si no alcanza, va al inicio de V2: es interno y no rompe nada.

**Total V1 ≈ 21 – 27 h** (≈ 11 – 14 h por persona).

---

## 4. V2 — Poderes, PvM y persistencia (hasta Sa 21 nov)

### Paso 2.1 — Física unificada (≈ 3 – 4 h)
- [ ] `Vector2` y `Contact`; `Figure.contactWith(Circle)`.
- [ ] `Ball.bounceOff(Figure, restitución, impulso)` reemplaza `collideWithFoot`, `collideWithHead` y `collideWithGB`. Borrar `Collidable`.
- [ ] Las constantes de la cancha pasan a un objeto `Field` con medidas, en vez de `Field.WIDTH`/`HEIGHT` estáticos usados en 26 lugares.

### Paso 2.2 — Poderes según el PDF (≈ 6 – 8 h)

| Clase | Regla del PDF |
|---|---|
| **`PowerBar`** | Se llena en 10 s; **+¼ al recibir gol**; al usarla se vacía y empieza a recargar en el mismo instante |
| `Player` | `activatePower()`: barra llena **y sin aura** → vacía la barra y pone el aura. **El aura no tiene límite de tiempo.** Con aura no se puede activar otro poder |
| `Player.touchBall` | Si tiene aura: `country.powerShot().launch(ball, this)` y el aura desaparece |
| **`PowerShot`** (Template Method) | `final launch(Ball, Player)`: marca el balón con el tiro y su dueño, y llama `shapeShot(Ball, Side)` (cómo sale). Abstractos: `baseDamage()` (40, 60, 40, 30) y `createEffect()` |
| `Match` | Si el balón con tiro golpea al rival → daño `baseDamage × kickMultiplier` del dueño + `rival.applyEffect(...)`. Si entra al arco → gol sin daño ni efecto |
| **`Effect`** (Strategy) | Duración en ticks; `speedFactor()`, `canMove()`, `canJump()`, `canKick()`, `onStart(Player)`, `onEnd(Player)`. **No se acumulan:** el nuevo reemplaza al anterior |
| `BlueAuraShot` + `PushEffect` (Corea) | Tiro recto y horizontal muy rápido · el rival es lanzado hacia su arco, 1.5 s |
| `LightningShot` + `ElectrocutedEffect` (Camerún) | Tiro recto · no puede saltar y se mueve al 50 %, 3 s |
| `TornadoShot` + `UnconsciousEffect` (Nigeria) | Remolino a ras de suelo que rebota rápido · lanzado al aire, no puede moverse, saltar ni patear, 3 s |
| `Country` (Factory) | Cada país crea su `PowerShot` |
| `MatchListener` | `onPowerActivated(Side)`, `onPowerShotHit(Side victim)` |
| Borrar | `Power`, `FreezePower` |

GUI: el **aura visible** alrededor del jugador, el **efecto visible** sobre el rival y la barra de poder en el HUD (todo leído desde `PlayerView`).

### Paso 2.3 — PvM (≈ 4 – 5 h)
- [ ] **`MachineProfile`** (enum, Factory): `CLUMSY`, `OFFENSIVE`, `DEFENSIVE`, `EXPERT`, con `create(Side, Random)`.
- [ ] **`MachineController`** abstracta (Template Method): `decide()` = percibir → elegir → armar la intención. Cada perfil implementa solo "elegir".
- [ ] **Torpe**: acciones al azar (`Random` inyectable, para poder probarlo).
- [ ] **Ofensiva**: ponerse detrás del balón y patear hacia el arco rival; usar el poder cuando va a tocar el balón.
- [ ] **Defensiva**: quedarse entre el balón y su arco; despejar.
- [ ] Experta: en V3.
- [ ] `MatchConfig` acepta el perfil; `ConfigPanel` habilita PvM con el selector de perfil.
- [ ] Borrar `COMController` y `COMState` (reutilizar su lógica en la ofensiva).

### Paso 2.4 — Persistencia (≈ 4 – 5 h)
- [ ] Dominio: interfaces **`MatchRepository`** (`save(Match, File)`, `open(File)`) y **`ProfileRepository`** (`load(name)`, `save(profile)`).
- [ ] Paquete **`persistence`**: implementación con serialización de Java (`Serializable` en las clases del partido; los listeners son `transient`).
- [ ] Fachada: `save(File)`, `open(File)` y perfiles al configurar.
- [ ] GUI: menú *Guardar* y *Abrir* con `JFileChooser`.
- [ ] Errores de archivo → `DOPOSoccerException` con un mensaje claro para el usuario.

### Paso 2.5 — Log de errores (≈ 1 h)
- [ ] Clase **`Log`** con `java.util.logging` que escribe en un archivo (`dopo-errores.log`): cada `DOPOSoccerException` y cada error inesperado queda registrado con su traza. Al usuario solo se le muestra el mensaje claro.

### Paso 2.6 — Pruebas y entrega (≈ 3 – 4 h)
- [ ] Pruebas: barra (10 s, +¼, sin doble activación con aura), cada efecto (duración, reemplazo), daño de poder (ejemplo del PDF: 60 × 1.4 = 84), tiro que entra sin tocar al rival, guardar y abrir el mismo estado, perfiles de máquina con `Random` fijo.
- [ ] **Planificación de las 4 versiones** (entregable de V2): objetivo y miniciclos de cada versión, y requisitos que no se incluirán.

**Total V2 ≈ 21 – 27 h.**

---

## 5. V3 — Progreso y máquina experta (hasta Do 22 nov)

- [ ] **Mejoras de estadísticas**: `PlayerProfile.upgrade(StatType)` con precio 500 × 2^(nivel − 1), máximo 10 y excepción si no alcanzan los puntos. Pantalla de mejoras antes del partido. (≈ 3 h)
- [ ] **Puntos y progreso persistidos**: al terminar, sumar 100 o 25 y guardar el perfil. (≈ 1.5 h)
- [ ] **`ExpertMachine`**: elige entre atacar y defender según el marcador, el tiempo, la vida propia y la del rival, y la barra de poder. (≈ 3 – 4 h)
- [ ] **Pruebas de aceptación**: escenarios del enunciado escritos como casos (configurar y jugar PvP por goles, por tiempo con muerte súbita, guardar y abrir, comprar una mejora...). (≈ 2 h)

**Total V3 ≈ 9.5 – 10.5 h.**

## 6. V4 — Ilusión y pulido (parcial)

- [ ] **`IllusionShot`** (EE. UU.): 8 balones iguales y 7 falsos que desaparecen al terminar el efecto de 2.5 s. Requiere que `MatchView` exponga una **lista** de balones. Confirmar antes las reglas (ver la sección 0). (≈ 3 – 4 h)
- [ ] Revisión de extensión: agregar un personaje o un perfil **solo** debe requerir una clase nueva y una línea en el enum (probarlo). (≈ 1 h)
- [ ] Javadoc, diagramas finales y retrospectiva. (≈ 2 – 3 h)

---

## 7. Qué pasa con los bugs anteriores

| Bug de `fix1` | Dónde se resuelve |
|---|---|
| Poder "control remoto" y barra llena al inicio | Desconectado en V1, rehecho con las reglas nuevas en 2.2 |
| Dueño del poder atraviesa el balón | Desaparece con 2.2 |
| La patada no le pega al balón en el piso | 1.3 |
| KICK mantenido patea muchas veces | 1.3 |
| `Timer` de 20 ms contra 60 FPS | 1.2 + 1.4 |
| `HumanController` comparte la referencia | 1.3 |
| `ReaderMatch` deja modificar el partido | 1.1 (vistas) |
| `applyInput` sin chequeo central | 1.3 |
| COM sin histéresis y defensa contra la pared | 2.3 (perfiles nuevos) |
| `Match` sin modos ni fin | 1.2 |
| `ScoreBoard` acoplado a `Player` | 1.2 |
| Knockback, `onTop`, `resetPosition` | 1.3 (ancla única) |
| Empuje con `getSpeed()` | 1.3 (`Stats`) |
| `bin/` versionado | 1.0 |
| Sin pruebas | 1.5 y en cada versión |
| Setters de más | 1.3 |
| `Collidable` vacía | 2.1 |
| `HeadSoccer` con configuración fija | 1.1 |
| Golpes repetidos del pie | 1.3 |

---

## 8. Resumen

| Versión | Horas | Fecha |
|---|---|---|
| V1 — PvP desacoplado + GUI + pruebas + diagramas | 21 – 27 | Do 15 nov |
| V2 — física unificada, poderes, PvM, persistencia, log | 21 – 27 | Sa 21 nov |
| V3 — mejoras, progreso, experta, aceptación | 9.5 – 10.5 | Do 22 nov |
| V4 — ilusión y pulido | 6 – 8 | Parcial |
| **Total** | **≈ 58 – 73 h** | |

### Reparto sugerido para V1 (2 personas, 2 días)
- **Persona A (dominio):** 1.1 → 1.2 → 1.3 y las pruebas de dominio.
- **Persona B (presentación y entrega):** empieza 1.4 apenas exista la fachada del paso 1.1 (pueden acordar primero las firmas y luego trabajar en paralelo), y después los diagramas de secuencia y la retrospectiva.
- **El paso 1.1 se hace juntos y primero:** es el contrato entre los dos.

### Reglas de trabajo
1. **La API primero, el interior después.** Nunca cambiar una firma de la fachada o de las vistas sin avisar al otro.
2. Un commit por paso; push al terminar cada paso.
3. Cada paso termina con sus pruebas en verde.
4. Nada de `instanceof`, ni de `getX().getY().getZ()` desde fuera de la clase dueña.

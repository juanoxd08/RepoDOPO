# DOPOSoccer 2026-2 — Hoja de ruta

Enunciado vigente: `DOPOSoccer_2026-2.pdf`.
Código actual: **combinación** del trabajo de Cristian y de Juan sobre `fix1` (ver la sección "Estado actual").

---

## Estado actual (después de combinar)

### Qué se combinó y de dónde salió cada cosa

| Origen | Qué aporta | Dónde quedó |
|---|---|---|
| **Cristian** (base) | Reglas del PDF funcionando: vida, daño por patada, aturdido 3 s, stats 1-10 con fórmulas del Anexo 2, 4 poderes con aura y barra de 10 s, +¼ al recibir gol, tiempo con muerte súbita y goles, puntos 100/25, 4 perfiles de máquina, resoluciones, GUI con menú/selección/HUD, **26 pruebas** | `src/` (todo lo que ya existía) |
| **Juan** (paso 1.1 y parte del 1.2) | `DOPOSoccerException`, `Modality`, `StatType`, `MatchListener`, `BallView`, `PlayerView`, `MatchView`, `MatchRule`, `MatchConfig` (Builder), `Side` con `dir()`/`startX()`/`opposite()`, `Physics.TICK_MS` | `src/domain/` |
| **Juan** (necesitan el `Match` nuevo) | `DOPOSoccer` (fachada), `GoalRule`, `ScoreBoard` con `EnumMap` | **`pendiente_paso_1_2/domain/`** (fuera de `src`, para que el proyecto compile) |
| Configuración de Eclipse | `.classpath` con JUnit y `.settings` con Java 17 (de Cristian) | raíz del proyecto |

### Decisiones al combinar (sin agregar funcionalidad)
- **`MatchType`**: nombres de Juan (`TIME`, `GOALS`) + nombres para mostrar de Cristian ("Por Tiempo", "Por Goles").
- **`Country`**: nombre de Juan `SOUTH_KOREA` (antes `KOREA`) + campos de Cristian (`displayName`, `powerName`) + `toString()` de Juan. **Se conservaron los 8 países de Cristian** (los 4 extra no están en el PDF; decidir en el paso 1.1b si se quitan).
- **`GameStatus`**: unión de los dos. `GOAL` → `GOAL_SCORED` y `GOLDEN_GOAL` → `SUDDEN_DEATH` (nombres de Juan), se agregó `NOT_STARTED` (Juan) y se mantienen `MENU`, `SELECT_PLAYERS`, `SELECT_CAMP` y `DRAW` (Cristian), que deben salir del dominio en el paso 1.4.
- **`Head`, `Side`, `Physics`**: versión de Juan (en `Head` solo había cambios de formato).
- **`Player`**: versión de Cristian. Los cambios de Juan en `Player` eran solo de nombres de variables y formato, sin cambio de comportamiento.
- **`module-info.java`**: no se incluye (Cristian lo había quitado para poder compilar las pruebas).
- Se renombraron todas las referencias en `src` (dominio, GUI y pruebas) a los nombres nuevos.

### Verificación
- Compila sin errores.
- **26/26 pruebas de Cristian pasan**, igual que en su versión original.
- Los archivos de `pendiente_paso_1_2/` **no compilan todavía** a propósito: usan métodos del `Match` nuevo (`Match(MatchConfig)`, `getGoals(Side)`, `setIntention`, `pause`, `resume`, `finish`, `getStatus`, `addListener`, `removeListener`) que se crean en el paso 1.2.

### Clases que conviven por ahora (se resuelven en V1)

| Vieja | Nueva | Se resuelve en |
|---|---|---|
| `HeadSoccer` | `DOPOSoccer` (en `pendiente_paso_1_2`) | 1.2 y 1.4 |
| `HeadSoccerException` | `DOPOSoccerException` | 1.3 |
| `ReaderMatch` | `MatchView` | 1.2 |
| `MatchTimer` + `if (matchType == ...)` en `Match` | `MatchRule` + `TimeRule`/`GoalRule` | 1.2 |
| `ScoreBoard` (por jugador) | `ScoreBoard` con `EnumMap` (en `pendiente_paso_1_2`) | 1.2 |
| `PlayerStats` | `Stats` + `StatType` | 1.3 |

### Bugs encontrados en el código actual (verificados ejecutándolo)
- **B-N1** La barra de poder **empieza llena** (`Power`: `charge = 100.0`). El PDF dice que empieza vacía.
- **B-N2** El efecto **Inconsciente (Nigeria) saca al jugador de la cancha**: la cabeza sube de y = 145 hasta y = −575 en 3 s (cancha de 200 de alto).
- **B-N3** El tiro de poder **solo sale al patear**; el PDF dice "cuando el personaje con poder **toca** el balón" (confirmar con el monitor).
- **B-N4** Velocidad y salto no siguen "+10 % sobre el valor base por punto" (`3 + 0.5 × speed`, `12 + 0.8 × jump`).
- **B-N5** `PlayerStats`: el constructor recorta en silencio los valores fuera de 1-10, pero los setters lanzan excepción.
- **B-N6** Heredados de `fix1` (no se tocaron): KICK mantenido patea varias veces; el pie que no patea atraviesa el balón; `onTop` pisado; `resetPosition` recrea cabeza, cuerpo y pie; el empuje entre jugadores usa `getSpeed()`.
- **B-N7** `instanceof IllusionPower` en `Match` (1) y en la GUI (2).

### Siguiente paso
**`TimeRule.java`** (paso 1.2), que es donde estaba Juan. La lógica ya existe en `Match.tick()` de Cristian (descontar tiempo, muerte súbita con empate) y en `MatchTimer`: hay que **moverla** a `TimeRule`, no escribirla de nuevo.

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

Medido en `fix1` (en el código combinado la situación es la misma: la GUI, `Match` y los poderes siguen accediendo a las partes internas del jugador):

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

**Alcance:** configuración (personajes, tipo de partido y su valor, resolución), PvP completo, vida y daño por patada, aturdido, pausa, terminar, mensaje de ganador con puntos.
Los poderes y la máquina **ya existen** (Cristian): se dejan funcionando, se arreglan sus dos bugs graves (B-N1, B-N2) y **no se invierte más tiempo en ellos antes de V1**.

Leyenda: ✅ hecho · 🟡 parcial · ⬜ pendiente.

### Paso 1.0 — Repo (≈ 0.25 h)
- ⬜ `.gitignore` con `bin/` y `git rm -r --cached ProyectoHeadSoccer/bin`.
- ✅ JUnit en el `.classpath` (de Cristian).
- ⬜ Decidir **JUnit 4 o 5**: las 26 pruebas usan `org.junit.Test` (JUnit 4). Lo más rápido es seguir con JUnit 4 para todas.

### Paso 1.1 — API pública ✅ (Juan) + ajustes (≈ 0.5 h)
- ✅ `DOPOSoccerException`, `Modality` (`PVP`, `PVC`), `MatchType`, `StatType`, `Country`, `GameStatus`, `BallView`, `PlayerView`, `MatchView`, `MatchListener`, `MatchConfig` (Builder).
- ✅ `DOPOSoccer` (fachada) escrita — está en `pendiente_paso_1_2/` hasta que exista el `Match` nuevo.
- ⬜ **1.1b** Decidir si `Country` se queda con los 4 países del PDF o con los 8 de Cristian. Si se quitan los 4 extra: borrar sus `case` en `HeadSoccer.createPowerForCountry`, sus colores en `PhotoHeadSoccer` y sus entradas en los arreglos de selección; `FreezePower` queda sin uso.
- ⬜ Mover la creación del poder a `Country` (Factory): `Country.createPower()` en vez del `switch` de `HeadSoccer.createPowerForCountry`.

### Paso 1.2 — Reglas del partido y `Match` nuevo (≈ 4 – 5 h) · *el más importante*

| Tarea | Estado | Detalle |
|---|---|---|
| `Side` con `dir()`, `opposite()`, `startX()` | ✅ | Falta usarlo: reemplazar los ternarios `side == Side.LEFT ? … : …` (están en poderes, COM, `Player` y `Foot`) |
| `Physics.TICK_MS` | ✅ | La GUI todavía usa `new Timer(16, …)` → cambiar a `Physics.TICK_MS` en 1.4 |
| `MatchRule` | ✅ | |
| `GoalRule` | 🟡 | Escrita, en `pendiente_paso_1_2/` |
| **`TimeRule`** | ⬜ | **Siguiente archivo.** Mover aquí la lógica de `Match.tick()` (tiempo, muerte súbita) y de `MatchTimer` (minutos → ticks, `getFormattedTime` puede quedar en la GUI) |
| `ScoreBoard` con `EnumMap` | 🟡 | Escrito, en `pendiente_paso_1_2/`. Al traerlo a `src`: quitar `goals`/`addGoal` de `Player`; en `COMController` cambiar `getScoreBoard().getPoints()[i]` por `getGoals(side)`; en la GUI leer goles desde `MatchView.getGoals(side)` |
| **`Match` nuevo** | ⬜ | (1) Constructor `Match(MatchConfig)`: crea jugadores y controles (reutilizar el código de `HeadSoccer.startActiveMatch`, que hoy está duplicado con `createDefaultMatch`). (2) Usa `MatchRule` en vez de `if (matchType == …)`. (3) `implements MatchView` en vez de `ReaderMatch`. (4) `setIntention(Side, Set)`, `pause()`, `resume()`, `finish()`, `getStatus()`. (5) `addListener`/`removeListener` y notificar `onGoal`, `onStunned`, `onMatchFinished` **en lugar de** `winnerNotification` y `lastScorerText` (texto en el dominio) |
| `Controller` y `COMController` | ⬜ | `decide(MatchView)` en vez de `ReaderMatch`; borrar `ReaderMatch` |
| Mover `DOPOSoccer`, `GoalRule` y `ScoreBoard` a `src/domain` | ⬜ | Cuando el `Match` nuevo compile. Borrar la carpeta `pendiente_paso_1_2` |
| Borrar `MatchTimer` | ⬜ | Después de `TimeRule` |

**Listo cuando:** existe una prueba que crea un partido con `MatchConfig.builder()...build()`, lo juega con `DOPOSoccer.tick()` y lee el resultado **solo** por `MatchView`.

### Paso 1.3 — Jugador y física (≈ 4 – 5 h)

| Tarea | Estado | Detalle |
|---|---|---|
| Vida, `takeDamage` (mínimo 0), aturdido 3 s, vida al máximo al terminar | ✅ | Cristian |
| Daño de patada `10 × multiplicador` | ✅ | Cristian (verificado: 120 → 108 con Kick 3) |
| Fórmulas del Anexo 2 (vida máxima, multiplicador) | ✅ | Cristian, en `PlayerStats` |
| **B-N1** barra vacía al inicio | ⬜ | `Power`: `charge = 0.0` (revisar las pruebas que asumen barra llena) |
| **B-N2** Inconsciente sale de la cancha | ⬜ | Limitar el tiempo que sube o poner techo para el jugador |
| **B-N4** velocidad y salto con +10 % por punto | ⬜ | `base × (1 + 0.1 × (nivel − 1))` |
| `Stats` + `PlayerProfile` | ⬜ | `PlayerStats` → `Stats` (con `StatType` y validación consistente, B-N5); `points` sale de `Player` a `PlayerProfile` |
| `Player implements PlayerView`, `Ball implements BallView` | ⬜ | Las figuras que se devuelven deben ser **copias** |
| `Player` encapsulado | ⬜ | `Match.resolveCollisions` no debe llamar `getHead()`, `getFoot()` ni `getBody()`: métodos `touchBall(Ball)`, `hitByPowerShot(Ball)`, etc. en `Player` |
| Ancla única | ⬜ | Resuelve `resetPosition` y el knockback (B-N6). *Si no alcanza, pasa a V2: es interno* |
| `applyInput` con flanco de subida en KICK y POWER, sin `stopKick()` | ⬜ | B-N6 |
| `onTop` unificado, empuje con `speedPx()`, arco de patada bajo, separar balón tras contacto | ⬜ | B-N6 |
| `HumanController` con `EnumSet` y copia defensiva | ⬜ | |

### Paso 1.4 — GUI solo con la fachada (≈ 3 – 4 h)
- ✅ Pantallas de menú, selección de jugadores, selección de cancha (resolución), partido con HUD y overlays (Cristian).
- ⬜ `HeadSoccerGUI` → `DOPOSoccerGUI`; usar `DOPOSoccer` en vez de `HeadSoccer`.
- ⬜ Entrada: `game.setIntention(side, set)` en vez de `getController1().receive(...)`; quitar `getPlayer1().stopKick()` de `keyReleased`.
- ⬜ Dibujo: leer **solo** `MatchView`/`PlayerView`/`BallView` (hoy hay 15 accesos del tipo `getHead()`/`getPower()` y 2 `instanceof IllusionPower`).
- ⬜ Mensajes con `MatchListener` (gol, aturdido, ganador con puntos).
- ⬜ Navegación de pantallas (`MENU`, `SELECT_PLAYERS`, `SELECT_CAMP`) **en la GUI**, no en `GameStatus`; la configuración elegida se arma con `MatchConfig.Builder`.
- ⬜ `FieldResolution` a `presentation` (es de pantalla).
- ⬜ `Timer` con `Physics.TICK_MS`.
- Opcional: separar `PhotoHeadSoccer` (644 líneas) en paneles. Si no alcanza, V2.

**Listo cuando:** buscar `getHead`, `getFoot`, `getBody`, `getController`, `getPower`, `instanceof` y `HeadSoccer` en `presentation` no da resultados.

### Paso 1.5 — Pruebas (≈ 2 h)
- ✅ 26 pruebas de Cristian (fórmulas, vida, aturdido, poderes, muerte súbita, puntos, máquinas, balón y gol).
- ⬜ Ajustarlas cuando cambie la API (constructor de `Match`, `ScoreBoard`, barra vacía).
- ⬜ Nuevas: `MatchConfig` (valores inválidos lanzan excepción), `GoalRule`, `TimeRule`, `ScoreBoard` (autogol), ejemplo del PDF (Kick 5 contra Survival 3 → 106), flanco de KICK.

### Paso 1.6 — Documentación (≈ 2 – 3 h)
- ⬜ Diagrama de clases actualizado.
- ⬜ 4 diagramas de secuencia: crear partido (GUI → `MatchConfig.Builder` → `DOPOSoccer.newMatch` → `Match`), un tick, patada que termina en gol, patada que deja al rival aturdido.
- ⬜ Retrospectiva (incluir el feedback del monitor y cómo se respondió con patrones).

**Lo que queda de V1 ≈ 16 – 20 h** (≈ 8 – 10 h por persona).

### Reparto sugerido
- **Juan:** 1.2 completo (sigue donde iba: `TimeRule` → `Match` nuevo → traer la fachada) y después 1.3.
- **Cristian:** 1.3 bugs B-N1, B-N2 y B-N4 (rápidos, en sus clases), y luego 1.4 (conoce la GUI) **apenas** el `Match` nuevo compile. Mientras tanto: diagramas de secuencia y retrospectiva.
- **Contrato entre los dos:** `MatchConfig`, `MatchView`, `PlayerView`, `BallView`, `MatchListener` y `DOPOSoccer` ya están escritos. No cambiar sus firmas sin avisar.

---

## 4. V2 — Poderes, PvM y persistencia (hasta Sa 21 nov)

### Paso 2.1 — Física unificada (≈ 3 – 4 h)
- ⬜ `Vector2` y `Contact`; `Figure.contactWith(Circle)`.
- ⬜ `Ball.bounceOff(Figure, restitución, impulso)` reemplaza `collideWithFoot`, `collideWithHead` y `collideWithGB`. Borrar `Collidable`.
- ⬜ Las medidas de la cancha en un objeto `Field` en vez de `Field.WIDTH`/`HEIGHT` estáticos.
- ⬜ Ancla única, si no se hizo en V1.

### Paso 2.2 — Poderes: de `enum` + `if` a Template Method y Strategy (≈ 3 – 4 h)
Las reglas ya funcionan (Cristian). Lo que falta es **diseño** y los bugs pendientes:
- 🟡 `Power` → **`PowerShot`** (Template Method): `final launch(...)` y cada país define cómo sale el tiro, su daño y su efecto. Hoy ya hay una subclase por país (`AuraAzulPower`, `LightningPower`, `TornadoPower`, `IllusionPower`).
- ⬜ `PlayerEffect` (`enum` + `if` en `Player`) → clases **`Effect`** (Strategy): `speedFactor()`, `canMove()`, `canJump()`, `canKick()`, `onStart`, `onEnd`.
- ⬜ B-N3: confirmar si el tiro sale con cualquier toque.
- ⬜ Quitar los `instanceof IllusionPower` (B-N7): `MatchView` expone la lista de balones.
- ⬜ `MatchListener`: `onPowerActivated(Side)`, `onPowerShotHit(Side victim)`.

### Paso 2.3 — PvM: del `switch` a Strategy (≈ 2.5 – 3.5 h)
- ✅ 4 perfiles funcionando en `COMController` (Cristian).
- ⬜ Una clase por perfil (Torpe, Ofensiva, Defensiva, Experta) con base `MachineController` (Template Method); `MachineProfile.create(Side, Random)` como Factory; borrar el `switch`, `COMController` y `COMState`.
- ⬜ `Random` inyectable para probar el perfil Torpe.
- ⬜ `MatchConfig` acepta el perfil y permite `PVC`.

### Paso 2.4 — Persistencia (≈ 4 – 5 h)
- ⬜ Dominio: interfaces `MatchRepository` (`save`, `open`) y `ProfileRepository`.
- ⬜ Paquete `persistence` con serialización de Java (listeners `transient`).
- ⬜ Fachada: `save(File)`, `open(File)`; GUI con `JFileChooser`.

### Paso 2.5 — Log de errores (≈ 1 h)
- ⬜ Clase `Log` con `java.util.logging` a un archivo; al usuario solo el mensaje claro de la `DOPOSoccerException`.

### Paso 2.6 — Pruebas y entrega (≈ 3 h)
- ⬜ Pruebas de efectos como clases, perfiles con `Random` fijo, guardar y abrir.
- ⬜ **Planificación de las 4 versiones** (entregable de V2).

**Total V2 ≈ 16.5 – 20 h.**

---

## 5. V3 — Progreso y máquina experta (hasta Do 22 nov)

- ⬜ **Mejoras de estadísticas**: `PlayerProfile.upgrade(StatType)`, precio 500 × 2^(nivel − 1), máximo 10, excepción si no alcanzan los puntos. Pantalla de mejoras. (≈ 3 h)
- 🟡 **Puntos** (Cristian ya los suma al terminar) → guardarlos en el perfil persistido. (≈ 1 h)
- 🟡 **Experta**: hoy ataca si va perdiendo y usa el poder cerca del balón. Ampliar con el tiempo, la vida y la barra del rival. (≈ 2 h)
- ⬜ **Pruebas de aceptación**. (≈ 2 h)

**Total V3 ≈ 8 h.**

## 6. V4 — Ilusión y pulido (parcial)

- 🟡 **Ilusión (EE. UU.)**: `IllusionPower` ya crea 7 balones falsos que siguen al real y desaparecen a los 2.5 s. Falta confirmar las reglas con el monitor y exponerlos por `MatchView` (sin `instanceof`). (≈ 1.5 h)
- ⬜ Prueba de extensión: agregar un personaje o un perfil con **una clase nueva y una línea en el enum**. (≈ 1 h)
- ⬜ Javadoc, diagramas finales y retrospectiva. (≈ 2 – 3 h)

---

## 7. Resumen

| Versión | Lo que queda (horas) | Fecha |
|---|---|---|
| V1 | 16 – 20 | Do 15 nov |
| V2 | 16.5 – 20 | Sa 21 nov |
| V3 | ≈ 8 | Do 22 nov |
| V4 | 4.5 – 5.5 | Parcial |
| **Total** | **≈ 45 – 54 h** | |

### Reglas de trabajo
1. **La API primero, el interior después.** No cambiar firmas de `MatchConfig`, vistas, `MatchListener` ni `DOPOSoccer` sin avisar.
2. **Mover, no reescribir:** casi todas las reglas ya existen en el código de Cristian; en V1 se trata de llevarlas a su clase correcta.
3. Un commit por paso; push al terminar cada paso; las 26 pruebas deben seguir en verde.
4. Nada de `instanceof`, ni de `getX().getY().getZ()` desde fuera de la clase dueña.

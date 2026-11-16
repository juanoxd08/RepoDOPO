# DOPOSoccer 2026-2 — Hoja de ruta

Código de partida: commit **`fix3`** (`6857c29`).
Enunciado: `DOPOSoccer_2026-2.pdf`.

**Prioridad general:** un diseño excelente (bajo acoplamiento, alta cohesión, patrones bien usados). Cada fase se hace de forma que **no haya que rehacerla** después.

---

## Calendario

| Entrega | Contenido según el PDF | Fecha |
|---|---|---|
| **V2** | Persistencia (guardar y abrir), nivel medio, **jugador contra máquina**. Planificación de las 4 versiones | **Sa 21 nov** |
| **V3** | Lo anterior + lo propuesto para el ciclo 3. **Pruebas de aceptación** | Do 22 nov |
| **V4** | Lo anterior + lo propuesto para el ciclo 4 | Parcial (S17) |
| Competencia | Todo completo | Vi 4 dic |

---

## Estado en `fix3` (verificado)

- Compila; **32 de 32 pruebas pasan** (26 de dominio y 6 de física).
- Fachada `DOPOSoccer`, `MatchConfig` (Builder), vistas de solo lectura, `MatchListener` (Observer), `MatchRule` con `TimeRule` y `GoalRule` (Strategy), `ScoreBoard` por lado.
- `Player` dividido en `PlayerBody`, `Life`, `EffectState` y `PlayerProfile`; `Power` ya no depende de `Player` (`PowerHit`).
- La GUI usa solo la fachada; no hay `instanceof`; `GameStatus` sin estados de menú; `Country` con los 4 personajes y su fábrica de poder.
- Física: la patada le pega al balón en el piso, la cabeza y el pie no penetran el travesaño, los jugadores chocan sin atravesarse.

---

## Bugs reportados (probados con simulaciones sin GUI sobre `fix3`)

### BUG-1 · El balón se va por debajo del piso cuando dos jugadores lo disputan — **confirmado**
- **Medición:** balón en el piso entre los dos jugadores, ambos caminan hacia él: **el borde inferior del balón llega a y = 219.3 (el piso está en 200) y pasa 16 de 120 ticks debajo del piso**. Vuelve a aparecer cuando los jugadores se mueven.
- **Causa (dos cosas juntas):**
  1. **Orden en `Match.resolveCollisions`:** `ball.collideWithEdges()` (paredes, techo y piso) se ejecuta **primero**, y después los jugadores empujan el balón con `bounceOff`. Ese empujón puede dejarlo fuera de la cancha y nadie lo corrige hasta el siguiente tick.
  2. **La normal del cuerpo apunta hacia abajo:** el rectángulo del cuerpo termina en y = 184 y un balón en el piso tiene su centro en y = 190. El punto más cercano del rectángulo queda **encima** del balón, así que la normal del contacto es (0, +1) y `bounceOff` **empuja el balón contra el piso**. Con un jugador de cada lado, el balón no tiene a dónde ir y se hunde.
- **Arreglo:**
  - En `resolveCollisions`, `ball.collideWithEdges()` va **al final**, después de todas las colisiones con jugadores: el piso y las paredes tienen la última palabra.
  - En `Ball.bounceOff`, si el balón está en el piso y la normal tiene componente hacia abajo (`normal.y > 0`), usar solo la componente horizontal (o una normal hacia arriba). Un balón apretado entre dos jugadores debe **salir hacia arriba**, no hundirse.
- **Prueba nueva:** disputa en el piso durante 120 ticks → el borde inferior del balón nunca pasa de `Field.HEIGHT`.

### BUG-2 · El autogol no se cuenta mientras el jugador sigue empujando — **confirmado**
- **Medición:** el jugador izquierdo empuja el balón hacia su arco manteniendo A durante 100 ticks y luego lo suelta: **en 150 ticks el gol nunca se marca**. El borde izquierdo del balón llega a x = **−2.9**, es decir, fuera de la cancha.
- **Causa:**
  1. La misma del BUG-1: el jugador empuja el balón **contra la pared** después de que `collideWithEdges` ya lo corrigió, y el balón queda con parte afuera.
  2. `Goal.detectGoal` usa `goalArea.containsCircle(ball)`, que exige que **todo** el balón esté dentro del rectángulo del arco, incluido su borde izquierdo ≥ 0. Si el balón está un poco "dentro de la pared", no cuenta. Al soltar A y mover al jugador, deja de empujarlo, el balón vuelve a la cancha y recién ahí cuenta.
- **Arreglo:**
  - El cambio de orden del BUG-1 ya ayuda.
  - **Detección del gol según la regla del PDF:** *"hay gol cuando el balón entra completamente en el arco"*, o sea, cuando **cruza por completo la línea de gol** y está debajo del travesaño. Para el arco izquierdo: `ball.x + r <= línea de gol (x = 60)` y `ball.y − r >= travesaño.bottom()`. No hace falta que el balón esté "dentro" de la pared del fondo.
  - Opcional: si el jugador aprieta el balón contra la pared, el que se detiene debe ser el **jugador** (el balón contra la pared es un obstáculo).
- **Prueba nueva:** empujar el balón al propio arco con A mantenida → el gol se marca **en el mismo tick** en que el balón cruza la línea, sin soltar la tecla.

### BUG-3 · El pie sube mucho al patear y a veces el balón traspasa — **no se reproduce en `fix3`**
- **Medición en `fix3`:** el pie sube **6 px** al patear (`KICK_LIFT_Y = 6`). Probé 96 posiciones del balón cerca del jugador (alturas de 150 a 190) y 96 balones que llegan rápido (vx de −8 a −20, distintos vy y momentos de patada): **en 0 casos el balón terminó detrás del jugador**.
- **Posibles explicaciones:**
  1. **Es el BUG-1 visto de otra forma:** el balón se hunde bajo el piso y "reaparece" al otro lado, lo que en pantalla parece que atravesó al jugador.
  2. **La versión local tiene otros valores** de `KICK_LIFT_Y`, `KICK_REACH_X` o del arco de la patada que no están en `fix3`.
- **Qué hacer:**
  - Arreglar primero el BUG-1 y volver a probar.
  - Si sigue pasando: subir la versión local, o anotar la situación exacta (dónde estaba el balón, hacia dónde iba, si alguien saltaba) y la convertimos en una prueba.
  - Prevención (diseño): en el barrido de la patada, revisar el contacto en **cada paso intermedio** del pie, no solo en su posición final del tick.

---

## Pendientes que quedaron del review de `fix3`

**Acoplamiento (rápido):**
- **A1.** `Match` llama 8 veces `player.getPower().algo()` (`finishPowerShot`, `updateShot`, `isPowerShotActive`, `onHitRival`). Encapsular en `Player`: `finishPowerShot()`, `updatePowerShot(ball)` y `powerHitOn(rival, ball)` que devuelva un `PowerHit` o nada.
- **A2.** Borrar los getters públicos de `Player` que nadie usa: `getHead()`, `getBody()`, `getFoot()` y `getStats()`.
- **A3.** Dejar una sola versión de los getters duplicados: `getVidaActual`/`getLife`, `getVidaMax`/`getMaxLife`, `isStunnedByZeroHealth`/`isStunned`, `getCurrentEffect`/`getActiveEffectName`.
- **A4.** `Match` crea los jugadores con el constructor viejo y les pasa el país **y** su poder por separado. Usar el constructor con `PlayerProfile` y que `Player` le pida el poder a su `Country`.
- **A5.** Borrar la interfaz `Collidable` (vacía; la implementan `Ball`, `Body`, `Foot` y `Head` sin usarla).

**Sensación de juego:**
- **J1.** `Physics.FRICTION = 0.6` por tick: un balón a 16.4 px/tick **recorre 41 px y se detiene en 12 ticks**; casi nunca rueda. Probar con **0.97-0.985**.
- **J2.** El disparo es muy rasante: sube unos 13 px y choca con el rival parado. Probar con una dirección más alta (por ejemplo, `(dir, −0.8)`), o que dependa de la altura del balón.
- **J3.** Al caer sobre la cabeza del rival, el jugador se resbala en unos 20 ticks. Decidir si así se quiere.

---

## Plan por fases

| Fase | Qué | Horas | Para |
|---|---|---|---|
| **1** | Bugs de física + pruebas | 2 – 3 | Ya |
| **2** | Diseño: cerrar acoplamiento y patrones pendientes | 6 – 8 | V2 |
| **3** | Detalles de dominio que pide el PDF | 4 – 6 | V2 / V3 |
| **4** | Persistencia y log de errores | 4 – 5 | **V2** |
| **5** | Jugador contra máquina (perfiles) | 3 – 4 | **V2** |
| **6** | Animación de los poderes | 2 – 3 | V2 / V3 |
| **7** | Sensación de juego | 1 – 1.5 | Cuando haya tiempo |
| **8** | Pruebas, diagramas y entregables | 4 – 6 | V2 y V3 |
| **Total** | | **≈ 26 – 36 h** | |

---

### Fase 1 — Bugs de física (≈ 2 – 3 h) · *lo primero*
- [ ] BUG-1: `collideWithEdges` al final de `resolveCollisions`, y en `bounceOff` no empujar el balón contra el piso.
- [ ] BUG-2: detección del gol por **línea de gol** (`Goal.isGoal(ball)`), según la regla del PDF.
- [ ] BUG-3: volver a probar después del BUG-1; si sigue, convertir el caso en una prueba y revisar el barrido de la patada.
- [ ] Pruebas nuevas en `DOPOSoccerPhysicsTest`: disputa en el piso, autogol empujando, balón contra la pared.

**Listo cuando:** las pruebas nuevas pasan y, jugando, el balón no desaparece bajo el piso y el autogol se marca al instante.

---

### Fase 2 — Diseño excelente (≈ 6 – 8 h)

**2.1 Cerrar el acoplamiento (A1-A5)** · ≈ 1 – 1.5 h

**2.2 Efectos como Strategy** · ≈ 1.5 h
- `EffectState` hoy tiene un `enum` con `if`/`switch` por efecto. Pasar a una interfaz o clase abstracta **`Effect`** con `speedFactor()`, `canMove()`, `canJump()`, `canKick()`, `onStart(...)`, `onTick(...)` y una clase por efecto: `PushedEffect`, `ElectrocutedEffect`, `KnockedOutEffect`, `ConfusedEffect`.
- `EffectState` guarda un `Effect` y su duración; el efecto nuevo **reemplaza** al anterior (regla del PDF).
- Agregar un efecto = una clase nueva (requisito de extensión del PDF).

**2.3 Poderes con Template Method** · ≈ 1.5 – 2 h
- `Power` → plantilla con `final` en los pasos fijos (barra, aura, ejecutar el tiro, producir el `PowerHit`) y métodos abstractos para lo propio de cada país: cómo sale el tiro (`shapeShot`), el daño base y el efecto que produce (`createEffect`).
- Cada país (`AuraAzulPower`, `LightningPower`, `TornadoPower`, `IllusionPower`) solo implementa lo suyo.

**2.4 Física y cancha sin constantes globales** · ≈ 1 – 1.5 h
- `Field.WIDTH`/`HEIGHT` se usan en muchas clases. Que `PlayerBody` y `Ball` reciban los límites de la cancha (un objeto `Field` con medidas) en vez de leer constantes estáticas.
- Constantes de superficies en `Physics` (restitución de cabeza, cuerpo, travesaño y pared; impulso del cabezazo), sin números sueltos en `PlayerBody`.
- `Ball` expone solo lo necesario; sus setters de velocidad, solo para el dominio.

**2.5 Revisión de cohesión** · ≈ 1 h
- Meta: ninguna clase del dominio con más de unas 200 líneas de código ni con responsabilidades mezcladas.
- Revisar `Match` (339 líneas): si sigue creciendo, sacar la detección de colisiones a un `CollisionResolver` y la creación de jugadores y controles a una fábrica (`MatchFactory`).

**Listo cuando:** agregar un personaje, un efecto o una regla de partido requiere **solo una clase nueva y una línea en un `enum`**. Se demuestra con una prueba.

---

### Fase 3 — Detalles de dominio del PDF (≈ 4 – 6 h)
- [ ] **Daño de patada con alcance:** confirmar que solo hace daño si el rival está al alcance del pie, y una vez por patada.
- [ ] **Mejora de estadísticas con puntos:** `PlayerProfile.upgrade(StatType)` con precio 500 × 2^(nivel − 1), máximo 10, `DOPOSoccerException` si no alcanzan los puntos. Exponerlo en la fachada.
- [ ] **Perfiles de jugador** en la configuración: `MatchConfig` recibe el `PlayerProfile` de cada lado, para que las estadísticas y los puntos se mantengan entre partidas.
- [ ] **Ilusión (EE. UU.):** `MatchView.getFakeBalls()` con los 7 balones falsos. Confirmar las reglas con el monitor.
- [ ] **Terminar a la fuerza:** confirmar con el monitor si abandonar reparte puntos (hoy sí).
- [ ] **Tiro de poder con cualquier toque:** confirmar si sale solo al patear o con cualquier contacto.
- [ ] **Información del jugador en la vista:** efecto activo como `PlayerEffect` (no `String`) y su progreso (0..1), para la GUI.

---

### Fase 4 — Persistencia y log (≈ 4 – 5 h) · *V2*
- [ ] Interfaces en el dominio: **`MatchRepository`** (`save`, `open`) y **`ProfileRepository`** (cargar y guardar perfiles). El dominio **no** sabe de archivos (inversión de dependencias).
- [ ] Paquete **`persistence`** con la implementación por serialización de Java. Los listeners y la GUI no se guardan (`transient`).
- [ ] Fachada: `save(File)`, `open(File)`, carga y guardado de perfiles.
- [ ] GUI: *Guardar* y *Abrir* con `JFileChooser`; errores con el mensaje de la `DOPOSoccerException`.
- [ ] **Log de errores** (requisito del PDF): clase `Log` con `java.util.logging` a un archivo; al usuario solo el mensaje claro.
- [ ] Pruebas: guardar y abrir deja el mismo estado (marcador, tiempo, vida, barra y posiciones).

---

### Fase 5 — Jugador contra máquina (≈ 3 – 4 h) · *V2*
- [ ] Del `switch` de `COMController` a **una clase por perfil** (Strategy): `ClumsyMachine`, `OffensiveMachine`, `DefensiveMachine` y `ExpertMachine`, con una base `MachineController` (Template Method: percibir → decidir → armar la intención).
- [ ] `MachineProfile` crea su controlador (Factory), con `Random` inyectable para probar el perfil Torpe.
- [ ] `MatchConfig` acepta `PVC` y el perfil; la GUI ya tiene el selector.
- [ ] Pruebas por perfil con escenas fijas (por ejemplo, "balón detrás de mí" → el defensivo se ubica entre el balón y su arco).

---

### Fase 6 — Animación de los poderes (≈ 2 – 3 h)
**Dominio (vistas):**
- `BallView`: qué país lanzó el tiro en vuelo (o ninguno).
- `PlayerView`: efecto activo como `PlayerEffect` y su progreso.
- `MatchView`: lista de balones falsos (Ilusión).

**Presentación:**
- Contador de frames propio de la vista para animar (aura que pulsa con `Math.sin`).
- **Aura del color de cada país**: azul (Corea), amarillo eléctrico (Camerún), verde (Nigeria), rojo y azul (EE. UU.).
- **Estela del balón con tiro de poder** (las últimas 6-8 posiciones, cada vez más transparentes).
- Un dibujo por tiro: recto y largo (Aura Azul), zigzag eléctrico (Rayo), espiral (Tornado), 7 balones falsos (Ilusión).
- Ícono del efecto sobre el rival en vez de texto, con una barrita de duración.

---

### Fase 7 — Sensación de juego (≈ 1 – 1.5 h)
- J1 fricción, J2 ángulo del disparo, J3 pararse sobre la cabeza del rival.
- Ajustar jugando, y después fijar los valores con pruebas.

---

### Fase 8 — Pruebas y entregables (≈ 4 – 6 h)
- [ ] Pruebas de cada fase (física, efectos, poderes, persistencia, perfiles de máquina).
- [ ] **Diagramas de secuencia nuevos**, ahora que la física está estable: patada → daño → aturdido, tiro de poder → golpe al rival → efecto, guardar y abrir partida.
- [ ] Actualizar el diagrama de clases (con `PlayerBody`, `Life`, `EffectState`, `PlayerProfile`, `PowerHit` y lo que se agregue en las fases 2, 4 y 5).
- [ ] **Planificación de las 4 versiones** (entregable de V2): objetivo y mini-ciclos de cada versión, y requisitos que no se incluirán.
- [ ] **Pruebas de aceptación** (V3): escenarios del enunciado como casos.

---

## Orden recomendado
1. **Fase 1** (bugs): el juego tiene que verse bien antes de lo demás.
2. **Fase 2** (diseño): antes de agregar funcionalidad, para que lo nuevo nazca bien hecho.
3. **Fases 4 y 5** (persistencia y PvM): lo que pide V2.
4. **Fase 3** (detalles del PDF), en paralelo con la 4.
5. **Fase 6** (animación) en paralelo, desde la GUI.
6. **Fase 7** cuando haya un rato.
7. **Fase 8** a lo largo de todo, no al final.

**Reparto sugerido:** Juan en el dominio (fases 1, 2, 3 y 4) y Cristian en la presentación (fase 6, la GUI de la persistencia y de los perfiles, y los diagramas). La fase 5 la puede tomar quien termine primero.

## Reglas de trabajo
1. La API pública (fachada, vistas, eventos y `MatchConfig`) no cambia sin avisar.
2. Cada bug se convierte en una prueba **antes** de arreglarlo.
3. Un commit por paso; push frecuente; todas las pruebas en verde.
4. Nada de `instanceof`, de cadenas `a.getB().getC()` desde fuera de la clase dueña, ni de constantes sueltas.

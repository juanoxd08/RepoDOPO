# DOPOSoccer 2026-2 — Hoja de ruta

Código de partida: commit **`fix9`** (`b2ccc9b`, 24 nov).
Enunciado: `DOPOSoccer_2026-2.pdf`.

> **Prioridad actual: la interfaz gráfica**, con la reorganización y los bugs del poder ya hechos en `fix9`; siguen **las imágenes, las animaciones y los archivos de sonido**. Después, lo que falta del dominio. **El resto de los bugs va al final.**

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
- [ ] **Siguen subidos archivos generados** (en `fix9` volvieron a cambiar): `DOPOSoccer.log` y `profiles.dat`. Agregarlos a `.gitignore` y sacarlos del repositorio sin borrarlos del disco (`git rm --cached ProyectoHeadSoccer/DOPOSoccer.log ProyectoHeadSoccer/profiles.dat`). Si no, cada partido cambia esos archivos y genera conflictos entre los dos.

---

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

## Prioridad 1 — Interfaz gráfica · ≈ 3 – 3.5 h

### 1.0 Datos del dominio que la GUI necesita · ✅ hecho en `fix8`
- [x] `PlayerView`: `getActiveEffect()` y `getEffectProgress()`.
- [x] `BallView`: `getPowerShotCountry()`, `isPowerShotCharging()` y `getPowerShotChargeProgress()`.
- [x] `MatchListener`: `onBallKicked`, `onPowerActivated`, `onPowerShot`, `onPowerHit` (y `onProfileSaveFailed`).
- [x] Pruebas de los eventos.

### 1.1 Lo que pide el PDF · casi terminado · ≈ 0.5 h
- [x] Nombres de los jugadores en la pantalla de selección.
- [x] Pantalla de mejoras con puntos, niveles, precio y botón "+".
- [x] Fin del partido con los puntos totales de cada jugador.
- [x] Filtro de extensión en el `JFileChooser`.
- [ ] Revisar: al **abrir** una partida, que aparezca en pausa con un mensaje para continuar.
- [ ] Mostrar el mensaje de `onProfileSaveFailed` en un diálogo (el evento ya existe).

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
- [ ] **`Theme`** (≈ 30 min): la clase existe, pero todavía hay 89 `new Color(...)` y 40 `new Font(...)` repartidos en los demás archivos. Moverlos a `Theme`.
- [x] **Teclado como mapa** (≈ 30 min): un `Map<Integer, PlayerAction>` por jugador en lugar de la cadena de `if` de `processContinuousKeys`, y que cada pantalla maneje sus propias teclas en vez del `switch` de `HeadSoccerGUI.keyPressed`.
- [ ] *(Opcional, ≈ 45 min)* `MatchFileDialogs` para guardar y abrir, y los botones de pausa/fin como `JButton` reales en vez de comprobar a mano si el clic cayó en el rectángulo.
- [ ] Después de cada paso: abrir el juego y probar menú → selección → partido → pausa → guardar → abrir → fin → mejoras.

### 1.1c Bugs del poder · 13, 8 y 12 ✅ en `fix9` · faltan el BUG-14 y el BUG-15, ≈ 35 min
- [x] BUG-13, BUG-8 y BUG-12 (detalle abajo, y medición en "Cambios en `fix9`").
- [ ] **BUG-14 · Un rival con aura que toca el balón mientras se carga otro tiro le "roba" el tiro.** Medición: Corea carga su tiro; Nigeria, con aura, camina hacia el balón quieto en el aire, lo toca y ejecuta su propio tiro. Desde ahí los dos poderes sostienen el balón al mismo tiempo, el tiro de Corea se pierde y sale solo el de Nigeria. **Arreglo:** en `PlayerBody`, no ejecutar el tiro por contacto mientras `ball.isPowerShotCharging()` (o resolverlo junto con el BUG-11).
- [ ] **BUG-15 · El que tira el poder sigue caminando durante la carga y atraviesa el balón.** Medición (Corea con aura, caminando a la derecha hacia el balón): al tocarlo, el balón sube y queda quieto en el aire 90 ticks (1.5 s), pero el jugador **sigue avanzando**: pasa a través del balón (13 de los 90 ticks está metido dentro de él) y termina 72 px **delante** del balón. Cuando el tiro sale, el balón parte desde atrás del jugador. **Comportamiento esperado:** mientras se carga su tiro, el jugador se queda quieto (no camina, no salta, no patea), como una animación de ejecución del poder. **Arreglo (≈ 20 min):** en `Power`, un método `isChargingShot()` (`ticksPerPower > 0`), y en `Player.isDisabled()` agregar `|| (power != null && power.isChargingShot())`; así `moveLeft`, `moveRight`, `jump` y `kick` quedan bloqueados solos. Prueba: con aura, caminar hacia el balón; durante la carga la posición del jugador no cambia. Para la GUI: `PlayerView` puede exponer `isChargingPower()` para dibujar la pose del poder (1.3).

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

### 1.3 Animación de los poderes · una animación sencilla por estado · ≈ 45 min – 1 h
**Decisión del equipo:** sin estela del tiro. Cada estado se muestra con un **tono de color opaco sobre la cabeza** que pulsa, un **símbolo corto** encima y una **barrita** de duración. Así se cumple el PDF: *"el aura del poder activo y los efectos sobre el rival deben ser visibles"*.

**Ya hecho en `fix9`:**
- [x] Contador de frames en `PlayerRenderer` para animar.
- [x] **Aura** del color de cada país que pulsa (`drawAura`).
- [x] **Anillo de carga** que se cierra alrededor del balón (`BallRenderer.drawChargingRing`, con `isPowerShotCharging()` y su progreso).
- [x] **Balones falsos** de la Ilusión (`getFakeBalls()`).
- [x] Etiqueta del efecto y barrita de duración (`drawStatusEffect`, con `getEffectProgress()`).

**Falta (todo en `PlayerRenderer.drawStatusEffect`):**
- [ ] **Tono sobre la cabeza que pulsa:** un `fillOval` del tamaño de la cabeza, con el color del efecto y transparencia que sube y baja:
  ```java
  double pulse = 0.5 + 0.5 * Math.sin(animFrame * 0.2);
  int alpha = (int) (90 + 60 * pulse);
  g2d.setColor(new Color(ec.getRed(), ec.getGreen(), ec.getBlue(), alpha));
  g2d.fillOval(hx - hr, hy - hr, hr * 2, hr * 2);
  ```
- [ ] **Símbolo corto** en lugar del texto `★ NOMBRE ★` (que es largo y se sale de la cabeza a esta escala):

  | Estado | Color | Símbolo |
  |---|---|---|
  | Empujado (Corea, 1.5 s) | Azul | `>>` o `<<`, hacia su propio arco |
  | Electrocutado (Camerún, 3 s) | Amarillo | zigzag de 3 líneas, o `!!` |
  | Inconsciente (Nigeria, 3 s) | Gris o morado | `Z z` |
  | Confundido (EE. UU., 2.5 s) | Rosado | `?` |
  | Aturdido (vida en 0, 3 s) | Blanco | `***` o 3 puntos girando |

- [ ] **Aturdido:** hoy usa blanco por defecto y la etiqueta `STUN`; darle su propio tono y símbolo de la tabla.
- [ ] Mover los colores de `getPowerColor` y `getEffectColor` a `Theme`, en un `EnumMap<Country, Color>` y un `EnumMap<PlayerEffect, Color>` (va junto con el punto de `Theme` de 1.1b).
- [ ] *(Opcional)* **Pose de poder** mientras el jugador carga su tiro, cuando se arregle el BUG-15 (`isChargingPower()`): por ejemplo, el aura más grande.
- [ ] *(Opcional)* **Golpe al rival:** un destello de 10 ticks sobre la cabeza con `onPowerHit`.

### 1.4 Sonidos · código hecho en `fix9` · faltan los archivos ≈ 30 min
- [x] Clase `SoundPlayer` en `presentation`: `javax.sound.sampled.Clip`, archivos **WAV**, cargados una sola vez en un `Map`.
- [x] Gol, aturdido, fin, patada, aura, tiro y golpe, cada uno con su evento (ya existen todos).
- [x] Opción de silenciar. Si un sonido falta o no hay audio, el juego sigue sin sonido (registrarlo en el log).
- [ ] **Agregar los 7 `.wav`** (16 bits, 44.1 kHz) en `src/presentation/resources/sounds/`: `goal.wav`, `stunned.wav`, `finish.wav`, `kick.wav`, `power_aura.wav`, `power_shot.wav`, `power_hit.wav`. Fuentes: sfxr.me (sin licencia), Kenney.nl (CC0), Freesound.org (filtrar CC0). Los CC-BY van en `CREDITOS.txt`.

**Listo cuando:** se puede configurar, jugar, mejorar estadísticas y guardar/abrir sin tocar código, y cada poder se distingue a simple vista.

---

## Prioridad 2 — Lo que falta del dominio · ≈ 2 – 2.5 h

### 2.1 Jugador contra máquina · *Cristian* · ≈ 0.5 h (falta lo de abajo)
- [x] **`OffensiveMachine`** (primero: la Experta delega en ella cuando va empatando o perdiendo): ubicarse detrás del balón respecto al arco rival, saltar si viene alto, patear si está al alcance, activar el poder cuando va a tocar el balón. Constantes con nombre, como en `DefensiveMachine`.
- [x] **`ClumsyMachine`**: acciones al azar con su `Random`, **mantenidas unos ticks** (si cambian en cada tick, solo tiembla).
- [ ] Borrar **`COMController`** y pasar su prueba de `DOPOSoccerStaticDynamicTest` a los perfiles nuevos.
- [ ] Con las figuras más pequeñas, revisar los alcances de `DefensiveMachine` (distancias pensadas para los tamaños anteriores).
- [ ] La defensa de la Experta contra el tiro de poder usa `ball.hasPowerShot()`, que hoy queda en `true` hasta el siguiente gol (BUG-8): hasta arreglarlo, puede quedarse defendiendo de más.

### 2.2 Pruebas de la máquina · ≈ 1 h
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

## Prioridad 3 — Entregables · ≈ 2 – 3 h
- [ ] **Pruebas de aceptación** (V3): jugar PvM contra cada perfil; partido por tiempo que termina en muerte súbita; guardar a mitad, cerrar y abrir; ganar, recibir 100 puntos y comprar una mejora.
- [x] **Diagramas de secuencia:** 16 terminados en Astah (faltan la 8 y la 9 del tiro de poder, que esperan los BUG-8, 11, 12 y 13).
- [ ] **Diagrama de clases:** `MachineController` y sus perfiles, `MatchRepository`, `ProfileRepository`, `ProfileView`, el paquete `persistence` y los paneles nuevos de la GUI.
- [x] **Retrospectiva** V3.
- [ ] **Planificación de versiones** actualizada.

---

## Prioridad 4 — Bugs (al final)

Los BUG-8, 12 y 13 se movieron al paso **1.1c**.

### BUG-7 · El balón no toma velocidad al conducirlo manteniendo D
- **Causa:** `Ball.bounceOff` solo usa la velocidad del balón; con el balón quieto, `v · n = 0` y solo se corrige la posición.
- **Arreglo:** velocidad relativa (`vn = (vBalón − vPie) · n`) o una velocidad mínima en la dirección del empujón cuando el pie que no patea empuja el balón en el piso.

### BUG-9 · Dos jugadores pegados, balón en medio, ambos patean: el balón traspasa a uno
- **Causa:** cada jugador resuelve su contacto por separado y en orden.
- **Arreglo:** si el balón toca a los dos en el mismo tick, que salga hacia arriba; y nunca dejarlo dentro de un cuerpo después de todas las correcciones.

### ✅ BUG-10 · Los balones falsos de la Ilusión no desaparecen si el tiro golpea al rival (arreglado en `fix9`)
- **Ojo:** ahora los falsos desaparecen **en cuanto** el tiro golpea o vence (máximo 2 s). El PDF dice que *"desaparecen al terminar el efecto"* (2.5 s). Preguntar al monitor si está bien así.
- **Causa:** se actualizan solo mientras el tiro está activo, y el golpe termina el tiro.
- **Arreglo:** que la duración de la ilusión no dependa de que el tiro siga activo (un *hook* que `updateShot` llame en todos los ticks).

### BUG-11 · El balón se comporta raro mientras se carga el tiro de poder (ver también BUG-14 y BUG-15)
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
| 0 | Sacar los archivos generados del repositorio | 0.1 |
| 1 | **GUI** (1.1 detalles → 1.1b `Theme` → 1.1c BUG-14 y BUG-15 → 1.2 revisar resoluciones y créditos → 1.3 tono y símbolo por estado → 1.4 archivos de sonido) | 3 – 3.5 |
| 2 | Dominio: borrar `COMController`, pruebas de la máquina | 1.5 – 2 |
| 3 | Entregables: pruebas de aceptación, diagrama de clases, planificación | 2 – 3 |
| 4 | Bugs 7, 9 y 11 | 1.75 – 2.75 |
| | **Total** | **≈ 8.5 – 11.5 h** (sin el dash) |

**Sugerencia de reparto:** Juan, la GUI (BUG-14 y 15, y el tono y símbolo por estado de 1.3); Cristian, los archivos de sonido, las pruebas de la máquina y borrar `COMController`.

## Reglas de trabajo
1. La GUI solo usa la fachada, las vistas, los eventos, los `enum` y `DOPOSoccerException`. Si falta un dato, se agrega a la vista; nunca se busca dentro del dominio.
2. La API pública (fachada, vistas, eventos y `MatchConfig`) no cambia sin avisar.
3. Un commit por paso; push frecuente; **todas las pruebas en verde antes del push**.
4. El dominio no importa nada de `presentation` ni de `persistence`.
5. No subir archivos generados al jugar (`*.log`, `*.dat`, partidas guardadas).

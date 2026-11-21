# DOPOSoccer 2026-2 — Hoja de ruta (dominio)

Código de partida: commit **`fix6`** (`44b1042`) + **aportes de Cristian combinados** (commit `e6d3bbc` en la rama `claude/brave-newton-pne4dc`).
Enunciado: `DOPOSoccer_2026-2.pdf`.

> Esta hoja cubre **solo el dominio** (y la capa de persistencia). Lo de la interfaz gráfica se lleva aparte.
> **Prioridad: lo que pide la entrega.** Los bugs de física quedan para después de la entrega.

---

## Calendario

| Entrega | Qué pide el PDF | Fecha |
|---|---|---|
| **V2** | Persistencia (guardar y abrir), nivel medio, **jugador contra máquina**, **planificación de las 4 versiones** | **Sa 21 nov** |
| **V3** | Lo anterior + lo propuesto para el ciclo 3, **pruebas de aceptación** | Do 22 nov |
| **V4** | Lo anterior + lo propuesto para el ciclo 4 | Parcial (S17) |
| Competencia | Todo completo | Vi 4 dic |

---

## Avances de `fix6` (verificado)

- Compila; **41 de 41 pruebas pasan** (todavía no hay pruebas nuevas de la máquina ni de los perfiles).
- **Jugador contra máquina ya se puede crear:** `MatchConfig.Builder.machineProfile(...)`, `build()` acepta `PVC` (exige perfil), `Match` crea la máquina con `config.getMachineProfile().create(Side.RIGHT)` y la GUI ya le pasa el perfil elegido.
- **`MachineProfile` es una Factory:** cada valor guarda un `Function<Side, MachineController>`; además tiene `create(side, random)` para el Torpe.
- **`MachineController`** ganó métodos de ayuda comunes: `shouldJumpForBall`, `shouldKickBall`, `ownGoalX`.
- **`DefensiveMachine`, terminada:** vuelve a una posición base frente a su arco cuando el balón está lejos (más de 130 unidades) y, si el balón se acerca, se ubica entre el balón y su arco y lo despeja. Probado: con el balón en x = 230 junto a su arco, lo despejó hasta x = 132 y volvió a su posición base.
- **`ExpertMachine`, terminada:** **compone** una ofensiva y una defensiva y elige a cuál delegar según el estado del partido: rival con aura → defensiva; rival aturdido o inconsciente → ofensiva; va ganando (salvo empate con poco tiempo) → defensiva; si no → ofensiva. Además activa el poder si está lleno y el balón está cerca.
- **`ClumsyMachine` y `OffensiveMachine`: solo el esqueleto** (`chooseActions` devuelve un conjunto vacío). Las termina Cristian.
  - Consecuencia, medida en partidos de 60 s: con los perfiles **Torpe** y **Ofensiva** la máquina **no se mueve nunca**, y la **Experta** tampoco se mueve mientras el partido esté empatado o vaya perdiendo, porque en esos casos delega en la ofensiva. Se arregla solo cuando exista la `OffensiveMachine`.
- **Persistencia del progreso, iniciada:** `ProfileRepository` (interfaz en el dominio) + `SerializedProfileRepository` (en `persistence`, un archivo `profiles.dat` con un mapa nombre → perfil; si el nombre no existe, devuelve un perfil nuevo). **Todavía no está conectada:** ni la fachada ni `Match` la usan.
- **Guardar partida** ahora pausa el partido antes de guardar. La GUI registra con `Log.record` los errores que atrapa.

---

## Aportes de Cristian combinados sobre `fix6` (commit `e6d3bbc`, verificado)

Su versión traía un merge sin resolver (marcas `<<<<<<<` en 4 archivos y 59 errores de compilación). Se tomó `fix6` como base y se agregó solo lo bueno. **Compila y pasan las 41 pruebas.**

**Lo que entró:**
- **`MatchView.getFakeBalls()`** (balones falsos de la Ilusión), sin `instanceof`: `Power.getDecoyBalls()` devuelve una lista vacía por defecto, `IllusionPower` lo sobrescribe devolviendo **copias**, `Player` delega y `Match` junta los de los dos jugadores. Probado: durante el tiro de EE. UU. devuelve los 7 falsos, y modificar la lista devuelta no mueve los reales.
- **`MatchView.getModality()`**: `Match` guarda la modalidad de la configuración.
- **`BallView.getHorizontalSpeed()` y `getVerticalSpeed()`** (solo lectura, para animar el balón).
- **`DOPOSoccerException`**: constructor con causa, `DOPOSoccerException(String, Throwable)`, y mensajes nuevos `NO_ACTIVE_MATCH`, `FILE_NOT_FOUND`, `INVALID_FILE` y `MAX_STAT_REACHED`.

**Lo que se dejó fuera, y por qué:**
- `DOPOSoccerLog` (en `domain`): duplicaba `persistence.Log` y escribía archivos desde el dominio.
- `Player.hasActivePowerShot()` y `consumePowerHit()`: repetían lo que ya hace `powerHitOn(...)`; además venían duplicados `finishPowerShot()` y `updatePowerShot()`.
- Su lado del conflicto en `Match` (quitaba el `transient` de los listeners: guardar partida habría fallado) y en `Power`/`IllusionPower` (usaba la API vieja: `affectBall` y un `updateShot` sobrescrito, que desde `fix4` es `final`).
- Sus `SAVE_ERROR` y `OPEN_ERROR`: ya existían; se dejaron los de `fix6`.

**Lo que queda por usar** (los mensajes nuevos todavía no los lanza nadie): ver 2.2 y Prioridad 3.

---

## Estado en `fix5` (verificado)

- Compila; **41 de 41 pruebas pasan** (19 de dominio, 12 de física, 7 estáticas y dinámicas, 3 de persistencia).
- **Persistencia de la partida, hecha:** `MatchRepository` (interfaz en el dominio) + `SerializedMatchRepository` (en `persistence`); la fachada tiene `save(File)` y `open(File)`; los listeners son `transient` y se vuelven a registrar al abrir.
- **Log de errores, hecho:** `persistence.Log` con `java.util.logging` en `DOPOSoccer.log`.
- **Base de la máquina:** `MachineController` abstracta con `decide` `final` (Template Method: valida → `chooseActions`) y el método de ayuda `moveToward`. **Todavía no tiene subclases**; la lógica sigue en `COMController` con un `switch`.
- **Bugs de física anteriores, corregidos (verificado):**
  - ✅ Cabezazo proporcional a cómo llega el balón (cae a 2 → sale a 5.9; cae a 20 → sale a 14.7).
  - ✅ Normal en el piso: simétrica y de largo 1 (0 de 63 casos al techo, para los dos lados).
  - ✅ La patada ya no traspasa al jugador (0 de 96).
  - ✅ Disputa en el piso y autogol empujando siguen bien.
- Se borró `COMState`.

---

## Prioridad 1 — Jugador contra máquina (V2) · ≈ 2 – 3 h restantes

El PDF pide la modalidad **jugador contra máquina** con **4 perfiles**: Torpe (*escoge al azar*), Ofensiva (*su prioridad es anotar*), Defensiva (*su prioridad es defender el arco*) y Experta (*decide según el estado del partido*).

### 1.1 Una clase por perfil (Strategy + Template Method)
- [ ] **`ClumsyMachine`** · *Cristian* · ≈ 0.5 h: el esqueleto ya tiene el `Random` inyectable. Falta `chooseActions`: elegir acciones al azar y **mantenerlas unos ticks** (si cambia en cada tick, la máquina solo tiembla). La lógica de `decideTorpe` de `COMController` sirve tal cual: el temporizador `randomActionTimer` y el conjunto `bufferedRandomActions` pasan a ser atributos de `ClumsyMachine`.
- [ ] **`OffensiveMachine`** · *Cristian* · ≈ 1 h: es **la más importante**, porque la Experta delega en ella cuando va empatando o perdiendo. Falta `chooseActions`: ubicarse detrás del balón respecto al arco rival (`ball.getX() − side.dir() × distancia`), saltar si el balón viene alto (`shouldJumpForBall`), patear si está al alcance (`shouldKickBall`) y activar el poder cuando va a tocar el balón. La lógica de `decideOfensiva` de `COMController` sirve como punto de partida; usar los métodos de ayuda de `MachineController` y constantes con nombre, como en `DefensiveMachine`.
- [x] **`DefensiveMachine`** · *Juan* · ✅ hecha en `fix6`.
- [x] **`ExpertMachine`** · *Juan* · ✅ hecha en `fix6` (compone ofensiva y defensiva).
- [ ] Cuando estén las 4: **borrar `COMController`** (hoy solo lo usa una prueba de `DOPOSoccerStaticDynamicTest`, que hay que pasar a los perfiles nuevos).
- [ ] Mejora pequeña: `ExpertMachine` compara el efecto del rival como texto (`PlayerEffect.KNOCKED_OUT.name().equals(rival.getActiveEffectName())`). Cuando `PlayerView` exponga el efecto como `PlayerEffect` (lo pide también la GUI), compararlo directamente.

### 1.2 `MachineProfile` como Factory · ✅ hecho en `fix6`
- [x] Cada valor del `enum` crea su controlador con `create(Side side)`.
- [x] `create(Side side, Random random)` para las pruebas del perfil Torpe.

### 1.3 Configuración y partido · ✅ hecho en `fix6`
- [x] `MatchConfig.Builder.machineProfile(...)`; `build()` acepta `PVC` y exige el perfil.
- [x] `Match` crea la máquina con `config.getMachineProfile().create(Side.RIGHT)`.
- [x] La GUI le pasa el perfil elegido al Builder.
- [ ] (Opcional) Exponer el perfil de la máquina por `MatchView`/`PlayerView`, si la GUI quiere mostrarlo en el HUD.

### 1.4 Pruebas · ≈ 1 h · *falta todo*
- [ ] **Torpe** con un `Random` de semilla fija: siempre las mismas acciones.
- [ ] **Ofensiva:** balón delante, entre ella y el arco rival → se acerca y patea.
- [ ] **Defensiva:** balón cerca de su arco → queda entre el balón y el arco; balón lejos → vuelve a su posición.
- [ ] **Experta:** va perdiendo → actúa como ofensiva; va ganando → como defensiva.
- [ ] Un partido de 60 s con cada perfil → termina sin excepciones.
- [ ] Guardar y abrir una partida PvM → la máquina sigue funcionando (su `Random` y su estado se serializan).

**Listo cuando:** se puede crear un partido PvM con cualquiera de los 4 perfiles desde `MatchConfig`, y cada perfil tiene su prueba.

---

## Prioridad 2 — Persistencia completa (V2) · ≈ 2 – 2.5 h restantes

El PDF pide dos cosas: **guardar y abrir el estado de un partido** (✅ hecho) y **mantener el progreso de los jugadores (puntos y estadísticas)** (🟡 el repositorio existe, falta conectarlo).

### 2.1 Progreso de los jugadores · ≈ 1.5 – 2 h restantes
- [x] Interfaz **`ProfileRepository`** en el dominio (`load(name)`, `save(profile)`) · ✅ `fix6`.
- [x] **`SerializedProfileRepository`** en `persistence` (`profiles.dat`, mapa nombre → perfil) · ✅ `fix6`.
- [ ] **Conectarlo** (esto es lo que hace que el progreso funcione de verdad):
  1. `MatchConfig.Builder`: recibir el **nombre** de cada jugador (`leftPlayerName(...)`, `rightPlayerName(...)`). Para la máquina, un nombre fijo por perfil (por ejemplo "COM Defensiva").
  2. **Fachada:** `DOPOSoccer(MatchRepository, ProfileRepository)`. En `newMatch(config)`, cargar los dos perfiles con `profileRepository.load(nombre)` y pasárselos al partido.
  3. **`Match`:** recibir los perfiles cargados en lugar de crear `new PlayerProfile("jugador 1")` (hoy los puntos se pierden en cada partido).
  4. **Guardado automático al terminar:** la fachada se registra como observadora del partido (o `Match` le avisa) y, en `onMatchFinished`, guarda los dos perfiles con `profileRepository.save(...)`.
  5. La GUI crea la fachada con los dos repositorios: `new DOPOSoccer(new SerializedMatchRepository(), new SerializedProfileRepository())`.
- [ ] Pruebas:
  - `SerializedProfileRepository` con un archivo temporal: guardar un perfil con puntos, cargarlo de nuevo → mismos puntos y estadísticas; cargar un nombre que no existe → perfil nuevo en 1 y con 0 puntos.
  - Integración: jugar un partido hasta el final, crear otro con los mismos nombres → el ganador tiene 100 puntos y el perdedor 25.
- [ ] Detalle de diseño: el perfil del jugador derecho en PvM es de la máquina; decidir si la máquina acumula puntos (pregunta para el monitor) o si solo se guarda el del humano.

### 2.2 Guardar y abrir partida: cerrar detalles · ≈ 0.5 h restantes
- [x] Al guardar, el partido queda en **pausa** · ✅ `fix6`.
- [ ] **Usar los mensajes nuevos de Cristian** (ya existen en `DOPOSoccerException`):
  - `DOPOSoccer.save` sin partido → `NO_ACTIVE_MATCH` (hoy lanza `INVALID_STATE`);
  - `SerializedMatchRepository.open`: archivo inexistente → `FILE_NOT_FOUND`; archivo que no es una partida (`ClassNotFoundException`, `ClassCastException`, `StreamCorruptedException`) → `INVALID_FILE`; otros errores de lectura → `OPEN_ERROR`;
  - en todos los casos, lanzar con el **constructor con causa** (`new DOPOSoccerException(mensaje, e)`), para que el log guarde el error original.
- [ ] Probar abrir un archivo que no existe y uno corrupto → `DOPOSoccerException` con mensaje claro, y la traza en el log.
- [ ] Probar guardar y abrir en PvM: la máquina sigue jugando después de abrir.

### 2.3 Log · 🟡 casi
- [x] La GUI registra con `Log.record(...)` las `DOPOSoccerException` que atrapa · ✅ `fix6`.
- [ ] Atrapar también las excepciones **inesperadas** del bucle del juego (`RuntimeException` dentro del `Timer`), registrarlas y mostrar un mensaje, para que el juego no se congele en silencio.
- [ ] Ojo con el diseño: `Log` vive en `persistence`. El dominio **no** debe importarlo (sería una dependencia al revés). Si el dominio necesita registrar algo, que lance la excepción y la capa de arriba la registre.

---

## Prioridad 3 — Estadísticas y puntos (requisitos del PDF) · ≈ 2 – 3 h

*"Permitir mejorar las estadísticas de cada jugador"*: cada partido da 100 puntos al ganador y 25 al perdedor; con esos puntos se mejoran las estadísticas **al inicio de los partidos**.

- [ ] `PlayerProfile.upgrade(StatType type)`:
  - precio = **500 × 2^(nivel actual − 1)** (500, 1000, 2000...). Confirmar con el monitor si el precio se duplica por estadística o por cada compra;
  - lanza `DOPOSoccerException(NOT_ENOUGH_POINTS)` si no alcanza, y **`MAX_STAT_REACHED`** (ya existe, lo agregó Cristian) si ya está en 10;
  - descuenta los puntos y sube el nivel.
- [ ] `PlayerProfile.upgradeCost(StatType type)`, para que la GUI muestre el precio.
- [ ] Fachada: `upgrade(String playerName, StatType type)` y lectura del perfil (puntos y niveles) **por una vista de solo lectura** (`ProfileView`), sin entregar el `PlayerProfile` real.
- [ ] Guardar el perfil después de cada mejora (Prioridad 2.1).
- [ ] Pruebas: comprar con puntos suficientes, sin puntos suficientes, con el nivel en 10, y el precio que se duplica.

---

## Prioridad 4 — Entregables de documentación (V2 y V3) · ≈ 3 – 4 h

- [ ] **Planificación de las 4 versiones** (V2): objetivo de cada versión, sus mini-ciclos y **los requisitos que no se van a incluir**. Propuesta:
  - V1: PvP básico desacoplado (hecho);
  - V2: persistencia, PvM con 4 perfiles, poderes completos;
  - V3: progreso y mejoras de estadísticas, pruebas de aceptación;
  - V4: Ilusión completa, animaciones y pulido.
- [ ] **Diagramas de secuencia nuevos:** decisión de la máquina (`decide` → `chooseActions` de un perfil), guardar partida, abrir partida, terminar un partido y guardar el progreso.
- [ ] **Diagrama de clases:** agregar `MachineController` y sus 4 perfiles, `MatchRepository`, `ProfileRepository`, el paquete `persistence` y `Log`.
- [ ] **Pruebas de aceptación** (V3): escenarios del PDF escritos como casos. Por ejemplo: configurar y jugar PvM contra cada perfil; partido por tiempo que termina en muerte súbita; guardar a mitad del partido, cerrar y abrir; ganar, recibir 100 puntos y comprar una mejora.
- [ ] Actualizar la retrospectiva.

---

## Prioridad 5 — Bugs (después de la entrega)

### BUG-7 · El balón no toma velocidad al conducirlo manteniendo D — **confirmado**
- **Medición:** el jugador camina a 3.5 por tick empujando el balón; la velocidad del balón queda en **0.00** durante todo el recorrido, y al soltar D el balón se detiene **en seco** (avanza 0 px).
- **Causa:** `Ball.bounceOff` solo mira la velocidad **del balón**. Si el balón está quieto y el pie se le mete encima, `v · n = 0`, no hay reflexión, y solo se corrige la **posición** (sacarlo `penetración × normal`). El balón avanza "empujado" pero sin velocidad propia.
- **Arreglo (dos opciones):**
  1. **La física correcta:** usar la **velocidad relativa**. `bounceOff` recibe también la velocidad del obstáculo (la del jugador) y calcula `vn = (vBalón − vObstáculo) · n`. Así un pie que avanza le transmite velocidad al balón.
  2. **La idea de Juan, más simple:** cuando el pie que **no patea** empuja el balón en el piso, darle una velocidad mínima fija en la dirección del empujón (por ejemplo, `max(vx actual, velocidad del jugador × 1.2)`). Es fácil de ajustar y de probar.
- **Prueba:** conducir 30 ticks con D y soltar → el balón sigue rodando un poco y su velocidad nunca es 0 mientras se lo empuja.

### BUG-8 · El balón queda "marcado" después de un tiro de poder — **confirmado en el dominio** (falta confirmar que es lo que se vio)
- **Medición:** después de que el tiro de poder golpea al rival, durante 2 s más (y hasta el siguiente gol), `ball.hasPowerShot()` sigue en `true` aunque el `Power` ya no tiene tiro activo. En la GUI, el balón se sigue viendo con el efecto del tiro.
- **Causa:** `Ball.setPowerShot(false)` solo se llama en `Match.resetPositions()` (después de un gol). Ni `Power.onHitRival` ni `finishPowerShot` desmarcan el balón.
- **Relacionado (por lectura de código):** si el tiro **no** golpea al rival ni termina en gol (por ejemplo, rebota en el travesaño), `powerShotActive` queda en `true` hasta el siguiente gol, y como `activateAura()` exige que no haya tiro activo, **el jugador no puede volver a usar el poder** aunque la barra se llene. El PDF solo bloquea el poder mientras el jugador tiene el aura.
- **Arreglo:** que el fin del tiro (golpe al rival, gol, o un tiempo máximo de vuelo, por ejemplo 2 s) **desmarque el balón** y termine el tiro. Lo más limpio es que `Power` reciba el balón al terminar (`finishPowerShot(ball)`) o que `Match` lo desmarque cuando `powerHitOn` devuelve un golpe.
- **Si lo que se vio fue el cartel de gol quieto en pantalla:** en el dominio, `GOAL_SCORED` dura 85 ticks y siempre vuelve a `PLAYING` (también con pausa en medio). Anotar cómo pasó para reproducirlo.

### BUG-9 · Dos jugadores pegados, balón en medio, ambos patean a la vez: el balón traspasa a uno — **confirmado**
- **Medición:** 27 combinaciones de separación y altura del balón → **en 15 el balón termina detrás de algún jugador**.
- **Causa:** cada jugador resuelve su contacto con el balón **por separado y en orden** (`player1.collideWithBall`, después `player2.collideWithBall`). La patada del primero saca al balón hacia el segundo y le da velocidad; la del segundo lo saca hacia el primero. Con los dos pies avanzando a la vez, el balón queda atrapado y la última corrección puede sacarlo por el lado equivocado.
- **Arreglo:**
  - Si el balón está tocando a los **dos** jugadores en el mismo tick, tratarlo como un caso especial: **sale hacia arriba** (un "globito"), con la componente horizontal de quien pateó más fuerte, o en 0 si fue parejo.
  - Además, no sacar nunca el balón **a través** de un cuerpo: si después de todas las correcciones el balón quedó dentro de un jugador, empujarlo hacia arriba.
- **Prueba:** el mismo escenario de las 27 combinaciones → 0 casos con el balón detrás de un jugador.

### BUG-10 · Los balones falsos de la Ilusión nunca desaparecen si el tiro golpea al rival — **confirmado** (venía de `fix6`)
- **Medición:** tiro de EE. UU. que golpea al rival → 200 ticks después siguen los 7 balones falsos (`getFakeBalls()` devuelve 7), quietos, con 145 ticks de ilusión "pendientes". El PDF dice que los falsos **desaparecen al terminar el efecto** (2.5 s).
- **Causa:** los falsos solo se actualizan (y su contador solo baja) en `updateActiveShot`, que `Power.updateShot` llama **mientras el tiro está activo**. Al golpear al rival, `onHitRival` termina el tiro y nadie vuelve a llamar `updateClones`: el contador se congela y la lista nunca se vacía.
- **Arreglo:** que la duración de la ilusión **no dependa** de que el tiro siga activo. Por ejemplo, un *hook* que `updateShot` llame en **todos** los ticks (`updateDecoys(ball)`), o que `IllusionPower` descuente su contador aparte, y al llegar a 0 vacíe la lista.
- **Por qué importa ahora:** cuando la GUI dibuje los falsos con `getFakeBalls()`, se van a quedar en pantalla.
- **Prueba:** tiro de la Ilusión que golpea al rival → a los 2.5 s `getFakeBalls()` está vacío.

---

## Pendientes de diseño (cuando haya tiempo)
- **D1.** Código muerto: `FreezePower` y `FrozenEffect` (ningún país los usa); `COMController` (ya ningún partido lo usa; solo una prueba) cuando existan los 4 perfiles.
- **D2.** `Field.WIDTH`/`HEIGHT` siguen siendo constantes globales en varias clases.
- **D3.** `Player.getPower()` sigue público y solo lo usan las pruebas.
- **D4.** Tamaño de clases: `Player` 262 líneas de código, `Match` 249, `PlayerBody` 206. No crecerlas; si `Match` sigue creciendo, sacar la creación de jugadores y controles a una `MatchFactory`.
- **D5.** Un **solo** log: `persistence.Log`. No crear otro en el dominio (se descartó `DOPOSoccerLog`).
- **D6.** Pruebas de `getFakeBalls()` y `getModality()` (los aportes de Cristian todavía no tienen pruebas propias).

---

## Resumen y orden

| Orden | Qué | Horas | Para |
|---|---|---|---|
| 1 | **Jugador contra máquina**: Torpe y Ofensiva (Cristian) + pruebas de los 4 perfiles | 2 – 3 | **V2 (sábado)** |
| 2 | **Conectar la persistencia del progreso** + pruebas + detalles de guardar/abrir | 2 – 2.5 | **V2 (sábado)** |
| 3 | Mejora de estadísticas con puntos | 2 – 3 | V2 o V3 |
| 4 | Planificación de versiones, diagramas, pruebas de aceptación | 3 – 4 | V2 y V3 |
| 5 | Bugs 7, 8, 9 y 10 | 2.5 – 3.5 | Después de la entrega |
| 6 | Pendientes de diseño | 1 – 2 | Cuando haya tiempo |
| | **Total** | **≈ 12.5 – 18 h** | |

**Para el sábado, lo mínimo:**
- **Cristian:** `OffensiveMachine` primero (sin ella, la Experta tampoco ataca) y luego `ClumsyMachine`. **Antes de seguir, traer esta rama** (`git pull origin claude/brave-newton-pne4dc`) en lugar de resolver su merge a mano: sus aportes ya están combinados aquí.
- **Juan:** conectar el progreso (2.1, pasos 1 a 5) y las pruebas de la máquina (1.4) para los perfiles que ya están.
- **Los dos:** la planificación de versiones (prioridad 4).

## Reglas de trabajo
1. La API pública (fachada, vistas, eventos y `MatchConfig`) no cambia sin avisar.
2. Cada funcionalidad nueva llega con su prueba.
3. Un commit por paso; push frecuente; todas las pruebas en verde.
4. Nada de `instanceof`, de cadenas `a.getB().getC()` desde fuera de la clase dueña, ni de constantes sueltas.
5. El dominio no importa nada de `presentation` ni de `persistence`.

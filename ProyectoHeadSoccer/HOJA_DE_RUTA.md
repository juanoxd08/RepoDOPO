# DOPOSoccer 2026-2 — Hoja de ruta (dominio)

Código de partida: commit **`fix5`** (`0b62834`).
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

## Prioridad 1 — Jugador contra máquina (V2) · ≈ 4 – 5 h

El PDF pide la modalidad **jugador contra máquina** con **4 perfiles**: Torpe (*escoge al azar*), Ofensiva (*su prioridad es anotar*), Defensiva (*su prioridad es defender el arco*) y Experta (*decide según el estado del partido*).

### 1.1 Una clase por perfil (Strategy + Template Method) · ≈ 2.5 h
- [ ] `ClumsyMachine extends MachineController`: acciones al azar que mantiene unos ticks. **`Random` inyectable** por constructor, para poder probarlo.
- [ ] `OffensiveMachine`: se ubica detrás del balón respecto al arco rival, salta si el balón viene alto, patea si está al alcance, usa el poder cuando va a tocar el balón.
- [ ] `DefensiveMachine`: se queda **entre el balón y su arco**; si el balón entra en su mitad, va a despejarlo. Nunca empuja el balón hacia su propio arco.
- [ ] `ExpertMachine`: **decide según el estado del partido**, usando solo `MatchView`. Por ejemplo:
  - va perdiendo, o queda poco tiempo con empate → se comporta como ofensiva;
  - va ganando → como defensiva;
  - el rival tiene el aura activa → defensiva (se protege del tiro);
  - tiene el poder listo y el balón cerca → lo activa;
  - el rival está aturdido o inmovilizado → ataca.
  - Lo más limpio: `ExpertMachine` **compone** una `OffensiveMachine` y una `DefensiveMachine` y elige a cuál delegarle en cada tick.
- [ ] La lógica actual de `COMController` se **mueve** a estas clases (no se reescribe). Después se borra `COMController`.
- [ ] Reemplazar los números sueltos (18, 26, 70, 130...) por constantes con nombre, y los `side == RIGHT ? … : …` por `side.dir()`.

### 1.2 `MachineProfile` como Factory · ≈ 0.5 h
- [ ] Cada valor del `enum` crea su controlador: `MachineController create(Side side)` (con `Supplier`/`Function`, como `Country` crea el poder).
- [ ] Una sobrecarga `create(Side side, Random random)` para las pruebas del perfil Torpe.

### 1.3 Configuración y partido · ≈ 0.5 h
- [ ] `MatchConfig.Builder`: método `machineProfile(MachineProfile)`; `build()` **deja de rechazar `PVC`** y valida que, si la modalidad es `PVC`, haya perfil.
- [ ] `Match`: el control derecho sale de `config.getMachineProfile().create(Side.RIGHT)`, en lugar de `new COMController(Side.RIGHT)`.
- [ ] `MatchView`/`PlayerView`: si la GUI necesita mostrar el perfil de la máquina, exponerlo por la vista.

### 1.4 Pruebas · ≈ 1 h
- [ ] **Torpe** con un `Random` de semilla fija: siempre las mismas acciones.
- [ ] **Ofensiva:** balón delante, entre ella y el arco rival → se acerca y patea.
- [ ] **Defensiva:** balón cerca de su arco → queda entre el balón y el arco; balón lejos → vuelve a su posición.
- [ ] **Experta:** va perdiendo → actúa como ofensiva; va ganando → como defensiva.
- [ ] Un partido de 60 s con cada perfil → termina sin excepciones.
- [ ] Guardar y abrir una partida PvM → la máquina sigue funcionando (su `Random` y su estado se serializan).

**Listo cuando:** se puede crear un partido PvM con cualquiera de los 4 perfiles desde `MatchConfig`, y cada perfil tiene su prueba.

---

## Prioridad 2 — Persistencia completa (V2) · ≈ 3 – 4 h

El PDF pide dos cosas: **guardar y abrir el estado de un partido** (✅ hecho) y **mantener el progreso de los jugadores (puntos y estadísticas)** (❌ falta).

### 2.1 Progreso de los jugadores · ≈ 2 – 2.5 h
- [ ] Interfaz en el dominio: **`ProfileRepository`** con `PlayerProfile load(String name)` (si no existe, crea uno nuevo con todo en 1 y 0 puntos) y `void save(PlayerProfile profile)`. Que lance `DOPOSoccerException`.
- [ ] Implementación en `persistence`: **`SerializedProfileRepository`** (un archivo con un mapa nombre → perfil). `PlayerProfile` y `PlayerStats` ya son serializables.
- [ ] `MatchConfig.Builder`: recibir el **nombre** (o el `PlayerProfile`) de cada lado. Hoy `Match` crea `new PlayerProfile("jugador 1")` en cada partido y **los puntos se pierden**.
- [ ] Fachada: `DOPOSoccer(MatchRepository, ProfileRepository)`; carga los perfiles al crear el partido y los **guarda automáticamente al terminar** (cuando `Match` reparte los 100/25 puntos).
- [ ] Prueba: jugar un partido, terminarlo, crear otro con los mismos nombres → los puntos se conservan.

### 2.2 Guardar y abrir partida: cerrar detalles · ≈ 0.5 h
- [ ] Al guardar, dejar el partido en **pausa**, para que al abrirlo no arranque solo.
- [ ] Probar abrir un archivo que no existe y uno corrupto → `DOPOSoccerException` con mensaje claro, y la traza en el log.
- [ ] Probar guardar y abrir en PvM (Prioridad 1).

### 2.3 Log · ≈ 0.5 h
- [ ] Que **todo** error se registre, no solo los de archivos: la GUI debería llamar `Log.record(...)` cuando atrapa una `DOPOSoccerException` o una excepción inesperada.
- [ ] Ojo con el diseño: `Log` vive en `persistence`. El dominio **no** debe importarlo (sería una dependencia al revés). Si el dominio necesita registrar algo, que lance la excepción y la capa de arriba la registre.

---

## Prioridad 3 — Estadísticas y puntos (requisitos del PDF) · ≈ 2 – 3 h

*"Permitir mejorar las estadísticas de cada jugador"*: cada partido da 100 puntos al ganador y 25 al perdedor; con esos puntos se mejoran las estadísticas **al inicio de los partidos**.

- [ ] `PlayerProfile.upgrade(StatType type)`:
  - precio = **500 × 2^(nivel actual − 1)** (500, 1000, 2000...). Confirmar con el monitor si el precio se duplica por estadística o por cada compra;
  - lanza `DOPOSoccerException(NOT_ENOUGH_POINTS)` si no alcanza, e `INVALID_STAT` si ya está en 10;
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

---

## Pendientes de diseño (cuando haya tiempo)
- **D1.** Código muerto: `FreezePower` y `FrozenEffect` (ningún país los usa); `COMController` cuando existan los 4 perfiles.
- **D2.** `Field.WIDTH`/`HEIGHT` siguen siendo constantes globales en varias clases.
- **D3.** `Player.getPower()` sigue público y solo lo usan las pruebas.
- **D4.** Tamaño de clases: `Player` 262 líneas de código, `Match` 249, `PlayerBody` 206. No crecerlas; si `Match` sigue creciendo, sacar la creación de jugadores y controles a una `MatchFactory`.

---

## Resumen y orden

| Orden | Qué | Horas | Para |
|---|---|---|---|
| 1 | **Jugador contra máquina** (4 perfiles, Factory, configuración, pruebas) | 4 – 5 | **V2 (sábado)** |
| 2 | **Persistencia del progreso** + detalles de guardar/abrir + log | 3 – 4 | **V2 (sábado)** |
| 3 | Mejora de estadísticas con puntos | 2 – 3 | V2 o V3 |
| 4 | Planificación de versiones, diagramas, pruebas de aceptación | 3 – 4 | V2 y V3 |
| 5 | Bugs 7, 8 y 9 | 2 – 3 | Después de la entrega |
| 6 | Pendientes de diseño | 1 – 2 | Cuando haya tiempo |
| | **Total** | **≈ 15 – 21 h** | |

**Para el sábado, lo mínimo:** prioridades 1 y 2, más la planificación de versiones (prioridad 4).

## Reglas de trabajo
1. La API pública (fachada, vistas, eventos y `MatchConfig`) no cambia sin avisar.
2. Cada funcionalidad nueva llega con su prueba.
3. Un commit por paso; push frecuente; todas las pruebas en verde.
4. Nada de `instanceof`, de cadenas `a.getB().getC()` desde fuera de la clase dueña, ni de constantes sueltas.
5. El dominio no importa nada de `presentation` ni de `persistence`.

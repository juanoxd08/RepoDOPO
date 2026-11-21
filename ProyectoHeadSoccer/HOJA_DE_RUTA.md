# DOPOSoccer 2026-2 — Hoja de ruta

Código de partida: commit **`fix7`** (`32064e8`).
Enunciado: `DOPOSoccer_2026-2.pdf`.

> **Prioridad actual: la interfaz gráfica.** Después, lo que falta del dominio. **Los bugs van al final.**

---

## Calendario

| Entrega | Qué pide el PDF | Fecha |
|---|---|---|
| **V3** | Lo anterior + lo propuesto para el ciclo 3, **pruebas de aceptación** | Do 22 nov |
| **V4** | Lo anterior + lo propuesto para el ciclo 4 | Parcial (S17) |
| Competencia | Todo completo | Vi 4 dic |

---

## Estado en `fix7` (verificado)

- Compila; **50 de 50 pruebas pasan** (22 de dominio, 12 de física, 7 estáticas y dinámicas, 9 de persistencia).

**Dominio terminado:**
- Fachada, `MatchConfig` (Builder), vistas de solo lectura, eventos (Observer), reglas de fin (Strategy), física con ancla única y `bounceOff`, vida, efectos (Strategy), poderes (Template Method), los 4 personajes con su fábrica (`Country`).
- **Guardar y abrir partidas** con mensajes claros: `FILE_NOT_FOUND`, `INVALID_FILE`, `OPEN_ERROR` y `NO_ACTIVE_MATCH`, siempre con la causa original.
- **Progreso de los jugadores:** `MatchConfig` recibe los nombres; la fachada carga los perfiles al crear el partido y los guarda al terminar (`profiles.dat`).
- **Mejora de estadísticas:** `PlayerProfile.upgrade` y `upgradeCost` (500 × 2^(nivel − 1), máximo 10); en la fachada, `getProfile(nombre)` (devuelve `ProfileView`, de solo lectura) y `upgrade(nombre, StatType)`.
- **Jugador contra máquina:** `MachineProfile` (Factory), `MachineController` (Template Method), `DefensiveMachine` y `ExpertMachine` terminadas.
- **Log de errores** (`persistence.Log`), también para los errores inesperados del bucle del juego.
- Para la GUI ya existen `MatchView.getFakeBalls()`, `MatchView.getModality()` y las velocidades del balón en `BallView`.
- Se borraron `FreezePower` y `FrozenEffect`, y `getPower()` ya no es público.

**Falta (detalle abajo):** la GUI (prioridad 1), `ClumsyMachine` y `OffensiveMachine` (Cristian), algunos datos del dominio para la GUI, pruebas de la máquina, casos borde del progreso y los bugs.

---

## Prioridad 1 — Interfaz gráfica · ≈ 10 – 13 h

### 1.0 Datos del dominio que la GUI necesita · ≈ 1.5 – 2 h · *hacer primero*
Sin esto, la GUI no sabe qué animación ni qué sonido usar. Todo por las vistas y los eventos, sin que la GUI busque dentro del dominio:
- [ ] **`PlayerView`:** `PlayerEffect getActiveEffect()` (hoy es un `String` con `getActiveEffectName()`) y `double getEffectProgress()` (0..1, cuánto le queda).
- [ ] **`BallView`:** `Country getPowerShotCountry()` (el país del tiro en vuelo, o `null`), `boolean isPowerShotCharging()` y `double getPowerShotChargeProgress()` (el balón quieto en el aire durante los 90 ticks del tiro sostenido).
- [ ] **`MatchListener`** (métodos `default` vacíos, para no romper nada): `onBallKicked(Side)`, `onPowerActivated(Side)`, `onPowerShot(Side, Country)` y `onPowerHit(Side víctima, double daño)`.
- [ ] Pruebas: cada evento se dispara una vez en el momento correcto.

### 1.1 Lo que pide el PDF y todavía no tiene la GUI · ≈ 3 – 4 h
- [ ] **Nombres de los jugadores** en la pantalla de selección (dos campos de texto) → `MatchConfig.Builder.leftPlayerName(...)` y `rightPlayerName(...)`. Sin esto, todos juegan como "jugador 1" y "jugador 2" y el progreso se mezcla.
- [ ] **Pantalla de mejoras** ("permitir mejorar las estadísticas de cada jugador al inicio de los partidos"): por jugador, sus puntos y sus 4 estadísticas con su nivel y el precio de la siguiente (`game.getProfile(nombre)`), y un botón "+" que llama `game.upgrade(nombre, StatType)`. Si lanza `DOPOSoccerException`, mostrar el mensaje y registrarlo en el log.
- [ ] **Fin del partido:** mostrar los **puntos ganados y los acumulados** de cada jugador (el PDF pide "mensaje notificando el ganador y los puntos obtenidos"), con acceso directo a la pantalla de mejoras.
- [ ] **HUD:** mostrar la modalidad y, en PvM, el perfil de la máquina (`MatchView.getModality()`).
- [ ] **Guardar y abrir:** filtro de extensión en el `JFileChooser` (por ejemplo `.dopo`); al abrir, mostrar el partido en pausa con un mensaje para continuar.

### 1.2 Imágenes y tamaños · ≈ 2 – 3 h
- [ ] **Caras** (un PNG por país, fondo transparente) dibujadas **dentro del círculo de la cabeza** (`getHeadShape()`), espejadas según el lado. Opcional: una variante por estado (pateando, aturdido, electrocutado...).
- [ ] **Arcos:** PNG dibujado **encima de los rectángulos del dominio** (`getCrossBar(side)` y `getGoalArea(side)`), con el travesaño invisible para la física. **El dibujo debe calzar con el rectángulo**: mientras se ajusta, dibujar también el borde del rectángulo.
- [ ] **Banderas** en el HUD, junto al nombre.
- [ ] **Re-ajustar tamaños** según la resolución elegida: todo se escala a partir de las medidas del dominio (`getFieldWidth()`, `getFieldHeight()` y las figuras), nunca con números fijos de píxeles.
- [ ] Cargar las imágenes **una sola vez** (`ImageIO.read(getClass().getResource(...))`) en un `EnumMap<Country, BufferedImage>`; si falta una, dibujar la forma actual y registrarlo en el log.
- [ ] `CREDITOS.txt` con la fuente de cada imagen con licencia CC-BY. No usar sprites del Head Soccer original.

### 1.3 Animación de los poderes · ≈ 2.5 – 3 h *(necesita 1.0)*
- [ ] Contador de frames propio de la vista, para animar (pulsos con `Math.sin`).
- [ ] **Barra llena** que parpadea; **aura** del color de cada país que pulsa.
- [ ] **Tiro sostenido:** anillo de energía que se cierra alrededor del balón (`isPowerShotCharging()` y su progreso).
- [ ] **Tiro en vuelo:** estela del color del país (la GUI guarda las últimas 6-8 posiciones); dibujo propio por país: recto (Corea), zigzag eléctrico (Camerún), espiral (Nigeria).
- [ ] **Ilusión:** dibujar los 7 balones falsos con `getFakeBalls()`, **idénticos** al real. ⚠️ Ver BUG-10: si el tiro golpea al rival, los falsos no desaparecen.
- [ ] **Efecto sobre el rival:** ícono animado (flechas, chispas, estrellas, signos de interrogación) y una barrita de duración, en lugar del texto.
- [ ] **Golpe al rival:** destello y el daño flotando (con `onPowerHit`).

### 1.4 Sonidos · ≈ 1 – 1.5 h *(los de patada y poder necesitan 1.0)*
- [ ] Clase `SoundPlayer` en `presentation`: `javax.sound.sampled.Clip`, archivos **WAV**, cargados una sola vez en un `Map`.
- [ ] Gol, aturdido y fin del partido con los eventos que ya existen; patada, aura, tiro y golpe con los eventos nuevos.
- [ ] Opción de silenciar. Si un sonido falta o no hay audio, el juego sigue sin sonido (registrarlo en el log).
- [ ] Fuentes con licencia libre: Kenney.nl (CC0), Freesound.org, OpenGameArt.org.

**Listo cuando:** se puede configurar, jugar, mejorar estadísticas y guardar/abrir sin tocar código, y cada poder se distingue a simple vista.

---

## Prioridad 2 — Lo que falta del dominio · ≈ 3.5 – 5 h

### 2.1 Jugador contra máquina · *Cristian* · ≈ 1.5 h
- [ ] **`OffensiveMachine`** (primero: la Experta delega en ella cuando va empatando o perdiendo, así que hoy tampoco ataca): ubicarse detrás del balón respecto al arco rival, saltar si viene alto (`shouldJumpForBall`), patear si está al alcance (`shouldKickBall`), activar el poder cuando va a tocar el balón. Usar constantes con nombre, como en `DefensiveMachine`.
- [ ] **`ClumsyMachine`**: acciones al azar con el `Random` que ya tiene, **mantenidas unos ticks** (si cambian en cada tick, solo tiembla).
- [ ] Borrar **`COMController`** y pasar la prueba de `DOPOSoccerStaticDynamicTest` (línea 148) a los perfiles nuevos.

### 2.2 Pruebas de la máquina · ≈ 1 h
- [ ] Defensiva: balón cerca de su arco → queda entre el balón y el arco; balón lejos → vuelve a su posición.
- [ ] Experta: va perdiendo → actúa como ofensiva; va ganando → como defensiva; rival con aura → defensiva.
- [ ] Torpe con un `Random` de semilla fija → siempre las mismas acciones.
- [ ] `MachineProfile.create(...)` devuelve la clase correcta para cada perfil.

### 2.3 Casos borde del progreso · ≈ 0.5 – 1 h
- [ ] **Abrir una partida vieja pisa los puntos nuevos:** el archivo de la partida trae una copia de los perfiles de cuando se guardó. Si después se ganaron puntos en otros partidos y luego se abre y termina esa partida vieja, se guardan los perfiles viejos y **se pierden esos puntos**. Arreglo: al terminar, cargar el perfil actual del repositorio y **sumarle solo los puntos de este partido**, en lugar de guardar el objeto completo.
- [ ] **Mismo nombre en los dos lados:** el segundo guardado pisa al primero. Validar en `build()` que los nombres sean distintos.
- [ ] **Error al guardar el perfil al terminar:** hoy se convierte en `IllegalStateException` dentro del tick. Mejor: registrarlo en el log y avisar por un evento (por ejemplo, `onProfileSaveFailed`) para que la GUI muestre un mensaje.
- [ ] Preguntas al monitor que pueden cambiar código: si la **máquina acumula puntos** (hoy guarda un perfil "COM Experta"), si **terminar a la fuerza reparte puntos** y las **reglas de la Ilusión**.

### 2.4 Diseño (opcional) · ≈ 1 h
- [ ] `Field.WIDTH`/`HEIGHT` siguen como constantes globales en 27 lugares del dominio. No afecta a la GUI (que escala), pero quedaría mejor un objeto `Field` con medidas.
- [ ] Pruebas de `getFakeBalls()` y `getModality()` desde `MatchView`.
- [ ] Tamaño de clases: `Player`, `Match` y `PlayerBody` rondan las 200-260 líneas; no crecerlas.

---

## Prioridad 3 — Entregables · ≈ 3 – 4 h
- [ ] **Pruebas de aceptación** (V3): escenarios del PDF como casos. Por ejemplo: jugar PvM contra cada perfil; partido por tiempo que termina en muerte súbita; guardar a mitad del partido, cerrar y abrir; ganar, recibir 100 puntos y comprar una mejora.
- [ ] **Diagramas:** decisión de la máquina, guardar y abrir partida, terminar un partido y guardar el progreso, comprar una mejora. En el diagrama de clases: `MachineController` y sus perfiles, `MatchRepository`, `ProfileRepository`, `ProfileView`, el paquete `persistence`.
- [ ] **Retrospectiva** y **planificación de versiones** actualizadas.

---

## Prioridad 4 — Bugs (al final)

### BUG-7 · El balón no toma velocidad al conducirlo manteniendo D
- **Medición:** el jugador empuja el balón a 3.5 por tick, pero la velocidad del balón queda en **0.00** y al soltar D se detiene en seco.
- **Causa:** `Ball.bounceOff` solo usa la velocidad **del balón**; con el balón quieto, `v · n = 0` y solo se corrige la posición.
- **Arreglo:** velocidad relativa (`vn = (vBalón − vPie) · n`) o, más simple, una velocidad mínima en la dirección del empujón cuando el pie que no patea empuja el balón en el piso.

### BUG-8 · El balón queda marcado después de un tiro de poder
- **Medición:** después de que el tiro golpea al rival, `ball.hasPowerShot()` sigue en `true` hasta el siguiente gol (la GUI lo sigue dibujando con el efecto).
- **Relacionado:** si el tiro no golpea al rival ni termina en gol, el tiro queda activo y **el jugador no puede volver a usar el poder** hasta el siguiente gol.
- **Arreglo:** que el fin del tiro (golpe, gol o un tiempo máximo de vuelo) desmarque el balón y termine el tiro.

### BUG-9 · Dos jugadores pegados, balón en medio, ambos patean: el balón traspasa a uno
- **Medición:** en 15 de 27 combinaciones el balón termina detrás de un jugador.
- **Causa:** cada jugador resuelve su contacto por separado y en orden.
- **Arreglo:** si el balón toca a los dos en el mismo tick, que salga hacia arriba; y nunca dejarlo dentro de un cuerpo después de todas las correcciones.

### BUG-10 · Los balones falsos de la Ilusión no desaparecen si el tiro golpea al rival
- **Medición:** 200 ticks después del golpe siguen los 7 falsos, quietos. El PDF dice que desaparecen al terminar el efecto (2.5 s).
- **Causa:** se actualizan solo mientras el tiro está activo, y el golpe termina el tiro.
- **Arreglo:** que la duración de la ilusión no dependa de que el tiro siga activo (un *hook* que `updateShot` llame en todos los ticks).
- **Importa para la GUI:** al dibujar los falsos (1.3), se quedarían en pantalla.

---

## Resumen

| Orden | Qué | Horas |
|---|---|---|
| 1 | **GUI** (1.0 datos del dominio → 1.1 lo que pide el PDF → 1.2 imágenes → 1.3 animaciones → 1.4 sonidos) | 10 – 13 |
| 2 | Dominio: máquina (Cristian), pruebas de la máquina, casos borde del progreso | 3.5 – 5 |
| 3 | Entregables: pruebas de aceptación, diagramas, retrospectiva | 3 – 4 |
| 4 | Bugs 7, 8, 9 y 10 | 2.5 – 3.5 |
| | **Total** | **≈ 19 – 25.5 h** |

**Sugerencia de reparto:** Juan, la GUI (1.0 primero, porque toca el dominio) y los casos borde del progreso; Cristian, `OffensiveMachine` y `ClumsyMachine`, y después la animación de los poderes y los sonidos. Si el BUG-10 molesta al dibujar la Ilusión, adelantarlo.

## Reglas de trabajo
1. La GUI solo usa la fachada, las vistas, los eventos, los `enum` y `DOPOSoccerException`. Si falta un dato, se agrega a la vista; nunca se busca dentro del dominio.
2. La API pública (fachada, vistas, eventos y `MatchConfig`) no cambia sin avisar.
3. Un commit por paso; push frecuente; todas las pruebas en verde.
4. El dominio no importa nada de `presentation` ni de `persistence`.

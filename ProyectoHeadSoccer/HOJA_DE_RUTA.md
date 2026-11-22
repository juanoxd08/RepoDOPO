# DOPOSoccer 2026-2 — Hoja de ruta

Código de partida: commit **`fix8`** (`fc6f28e`).
Enunciado: `DOPOSoccer_2026-2.pdf`.

> **Prioridad actual: la interfaz gráfica.** Después, lo que falta del dominio. **Los bugs van al final**, salvo la prueba que falla (ver "Antes de seguir").

---

## Calendario

| Entrega | Qué pide el PDF | Fecha |
|---|---|---|
| **V3** | Lo anterior + lo propuesto para el ciclo 3, **pruebas de aceptación** | Do 22 nov |
| **V4** | Lo anterior + lo propuesto para el ciclo 4 | Parcial (S17) |
| Competencia | Todo completo | Vi 4 dic |

---

## Antes de seguir · ≈ 15 min

- [ ] **Una prueba fallaba y se quitó temporalmente:** `DOPOSoccerPhysicsTest.testFloorDisputeNeverPushesBallBelowFloor`. Hoy pasan las 55 que quedan. **Después de aplicar el arreglo de abajo, volver a ponerla** (el código está al final de este punto). No es un problema de la prueba: con las figuras más pequeñas de `fix8`, el balón queda debajo de la cabeza entre los dos cuerpos, la cabeza lo empuja hacia abajo y en el tick 4 **queda 10 px debajo del piso**. `bounceOff` solo corrige la normal cuando el balón **ya** toca el piso, no cuando el empujón lo mete en él. Arreglo (probado: quedan las 56 en verde), en `Ball.bounceOff`, justo después del `translate`:
  ```java
  if(this.circle.getY() + this.circle.getRadius() > Field.HEIGHT){
      this.circle.setY(Field.HEIGHT - this.circle.getRadius());
  }
  ```
  <details><summary>Prueba quitada (pegarla de nuevo en <code>DOPOSoccerPhysicsTest</code>, después de <code>testKickBallCloseToFootNeverEndsBehindPlayer</code>)</summary>

  ```java
      /**
       * Verifies that two players sandwiching the ball on the ground never push the ball below the floor.
       */
      @Test
      public void testFloorDisputeNeverPushesBallBelowFloor() {
          Player left = new Player(new PlayerProfile("Left"), Country.SOUTH_KOREA, Side.LEFT);
          Player right = new Player(new PlayerProfile("Right"), Country.CAMEROON, Side.RIGHT);
          double midX = Field.WIDTH / 2.0;
          left.translate((midX - 22.0) - left.getHeadShape().getX(), 0.0);
          right.translate((midX + 22.0) - right.getHeadShape().getX(), 0.0);
          Ball ball = new Ball(midX, Field.HEIGHT - 10.0, 10.0);

          for (int t = 0; t < 120; t++) {
              left.moveRight();
              right.moveLeft();
              left.tick();
              right.tick();
              ball.tick();
              ball.collideWithEdges();
              left.collideWithPlayer(right);
              right.collideWithPlayer(left);
              left.collideWithBall(ball);
              right.collideWithBall(ball);

              assertTrue("Ball sank below floor at tick " + t,
                      ball.getY() + ball.getRadius() <= Field.HEIGHT + 0.001);
          }
      }
  ```
  </details>

- [ ] **Se subieron archivos generados:** `DOPOSoccer.log` y `profiles.dat`. Agregarlos a `.gitignore` y sacarlos del repositorio sin borrarlos del disco (`git rm --cached ProyectoHeadSoccer/DOPOSoccer.log ProyectoHeadSoccer/profiles.dat`). Si no, cada partido cambia esos archivos y genera conflictos entre los dos.

---

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

## Prioridad 1 — Interfaz gráfica · ≈ 8.5 – 10 h

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

### 1.1b Reorganizar `PhotoHeadSoccer` · ≈ 3 h · *hacer antes de 1.2 y 1.3*
`PhotoHeadSoccer` tiene 909 de las 2083 líneas de la GUI y hace cinco cosas: guarda la configuración del partido (y los otros paneles la usan como depósito), arma el `MatchConfig`, dibuja todo, maneja los clics sobre botones dibujados a mano y guarda/abre partidas. Si no se separa ahora, las caras, el giro del balón y las animaciones de los poderes terminan en la misma clase. **No cambia nada de lo que se ve en pantalla.**
- [ ] **`MatchSetup`** (≈ 45 min): mover ahí los campos de selección (modalidad, países, nombres, tipo de partido, minutos/goles, resolución, perfil de la máquina) con sus `get`/`set`/`cycle...`, y un `toConfig()` que arme el `MatchConfig` con el `Builder`. `SelectPlayersPanel`, `SelectCampPanel` y `UpgradesPanel` reciben el `MatchSetup`, no `PhotoHeadSoccer`.
- [ ] **Renderers** (≈ 1 – 1.5 h): cada uno con `draw(Graphics2D g, MatchView match, double escala)`:
  - `FieldRenderer`: cancha y arcos (aquí va el arco dibujado a mano de 1.2).
  - `PlayerRenderer`: jugadores (aquí van las caras de 1.2 y el aura/efectos de 1.3).
  - `BallRenderer`: balón (giro, estela, anillo de carga, balones falsos).
  - `HudRenderer`: marcador, tiempo, vida, barra de poder, banderas.
  - `OverlayRenderer`: pausa, gol, empate y fin del partido.
  - `PhotoHeadSoccer` queda como el panel del partido: llama a los renderers en orden y escucha los eventos.
- [ ] **`Theme`** (≈ 30 min): colores por país, fuentes y los métodos de botones (`styleToggle`, `createStyledButton`, …) en un solo lugar. Hoy hay 73 `new Color(...)` y 40 `new Font(...)` repartidos en 6 archivos, con métodos repetidos.
- [ ] **Teclado como mapa** (≈ 30 min): un `Map<Integer, PlayerAction>` por jugador en lugar de la cadena de `if` de `processContinuousKeys`, y que cada pantalla maneje sus propias teclas en vez del `switch` de `HeadSoccerGUI.keyPressed`.
- [ ] *(Opcional, ≈ 45 min)* `MatchFileDialogs` para guardar y abrir, y los botones de pausa/fin como `JButton` reales en vez de comprobar a mano si el clic cayó en el rectángulo.
- [ ] Después de cada paso: abrir el juego y probar menú → selección → partido → pausa → guardar → abrir → fin → mejoras.

### 1.2 Imágenes y dibujo · ≈ 1.5 – 2 h
- [x] Balón (`Ball.png`) y banderas.
- [ ] **Giro del balón:** acumular un ángulo con la velocidad horizontal (`angulo += getHorizontalSpeed() / getRadius()`) y dibujar la imagen con `g2d.rotate(angulo, cx, cy)`, restaurando la transformación después.
- [ ] **Arcos dibujados a mano** con Java2D sobre `getGoalArea(side)` y `getCrossBar(side)` escalados: red con líneas semitransparentes y travesaño/poste blancos. Con el travesaño de 5 px, revisar que se vea a la resolución elegida.
- [ ] **Caras:** como no se encontraron PNG, dibujarlas por código: una cuadrícula de 16 × 16 caracteres (piel, pelo, ojos, boca...) con una paleta de colores por país, pintada dentro del círculo de la cabeza y espejada según el lado. Otra opción es hacerlas en Piskel (gratis) y exportarlas como PNG.
- [ ] Re-ajustar tamaños según la resolución: todo se escala desde las medidas del dominio, nunca con píxeles fijos. Con las figuras más pequeñas de `fix8`, revisar que se lea bien.
- [ ] `CREDITOS.txt` con la fuente de cada imagen.

### 1.3 Animación de los poderes · ≈ 2.5 – 3 h
- [ ] Contador de frames propio de la vista, para animar (pulsos con `Math.sin`).
- [ ] **Barra llena** que parpadea; **aura** del color de cada país que pulsa (`onPowerActivated`).
- [ ] **Tiro sostenido:** anillo de energía que se cierra alrededor del balón (`isPowerShotCharging()` y su progreso).
- [ ] **Tiro en vuelo:** estela del color del país (`getPowerShotCountry()`; la GUI guarda las últimas 6-8 posiciones); recto (Corea), zigzag eléctrico (Camerún), espiral (Nigeria).
- [ ] **Ilusión:** dibujar los 7 balones falsos con `getFakeBalls()`, idénticos al real (hoy la GUI no los dibuja). ⚠️ Ver BUG-10.
- [ ] **Efecto sobre el rival:** ícono animado y barrita de duración con `getActiveEffect()` y `getEffectProgress()`.
- [ ] **Golpe al rival:** destello y el daño flotando (`onPowerHit`).

### 1.4 Sonidos · ≈ 1 – 1.5 h
- [ ] Clase `SoundPlayer` en `presentation`: `javax.sound.sampled.Clip`, archivos **WAV**, cargados una sola vez en un `Map`.
- [ ] Gol, aturdido, fin, patada, aura, tiro y golpe, cada uno con su evento (ya existen todos).
- [ ] Opción de silenciar. Si un sonido falta o no hay audio, el juego sigue sin sonido (registrarlo en el log).
- [ ] Fuentes libres: Kenney.nl (CC0), Freesound.org, OpenGameArt.org.

**Listo cuando:** se puede configurar, jugar, mejorar estadísticas y guardar/abrir sin tocar código, y cada poder se distingue a simple vista.

---

## Prioridad 2 — Lo que falta del dominio · ≈ 3 – 4 h

### 2.1 Jugador contra máquina · *Cristian* · ≈ 1.5 h
- [ ] **`OffensiveMachine`** (primero: la Experta delega en ella cuando va empatando o perdiendo): ubicarse detrás del balón respecto al arco rival, saltar si viene alto, patear si está al alcance, activar el poder cuando va a tocar el balón. Constantes con nombre, como en `DefensiveMachine`.
- [ ] **`ClumsyMachine`**: acciones al azar con su `Random`, **mantenidas unos ticks** (si cambian en cada tick, solo tiembla).
- [ ] Borrar **`COMController`** y pasar su prueba de `DOPOSoccerStaticDynamicTest` a los perfiles nuevos.
- [ ] Con las figuras más pequeñas, revisar los alcances de `DefensiveMachine` (distancias pensadas para los tamaños anteriores).

### 2.2 Pruebas de la máquina · ≈ 1 h
- [ ] Defensiva: balón cerca de su arco → queda entre el balón y el arco; balón lejos → vuelve a su posición.
- [ ] Experta: va perdiendo → ofensiva; va ganando → defensiva; rival con aura → defensiva.
- [ ] Torpe con un `Random` de semilla fija → siempre las mismas acciones.
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

## Prioridad 3 — Entregables · ≈ 3 – 4 h
- [ ] **Pruebas de aceptación** (V3): jugar PvM contra cada perfil; partido por tiempo que termina en muerte súbita; guardar a mitad, cerrar y abrir; ganar, recibir 100 puntos y comprar una mejora.
- [ ] **Diagramas:** decisión de la máquina, guardar y abrir, terminar un partido y guardar el progreso, comprar una mejora. En el de clases: `MachineController` y sus perfiles, `MatchRepository`, `ProfileRepository`, `ProfileView`, el paquete `persistence` y los paneles nuevos de la GUI.
- [ ] **Retrospectiva** y **planificación de versiones** actualizadas.

---

## Prioridad 4 — Bugs (al final)

### BUG-7 · El balón no toma velocidad al conducirlo manteniendo D
- **Causa:** `Ball.bounceOff` solo usa la velocidad del balón; con el balón quieto, `v · n = 0` y solo se corrige la posición.
- **Arreglo:** velocidad relativa (`vn = (vBalón − vPie) · n`) o una velocidad mínima en la dirección del empujón cuando el pie que no patea empuja el balón en el piso.

### BUG-8 · El balón sigue con el efecto del poder después de terminar el tiro
- **Medición:** después de que el tiro golpea al rival, `ball.hasPowerShot()` sigue en `true` por lo menos 50 ticks después, con el balón ya quieto en el piso. La GUI lo sigue dibujando con el efecto. Solo se desmarca en `Match.resetPositions()`, es decir, con el siguiente gol.
- **Causa:** `Power.onHitRival` termina el tiro (`powerShotActive = false`), pero nadie llama `ball.setPowerShot(false)`.
- **Relacionado:** si el tiro no golpea al rival ni termina en gol, queda activo sin tiempo máximo y **el jugador no puede volver a usar el poder** hasta el siguiente gol.
- **Arreglo:** en `Match.resolveCollisions`, después de aplicar el golpe, `ball.setPowerShot(false)` y `finishPowerShot()` del tirador. Además, un tiempo máximo de vuelo en `Power` (por ejemplo, 2 s después de soltar el tiro) que haga lo mismo.

### BUG-9 · Dos jugadores pegados, balón en medio, ambos patean: el balón traspasa a uno
- **Causa:** cada jugador resuelve su contacto por separado y en orden.
- **Arreglo:** si el balón toca a los dos en el mismo tick, que salga hacia arriba; y nunca dejarlo dentro de un cuerpo después de todas las correcciones.

### BUG-10 · Los balones falsos de la Ilusión no desaparecen si el tiro golpea al rival
- **Causa:** se actualizan solo mientras el tiro está activo, y el golpe termina el tiro.
- **Arreglo:** que la duración de la ilusión no dependa de que el tiro siga activo (un *hook* que `updateShot` llame en todos los ticks).

### BUG-11 · El balón se comporta raro mientras se carga el tiro de poder
- **Medición:** durante los 90 ticks (1.5 s) en que el balón queda quieto en el aire, el rival lo atraviesa caminando. En cada tick el choque mueve el balón unos píxeles y el siguiente tick `Power.updateShot` lo vuelve a poner en su sitio con velocidad 0, así que tiembla y no detiene a nadie. Si el rival lo patea, la patada no hace nada.
- **Causa:** el balón en carga se resuelve como un balón normal (el choque mueve **al balón**), pero `updateShot` lo teletransporta de vuelta en cada tick.
- **Arreglo:** mientras `ball.isPowerShotCharging()`, que el balón sea un obstáculo fijo: en `Player.collideWithBall`, en vez de `bounceOff`, sacar **al jugador** del círculo del balón (como `resolveCircleAgainstBar` con el arco) y no permitir patearlo. Otra opción, más simple: que en la carga el balón no choque con nadie.
- **Después del golpe:** el balón rebota contra la cabeza del rival con el impulso de cabeza (sale a −15 px/tick de vuelta hacia el tirador). Decidir si se quiere así o si el balón debe perder velocidad al golpear.

### BUG-12 · El poder solo se ejecuta pateando, y con las dos auras el rival no responde
- **El PDF dice "toca", no "patea":** regla 4, *"Cuando el personaje con poder **toca** el balón, ejecuta su tiro de poder y el aura desaparece."* Hoy solo se ejecuta en `collideFootWithBall`, si el pie está pateando y el balón está al frente. Tocarlo con la cabeza, el cuerpo o el pie sin patear no hace nada.
- **Medición (las dos auras activas):** el tiro de Corea golpea a Camerún, que tiene el aura; Camerún recibe 40 de daño y el empuje, **y su aura sigue igual**: no ejecuta su tiro aunque el balón lo tocó.
- **Arreglo:** ejecutar el tiro cuando el balón toque cualquier parte del jugador con aura (cabeza, cuerpo o pie), no solo al patear.
- **Pregunta al monitor:** cuando el tiro del rival golpea a alguien que tiene aura, ¿primero recibe el daño y el efecto (regla 5) y después responde con su propio tiro (regla 4), o solo uno de los dos? Mientras tanto, proponer: se aplica el golpe y luego el que fue golpeado ejecuta su tiro.

### BUG-13 · A veces el tiro de poder toca al rival y no le hace nada
- **Medición:** en 243 tiros simulados por país, contra el rival a distintas distancias y saltando en distintos momentos, el balón **toca al rival y no cuenta como golpe** en 36 (Corea), 36 (Camerún), 74 (Nigeria) y 61 (EE. UU.). No es solo el efecto: tampoco hay daño ni `onPowerHit`.
- **Causa:** en `Match.resolveCollisions`, primero va `player2.collideWithBall(ball)`, que hace rebotar el balón y lo saca del cuerpo del rival, y **después** `powerHitOn(...)`, que pregunta `isHitByBall`. Para entonces el balón ya no lo toca.
- **Arreglo (probado en la simulación: los toques sin golpe bajan a 0 en los 4 países):** en `resolveCollisions`, calcular `hit1` y `hit2` **antes** de los `collideWithBall`, y aplicar `receiveHit` y los eventos como ahora.
- **Relacionado:** con el BUG-12 (el que tiene aura y es golpeado) hay que decidir el orden: primero el golpe y luego su tiro.

### Dash
- **No lo pide el PDF:** los movimientos son "mover, saltar, patear y activar el poder". Es opcional, y conviene dejarlo para después de los bugs y los entregables. Si se hace: una `PlayerAction.DASH` con su tecla, un impulso horizontal de pocos ticks y un tiempo de espera, todo dentro de `PlayerBody`.

---

## Resumen

| Orden | Qué | Horas |
|---|---|---|
| 0 | Prueba que falla y archivos generados en el repositorio | 0.25 |
| 1 | **GUI** (1.1 detalles → **1.1b reorganizar `PhotoHeadSoccer`** → 1.2 giro, arcos, caras → 1.3 animaciones → 1.4 sonidos) | 8.5 – 10 |
| 2 | Dominio: máquina (Cristian) y sus pruebas | 2.5 – 3.5 |
| 3 | Entregables: pruebas de aceptación, diagramas, retrospectiva | 3 – 4 |
| 4 | Bugs 7 a 13 (el 8, el 12 y el 13 primero: afectan la GUI y el PDF) | 3.75 – 5.25 |
| | **Total** | **≈ 18 – 23 h** (sin el dash) |

**Sugerencia de reparto:** Juan, la GUI (1.1b primero, luego 1.2 y 1.3); Cristian, `OffensiveMachine` y `ClumsyMachine`, y después los sonidos. Si el BUG-10 molesta al dibujar la Ilusión, adelantarlo.

## Reglas de trabajo
1. La GUI solo usa la fachada, las vistas, los eventos, los `enum` y `DOPOSoccerException`. Si falta un dato, se agrega a la vista; nunca se busca dentro del dominio.
2. La API pública (fachada, vistas, eventos y `MatchConfig`) no cambia sin avisar.
3. Un commit por paso; push frecuente; **todas las pruebas en verde antes del push**.
4. El dominio no importa nada de `presentation` ni de `persistence`.
5. No subir archivos generados al jugar (`*.log`, `*.dat`, partidas guardadas).

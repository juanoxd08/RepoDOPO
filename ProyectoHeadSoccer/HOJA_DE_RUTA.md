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

- [ ] **Una prueba falla:** `DOPOSoccerPhysicsTest.testFloorDisputeNeverPushesBallBelowFloor` (55 de 56 pasan). No es un problema de la prueba: con las figuras más pequeñas de `fix8`, el balón queda debajo de la cabeza entre los dos cuerpos, la cabeza lo empuja hacia abajo y en el tick 4 **queda 10 px debajo del piso**. `bounceOff` solo corrige la normal cuando el balón **ya** toca el piso, no cuando el empujón lo mete en él. Arreglo (probado: quedan las 56 en verde), en `Ball.bounceOff`, justo después del `translate`:
  ```java
  if(this.circle.getY() + this.circle.getRadius() > Field.HEIGHT){
      this.circle.setY(Field.HEIGHT - this.circle.getRadius());
  }
  ```
- [ ] **Se subieron archivos generados:** `DOPOSoccer.log` y `profiles.dat`. Agregarlos a `.gitignore` y sacarlos del repositorio sin borrarlos del disco (`git rm --cached ProyectoHeadSoccer/DOPOSoccer.log ProyectoHeadSoccer/profiles.dat`). Si no, cada partido cambia esos archivos y genera conflictos entre los dos.

---

## Estado en `fix8` (verificado)

- Compila; **55 de 56 pruebas pasan** (la que falla está arriba).
- **Nuevo en `fix8`:**
  - **Datos para la GUI (todo el 1.0):** `PlayerView.getActiveEffect()` y `getEffectProgress()`; `BallView.getPowerShotCountry()`, `isPowerShotCharging()` y `getPowerShotChargeProgress()`; eventos `onBallKicked`, `onPowerActivated`, `onPowerShot`, `onPowerHit` y `onProfileSaveFailed`, con sus pruebas.
  - **GUI separada en paneles:** `MenuPanel`, `SelectPlayersPanel`, `SelectCampPanel`, `UpgradesPanel` e `ImageAssets`; imagen del balón y banderas de los 4 países.
  - Nombres de los jugadores, pantalla de mejoras, puntos totales al terminar, filtro de extensión al guardar y abrir.
  - `MatchConfig` rechaza el **mismo nombre en los dos lados**; el error al guardar el perfil ahora **se avisa con `onProfileSaveFailed`** en vez de lanzar `IllegalStateException`.
  - Figuras más pequeñas: balón de radio 8, cabeza 14, pie 8, cuerpo 18 × 16, arcos de 30 px de ancho con travesaño de 5 px.
- **Sigue igual:** `ClumsyMachine` y `OffensiveMachine` devuelven un conjunto vacío (la máquina no se mueve con esos perfiles, y la Experta tampoco ataca); `COMController` sigue existiendo; los bugs 7 a 10 no se tocaron.

---

## Prioridad 1 — Interfaz gráfica · ≈ 5.5 – 7 h

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
- [ ] **Abrir una partida vieja pisa los puntos nuevos:** al terminar, cargar el perfil actual del repositorio y **sumarle solo los puntos de este partido**, en lugar de guardar el objeto completo que viene en el archivo.
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

### BUG-8 · El tiro de poder no termina si no golpea ni hace gol
- `fix8` agregó la carga del tiro (balón quieto en el aire), pero el tiro sigue sin tiempo máximo: si no golpea al rival ni termina en gol, queda activo, `ball.hasPowerShot()` sigue en `true` y **el jugador no puede volver a usar el poder** hasta el siguiente gol.
- **Arreglo:** que el fin del tiro (golpe, gol o un tiempo máximo de vuelo) desmarque el balón y llame `finishPowerShot()`.

### BUG-9 · Dos jugadores pegados, balón en medio, ambos patean: el balón traspasa a uno
- **Causa:** cada jugador resuelve su contacto por separado y en orden.
- **Arreglo:** si el balón toca a los dos en el mismo tick, que salga hacia arriba; y nunca dejarlo dentro de un cuerpo después de todas las correcciones.

### BUG-10 · Los balones falsos de la Ilusión no desaparecen si el tiro golpea al rival
- **Causa:** se actualizan solo mientras el tiro está activo, y el golpe termina el tiro.
- **Arreglo:** que la duración de la ilusión no dependa de que el tiro siga activo (un *hook* que `updateShot` llame en todos los ticks).

---

## Resumen

| Orden | Qué | Horas |
|---|---|---|
| 0 | Prueba que falla y archivos generados en el repositorio | 0.25 |
| 1 | **GUI** (1.1 detalles → 1.2 giro, arcos, caras → 1.3 animaciones → 1.4 sonidos) | 5.5 – 7 |
| 2 | Dominio: máquina (Cristian), sus pruebas, abrir partida vieja | 3 – 4 |
| 3 | Entregables: pruebas de aceptación, diagramas, retrospectiva | 3 – 4 |
| 4 | Bugs 7, 8, 9 y 10 | 2.5 – 3.5 |
| | **Total** | **≈ 14 – 19 h** |

**Sugerencia de reparto:** Juan, la GUI (1.2 y 1.3) y abrir partida vieja; Cristian, `OffensiveMachine` y `ClumsyMachine`, y después los sonidos. Si el BUG-10 molesta al dibujar la Ilusión, adelantarlo.

## Reglas de trabajo
1. La GUI solo usa la fachada, las vistas, los eventos, los `enum` y `DOPOSoccerException`. Si falta un dato, se agrega a la vista; nunca se busca dentro del dominio.
2. La API pública (fachada, vistas, eventos y `MatchConfig`) no cambia sin avisar.
3. Un commit por paso; push frecuente; **todas las pruebas en verde antes del push**.
4. El dominio no importa nada de `presentation` ni de `persistence`.
5. No subir archivos generados al jugar (`*.log`, `*.dat`, partidas guardadas).

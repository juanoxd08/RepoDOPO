# DOPOSoccer 2026-2 — Hoja de ruta

Código de partida: commit **`fix4`** (`2a8a8c0`).
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

## Estado en `fix4` (verificado)

- Compila; **32 de 32 pruebas pasan**.
- **Diseño, avances de `fix4`:**
  - **Efectos como Strategy:** interfaz `Effect` con una clase por efecto (`PushedEffect`, `ElectrocutedEffect`, `KnockedOutEffect`, `ConfusedEffect`, `FrozenEffect`, `NoneEffect`); `PlayerEffect` crea cada una (Factory). `EffectState` quedó en 35 líneas.
  - **Poderes con Template Method:** los pasos comunes de `Power` son `final` (cargar, +¼, activar aura, ejecutar el tiro, `onHitRival`, terminar el tiro).
  - **Acoplamiento cerrado (A1-A5):** `Match` ya no llama `getPower()` (usa `finishPowerShot`, `updatePowerShot` y `powerHitOn`); se borraron los getters sin uso y los duplicados de `Player`; `Match` crea los jugadores con `PlayerProfile` y `Player` pide el poder a su país; `Collidable` se eliminó.
  - Constantes de superficies en `Physics` (`HEAD_RESTITUTION`, `HEAD_IMPULSE`, `KICK_LIFT_FACTOR`, etc.).
- **Física, cambios de `fix4`:** gravedad 1.0, fricción 0.95 (el balón rueda 159 px desde 8 px/tick, igual hacia los dos lados), disparo más alto (`KICK_LIFT_FACTOR = −1.45`), y el piso ya no hace rebotar el balón si cae lento.
- **Bugs anteriores resueltos (verificado):**
  - ✅ Disputa en el piso: el balón **ya no baja del piso** (0 de 120 ticks; antes 16).
  - ✅ Autogol empujando con A: **se marca sin soltar la tecla** (en el tick 6; antes nunca).
  - ✅ Travesaño, choque entre jugadores: siguen bien (0 px de penetración).

---

## Bugs abiertos (probados con simulaciones sin GUI sobre `fix4`)

### BUG-4 · El cabezazo sale siempre a la misma velocidad — **confirmado**
- **Medición:** un balón que cae sobre la cabeza a 2, 6, 12 o 20 unidades/tick **sale siempre a 10.0**.
- **Causa:** en `Ball.bounceOff`, cuando hay impulso (`impulse > 0`), la velocidad se **reemplaza** por `normal × impulso` (`v = normal.scale(impulse)`). Se ignora por completo cómo venía el balón.
- **Arreglo:** reflejar primero la velocidad que traía (como cualquier rebote) y **sumarle** el impulso en la dirección de la normal: `v = reflejo(v) + normal × HEAD_IMPULSE`. Así un balón que llega rápido sale más rápido que uno que llega lento. Si se quiere un tope, limitar la rapidez final con una constante (`MAX_BALL_SPEED`).
- **Prueba nueva:** balón que cae a 4 y a 16 sobre la cabeza → el segundo sale más rápido que el primero.

### BUG-5 · El balón sale disparado al techo con toques suaves, y los lados no se comportan igual — **confirmado** (explica dos de los reportes)
- **Medición:**
  - Balón que cae junto al pie quieto del jugador **izquierdo**: **llega al techo en 3 de 63 casos**, con una velocidad hacia arriba de hasta **122.8**. Con el jugador **derecho**: 0 de 63, como máximo 5.2.
  - Jugador caminando contra un balón quieto en el piso: el **izquierdo** lo mueve a 0.9 unidades/tick como máximo y el **derecho** a 2.3. Esto es lo que se siente como "el izquierdo tiene distinta fricción": **la fricción del piso es igual para los dos lados** (159 px en cada dirección); lo que cambia es **el empujón**.
- **Causa:** el bloque nuevo de `Ball.bounceOff` para el balón en el piso:
  ```java
  if (onFloor && normal.getY() > 0) {
      if (normal.getX() > 0.01) {                 // ← solo mira x POSITIVO
          normal = new Vector2(normal.getX(), 0);  // ← vector sin normalizar (largo < 1)
      } else {
          normal = new Vector2(0, -3);             // ← largo 3, no 1
      }
  }
  ```
  1. **Asimetría:** `normal.getX() > 0.01` solo contempla las normales que apuntan a la derecha. Cuando apuntan a la izquierda, cae en el `else` aunque el contacto sea casi horizontal. Por eso cada lado se comporta distinto.
  2. **Normal de largo 3:** la normal debe medir 1. Con largo 3, el balón se saca **3 veces** la penetración, y en la reflexión el término `(v·n)·n` se multiplica por **9**. Un balón que cae a 4 sale hacia arriba a más de 50; por eso llega al techo.
  3. **Normal horizontal sin normalizar:** `(normal.x, 0)` mide menos de 1, así que el empujón hacia ese lado queda débil.
- **Arreglo:**
  ```java
  if (onFloor && normal.getY() > 0) {
      if (Math.abs(normal.getX()) > 0.01) {
          normal = new Vector2(Math.signum(normal.getX()), 0);   // horizontal y de largo 1
      } else {
          normal = new Vector2(0, -1);                            // hacia arriba y de largo 1
      }
  }
  ```
  Y una regla general: **toda normal que use `bounceOff` debe tener largo 1**. Conviene verificarlo con una prueba de `Contact` (o normalizar dentro de `Contact`).
- **Pruebas nuevas:**
  - Balón que cae junto al pie de cada jugador: nunca supera una velocidad hacia arriba razonable (por ejemplo, 15) y nunca toca el techo.
  - **Simetría:** cada prueba de física se corre para `LEFT` y para `RIGHT` con el escenario reflejado, y los resultados deben ser iguales (con el signo de x invertido).

### BUG-6 · Al patear un balón pegado al pie, a veces el balón traspasa al jugador — **ahora sí se reproduce**
- **Medición:** balón quieto delante del pie (96 posiciones): **en 3 casos el balón termina detrás del jugador** (`dx = 0` y `dx = 4`, con el balón a ras del piso). En `fix3` eran 0.
- **Causa probable:** cuando el balón está casi debajo del centro del pie, la normal del contacto pie–balón apunta **hacia abajo**. Al patear, `collideFootWithBall` saca el balón en esa dirección (contra el piso) y, en el mismo tick, la corrección del piso y el rebote contra el cuerpo lo empujan hacia atrás. Es el mismo tipo de problema que el BUG-5: **normales hacia abajo con el balón en el piso**.
- **Arreglo:** en la patada, si el balón está a la altura del pie, sacarlo en la dirección de la patada (`side.dir()`, horizontal) en lugar de la normal; y tratar `dx ≈ 0` como "delante del pie". Con el arreglo del BUG-5 puede que desaparezca sola; hay que volver a medir.
- **Prueba nueva:** balón quieto a 0-8 unidades delante del pie, a ras del piso, más patada → el balón nunca termina detrás del jugador.

---

## Pendientes de diseño que quedan

- **D1.** Código muerto: `FreezePower` y `FrozenEffect` (ningún país los usa), `COMState` (ya no se usa). Borrarlos, o dejar `FreezePower` si van a agregar un quinto personaje.
- **D2.** `Field.WIDTH`/`HEIGHT` siguen como constantes globales en 9 clases (`PlayerBody` 8 veces, `Ball` 7). Ver fase 2.4.
- **D3.** `Player.getPower()` sigue público y solo lo usan las pruebas. Las pruebas podrían usar los métodos de `Player` (o `PlayerView.getPowerProgress()`/`hasAura()`), y `getPower()` dejar de ser público.
- **D4.** Tamaño de clases: `Player` 261 líneas de código, `Match` 241, `PlayerBody` 206. Están cerca del límite; no crecerlas más (ver fase 2.5).

---

## Plan por fases

| Fase | Qué | Horas | Para |
|---|---|---|---|
| **1** | Bugs de física (BUG-4, 5 y 6) + pruebas de simetría | 1.5 – 2.5 | Ya |
| **2** | Diseño: lo que queda (2.4, 2.5 y código muerto) | 2 – 3 | V2 |
| **3** | Detalles de dominio que pide el PDF | 4 – 6 | V2 / V3 |
| **4** | Persistencia y log de errores | 4 – 5 | **V2** |
| **5** | Jugador contra máquina (perfiles) | 3 – 4 | **V2** |
| **6** | Animación de los poderes | 2 – 3 | V2 / V3 |
| **7** | Sensación de juego (lo que falte después de la fase 1) | 0.5 – 1 | Cuando haya tiempo |
| **8** | Pruebas, diagramas y entregables | 4 – 6 | V2 y V3 |
| **Total** | | **≈ 21 – 30 h** | |

---

### Fase 1 — Bugs de física (≈ 1.5 – 2.5 h) · *lo primero*
- [x] ~~BUG-1: disputa en el piso~~ (resuelto en `fix4`).
- [x] ~~BUG-2: autogol empujando~~ (resuelto en `fix4`).
- [ ] **BUG-5** primero (normales en el piso: `Math.abs`, largo 1). Es la causa de dos reportes y probablemente del BUG-6.
- [ ] **BUG-4**: cabezazo = reflejo + impulso.
- [ ] **BUG-6**: volver a medir después del BUG-5; si sigue, sacar el balón en la dirección de la patada.
- [ ] Pruebas nuevas en `DOPOSoccerPhysicsTest`: cabezazo con dos velocidades, balón que cae junto al pie (sin ir al techo), patada con el balón pegado al pie, disputa en el piso y autogol empujando (para que no vuelvan los bugs 1 y 2).
- [ ] **Pruebas de simetría:** cada escenario de física, para `LEFT` y para `RIGHT`.

**Listo cuando:** todas las pruebas de física pasan para los dos lados y, jugando, el balón no se dispara al techo con toques suaves.

---

### Fase 2 — Diseño excelente (≈ 6 – 8 h)

**2.1 Cerrar el acoplamiento (A1-A5)** · ✅ hecho en `fix4`

**2.2 Efectos como Strategy** · ✅ hecho en `fix4`
- `EffectState` hoy tiene un `enum` con `if`/`switch` por efecto. Pasar a una interfaz o clase abstracta **`Effect`** con `speedFactor()`, `canMove()`, `canJump()`, `canKick()`, `onStart(...)`, `onTick(...)` y una clase por efecto: `PushedEffect`, `ElectrocutedEffect`, `KnockedOutEffect`, `ConfusedEffect`.
- `EffectState` guarda un `Effect` y su duración; el efecto nuevo **reemplaza** al anterior (regla del PDF).
- Agregar un efecto = una clase nueva (requisito de extensión del PDF).

**2.3 Poderes con Template Method** · ✅ hecho en `fix4`
- `Power` → plantilla con `final` en los pasos fijos (barra, aura, ejecutar el tiro, producir el `PowerHit`) y métodos abstractos para lo propio de cada país: cómo sale el tiro (`shapeShot`), el daño base y el efecto que produce (`createEffect`).
- Cada país (`AuraAzulPower`, `LightningPower`, `TornadoPower`, `IllusionPower`) solo implementa lo suyo.

**2.4 Física y cancha sin constantes globales** · 🟡 parcial (las constantes de superficies ya están en `Physics`) · ≈ 1 h
- `Field.WIDTH`/`HEIGHT` se usan en muchas clases. Que `PlayerBody` y `Ball` reciban los límites de la cancha (un objeto `Field` con medidas) en vez de leer constantes estáticas.
- Constantes de superficies en `Physics` (restitución de cabeza, cuerpo, travesaño y pared; impulso del cabezazo), sin números sueltos en `PlayerBody`.
- `Ball` expone solo lo necesario; sus setters de velocidad, solo para el dominio.

**2.5 Revisión de cohesión** · ≈ 1 h
- Meta: ninguna clase del dominio con más de unas 200 líneas de código ni con responsabilidades mezcladas.
- [ ] Código muerto (D1) y `getPower()` público (D3).
- Revisar `Match` (241 líneas): si sigue creciendo, sacar la detección de colisiones a un `CollisionResolver` y la creación de jugadores y controles a una fábrica (`MatchFactory`).

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
- 🟡 J1 fricción (ya en 0.95) y J2 ángulo del disparo (ya en −1.45): **ajustados en `fix4`**; revisar jugando después de la fase 1.
- J3 pararse sobre la cabeza del rival (sigue resbalándose en unos 20 ticks): decidir.
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
2. **Fase 2** (lo que queda de diseño): rápido, antes de agregar funcionalidad.
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

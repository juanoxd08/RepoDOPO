# POOBSoccer — Plan de trabajo del dominio

Objetivo: dejar el paquete `domain` completo, robusto y probado, listo para conectar la GUI.
Orden pensado para construir sobre lo que ya existe: primero estabilizar, luego orquestar, luego reglas, luego COM.

Los IDs `B#` son bugs y `M#` son mejoras. Estimados en horas para alguien que conoce Java; incluyen pruebas básicas de cada punto.

---

## Fase 0 — Repo sano (≈ 1 h)

| # | Tarea | Resuelve | Horas |
|---|---|---|---|
| 0.1 | Subir `Player.java` y `Power.java` actuales (`getPower`, `usePower`, `isReady`) para que el repo compile | B2 | 0.25 |
| 0.2 | `.gitignore` con `bin/` y `git rm -r --cached bin` | B3, M25 | 0.25 |
| 0.3 | Borrar `module-info.java` o dejarlo limpio (cuando llegue la GUI: `requires java.desktop`) | M25 | 0.1 |
| 0.4 | Configurar JUnit 5 en el proyecto y crear el paquete `test` con una prueba trivial | M22 | 0.4 |

**Listo cuando:** se clona el repo, compila y corre `mvn/gradle/Eclipse test` en verde.

---

## Fase 1 — Base geométrica y constantes (≈ 5–7 h)

| # | Tarea | Resuelve | Horas |
|---|---|---|---|
| 1.1 | Clase `Physics`: `FPS`, `GRAVITY`, `RESTITUTION`, `CORNER_RESTITUTION`, `FRICTION`, `seconds(double)`; quitar las constantes duplicadas de `Ball` y `Player` | M5 | 0.5 |
| 1.2 | `Figure`: `x` e `y` como `double` y `private`, getters, `translate(dx, dy)` | M2, M4, B5 | 1 |
| 1.3 | Constructores de `Circle(x, y, radius)` y `Rectangle(x, y, w, h)`; renombrar `radious` → `radius` | B1, M1, M24 | 0.5 |
| 1.4 | Corregir `Rectangle.detectCollisionWithCircle` con el punto más cercano | B12 | 0.5 |
| 1.5 | Métodos de apoyo en `Rectangle`: `containsCircle(Circle)` (para el gol completo), `left()`, `right()`, `top()`, `bottom()` | B10 | 0.5 |
| 1.6 | Pruebas JUnit de geometría: círculo-círculo, esquinas círculo-rectángulo, rectángulo-rectángulo, `containsCircle` | M22 | 1.5–2 |
| 1.7 | Revisar todo el código y reemplazar los accesos directos `.x +=` por `translate` | M2 | 1–2 |

**Listo cuando:** ninguna clase toca `x`/`y` de otra directamente y todas las pruebas de geometría pasan.

---

## Fase 2 — Entidades robustas (≈ 9–13 h)

### 2A. Balón
| # | Tarea | Resuelve | Horas |
|---|---|---|---|
| 2.1 | Constructor `Ball(x, y, radius)`, `reset(x, y)` (velocidades en 0), `copy()` (lo usará la COM) | B1, M20 | 0.5 |
| 2.2 | `Ball.tick()` (renombrar `move`): integrar, gravedad, ajuste al piso, fricción bien calculada | B4, B5, B6 | 1 |
| 2.3 | `collideWithEdges`: reposicionar dentro del campo y forzar el signo con `Math.abs` | B8 | 0.5 |
| 2.4 | Proteger las divisiones por `distance == 0` (usar una normal por defecto) | B7 | 0.25 |
| 2.5 | Cabezazo con fuerza de las stats en vez del 50 fijo | B9 | 0.25 |
| 2.6 | Pruebas: el balón cae, rebota, se detiene, no atraviesa el piso ni las paredes | M22 | 1–1.5 |

### 2B. Partes del jugador y stats
| # | Tarea | Resuelve | Horas |
|---|---|---|---|
| 2.7 | `PlayerStats(speed, jump, strength)` 1–5 con validación (`POOBSoccerException`) y conversión a valores físicos (`speedPx()`, `jumpPx()`, `kickForce()`) | B26, M14, M21 | 1 |
| 2.8 | Crear `POOBSoccerException` con mensajes constantes | M21 | 0.25 |
| 2.9 | Constructores de `Head`, `Body` y `Foot`; renombrar `kicksRecieved` → `kicksReceived` | B1, M24 | 0.5 |
| 2.10 | `Foot`: el estado de patada con duración en ticks (`kickTicks`) en vez de toggles | B24 (base) | 0.5 |

### 2C. Jugador
| # | Tarea | Resuelve | Horas |
|---|---|---|---|
| 2.11 | Constructor `Player(name, country, side, stats, x)` que arma cabeza, cuerpo y pie con posiciones relativas | B1 | 1 |
| 2.12 | `Player.translate()` y simplificar `moveLeft/Right`, `dash`, `collideWithEdges`, `moveVertical`, `collideWithPlayer` | M3 | 1 |
| 2.13 | Clamp a los bordes después de cada movimiento | B16 | 0.25 |
| 2.14 | `stunTicks` en `Player`: `stun(seconds)`, `isStunned()`, se descuenta en `tick()`. Las 5 patadas → `stun(3)` | B14, M8 | 0.5 |
| 2.15 | `applyKnockback` (antes `isStruck`/`isNotStruck`) usando `Player.translate`, no solo la cabeza | B13 | 0.5 |
| 2.16 | Unificar `onTop`: una sola evaluación de "¿tengo soporte debajo?" por tick (suelo, cabeza rival, travesaño) y permitir saltar desde ahí | B17, B18 | 1 |
| 2.17 | `Player.tick()`: gravedad, aturdido, patada, poder | M7 | 0.5 |
| 2.18 | Pruebas: moverse, no salir del campo, saltar, aterrizar, aturdido de 3 s exactos en ticks | M22 | 1.5–2 |

**Listo cuando:** se puede crear un `Player` y un `Ball`, llamar `tick()` N veces y todo se comporta sin `null` ni valores raros.

---

## Fase 3 — Cancha, marcador y núcleo del partido (≈ 8–11 h)

| # | Tarea | Resuelve | Horas |
|---|---|---|---|
| 3.1 | `Goal(side, crossbar, goalArea)`: separar travesaño de zona de gol; `isGoal(Ball)` usa `containsCircle` | B10, B11, M17 | 1 |
| 3.2 | `Field` con constructor que crea los dos arcos según `WIDTH`/`HEIGHT`; `getGoal(Side)` | B1 | 0.5 |
| 3.3 | `ScoreBoard` con `int left, right`, `goalIn(Side goalSide)` (el punto va al lado contrario: autogol resuelto), `leader()` como `Optional<Side>` | B19, B20, M16 | 1 |
| 3.4 | Quitar `points` de `Player` | M16 | 0.25 |
| 3.5 | `GameStatus`: `NOT_STARTED`, `PLAYING`, `PAUSED`, `GOAL_SCORED`, `GOLDEN_GOAL`, `FINISHED` | M19 | 0.25 |
| 3.6 | `HeadSoccer`: constructor (jugadores, modo, controllers), `start()`, `pause()`, `resume()`, getters de solo lectura para la vista | M6 | 1 |
| 3.7 | `HeadSoccer.tick()`: orden fijo → inputs → jugadores → balón → colisiones → gol → modo | M6, M7 | 1.5 |
| 3.8 | `resolveCollisions()`: jugador-jugador, jugador-arco, balón-pie, balón-cabeza, balón-cuerpo, balón-travesaño, balón-bordes. Decidir: completar `Collidable` o eliminarla | M18 | 1.5–2 |
| 3.9 | Tras un gol: `GOAL_SCORED` con una pausa corta en ticks → `resetPositions()` → el balón cae del centro | M20 | 0.75 |
| 3.10 | Pruebas: gol completo vs. tocando la línea, autogol, reinicio, pausa congela todo | M22 | 1.5–2 |

**Listo cuando:** con dos jugadores quietos y un balón lanzado hacia un arco, `tick()` repetido produce el gol, suma al lado correcto y reinicia.

---

## Fase 4 — Controllers e input (≈ 4–5 h)

| # | Tarea | Resuelve | Horas |
|---|---|---|---|
| 4.1 | `PlayerInput` (left, right, jump, kick, power), inmutable o con `reset()` | M9 | 0.25 |
| 4.2 | Interfaz `Controller` (o `PlayerBrain`): `PlayerInput decide(HeadSoccer game, Player me)` | M9 | 0.25 |
| 4.3 | `HumanController` pasivo: `press(action)`, `release(action)`, devuelve el estado actual | M10 | 0.5 |
| 4.4 | `HeadSoccer.applyInput(player, input)`: filtra aturdido, invierte con Confusión, valida poder | M11, B25 | 1 |
| 4.5 | Patada como pulso: al presionar, se patea N ticks; no se re-patea manteniendo la tecla | B24 | 0.5 |
| 4.6 | `ScriptedController` para pruebas (devuelve una secuencia fija) | M22 | 0.5 |
| 4.7 | Pruebas: aturdido ignora input, confusión invierte, input del humano llega al jugador | M22 | 1–1.5 |

**Listo cuando:** `HeadSoccer` no distingue humano de máquina; solo pide `decide()` y aplica.

---

## Fase 5 — Poderes (≈ 7–10 h)

| # | Tarea | Resuelve | Horas |
|---|---|---|---|
| 5.1 | `Power` con template method: `charge`, `MAX`, `chargePerTick`, `isReady()`, `arm()` (se dispara al próximo golpe al balón), `tick()` que descuenta duración y llama `stop` | B22, M12 | 1.5 |
| 5.2 | Regla del PDF: el poder se activa **al golpear el balón**; si el balón impacta al rival, se aplica el efecto | — | 1 |
| 5.3 | Refactor de `FreezePower`: `dir` en vez de ramas duplicadas, duración en ticks; quitar la dependencia de `stopPower` manual | B15, M23 | 0.75 |
| 5.4 | `ShrinkPower` (Reducción): escala el radio de cabeza y pie y el tamaño del cuerpo, y lo restaura | — | 1.25 |
| 5.5 | `ConfusionPower`: activa la bandera de controles invertidos en el rival | — | 0.5 |
| 5.6 | `Country` con su `Supplier<Power>` y stats por defecto de cada personaje | M13 | 0.75 |
| 5.7 | `activePower()` / `powerProgress()` de solo lectura para la vista (el PDF exige mostrarlos) | — | 0.25 |
| 5.8 | Pruebas: la barra se carga en el tiempo esperado, no se usa sin llenarse, cada efecto dura y se revierte | M22 | 1.5–2 |

**Listo cuando:** cada país tiene su poder, la barra se carga con el tiempo y los efectos se quitan solos.

---

## Fase 6 — Tipos de partido (≈ 5–8 h)

| # | Tarea | Resuelve | Horas |
|---|---|---|---|
| 6.1 | `MatchMode` abstracto: `tick(game)`, `onGoal(side, game)`, `isOver(game)`, `winner(game)` | M15 | 0.5 |
| 6.2 | `TimeMatch`: 60 s en ticks; al acabar, si hay empate → `GOLDEN_GOAL`; el primer gol gana. Reemplaza a `MatchTimer` (o lo usa por dentro) | B21 | 1.25 |
| 6.3 | `GoalsMatch(3 \| 5 \| 7)`: valida el objetivo, gana quien llega primero | — | 0.5 |
| 6.4 | `SurvivalMatch`: balones como vidas, cada gol le quita un balón al rival; serie de partidos contra la COM, contador de partidos ganados. Solo PvsM (validar con excepción) | — | 2–3 |
| 6.5 | Fábrica o configuración de partido: `MatchConfig(modalidad, tipo, jugadores)` → crea `HeadSoccer` con controllers y modo correctos | — | 0.75 |
| 6.6 | Pruebas por modo: fin por tiempo, gol de oro, llegar a N, quedarse sin balones, supervivencia rechaza PvsP | M22 | 1–2 |

⚠️ Aclarar con el profe: el PDF dice "por tiempo" en la intro, pero no lo lista en "Tipos de partido", y las reglas dicen 60 s.

---

## Fase 7 — COM (≈ 8–12 h)

| # | Tarea | Resuelve | Horas |
|---|---|---|---|
| 7.1 | Eliminar `MachinePlayer`; crear `MachineController implements Controller` | M9, B25 | 0.5 |
| 7.2 | Percepción: hechos con nombre (`ballIsBehindMe`, `distToBall`, `ballThreatensMyGoal`, `rivalNear`, `powerReady`), escritos con `dir` para servir a los dos lados | M23 | 1.5 |
| 7.3 | Predicción con `ball.copy()` avanzado N ticks: punto de caída y posición futura | — | 1.5 |
| 7.4 | `enum AiState` (DEFEND, ATTACK, INTERCEPT, WAIT) + `chooseState` con histéresis | M10, B23 | 2 |
| 7.5 | Compromiso: sostener la patada y el salto N ticks; decidir cada `reactionTicks` | B24 | 0.5 |
| 7.6 | Uso del poder con intención (cuando va a golpear hacia el arco rival) | — | 0.5 |
| 7.7 | `Difficulty` (reacción, error aleatorio con `Random` inyectable, agresividad) | — | 0.75 |
| 7.8 | Pruebas por escena: balón detrás → DEFEND, balón adelante y cerca → ATTACK, nunca empuja hacia su arco | M22 | 1.5–2 |
| 7.9 | Simulación MvsM sin GUI (N partidos) para verificar que no se queda trabada ni hace autogoles en bucle | — | 1 |

---

## Fase 8 — Cierre y pulido (≈ 3–5 h)

| # | Tarea | Resuelve | Horas |
|---|---|---|---|
| 8.1 | Nombres: un solo idioma, `name`, `rival`, `applyKnockback`, `FRICTION` | M24 | 0.5 |
| 8.2 | Quitar código muerto: atributos sin uso de `PlayerStats` (`dash`, `survival`, `kick`, `power`) si no se usan, `Collidable` si se descartó | — | 0.5 |
| 8.3 | Javadoc en las clases públicas (lo piden en POOB) | — | 1–2 |
| 8.4 | Revisar la API que verá la GUI: solo lectura, sin exponer listas o figuras mutables | — | 0.5 |
| 8.5 | Diagrama de clases actualizado (Astah u otra herramienta) | — | 0.5–1 |
| 8.6 | Actualizar `PROJECT_STATE.md` | — | 0.25 |

---

## Resumen

| Fase | Horas |
|---|---|
| 0 — Repo sano | ≈ 1 |
| 1 — Geometría y constantes | 5 – 7 |
| 2 — Entidades | 9 – 13 |
| 3 — Núcleo del partido | 8 – 11 |
| 4 — Controllers e input | 4 – 5 |
| 5 — Poderes | 7 – 10 |
| 6 — Tipos de partido | 5 – 8 |
| 7 — COM | 8 – 12 |
| 8 — Cierre | 3 – 5 |
| **Total** | **≈ 50 – 72 h** |

Con dos personas a 10–15 h/semana cada una: **≈ 2.5 a 3.5 semanas**.

### Cómo repartir entre dos
- Fases 0–3: juntos o en pareja (todo depende de esto).
- Después de la Fase 4, en paralelo: **persona A** → Fases 5 y 6; **persona B** → Fase 7.
- Fase 8: los dos.

### Riesgos
- **Afinar la física** sin GUI es difícil; los números finales se ajustarán viendo el juego.
- **Supervivencia** es el punto más ambiguo del enunciado.
- **La COM** puede crecer sin límite; dense un tope de horas.

---

## Anexo — Referencias

### Mejoras (M)
1. Constructores en todas las clases.
2. `x`/`y` privados en `Figure` + `translate(dx, dy)`.
3. `Player.translate()`.
4. Posiciones y velocidades en `double`.
5. Clase `Physics` con las constantes.
6. `HeadSoccer.tick()` como único punto de avance.
7. Duraciones en ticks dentro del dominio.
8. `stunTicks` en vez de `boolean`.
9. `Controller` + `PlayerInput`; eliminar `MachinePlayer`.
10. `HumanController` pasivo, `MachineController` con `enum AiState`.
11. `applyInput()` centralizado.
12. `Power` con template method.
13. `Country` crea su `Power`.
14. `PlayerStats` velocidad, salto y fuerza 1–5.
15. `MatchMode` + `TimeMatch`, `GoalsMatch`, `SurvivalMatch`.
16. `ScoreBoard` por `Side` con `goalIn(Side)`.
17. `Goal` separa `crossbar` y `goalArea`.
18. Colisiones centralizadas.
19. `GameStatus` con `NOT_STARTED` y `GOAL_SCORED`.
20. `resetPositions()` después de cada gol.
21. `POOBSoccerException`.
22. Pruebas JUnit.
23. `dir` en vez de ramas por lado.
24. Nombres e idioma consistentes.
25. `.gitignore` y `module-info`.

### Bugs (B)
1. Sin constructores → `NullPointerException`.
2. El repo no compila (faltan `Player.java` y `Power.java`).
3. `bin/` versionado.
4. `Ball.java:84`: cast antes de multiplicar.
5. Velocidades < 1 se vuelven 0.
6. El balón atraviesa el piso.
7. División por cero → NaN.
8. El balón vibra pegado a la pared.
9. Cabezazo con fuerza fija de 50.
10. El gol solo mira el centro del balón.
11. Travesaño y zona de gol mezclados.
12. Choques falsos en las esquinas.
13. El knockback mueve solo la cabeza.
14. El aturdido no tiene duración.
15. El congelado nunca termina.
16. El jugador se sale del campo.
17. No puede saltar estando `onTop`.
18. `onTop` pisado entre métodos.
19. Referencias duplicadas y nulas en `ScoreBoard`.
20. `getWinner()` devuelve `null`.
21. `MatchTimer` desconectado y sin pausa.
22. La barra de poder no se carga.
23. La COM empuja el balón a su arco.
24. Patada que parpadea.
25. La COM se salta las reglas del input.
26. `PlayerStats` no coincide con el PDF.

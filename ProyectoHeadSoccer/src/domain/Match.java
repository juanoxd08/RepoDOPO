package domain;

import java.util.Set;

/**
 * Represents a soccer match session managing participants, field elements, physics resolution, game loop,
 * goal celebrations, timer, and win conditions according to DOPOSoccer.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class Match implements ReaderMatch {
    protected Player player1;
    protected Player player2;
    protected Controller controller1;
    protected Controller controller2;
    protected Field field;
    protected Ball ball;
    protected ScoreBoard scoreBoard;
    protected GameStatus state;
    protected MatchTimer timer;
    protected MatchType matchType;
    protected int targetGoals;

    protected int goalCelebrationTicks;
    protected String lastScorerText = "";
    protected String winnerNotification = "";
    protected boolean pointsAwarded = false;

    /**
     * Constructs a soccer match binding players, controllers, match type, and durations.
     * 
     * @param player1 First player instance
     * @param player2 Second player instance
     * @param controller1 Controller managing player 1
     * @param controller2 Controller managing player 2
     * @param matchType Scoring type (TIME or GOALS)
     * @param durationSeconds Match duration in seconds (for TIME)
     * @param targetGoals Goal limit to win (for GOALS)
     */
    public Match(Player player1, Player player2, Controller controller1, Controller controller2, 
                 MatchType matchType, int durationSeconds, int targetGoals) {
        this.player1 = player1;
        this.player2 = player2;
        this.controller1 = controller1;
        this.controller2 = controller2;
        this.matchType = (matchType != null) ? matchType : MatchType.TIME;
        this.targetGoals = (targetGoals > 0) ? targetGoals : 3;
        this.timer = new MatchTimer(durationSeconds > 0 ? durationSeconds : 60);

        this.field = new Field();
        this.ball = new Ball(Field.WIDTH / 2.0, 40.0, 10);
        this.scoreBoard = new ScoreBoard(player1, player2);
        this.state = GameStatus.PLAYING;
        this.goalCelebrationTicks = 0;
        this.pointsAwarded = false;

        resetPositions();
    }

    /**
     * Legacy constructor defaulting to standard 60-second timed match.
     */
    public Match(Player player1, Player player2, Controller controller1, Controller controller2) {
        this(player1, player2, controller1, controller2, MatchType.TIME, 60, 3);
    }

    /**
     * Resets player and ball positions back to kickoff spots, with ball dropping from center.
     */
    public void resetPositions() {
        if (ball != null) {
            ball.reset(Field.WIDTH / 2.0, 40.0);
        }
        if (player1 != null) {
            player1.resetPosition();
            if (player1.getPower() != null) {
                player1.getPower().finishPowerShot();
            }
        }
        if (player2 != null) {
            player2.resetPosition();
            if (player2.getPower() != null) {
                player2.getPower().finishPowerShot();
            }
        }
    }

    /**
     * Executes one frame tick of the match cycle.
     */
    public void tick() {
        // Manejo de celebración de gol
        if (state == GameStatus.GOAL_SCORED) {
            goalCelebrationTicks--;
            if (goalCelebrationTicks <= 0) {
                resetPositions();
                state = GameStatus.PLAYING;
            }
            return;
        }

        if (state != GameStatus.PLAYING && state != GameStatus.SUDDEN_DEATH) return;

        // Cuenta regresiva del temporizador si el partido es por tiempo
        if (matchType == MatchType.TIME && state == GameStatus.PLAYING) {
            timer.tick();
            if (timer.isTimeUp()) {
                if (scoreBoard.isTie()) {
                    // Empate: Muerte súbita / Gol de oro según el enunciado
                    state = GameStatus.SUDDEN_DEATH;
                    lastScorerText = "¡GOL DE ORO! MUERTE SÚBITA";
                } else {
                    finishMatch();
                    return;
                }
            }
        }

        // Consultar decisiones de los controladores
        if (controller1 != null) {
            Set<PlayerAction> a1 = controller1.decide(this);
            player1.applyInput(a1, ball, player2);
        }
        if (controller2 != null) {
            Set<PlayerAction> a2 = controller2.decide(this);
            player2.applyInput(a2, ball, player1);
        }

        // Avanzar estado de entidades
        player1.tick();
        player2.tick();
        ball.tick();

        // Actualizar clones si hay poder de Ilusión activo
        if (player1.getPower() instanceof IllusionPower) {
            ((IllusionPower) player1.getPower()).updateClones(ball);
        }
        if (player2.getPower() instanceof IllusionPower) {
            ((IllusionPower) player2.getPower()).updateClones(ball);
        }

        resolveCollisions();

        // Detección de goles
        if (field.getLeftGoal().detectGoal(ball)) {
            onGoal(Side.LEFT);
        } else if (field.getRightGoal().detectGoal(ball)) {
            onGoal(Side.RIGHT);
        }
    }

    /**
     * Resolves all physical collisions between entities, pitch boundaries, and goals.
     */
    public void resolveCollisions() {
        ball.collideWithEdges();
        ball.collideWithGB(field.getLeftGoal().getCrossBar());
        ball.collideWithGB(field.getRightGoal().getCrossBar());

        // Colisión del balón con cuerpo, cabeza y pie de Jugador 1
        ball.collideWithGB(player1.getBody().getRectangle());
        ball.collideWithHead(player1.getHead());
        player1.collideMyFootWithBall(ball);

        // Colisión del balón con cuerpo, cabeza y pie de Jugador 2
        ball.collideWithGB(player2.getBody().getRectangle());
        ball.collideWithHead(player2.getHead());
        player2.collideMyFootWithBall(ball);

        player1.collideWithEdges();
        player2.collideWithEdges();

        player1.collideWithGoal(field.getLeftGoal());
        player1.collideWithGoal(field.getRightGoal());
        player2.collideWithGoal(field.getLeftGoal());
        player2.collideWithGoal(field.getRightGoal());

        player1.collideWithPlayer(player2);
        player2.collideWithPlayer(player1);

        // Interacción de tiros de poder activos con el oponente
        if (player1.getPower() != null && player1.getPower().isPowerShotActive()) {
            if (ball.getCircle().detectCollisionWithCircle(player2.getHead().getCircle()) ||
                ball.getCircle().detectCollisionWithRectangle(player2.getBody().getRectangle()) ||
                ball.getCircle().detectCollisionWithCircle(player2.getFoot().getCircle())) {
                player1.getPower().onHitRival(player2, player1);
            }
        }

        if (player2.getPower() != null && player2.getPower().isPowerShotActive()) {
            if (ball.getCircle().detectCollisionWithCircle(player1.getHead().getCircle()) ||
                ball.getCircle().detectCollisionWithRectangle(player1.getBody().getRectangle()) ||
                ball.getCircle().detectCollisionWithCircle(player1.getFoot().getCircle())) {
                player2.getPower().onHitRival(player1, player2);
            }
        }
    }

    /**
     * Handles goal events by awarding points, updating power bars, and transitioning states.
     * 
     * @param goalSide Side of the goal where the ball entered
     */
    public void onGoal(Side goalSide) {
        scoreBoard.goalIn(goalSide);

        // Regla: Cuando un jugador recibe un gol, su barra aumenta de inmediato un cuarto (1/4)
        if (goalSide == Side.LEFT) {
            // Gol para Player 2 (arco izquierdo defendido por Player 1)
            player1.onGoalReceived();
            lastScorerText = "¡GOL DE " + player2.getName().toUpperCase() + "!";
        } else {
            // Gol para Player 1 (arco derecho defendido por Player 2)
            player2.onGoalReceived();
            lastScorerText = "¡GOL DE " + player1.getName().toUpperCase() + "!";
        }

        // Si era Gol de Oro (muerte súbita), el partido termina de inmediato
        if (state == GameStatus.SUDDEN_DEATH) {
            finishMatch();
            return;
        }

        // Si el partido es por goles y se alcanzó la meta
        if (matchType == MatchType.GOALS) {
            int p1Score = scoreBoard.getPoints()[0];
            int p2Score = scoreBoard.getPoints()[1];
            if (p1Score >= targetGoals || p2Score >= targetGoals) {
                finishMatch();
                return;
            }
        }

        // Transición a estado de celebración GOAL
        this.state = GameStatus.GOAL_SCORED;
        this.goalCelebrationTicks = 85; // ~1.4 segundos de animación visual
    }

    private void finishMatch() {
        this.state = GameStatus.FINISHED;
        awardMatchEndPoints();

        Player winner = scoreBoard.getWinner();
        if (winner != null) {
            this.winnerNotification = "¡GANADOR: " + winner.getName().toUpperCase() + "! (+100 PUNTOS)";
        } else {
            this.winnerNotification = "¡EMPATE TÉCNICO!";
        }
    }

    /**
     * Awards 100 points to winner and 25 points to loser according to rules.
     */
    private void awardMatchEndPoints() {
        if (pointsAwarded) return;
        pointsAwarded = true;

        Player winner = scoreBoard.getWinner();
        if (winner == player1) {
            player1.addPoints(100);
            player2.addPoints(25);
        } else if (winner == player2) {
            player2.addPoints(100);
            player1.addPoints(25);
        } else {
            player1.addPoints(25);
            player2.addPoints(25);
        }
    }

    @Override public Player getPlayer1() { return player1; }
    @Override public Player getPlayer2() { return player2; }
    public Controller getController1() { return controller1; }
    public void setController1(Controller controller1) { this.controller1 = controller1; }
    public Controller getController2() { return controller2; }
    public void setController2(Controller controller2) { this.controller2 = controller2; }
    @Override public Field getField() { return field; }
    @Override public Ball getBall() { return ball; }
    @Override public ScoreBoard getScoreBoard() { return scoreBoard; }
    @Override public GameStatus getState() { return state; }
    public void setState(GameStatus state) { this.state = state; }
    @Override public MatchTimer getTimer() { return timer; }
    @Override public MatchType getMatchType() { return matchType; }
    @Override public int getTargetGoals() { return targetGoals; }
    public String getLastScorerText() { return lastScorerText; }
    public String getWinnerNotification() { return winnerNotification; }
    public int getGoalCelebrationTicks() { return goalCelebrationTicks; }
}

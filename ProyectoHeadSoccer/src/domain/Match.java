package domain;

import java.util.Set;

public class Match implements ReaderMatch {
    protected Player player1;
    protected Player player2;
    protected Controller controller1;
    protected Controller controller2;
    protected Field field;
    protected Ball ball;
    protected ScoreBoard scoreBoard;
    protected GameStatus state;

    public Match(Player player1, Player player2, Controller controller1, Controller controller2) {
        this.player1 = player1;
        this.player2 = player2;
        this.controller1 = controller1;
        this.controller2 = controller2;
        this.field = new Field();
        this.ball = new Ball(Field.WIDTH / 2.0, Field.HEIGHT / 2.0, 10);
        this.scoreBoard = new ScoreBoard(player1, player2);
        this.state = GameStatus.PLAYING;
        resetPositions();
    }

    public void resetPositions() {
        if (ball != null) {
            ball.reset(Field.WIDTH / 2.0, Field.HEIGHT / 2.0);
        }
        if (player1 != null) {
            player1.resetPosition();
        }
        if (player2 != null) {
            player2.resetPosition();
        }
    }

    @Override
    public Player getPlayer1() {
        return player1;
    }

    @Override
    public Player getPlayer2() {
        return player2;
    }

    public Controller getController1() {
        return controller1;
    }

    public void setController1(Controller controller1) {
        this.controller1 = controller1;
    }

    public Controller getController2() {
        return controller2;
    }

    public void setController2(Controller controller2) {
        this.controller2 = controller2;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public Ball getBall() {
        return ball;
    }

    @Override
    public ScoreBoard getScoreBoard() {
        return scoreBoard;
    }

    @Override
    public GameStatus getState() {
        return state;
    }

    public void setState(GameStatus state) {
        this.state = state;
    }

    public void resolveCollisions() {
        // Balon con bordes y arcos
        ball.collideWithEdges();
        ball.collideWithGB(field.getLeftGoal().getCrossBar());
        ball.collideWithGB(field.getRightGoal().getCrossBar());

        // Balon con cuerpos, pies y cabezas
        ball.collideWithGB(player1.getBody().getRectangle());
        ball.collideWithGB(player2.getBody().getRectangle());
        ball.collideWithFoot(player1.getFoot());
        ball.collideWithFoot(player2.getFoot());
        ball.collideWithHead(player1.getHead());
        ball.collideWithHead(player2.getHead());

        // Jugadores con balon
        player1.collideMyFootWithBall(ball);
        player2.collideMyFootWithBall(ball);

        // Jugadores con bordes
        player1.collideWithEdges();
        player2.collideWithEdges();

        // Patadas a la cabeza
        player1.getHead().collideWithFoot(player2.getFoot());
        player2.getHead().collideWithFoot(player1.getFoot());

        // Jugadores con arcos
        player1.collideWithGoal(field.getLeftGoal());
        player1.collideWithGoal(field.getRightGoal());
        player2.collideWithGoal(field.getLeftGoal());
        player2.collideWithGoal(field.getRightGoal());

        // Jugadores entre si
        player1.collideWithPlayer(player2);
        player2.collideWithPlayer(player1);
    }

    public void onGoal(Side goalSide) {
        scoreBoard.goalIn(goalSide);
        resetPositions();
    }

    public void tick() {
        if (state != GameStatus.PLAYING && state != GameStatus.GOLDEN_GOAL) return;

        // 1. Decidir acciones de los controladores y aplicarlas
        if (controller1 != null) {
            Set<PlayerAction> a1 = controller1.decide(this);
            player1.applyInput(a1, ball, player2);
        }
        if (controller2 != null) {
            Set<PlayerAction> a2 = controller2.decide(this);
            player2.applyInput(a2, ball, player1);
        }

        // 2. Avanzar fisicas
        player1.tick();
        player2.tick();
        ball.tick();

        // 3. Resolver colisiones
        resolveCollisions();

        // 4. Verificar si hubo gol
        if (field.getLeftGoal().detectGoal(ball)) {
            onGoal(Side.LEFT);
        } else if (field.getRightGoal().detectGoal(ball)) {
            onGoal(Side.RIGHT);
        }
    }
}

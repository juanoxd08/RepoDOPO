package domain;

import java.util.Set;

/**
 * Represents a soccer match session managing participants, field elements, physics resolution, and game loop.
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

    /**
     * Constructs a soccer match binding players and their respective controllers.
     * 
     * @param player1 First player instance
     * @param player2 Second player instance
     * @param controller1 Controller managing player 1
     * @param controller2 Controller managing player 2
     */
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

    /**
     * Resets player and ball positions back to kickoff spots.
     */
    public void resetPositions() {
        if (ball != null) {
            ball.reset(Field.WIDTH / 2.0, Field.HEIGHT / 2.0);
        }
        if (player1 != null) {
            player1.resetPosition();
            if (player1.getPower() != null) {
                player1.getPower().setActive(false);
            }
        }
        if (player2 != null) {
            player2.resetPosition();
            if (player2.getPower() != null) {
                player2.getPower().setActive(false);
            }
        }
    }

    /**
     * Returns player 1.
     * 
     * @return Player 1 instance
     */
    @Override
    public Player getPlayer1() {
        return player1;
    }

    /**
     * Returns player 2.
     * 
     * @return Player 2 instance
     */
    @Override
    public Player getPlayer2() {
        return player2;
    }

    /**
     * Returns the controller of player 1.
     * 
     * @return Controller 1 instance
     */
    public Controller getController1() {
        return controller1;
    }

    /**
     * Assigns the controller of player 1.
     * 
     * @param controller1 Controller instance to assign
     */
    public void setController1(Controller controller1) {
        this.controller1 = controller1;
    }

    /**
     * Returns the controller of player 2.
     * 
     * @return Controller 2 instance
     */
    public Controller getController2() {
        return controller2;
    }

    /**
     * Assigns the controller of player 2.
     * 
     * @param controller2 Controller instance to assign
     */
    public void setController2(Controller controller2) {
        this.controller2 = controller2;
    }

    /**
     * Returns the match field.
     * 
     * @return Field instance
     */
    @Override
    public Field getField() {
        return field;
    }

    /**
     * Returns the match ball.
     * 
     * @return Ball instance
     */
    @Override
    public Ball getBall() {
        return ball;
    }

    /**
     * Returns the match scoreboard.
     * 
     * @return ScoreBoard instance
     */
    @Override
    public ScoreBoard getScoreBoard() {
        return scoreBoard;
    }

    /**
     * Returns the current game status.
     * 
     * @return Active GameStatus
     */
    @Override
    public GameStatus getState() {
        return state;
    }

    /**
     * Sets the match game status.
     * 
     * @param state New GameStatus to apply
     */
    public void setState(GameStatus state) {
        this.state = state;
    }

    /**
     * Resolves all physical collisions between entities, pitch boundaries, and goals.
     */
    public void resolveCollisions() {
        ball.collideWithEdges();
        ball.collideWithGB(field.getLeftGoal().getCrossBar());
        ball.collideWithGB(field.getRightGoal().getCrossBar());

        if (player1.getPower() == null || !player1.getPower().isActive()) {
            ball.collideWithGB(player1.getBody().getRectangle());
            ball.collideWithFoot(player1.getFoot());
            ball.collideWithHead(player1.getHead());
            player1.collideMyFootWithBall(ball);
        }

        if (player2.getPower() == null || !player2.getPower().isActive()) {
            ball.collideWithGB(player2.getBody().getRectangle());
            ball.collideWithFoot(player2.getFoot());
            ball.collideWithHead(player2.getHead());
            player2.collideMyFootWithBall(ball);
        }

        player1.collideWithEdges();
        player2.collideWithEdges();

        player1.getHead().collideWithFoot(player2.getFoot());
        player2.getHead().collideWithFoot(player1.getFoot());

        player1.collideWithGoal(field.getLeftGoal());
        player1.collideWithGoal(field.getRightGoal());
        player2.collideWithGoal(field.getLeftGoal());
        player2.collideWithGoal(field.getRightGoal());

        player1.collideWithPlayer(player2);
        player2.collideWithPlayer(player1);

        if (player1.getPower() != null && player1.getPower().isActive()) {
            if (ball.getCircle().detectCollisionWithCircle(player2.getHead().getCircle()) ||
                ball.getCircle().detectCollisionWithRectangle(player2.getBody().getRectangle()) ||
                ball.getCircle().detectCollisionWithCircle(player2.getFoot().getCircle())) {
                player1.getPower().applyPower(ball, player2);
                player1.getPower().setActive(false);
            }
        }

        if (player2.getPower() != null && player2.getPower().isActive()) {
            if (ball.getCircle().detectCollisionWithCircle(player1.getHead().getCircle()) ||
                ball.getCircle().detectCollisionWithRectangle(player1.getBody().getRectangle()) ||
                ball.getCircle().detectCollisionWithCircle(player1.getFoot().getCircle())) {
                player2.getPower().applyPower(ball, player1);
                player2.getPower().setActive(false);
            }
        }
    }

    /**
     * Handles goal events by awarding points and resetting kickoff positions.
     * 
     * @param goalSide Side of the goal where the ball entered
     */
    public void onGoal(Side goalSide) {
        scoreBoard.goalIn(goalSide);
        resetPositions();
    }

    /**
     * Executes one frame tick of the match cycle, querying inputs, advancing entities, and checking goals.
     */
    public void tick() {
        if (state != GameStatus.PLAYING && state != GameStatus.GOLDEN_GOAL) return;

        if (controller1 != null) {
            Set<PlayerAction> a1 = controller1.decide(this);
            player1.applyInput(a1, ball, player2);
        }
        if (controller2 != null) {
            Set<PlayerAction> a2 = controller2.decide(this);
            player2.applyInput(a2, ball, player1);
        }

        player1.tick();
        player2.tick();
        ball.tick();

        resolveCollisions();

        if (field.getLeftGoal().detectGoal(ball)) {
            onGoal(Side.LEFT);
        } else if (field.getRightGoal().detectGoal(ball)) {
            onGoal(Side.RIGHT);
        }
    }
}

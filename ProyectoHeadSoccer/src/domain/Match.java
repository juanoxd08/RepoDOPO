package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a soccer match session managing participants, field elements, physics resolution, game loop,
 * goal celebrations, timer, and win conditions according to DOPOSoccer.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class Match implements MatchView {
    protected Player player1;
    protected Player player2;
    protected Controller controller1;
    protected Controller controller2;
    protected Field field;
    protected Ball ball;
    protected ScoreBoard scoreBoard;
    protected GameStatus state;
    private GameStatus stateBeforePause;
    protected MatchRule rule;
    protected List<MatchListener>  listeners = new ArrayList<>();
  

    protected int goalCelebrationTicks;
    protected boolean pointsAwarded = false;

    /**
     * Constructs a soccer match from a validated MatchConfig.
     *
     * @param config Validated match configuration
     * @throws DOPOSoccerException if configuration or rule initialization fails
     */
    public Match(MatchConfig config) throws DOPOSoccerException{
        this.ball = new Ball();
        this.field = new Field();
        this.scoreBoard = new ScoreBoard();
        this.player1 = new Player(new PlayerProfile("jugador 1"), config.getLeftCountry(), Side.LEFT);
        this.player2 = new Player(new PlayerProfile("jugador 2"), config.getRightCountry(), Side.RIGHT);
        this.controller1 = new HumanController();
        this.controller2 = (config.getModality() == Modality.PVC) ? new COMController(Side.RIGHT) : new HumanController();
        this.rule = (config.getMatchType() == MatchType.GOALS) ? new GoalRule(config.getTargetValue()) : new TimeRule(config.getTargetValue());
        this.state = GameStatus.PLAYING;
        this.stateBeforePause = GameStatus.NOT_STARTED;
        this.goalCelebrationTicks = 0;
        this.pointsAwarded = false;
        resetPositions();
    }


    /**
     * Resets player and ball positions back to kickoff spots, with ball dropping from center.
     */
    public void resetPositions() {
        if (ball != null) {
            ball.reset(Field.WIDTH / 2.0, 40.0);
            ball.setPowerShot(false);
        }
        if (player1 != null) {
            player1.resetPosition();
            player1.finishPowerShot();
        }
        if (player2 != null) {
            player2.resetPosition();
            player2.finishPowerShot();
        }
    }

    /**
     * Executes one frame tick of the match cycle.
     */
    public void tick() {
        if (state == GameStatus.GOAL_SCORED) {
            goalCelebrationTicks--;
            if (goalCelebrationTicks <= 0) {
                resetPositions();
                state = GameStatus.PLAYING;
            }
            return;
        }

        if (state != GameStatus.PLAYING && state != GameStatus.SUDDEN_DEATH) return;
        rule.onTick(this);
        if (rule.isOver(this)) {
            finishMatch();
            return;
        }

       
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
        
        player1.updatePowerShot(ball);
        player2.updatePowerShot(ball);

        boolean wasStunned1 = player1.isStunned();
        boolean wasStunned2 = player2.isStunned();

        resolveCollisions();

        if (!wasStunned1 && player1.isStunned()) {
            for (MatchListener listener : listeners) listener.onStunned(Side.LEFT);
        }
        if (!wasStunned2 && player2.isStunned()) {
            for (MatchListener listener : listeners) listener.onStunned(Side.RIGHT);
        }

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
        player1.clearSupport();
        player2.clearSupport();





        player1.collideWithEdges();
        player2.collideWithEdges();

        ball.collideWithEdges();
        ball.bounceOff(field.getLeftGoal().getCrossBar(), Physics.CROSSBAR_RESTITUTION, 0.0);
        ball.bounceOff(field.getRightGoal().getCrossBar(), Physics.CROSSBAR_RESTITUTION, 0.0);



        player1.collideWithGoal(field.getLeftGoal());
        player1.collideWithGoal(field.getRightGoal());
        player2.collideWithGoal(field.getLeftGoal());
        player2.collideWithGoal(field.getRightGoal());

        player1.collideWithPlayer(player2);
        player2.collideWithPlayer(player1);

        player1.collideWithBall(ball);
        player2.collideWithBall(ball);


        player2.receiveHit(player1.powerHitOn(player2, ball));
        player1.receiveHit(player2.powerHitOn(player1, ball));
    }

    /**
     * Handles goal events by awarding points, updating power bars, and transitioning states.
     * 
     * @param goalSide Side of the goal where the ball entered
     */
    public void onGoal(Side goalSide) {
        scoreBoard.goalIn(goalSide);

        if (goalSide == Side.LEFT) {
            player1.onGoalReceived();
            for(MatchListener listener: listeners) listener.onGoal(goalSide.opposite());
        } else {

            player2.onGoalReceived();
            for(MatchListener listener: listeners) listener.onGoal(goalSide.opposite());
        }

        
        if (state == GameStatus.SUDDEN_DEATH) {
            finishMatch();
            return;
        }

        

        if (rule.isOver(this)) {
            finishMatch();
            return;
        }

        this.state = GameStatus.GOAL_SCORED;
        this.goalCelebrationTicks = 85; 
    }

    private void finishMatch() {
        if (pointsAwarded) return;
        pointsAwarded = true;
        Side winSide;
        this.state = GameStatus.FINISHED;
        

        Optional<Side> winner = scoreBoard.getLeader();
        if(winner.isPresent()){
            winSide = winner.get();
            Side loseSide = winSide.opposite();
            if(player1.getSide() == winSide)  {
                player1.addPoints(100);
                player2.addPoints(25);
            }
             else {
                player2.addPoints(100);
                player1.addPoints(25);
            }
            for(MatchListener listener: listeners) listener.onMatchFinished(winSide, 100, 25);
        }else{
            player1.addPoints(25);
            player2.addPoints(25);
            for(MatchListener listener: listeners) listener.onMatchFinished(null, 25, 25);
        }

        
        
    }

    public void pause(){
        if(state != GameStatus.NOT_STARTED &&
        state != GameStatus.PAUSED &&
        state != GameStatus.FINISHED
        ){
            stateBeforePause = state;
            state = GameStatus.PAUSED;
        }
    }

    public void resume(){
        if(state == GameStatus.PAUSED){
            state = (stateBeforePause != null) ? stateBeforePause : GameStatus.PLAYING;
            stateBeforePause = null;
        }
    }

    public void finish(){
        finishMatch();
    }

    public void setIntention(Side side, Set<PlayerAction> actions) {
        if (side == Side.LEFT && controller1 != null) {
            controller1.receive(actions);
        } else if (side == Side.RIGHT && controller2 != null) {
            controller2.receive(actions);
        }
    }

    public void addListener(MatchListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(MatchListener listener) {
        listeners.remove(listener);
    }




    @Override
    public GameStatus getStatus() {
        return state;
    }

    @Override
    public int getRemainingSeconds() {
        return rule.remainingSeconds(this);
    }

    @Override
    public boolean isSuddenDeath() {
        return state == GameStatus.SUDDEN_DEATH;
    }

    @Override
    public int getGoals(Side side) {
        return scoreBoard.getGoals(side);
    }

    @Override
    public PlayerView getPlayer(Side side) {
        return (side == Side.LEFT) ? player1 : player2;
    }

    @Override
    public BallView getBall() {
        return ball;
    }

    @Override
    public Optional<Side> getWinner() {
        return rule.winner(this);
    }

    @Override
    public int getFieldWidth() {
        return Field.WIDTH;
    }

    @Override
    public int getFieldHeight() {
        return Field.HEIGHT;
    }

    @Override
    public Rectangle getGoalArea(Side side) {
        Rectangle area = field.getGoal(side).getGoalArea();
        return new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    }

    @Override
    public Rectangle getCrossBar(Side side) {
        Rectangle bar = field.getGoal(side).getCrossBar();
        return new Rectangle(bar.getX(), bar.getY(), bar.getWidth(), bar.getHeight());
    }


    public ScoreBoard getScoreBoard() { return scoreBoard; }
    public void setState(GameStatus state) { this.state = state; }
    public int getGoalCelebrationTicks() { return goalCelebrationTicks; }
    public Controller getController1() { return controller1; }
    public void setController1(Controller controller1) { this.controller1 = controller1; }
    public Controller getController2() { return controller2; }
    public void setController2(Controller controller2) { this.controller2 = controller2; }
}

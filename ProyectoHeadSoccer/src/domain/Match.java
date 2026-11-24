package domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
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
public class Match implements MatchView, Serializable {
    private static final long serialVersionUID = 1L;
    private static final double CROSSBAR_ROLL_SPEED = 1.5;
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
    protected transient List<MatchListener> listeners = new ArrayList<>();
    private final Modality modality;
  

    protected int goalCelebrationTicks;
    protected boolean pointsAwarded = false;

    /**
     * Constructs a soccer match from a validated MatchConfig.
     *
     * @param config Validated match configuration
     * @throws DOPOSoccerException if configuration or rule initialization fails
     */
    public Match(MatchConfig config) throws DOPOSoccerException{
        this(config, null, null);
     
    }

    /**
     * Constructs a soccer match from a validated MatchConfig and preloaded player profiles.
     *
     * @param config Validated match configuration
     * @param profile1 Preloaded left player profile, or null to create a new default one
     * @param profile2 Preloaded right player profile, or null to create a new default one
     * @throws DOPOSoccerException if configuration or rule initialization fails
     */
    public Match(MatchConfig config, PlayerProfile profile1, PlayerProfile profile2) throws DOPOSoccerException{
        this.ball = new Ball();
        this.field = new Field();
        this.scoreBoard = new ScoreBoard();
        PlayerProfile p1 = (profile1 != null) ? profile1 : new PlayerProfile(config.getLeftPlayerName());
        PlayerProfile p2 = (profile2 != null) ? profile2 : new PlayerProfile(config.getRightPlayerName());
        this.player1 = new Player(p1, config.getLeftCountry(), Side.LEFT);
        this.player2 = new Player(p2, config.getRightCountry(), Side.RIGHT);
        this.controller1 = new HumanController();
        this.controller2 = (config.getModality() == Modality.PVC) ? config.getMachineProfile().create(Side.RIGHT) : new HumanController();
        this.rule = (config.getMatchType() == MatchType.GOALS) ? new GoalRule(config.getTargetValue()) : new TimeRule(config.getTargetValue());
        this.state = GameStatus.PLAYING;
        this.stateBeforePause = GameStatus.NOT_STARTED;
        this.goalCelebrationTicks = 0;
        this.pointsAwarded = false;
        this.modality = config.getModality();

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

        boolean hadAura1 = player1.hasAura();
        boolean hadAura2 = player2.hasAura();
        if (controller1 != null) {
            Set<PlayerAction> a1 = controller1.decide(this);
            player1.applyInput(a1, ball, player2);
            if(!hadAura1 && player1.hasAura()) {
                for(MatchListener listener : listeners) listener.onPowerActivated(player1.getSide());
            }
            
        }
        if (controller2 != null) {
            Set<PlayerAction> a2 = controller2.decide(this);
            player2.applyInput(a2, ball, player1);
            if(!hadAura2 && player2.hasAura()) {
                for(MatchListener listener : listeners) listener.onPowerActivated(player2.getSide());
            }
            
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

        boolean hadAura1 = player1.hasAura();
        boolean hadAura2 = player2.hasAura();



        player1.collideWithEdges();
        player2.collideWithEdges();

        ball.collideWithEdges();
        ball.bounceOff(field.getLeftGoal().getCrossBar(), Physics.CROSSBAR_RESTITUTION, 0.0);
        ball.bounceOff(field.getRightGoal().getCrossBar(), Physics.CROSSBAR_RESTITUTION, 0.0);
        rollOffCrossBar(field.getLeftGoal().getCrossBar(), 1.0);
        rollOffCrossBar(field.getRightGoal().getCrossBar(), -1.0);



        player1.collideWithGoal(field.getLeftGoal());
        player1.collideWithGoal(field.getRightGoal());
        player2.collideWithGoal(field.getLeftGoal());
        player2.collideWithGoal(field.getRightGoal());

        player1.collideWithPlayer(player2);
        player2.collideWithPlayer(player1);

        PowerHit hit1 = player1.powerHitOn(player2, ball);
        if (hit1 != null) {
            player1.finishPowerShot();
            ball.setPowerShot(false);
            player2.receiveHit(hit1);
            for (MatchListener listener : listeners) {
                listener.onPowerHit(player2.getSide(), hit1.getDamage());
            }
        }

        PowerHit hit2 = player2.powerHitOn(player1, ball);
        if (hit2 != null) {
            player2.finishPowerShot();
            ball.setPowerShot(false);
            player1.receiveHit(hit2);
            for (MatchListener listener : listeners) {
                listener.onPowerHit(player1.getSide(), hit2.getDamage());
            }
        }

        if(player1.collideWithBall(ball)){
            for(MatchListener listener : listeners) listener.onBallKicked(player1.getSide());
        }
        if(player2.collideWithBall(ball)){
            for(MatchListener listener : listeners) listener.onBallKicked(player2.getSide());
        }

        if(hadAura1 && !player1.hasAura()){
            for(MatchListener listener : listeners){
                listener.onPowerShot(player1.getSide(), player1.getCountry());
            }
        }

        if(hadAura2 && !player2.hasAura()){
            for(MatchListener listener : listeners){
                listener.onPowerShot(player2.getSide(), player2.getCountry());
            }
        }


    }

    /**
     * Gives a ball resting on top of a crossbar a small push toward the pitch, so it never stays there forever.
     *
     * @param bar Crossbar the ball may be resting on
     * @param towardField Horizontal direction of the pitch from that crossbar (1 or -1)
     */
    private void rollOffCrossBar(Rectangle bar, double towardField) {
        double bottom = ball.getY() + ball.getRadius();
        boolean onTop = Math.abs(bottom - bar.getY()) <= 1.0
                && ball.getX() >= bar.getX() && ball.getX() <= bar.getX() + bar.getWidth();
        if (onTop && Math.abs(ball.getHorizontalSpeed()) < CROSSBAR_ROLL_SPEED) {
            ball.setHorizontalSpeed(towardField * CROSSBAR_ROLL_SPEED);
        }
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

    /*
     * Transitions the match to FINISHED, awards end-of-match points once, and notifies listeners.
     */
    private void finishMatch() {
        if (pointsAwarded) return;
        pointsAwarded = true;
        Side winSide;
        this.state = GameStatus.FINISHED;

        Optional<Side> winner = scoreBoard.getLeader();
        if (winner.isPresent()) {
            winSide = winner.get();
            if (player1.getSide() == winSide) {
                player1.addPoints(100);
                player2.addPoints(25);
            } else {
                player2.addPoints(100);
                player1.addPoints(25);
            }
            for (MatchListener listener : listeners) listener.onMatchFinished(winSide, 100, 25);
        } else {
            player1.addPoints(25);
            player2.addPoints(25);
            for (MatchListener listener : listeners) listener.onMatchFinished(null, 25, 25);
        }
    }

    /**
     * Pauses the match if it is currently active and remembers the prior state.
     */
    public void pause() {
        if (state != GameStatus.NOT_STARTED &&
                state != GameStatus.PAUSED &&
                state != GameStatus.FINISHED) {
            stateBeforePause = state;
            state = GameStatus.PAUSED;
        }
    }

    /**
     * Resumes the match from pause, restoring the state prior to pausing.
     */
    public void resume() {
        if (state == GameStatus.PAUSED) {
            state = (stateBeforePause != null) ? stateBeforePause : GameStatus.PLAYING;
            stateBeforePause = null;
        }
    }

    /**
     * Immediately concludes the match and distributes end-of-match rewards.
     */
    public void finish() {
        finishMatch();
    }

    /**
     * Forwards the current input action set to the controller assigned to the given side.
     *
     * @param side Target player side
     * @param actions Set of actions requested on the current frame
     */
    public void setIntention(Side side, Set<PlayerAction> actions) {
        if (side == Side.LEFT && controller1 != null) {
            controller1.receive(actions);
        } else if (side == Side.RIGHT && controller2 != null) {
            controller2.receive(actions);
        }
    }

    /**
     * Registers a listener to receive match lifecycle and gameplay events.
     *
     * @param listener Listener to add
     */
    public void addListener(MatchListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregisters a previously added match listener.
     *
     * @param listener Listener to remove
     */
    public void removeListener(MatchListener listener) {
        listeners.remove(listener);
    }

    /*
     * Restores transient listener collections after Java deserialization.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        listeners = new ArrayList<>();
    }

    /**
     * Returns the current lifecycle status of the match.
     *
     * @return Active GameStatus
     */
    @Override
    public GameStatus getStatus() {
        return state;
    }

    /**
     * Returns the remaining regulation time in seconds, or -1 for goal-based matches.
     *
     * @return Remaining seconds or -1
     */
    @Override
    public int getRemainingSeconds() {
        return rule.remainingSeconds(this);
    }

    /**
     * Indicates whether the match is currently in sudden-death golden goal mode.
     *
     * @return True if sudden death is active
     */
    @Override
    public boolean isSuddenDeath() {
        return state == GameStatus.SUDDEN_DEATH;
    }

    /**
     * Returns the number of goals scored by the specified side.
     *
     * @param side Target side
     * @return Goal count
     */
    @Override
    public int getGoals(Side side) {
        return scoreBoard.getGoals(side);
    }

    /**
     * Returns a read-only view of the player on the specified side.
     *
     * @param side Target side
     * @return PlayerView for the side
     */
    @Override
    public PlayerView getPlayer(Side side) {
        return (side == Side.LEFT) ? player1 : player2;
    }

    /**
     * Returns a read-only view of the ball in play.
     *
     * @return BallView instance
     */
    @Override
    public BallView getBall() {
        return ball;
    }

    /**
     * Returns the winning side according to the active match rule, if decided.
     *
     * @return Optional containing the winning side, or empty if undecided
     */
    @Override
    public Optional<Side> getWinner() {
        return rule.winner(this);
    }

    /**
     * Returns the logical width of the playing field.
     *
     * @return Field width
     */
    @Override
    public int getFieldWidth() {
        return Field.WIDTH;
    }

    /**
     * Returns the logical height of the playing field.
     *
     * @return Field height
     */
    @Override
    public int getFieldHeight() {
        return Field.HEIGHT;
    }

    /**
     * Returns a defensive copy of the goal scoring area for the given side.
     *
     * @param side Goal side
     * @return Goal area rectangle copy
     */
    @Override
    public Rectangle getGoalArea(Side side) {
        Rectangle area = field.getGoal(side).getGoalArea();
        return new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    }

    /**
     * Returns a defensive copy of the goal crossbar hitbox for the given side.
     *
     * @param side Goal side
     * @return Crossbar rectangle copy
     */
    @Override
    public Rectangle getCrossBar(Side side) {
        Rectangle bar = field.getGoal(side).getCrossBar();
        return new Rectangle(bar.getX(), bar.getY(), bar.getWidth(), bar.getHeight());
    }

    /**
     * Returns the scoreboard tracking both sides' goals.
     *
     * @return Match ScoreBoard
     */
    public ScoreBoard getScoreBoard() { return scoreBoard; }

    /**
     * Updates the current lifecycle status of the match.
     *
     * @param state New GameStatus
     */
    public void setState(GameStatus state) { this.state = state; }

    /**
     * Returns the remaining ticks of the active goal celebration freeze.
     *
     * @return Remaining celebration ticks
     */
    public int getGoalCelebrationTicks() { return goalCelebrationTicks; }

    /**
     * Returns the controller assigned to the left player.
     *
     * @return Left player controller
     */
    public Controller getController1() { return controller1; }

    /**
     * Assigns a new controller to the left player.
     *
     * @param controller1 Left player controller
     */
    public void setController1(Controller controller1) { this.controller1 = controller1; }

    /**
     * Returns the controller assigned to the right player.
     *
     * @return Right player controller
     */
    public Controller getController2() { return controller2; }

    /**
     * Assigns a new controller to the right player.
     *
     * @param controller2 Right player controller
     */
    public void setController2(Controller controller2) { this.controller2 = controller2; }

    /**
     * Returns the underlying player profile for the given side for domain persistence coordination.
     *
     * @param side Player side
     * @return Player profile for that side
     */
    PlayerProfile getProfile(Side side) {
        return (side == Side.LEFT) ? player1.getProfile() : player2.getProfile();
    }

    /**
     * Returns the configured game modality for this match.
     *
     * @return Active Modality
     */
    @Override
    public Modality getModality() {
        return modality;
    }

    /**
     * Returns defensive copies of all active illusion decoy balls on the field.
     *
     * @return List of decoy ball circles
     */
    @Override
    public List<Circle> getFakeBalls() {
        List<Circle> fakeBalls = new ArrayList<>();
        if (player1 != null) {
            fakeBalls.addAll(player1.getDecoyBalls());
        }
        if (player2 != null) {
            fakeBalls.addAll(player2.getDecoyBalls());
        }
        return fakeBalls;
    }
}

package domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Machine controller implementing the Defensiva (defensive) profile prioritizing goal protection.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class DefensiveMachine extends MachineController {
    private static final long serialVersionUID = 1L;
    private static final double HOME_GOAL_OFFSET = 70.0;
    private static final double DEFEND_ZONE_RADIUS = 170.0;
    private static final double BEHIND_BALL_OFFSET = 12.0;
    private static final double MOVE_TOLERANCE = 10.0;
    private static final double JUMP_MAX_DX = 30.0;
    private static final double JUMP_HEAD_MARGIN_Y = 10.0;
    private static final double KICK_MAX_DX = 28.0;
    private static final double KICK_MAX_HEIGHT = 70.0;

    /**
     * Constructs a defensive machine controller for the given pitch side.
     *
     * @param side Pitch side controlled by this AI
     */
    public DefensiveMachine(Side side) {
        super(side);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<PlayerAction> chooseActions(MatchView match, PlayerView me, BallView ball, PlayerView rival) {
        Set<PlayerAction> actions = EnumSet.noneOf(PlayerAction.class);
        double myX = me.getHeadShape().getX();
        double goalX = ownGoalX(match);
        double homeX = goalX + side.dir() * HOME_GOAL_OFFSET;
        if(Math.abs(ball.getX() - goalX) > DEFEND_ZONE_RADIUS){
            moveToward(myX, homeX, MOVE_TOLERANCE, actions);
        }else{
            double defendX = ball.getX() - side.dir()*BEHIND_BALL_OFFSET;
            moveToward(myX, defendX, 4.0, actions);
            if(shouldJumpForBall(me, ball, JUMP_MAX_DX, JUMP_HEAD_MARGIN_Y)) actions.add(PlayerAction.JUMP);
            if(shouldKickBall(match, me, ball, KICK_MAX_DX, KICK_MAX_HEIGHT)){
                if((ball.getX() - myX)*side.dir() > -10){
                    actions.add(PlayerAction.KICK);
                }  
            } 
        }
        return actions;
        
        
    }
}

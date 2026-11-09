package domain;

/**
 * Interface defining default callback reactions for physical collisions between game entities.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public interface Collidable {

    /**
     * Reaction callback upon colliding with a ball.
     * 
     * @param ball Ball collided with
     */
    default void collideWithBall(Ball ball) {}

    /**
     * Reaction callback upon colliding with a player's head.
     * 
     * @param head Head collided with
     */
    default void collideWithHead(Head head) {}

    /**
     * Reaction callback upon colliding with a player's foot.
     * 
     * @param foot Foot collided with
     */
    default void collideWithFoot(Foot foot) {}
}

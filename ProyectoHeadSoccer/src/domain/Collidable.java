package domain;

public interface Collidable {
    default void collideWithBall(Ball ball) {}
    default void collideWithHead(Head head) {}
    default void collideWithFoot(Foot foot) {}
}

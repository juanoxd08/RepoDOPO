package domain;

import java.util.Optional;

/**
 * Encapsulates a player's physical limbs (head, torso, foot), anchor position,
 * vertical kinematics, and collision resolution against field boundaries, goals,
 * the ball, and opponents.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class PlayerBody {
    private final Side side;
    private final Head head;
    private final Body body;
    private final Foot foot;
    private Vector2 anchor;
    private double verticalSpeed;
    private boolean onTop;

    private void syncShapes(){
        head.getCircle().setX(anchor.getX());
        head.getCircle().setY(anchor.getY() - 55);
        
        body.getRectangle().setX(anchor.getX() - 13);
        body.getRectangle().setY(anchor.getY() - 38);

        foot.getCircle().setX(anchor.getX() + side.dir()*8 + foot.getKickOffsetX(side));
        foot.getCircle().setY(anchor.getY() - 12 + foot.getKickOffsetY());
        
    }

    /**
     * Constructs a player body at the starting position for the given side.
     *
     * @param side Field side defended by this player
     */
    public PlayerBody(Side side){
        this.side = side;
        anchor = new Vector2(side.startX(), Field.HEIGHT);
        head = new Head(anchor.getX(), side);
        body = new Body(anchor.getX(), side);
        foot = new Foot(anchor.getX(), side);
        verticalSpeed = 0;
        onTop = false;
        syncShapes();
    }

    /**
     * Resets the body to its starting anchor position and clears velocity and kick state.
     */
    public void resetPosition() {
        anchor = new Vector2(side.startX(), Field.HEIGHT);
        verticalSpeed = 0;
        onTop = false;
        foot.stopKicking(side);
        syncShapes();
    }

    /**
     * Translates the entire body by the given delta.
     *
     * @param dx Horizontal displacement in pixels
     * @param dy Vertical displacement in pixels
     */
    public void translate(double dx, double dy) {
        anchor = anchor.add(new Vector2(dx, dy));
        syncShapes();
    }

    /**
     * Applies an upward jump impulse if the player is grounded or standing on a surface.
     *
     * @param jumpImpulse Upward impulse magnitude in pixels per tick
     */
    public void jump(double jumpImpulse) {
        if (anchor.getY() >= Field.HEIGHT - 2.0 || onTop) {
            verticalSpeed = -jumpImpulse;
            onTop = false;
        }
    }

    /**
     * Advances foot kick sweep and vertical kinematics (gravity, ceiling, and ground clamp).
     */
    public void tick() {
        foot.tick(side);
        translate(0, verticalSpeed);

        double topY = head.getCircle().getY() - head.getCircle().getRadius();
        if (topY < 0) {
            translate(0, -topY);
            if (verticalSpeed < 0) {
                verticalSpeed = 0;
            }
        }

        if (anchor.getY() < Field.HEIGHT && !onTop) {
            verticalSpeed += Physics.GRAVITY;
        } else {
            if (anchor.getY() > Field.HEIGHT) {
                translate(0, Field.HEIGHT - anchor.getY());
            }
            verticalSpeed = 0;
        }
    }

    /**
     * Resolves solid collision between head, body, and foot against a goal crossbar.
     *
     * @param goal Goal to test against
     */
    public void collideWithGoal(Goal goal) {
        if (goal == null || goal.getCrossBar() == null) return;
        Rectangle bar = goal.getCrossBar();
        resolveCircleAgainstBar(head.getCircle(), bar);
        resolveCircleAgainstBar(foot.getCircle(), bar);
        resolveBodyAgainstBar(body.getRectangle(), bar);
    }

    private void resolveCircleAgainstBar(Circle circle, Rectangle bar) {
        Optional<Contact> contactOpt = bar.contactWith(circle);
        if (!contactOpt.isPresent()) return;

        Contact c = contactOpt.get();
        Vector2 n = c.getNormal();
        translate(n.getX() * c.getPenetration(), n.getY() * c.getPenetration());

        if (n.getY() < -0.5 && verticalSpeed >= 0) {
            verticalSpeed = 0;
            onTop = true;
        } else if (n.getY() > 0.5) {
            verticalSpeed = Math.max(verticalSpeed, 0);
        }
    }

    private void resolveBodyAgainstBar(Rectangle br, Rectangle bar) {
        double overlapX = Math.min(br.right(), bar.right()) - Math.max(br.left(), bar.left());
        double overlapY = Math.min(br.bottom(), bar.bottom()) - Math.max(br.top(), bar.top());
        if (overlapX <= 0 || overlapY <= 0) return;

        double bodyCenterX = (br.left() + br.right()) / 2.0;
        double barCenterX = (bar.left() + bar.right()) / 2.0;
        double bodyCenterY = (br.top() + br.bottom()) / 2.0;
        double barCenterY = (bar.top() + bar.bottom()) / 2.0;

        if (overlapX < overlapY) {
            translate(bodyCenterX < barCenterX ? -overlapX : overlapX, 0);
        } else if (bodyCenterY < barCenterY) {
            translate(0, -overlapY);
            if (verticalSpeed >= 0) {
                verticalSpeed = 0;
                onTop = true;
            }
        } else {
            translate(0, overlapY);
            verticalSpeed = Math.max(verticalSpeed, 0);
        }
    }

    /**
     * Resolves ball collisions against this body, prioritizing the foot while kicking.
     *
     * @param ball Ball in play
     * @param power Player power to trigger if aura is active on kick contact
     */
    public void collideWithBall(Ball ball, Power power){
        if(ball == null) return;
        if(foot.isKicking()){
            collideFootWithBall(ball, power);
            ball.bounceOff(head.getCircle(), Physics.HEAD_RESTITUTION, Physics.HEAD_IMPULSE);
            ball.bounceOff(body.getRectangle(), Physics.BODY_RESTITUTION, 0.0);
        }else{
            ball.bounceOff(head.getCircle(), Physics.HEAD_RESTITUTION, Physics.HEAD_IMPULSE);
            ball.bounceOff(body.getRectangle(), Physics.BODY_RESTITUTION, 0.0); 
            collideFootWithBall(ball, power);          
        }
    }

    private void collideFootWithBall(Ball ball, Power power){
        Circle footCircle = this.foot.getCircle();
        Optional<Contact> contact = footCircle.contactWith(ball.getCircle());
        if(contact.isEmpty()) return;
        Contact c = contact.get();
        if(foot.isKicking()){
            ball.getCircle().translate(c.getNormal().getX() * c.getPenetration(), c.getNormal().getY() * c.getPenetration());
            boolean inFront = (ball.getX() - footCircle.getX()) * side.dir() > -ball.getRadius();
            if(inFront){
                Vector2 vel = new Vector2(side.dir(), Physics.KICK_LIFT_FACTOR).normalized().scale(foot.getPowerKicking());
                ball.setHorizontalSpeed(vel.getX());
                ball.setVerticalSpeed(vel.getY());
                if(power != null && power.isAuraActive()){
                    power.executePowerShot(ball, side);
                }
                return;
            }
        }
        ball.bounceOff(footCircle, Physics.FOOT_RESTITUTION, 0.0);
    }

    /**
     * Checks whether the ball overlaps any limb of this body.
     *
     * @param ball Ball in play
     * @return True if any limb overlaps the ball
     */
    public boolean isHitByBall(Ball ball) {
        if (ball == null || ball.getCircle() == null) return false;
        Circle ballCircle = ball.getCircle();
        return ballCircle.detectCollisionWithCircle(head.getCircle())
                || ballCircle.detectCollisionWithRectangle(body.getRectangle())
                || ballCircle.detectCollisionWithCircle(foot.getCircle());
    }

    public void collideWithPlayer(PlayerBody other){
        
    }

    /**
     * Clears support state before collision resolution on each tick.
     */
    public void clearSupport() {
        this.onTop = false;
    }

    /**
     * Starts the foot kick sweep animation.
     */
    public void startKick() {
        foot.kicking(side);
        syncShapes();
    }

    /**
     * Stops the foot kick animation and returns the foot to resting position.
     */
    public void stopKick() {
        foot.stopKicking(side);
        syncShapes();
    }

    /** @return True if the foot is currently executing a kick */
    public boolean isKicking() {
        return foot.isKicking();
    }

    /** @return Vertical speed in pixels per tick */
    public double getVerticalSpeed() {
        return verticalSpeed;
    }

    /** @param verticalSpeed Vertical speed in pixels per tick */
    public void setVerticalSpeed(double verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    /** @return Defensive copy of the head circle */
    public Circle getHeadShape() {
        Circle hc = head.getCircle();
        return new Circle(hc.getX(), hc.getY(), hc.getRadius());
    }

    /** @return Defensive copy of the body rectangle */
    public Rectangle getBodyShape() {
        Rectangle br = body.getRectangle();
        return new Rectangle(br.getX(), br.getY(), br.getWidth(), br.getHeight());
    }

    /** @return Defensive copy of the foot circle */
    public Circle getFootShape() {
        Circle fc = foot.getCircle();
        return new Circle(fc.getX(), fc.getY(), fc.getRadius());
    }

    /** @return Internal head entity */
    public Head getHead() {
        return head;
    }

    /** @return Internal body entity */
    public Body getBody() {
        return body;
    }

    /**
     * Restricts the player horizontally within the pitch boundaries.
     */
    public void collideWithEdges() {
        double r = head.getCircle().getRadius();
        double hx = head.getCircle().getX();
        if (hx - r < 0) {
            translate(-(hx - r), 0);
        }
        if (hx + r > Field.WIDTH) {
            translate(Field.WIDTH - (hx + r), 0);
        }
    }

    /**
     * Resolves physical separation and head-standing support against an opponent body.
     *
     * @param other Opponent body
     * @param pushSpeed Horizontal separation speed
     */
    public void collideWithPlayer(PlayerBody other, double pushSpeed) {
        if (other == null) return;
        boolean overlaps = head.getCircle().detectCollisionWithCircle(other.head.getCircle())
                || head.getCircle().detectCollisionWithRectangle(other.body.getRectangle())
                || body.getRectangle().detectCollisionWithCircle(other.head.getCircle())
                || body.getRectangle().detectCollisionWithRectangle(other.body.getRectangle())
                || foot.getCircle().detectCollisionWithCircle(other.head.getCircle())
                || foot.getCircle().detectCollisionWithCircle(other.foot.getCircle())
                || foot.getCircle().detectCollisionWithRectangle(other.body.getRectangle())
                || head.getCircle().detectCollisionWithCircle(other.foot.getCircle());

        if (!overlaps) return;

        if (foot.getCircle().detectCollisionWithCircle(other.head.getCircle())
                && foot.getCircle().getY() < other.head.getCircle().getY()) {
            verticalSpeed = 0;
            onTop = true;
        }

        double push = (anchor.getX() < other.anchor.getX()) ? -pushSpeed : pushSpeed;
        translate(push, 0);
        collideWithEdges();
    }

    /**
     * Checks whether this body's active kicking foot overlaps the opponent's head or body.
     *
     * @param other Opponent body
     * @return True if an active kick connects
     */
    public boolean isKickHitting(PlayerBody other) {
        if (other == null || !foot.isKicking()) return false;
        return foot.getCircle().detectCollisionWithCircle(other.head.getCircle())
                || foot.getCircle().detectCollisionWithRectangle(other.body.getRectangle());
    }

    /** @return Internal foot entity */
    public Foot getFoot() {
        return foot;
    }

}

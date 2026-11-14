package domain;

/**
 * Enumeration representing pitch sides (LEFT or RIGHT) assigned to players and goals.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
 public enum Side {
     LEFT(1, 100.0),
     RIGHT(-1, Field.WIDTH - 100.0);
     private final int direction;
     private final double startX;
     Side(int direction, double startX) {
         this.direction = direction;
         this.startX = startX;
     }
     public int dir() {
         return direction;
     }
     public double startX() {
         return startX;
     }
     public Side opposite() {
         return this == LEFT ? RIGHT : LEFT;
     }
 }

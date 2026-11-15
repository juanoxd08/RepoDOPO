package domain;

import java.util.Vector;

public class Vector2 {
    private final double x;
    private final double y;

    public Vector2(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public  Vector2 add(Vector2 other){
        return new Vector2(this.x + other.getX(), this.y + other.getY());
    }

    public  Vector2 sub(Vector2 other){
        return new Vector2(this.x - other.getX(), this.y - other.getY());
    }
    
    public  Vector2 scale(double factor){
        return new Vector2(this.x * factor, this.y * factor);
    }

    public double dot(Vector2 other){
        return this.x*other.getX() + this.y*other.getY();
    }

    public double length(){
        return Math.hypot(x, y);
    }

    public Vector2 normalized(){
        double len = length();
        return (len == 0) ? new Vector2(0, 0) : scale(1.0 / len);
    }

}

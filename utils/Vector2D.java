package utils;

import java.io.Serializable;

public class Vector2D implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private double x;
    private double y;
    
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector2D() {
        this(0, 0);
    }
    
    public double distanceTo(Vector2D other) {
        if (other == null) return Double.MAX_VALUE;
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public Vector2D normalize() {
        double magnitude = Math.sqrt(x * x + y * y);
        if (magnitude > 0) {
            return new Vector2D(x / magnitude, y / magnitude);
        }
        return new Vector2D(0, 0);
    }
    
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }
    
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }
    
    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }
    
    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }
    
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector2D vector2D = (Vector2D) obj;
        return Math.abs(x - vector2D.x) < 0.0001 && 
               Math.abs(y - vector2D.y) < 0.0001;
    }
    
    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(x);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
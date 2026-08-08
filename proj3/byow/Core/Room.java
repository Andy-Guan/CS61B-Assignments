package byow.Core;

public class Room {
    public int x;
    public int y;
    public int width;
    public int height;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Decide whether two rooms are overlapped
     * @param other
     */
    public boolean overlaps(Room other) {
        return (this.x <= other.x + other.width + 1) &&
                (this.x + this.width + 1 >= other.x) &&
                (this.y <= other.y + other.height + 1) &&
                (this.y + this.height + 1 >= other.y);
    }

    /**
     * Get the x center
     */
    public int getCenterX() {
        return x + width / 2;
    }

    /**
     * Get the y center
     */
    public int getCenterY() {
        return y + height / 2;
    }
}

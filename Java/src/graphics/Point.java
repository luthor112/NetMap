package graphics;

public class Point {								// 2D point
	public static final long serialVersionUID = 1L;
	public int x;									// Coordinates
	public int y;
	
	public Point(int x, int y) {					// Constructors
		this.x = x;
		this.y = y;
	}
	
	public Point() {
		this(0, 0);
	}
}

public class Coordinates {

	Float x;
	Float y;

	public Coordinates() {
		this(0.f, 0.f);
	}

	public Coordinates(Float x, Float y) {
		this.x = x;
		this.y = y;
	}

	public Float getX() {
		return x;
	}

	public Float getY() {
		return y;
	}

	public void setX(Float x) {
		this.x = x;
	}

	public void setY(Float y) {
		this.y = y;
	}

}

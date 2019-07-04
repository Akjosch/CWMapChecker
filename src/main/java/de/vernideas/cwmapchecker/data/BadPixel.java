package de.vernideas.cwmapchecker.data;

import java.util.Objects;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

public class BadPixel {
	@Getter @Setter private int x;
	@Getter @Setter private int y;
	@Getter @Setter private int red;
	@Getter @Setter private int green;
	@Getter @Setter private int blue;

	public BadPixel(int x, int y, Color c) {
		Objects.requireNonNull(c);
		red = (int)Math.round(c.getRed() * 255);
		green = (int)Math.round(c.getGreen() * 255);
		blue = (int)Math.round(c.getBlue() * 255);
		this.x = x;
		this.y = y;
	}
	
	public Color getColor() {
		return Color.rgb(red, green, blue);
	}
	
	@Override public String toString() {
		return String.format("bad pixel %d;%d;%d at %d,%d", red, green, blue, x, y);
	}
}

package de.vernideas.cwmapchecker.data;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

public final class Province {
	public final static Comparator<Province> NAME_COMPARATOR = new Comparator<Province>() {
		private final Collator COLLATOR = Collator.getInstance(Locale.ROOT);
		{
			COLLATOR.setStrength(Collator.CANONICAL_DECOMPOSITION);
		}
		
		@Override public int compare(Province o1, Province o2) {
			int result = COLLATOR.compare(o1.name, o2.name);
			return result != 0 ? result : Integer.compare(o1.id, o2.id);
		}
	};
	
	private long sumXPos;
	private long sumYPos;
	
	@Getter @Setter private int id;
	@Getter @Setter private String name;
	@Getter @Setter private int red;
	@Getter @Setter private int green;
	@Getter @Setter private int blue;
	@Getter @Setter private Color color;
	@Getter @Setter private int size;
	
	public Province() {
		this.id = 0;
		this.name = "";
		this.color = Color.BLACK;
		this.size = 0;
		this.sumXPos = 0;
		this.sumYPos = 0;
	}
	
	public Province(String line) {
		// Parse Clausewitz province lines
		String[] parts = Objects.requireNonNull(line).split(";", -1);
		if(parts.length < 4) {
			throw new IllegalArgumentException("Not a correct province definition line: '" + line + "'");
		}
		if(parts[1].length() == 0 || parts[2].length() == 0 || parts[3].length() == 0) {
			throw new IllegalArgumentException("Not a correct province definition line: '" + line + "'");
		}
		this.id = parts[0].length() <= 0 ? -1 : Integer.parseInt(parts[0]);
		this.name = parts.length >= 5 ? parts[4] : "- unnamed -";
		setColor(parts[1], parts[2], parts[3]);
		this.size = 0;
		this.sumXPos = 0;
		this.sumYPos = 0;
	}
	
	public int getX() {
		return size <= 0 ? 0 : (int) Math.floor(1.0 * sumXPos / size + 0.5);
	}
	
	public int getY() {
		return size <= 0 ? 0 : (int) Math.floor(1.0 * sumYPos / size + 0.5);
	}
	
	@Override public String toString() {
		return String.format("%s (#%d)", name, id);
	}
	
	public void setColor(int r, int g, int b) {
		this.red = r;
		this.green = g;
		this.blue = b;
		this.color = Color.rgb(r, g, b);
	}
	
	public void setColor(String r, String g, String b) {
		setColor(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
	}
	
	public void addPixel(int x, int y) {
		sumXPos += x;
		sumYPos += y;
		++ size;
	}
}

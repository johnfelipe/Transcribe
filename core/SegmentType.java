package core;

import java.awt.Color;

public class SegmentType {
	public static SegmentType DEFAULT = new SegmentType("Default", Color.ORANGE);
	
	private String name;
	private Color reprColor;
	
	public SegmentType(String name, Color repr) {
		this.name = name;
		this.reprColor = repr;
	}
	
	public String name() {
		return name;
	}
	
	public void name(String name) {
		this.name = name;
	}
	
	public Color color() {
		return reprColor;
	}
	
	public void color(Color col) {
		reprColor = col;
	}
}

package core;

public class Segment implements Cloneable {
	private double startMs;
	private double endMs;
	public String identifier;
	private String segmentTypeName;
	private String transcript;
	
	public Segment(double startMs, double endMs) {
		this.startMs = endMs;
		this.endMs = endMs;
		
		this.transcript = "";
		this.segmentTypeName = "$default";
	}
	
	public Segment(double startMs, double endMs, String st) {
		this.segmentTypeName = st;
		this.startMs = startMs;
		this.endMs = endMs;
		this.transcript = "";
	}
	
	public void setRange(double startms, double endms) {
		startMs = startms;
		endMs = endms;
	}
	
	public Segment clone() {
		Segment seg = new Segment(startMs, endMs, segmentTypeName);
		seg.identifier = identifier;
		seg.transcript = transcript;
		return seg;
	}
	
	public double start() {
		return startMs;
	}
	
	public double end() {
		return endMs;
	}
	
	public void segmentType(String name) {
		this.segmentTypeName = name;
	}
	
	public String segmentType() {
		return segmentTypeName;
	}
	
	public void transcript(String transcript) {
		this.transcript=transcript;
	}
	
	public String transcript() {
		return transcript;
	}
}

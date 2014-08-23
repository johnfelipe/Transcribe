package compile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.Audio;
import core.Segment;

public class Compiler {
	private Audio aud;
	private ArrayList<Preprocessor> processors;
	private Collapse.Format fmt;

	public Compiler(Audio au, Collapse.Format fmt) {
		this.aud = au;
		this.processors = new ArrayList<Preprocessor>();
		this.fmt = fmt;
	}
	
	public void collapseFormat(Collapse.Format fmt) {
		this.fmt = fmt;
	}
	
	public Audio audio() {
		return aud;
	}
	
	public Collapse.Format collapseFormat() {
		return this.fmt;
	}
	
	public List<Preprocessor> processors() {
		return processors;
	}
	
	public void addProcessor(Preprocessor p) {
		processors.add(p);
	}
	
	private Set<Segment> cloneSegments() {
		Set<Segment> clon = new HashSet<Segment>();
		for(Segment seg : aud.segments()) {
			clon.add(seg);
		}
		return clon;
	}
	
	public String compile() {
		Set<Segment> segments = cloneSegments();
		
		for(Preprocessor proc : processors) {
			segments = proc.process(segments);
		}
		
		return (new Collapse(fmt)).process(segments).iterator().next().transcript();
	}
}

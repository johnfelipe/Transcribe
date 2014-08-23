package compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import core.Segment;
import core.SegmentTypeManager;

//collapses all segments into one null-type segment
public class Collapse implements Preprocessor {
	public static interface Format {
		public String format(Segment seg);
	};
	
	private Collapse.Format fmt;
	
	public Collapse(Collapse.Format fmt) {
		this.fmt = fmt;
	}
	
	@Override
	public Set<Segment> process(Set<Segment> in) {
		ArrayList<Segment> segments = new ArrayList<Segment>(Arrays.asList(in.toArray(new Segment[in.size()])));
		Collections.sort(segments, new Comparator<Segment>() {
			@Override
			public int compare(Segment s1, Segment s2) {
				if(s1.start() < s2.start()) {
					return -1;
				}
				else {
					return 1;
				}
			}
		});
		
		Segment out = new Segment(-1, -1, null);
		String transcript = "";

		for(Segment seg : segments) {
			transcript += fmt.format(seg);
		}
		
		out.transcript(transcript.trim());
		Set<Segment> s = new HashSet<Segment>();
		s.add(out);
		return s;
	}

}

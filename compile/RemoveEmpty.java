package compile;

import java.util.HashSet;
import java.util.Set;

import core.Segment;

public class RemoveEmpty implements Preprocessor {
	@Override
	public Set<Segment> process(Set<Segment> in) {
		Set<Segment> segs = new HashSet<Segment>();
		
		for(Segment seg : in) {
			if(seg.transcript().trim().length() > 0) {
				segs.add(seg);
			}
		}
		
		return segs;
	}
}

package compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import core.Segment;

//merges same type segments across an interval with a merge operation
//used for '[pause]', '. . .', etc
public class IntervalMerger implements Preprocessor {
	public static interface MergeOperation {
		Segment merge(Segment s1, Segment s2);
	};
	
	private double min;
	private double max;
	private MergeOperation oper;
	
	public IntervalMerger(double msMin, double msMax, MergeOperation oper) {
		min = msMin;
		max = msMax;
		this.oper = oper;
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
		
		for(int s = 0; s < segments.size()-1; s++) {
			//break symbol means no merging.
			if(segments.get(s).transcript().trim().endsWith(SymbolDefinitions.SYMB_BREAK)) {
				continue;
			}
			
			if(segments.get(s+1).segmentType().equals(segments.get(s).segmentType())) {
				double df = segments.get(s+1).start() - segments.get(s).end();
				if(df >= min && df <= max) {
					Segment m = oper.merge(segments.get(s), segments.get(s+1));
					if(m != null) {
						segments.set(s, m); 
						segments.remove(s+1);
						s--;
					}
				}
			}
		}

		HashSet<Segment> segset = new HashSet<Segment>();
		for(Segment seg : segments) {
			segset.add(seg);
		}
		
		return segset;
	}
}

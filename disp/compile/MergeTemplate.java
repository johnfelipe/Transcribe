package disp.compile;

import compile.IntervalMerger;
import core.Segment;

public class MergeTemplate {
	public boolean removeNonAlphanumAtJoin;
	public boolean alphaNumEndsOnly;
	
	public String mergeText;
	
	public double minDist;
	public double maxDist;
	
	public MergeTemplate() {
		minDist = 0.0;
		maxDist = 0.0;
		mergeText = "";
		removeNonAlphanumAtJoin = false;
		alphaNumEndsOnly = false;
	}
	
	public IntervalMerger createMerger() {
		return new IntervalMerger(minDist, maxDist, new IntervalMerger.MergeOperation() {
			@Override
			public Segment merge(Segment s1, Segment s2) {
				Segment m = s1.clone();
				
				String s1t = s1.transcript().trim();
				String s2t = s2.transcript().trim();

				if(!removeNonAlphanumAtJoin) {
					if(alphaNumEndsOnly && !(s1t.matches("^.+?[a-zA-Z]+$") && s2t.matches("^[a-zA-Z]+.+?$"))) {
						return null;
					}
				}
				
				if(removeNonAlphanumAtJoin) {
					while(s1t.matches("^.+?\\p{Punct}+$")) {
						s1t = s1t.substring(0, s1t.length()-1);
					}
					while(s2t.matches("^\\p{Punct}+.+?$")) {
						s2t = s2t.substring(1, s2t.length());
					}
				}
				
				m.transcript(s1t+mergeText+s2t);
				return m;
			}
		});
	}
	
	public String toString() {
		return minDist + "ms-" + maxDist + "ms gap merge with ((" + mergeText + "))"
				+ (removeNonAlphanumAtJoin ? " [Del Non-A# Ends]" : "")
				+ (alphaNumEndsOnly ? " [A# Ends Only]" : "");
	}
}

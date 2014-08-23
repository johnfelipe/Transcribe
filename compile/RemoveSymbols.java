package compile;

import java.util.HashSet;
import java.util.Set;

import core.Segment;

public class RemoveSymbols implements Preprocessor {
	@Override
	public Set<Segment> process(Set<Segment> in) {
		Set<Segment> out = new HashSet<Segment>();
		for(Segment seg : in) {
			Segment s = seg.clone();
			String ts = s.transcript();
			String tst = ts.trim();
			for(String symbol : SymbolDefinitions.SYMBOLS) {
				if(tst.endsWith(symbol)) {
					int idx = ts.lastIndexOf(symbol);
					if(idx+symbol.length() < ts.length()) {
						ts = ts.substring(0, idx) + ts.substring(idx+symbol.length(), ts.length());
					}
					else {
						ts = ts.substring(0, idx);
					}
					tst = ts.trim();
				}
			}
			s.transcript(ts);
			out.add(s);
		}
		return out;
	}
}

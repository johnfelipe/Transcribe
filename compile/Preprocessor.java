package compile;

import java.util.Set;

import core.Segment;

public interface Preprocessor {
	Set<Segment> process(Set<Segment> in);
}

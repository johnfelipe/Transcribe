package compile.format;

import compile.Collapse;
import core.Audio;
import core.Segment;
import core.SegmentTypeManager;

//TODO tab in lines
//TODO line height control
public class DefaultFormat implements Collapse.Format {
	public static final String TYPE_EXPR = "$Type";
	public static final String TEXT_EXPR = "$Text";
	public static final String TIME_EXPR = "$Time";
	public static final String LINEFEED_EXPR = "\\n";
	public static final String CAR_RET_EXPR = "\\r";
	public static final String TAB_EXPR = "\\t";
	
	public static final String DEFAULT_FMT_STRING = "$Type: $Text\\r\\n";
	
	private SegmentTypeManager stm;
	private String formatString;
	
	public DefaultFormat(SegmentTypeManager stm) {
		this.stm = stm;
		this.formatString = DEFAULT_FMT_STRING;
	}
	
	public void formatString(String type) {
		this.formatString = type;
	}
	
	@Override
	public String format(Segment seg) {
		String out = formatString.replace(TYPE_EXPR, stm.get(seg.segmentType()).name())
								 .replace(TEXT_EXPR, seg.transcript())
								 .replace(TIME_EXPR, Audio.msString(seg.start()))
								 .replace(LINEFEED_EXPR, "\n")
								 .replace(CAR_RET_EXPR, "\r")
								 .replace(TAB_EXPR, "\t");
		return out;
	}
}
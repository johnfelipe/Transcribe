package compile.format;

import compile.Collapse;
import core.Audio;
import core.Segment;
import core.SegmentTypeManager;
import core.Time;

//TODO tab in lines
//TODO line height control
public class DefaultFormat implements Collapse.Format {
	public static final String TYPE_EXPR = "\\type";
	public static final String TEXT_EXPR = "\\text";
	
	public static final String TIME_HH_EXPR = "\\hh";
	public static final String TIME_MM_EXPR = "\\mm";
	public static final String TIME_SS_EXPR = "\\ss";
	public static final String TIME_MS3_EXPR = "\\ms3";
	
	public static final String TIME_H_EXPR = "\\h";
	public static final String TIME_M_EXPR = "\\m";
	public static final String TIME_S_EXPR = "\\s";
	public static final String TIME_MS_EXPR = "\\ms";
	
	public static final String LINEFEED_EXPR = "\\n";
	public static final String CAR_RET_EXPR = "\\r";
	public static final String TAB_EXPR = "\\t";
	
	public static final String DEFAULT_FMT_STRING = "\\type: \\text\\r\\n";
	
	private SegmentTypeManager stm;
	private String formatString;
	
	public DefaultFormat(SegmentTypeManager stm) {
		this.stm = stm;
		this.formatString = DEFAULT_FMT_STRING;
	}
	
	public void formatString(String type) {
		this.formatString = type;
	}
	
	private String format(Segment seg, String str) {
		Time t = new Time(seg.start());
		
		String base = str.replace(TYPE_EXPR, stm.get(seg.segmentType()).name())
				 .replace(TIME_HH_EXPR, t.hh())
				 .replace(TIME_MM_EXPR, t.mm())
				 .replace(TIME_SS_EXPR, t.ss())
				 .replace(TIME_MS3_EXPR, t.ms3())
				 .replace(TIME_H_EXPR, t.h())
				 .replace(TIME_MM_EXPR, t.mm())
				 .replace(TIME_SS_EXPR, t.ss())
				 .replace(TIME_MS_EXPR, t.ms())
				 .replace(LINEFEED_EXPR, "\n")
				 .replace(CAR_RET_EXPR, "\r")
				 .replace(TAB_EXPR, "\t");
		
		if(seg != null) {
			base = base.replace(TEXT_EXPR, format(null, seg.transcript()));
		}
		
		return base;
	}
	
	@Override
	public String format(Segment seg) {
		return format(seg, formatString);
	}
}
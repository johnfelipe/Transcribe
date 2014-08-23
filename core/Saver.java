package core;

import java.awt.Color;
import java.io.File;
import java.util.Base64;
import java.util.Set;

import javax.swing.ListModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import disp.AudioVisual;
import disp.AudioVisualController;
import disp.CompileSettings;
import disp.compile.MergeTemplate;

public class Saver {
	private String saved;
	private Audio audio;
	private AudioVisualController avc;
	private AudioTools at;
	private AudioVisual av;
	
	//TODO save/load Compiler settings
	
	@SuppressWarnings("unchecked")
	public Saver(Audio audio, AudioVisualController avc, AudioTools at, AudioVisual av) {
		JSONObject obj = new JSONObject();
		
		this.audio = audio;
		this.avc = avc;
		this.at = at;
		this.av = av;
		
		try {
			obj.put("audio", audio());
			obj.put("at", at());
			obj.put("av", av());
			obj.put("avc", avc());
			
			saved = obj.toJSONString();
		}
		catch(Exception e) {
			saved = null;
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject audio() {
		JSONObject out = new JSONObject();
		
		//base64 because json library bugs
		out.put("file", new String(U.b64e(new File(audio.file()).getAbsolutePath())));
		
		//types
		{
			SegmentTypeManager stm = audio.typeManager();
			JSONArray types = new JSONArray();
			
			for(String typename : stm.types()) {
				SegmentType type = stm.get(typename);
				JSONObject otype = new JSONObject();
				
				//color
				{
					Color col = type.color();
					JSONArray acol = new JSONArray();
					acol.add(col.getRed()); 
					acol.add(col.getGreen());
					acol.add(col.getBlue());
					otype.put("color", acol);
				}
				
				otype.put("decl", U.b64e(typename));
				otype.put("name", U.b64e(type.name()));
				
				types.add(otype);
			}
			
			out.put("types", types);
		}
		
		//segments
		{
			Set<Segment> segments = audio.segments();
			JSONArray aSegments = new JSONArray();
			
			for(Segment seg : segments) {
				JSONObject oSeg = new JSONObject();
				oSeg.put("start", seg.start());
				oSeg.put("end", seg.end());
				oSeg.put("type", U.b64e(seg.segmentType()));
				oSeg.put("transcript", U.b64e(seg.transcript()));
				
				aSegments.add(oSeg);
			}
			
			out.put("segments", aSegments);
		}
		
		//selection range
		{
			out.put("selectStart", audio.getSelectedRegionStart());
			out.put("selectEnd", audio.getSelectedRegionEnd());
		}
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject avc() {
		JSONObject out = new JSONObject();
		out.put("selected", U.b64e(avc.selectedSegmentType()));
		
		JSONObject compiler = new JSONObject();
		{
			CompileSettings cs = avc.settings;
			
			JSONArray mergeTemplates = new JSONArray();
			{
				ListModel<MergeTemplate> mtl = cs.mtListModel();
				for(int i = 0; i < mtl.getSize(); i++) {
					MergeTemplate mt = mtl.getElementAt(i);
					JSONObject mto = new JSONObject();
					
					mto.put("rNAAJ", mt.removeNonAlphanumAtJoin);
					mto.put("aNEO", mt.alphaNumEndsOnly);
					mto.put("mergeText", U.b64e(mt.mergeText));
					mto.put("minDist", mt.minDist);
					mto.put("maxDist", mt.maxDist);
					
					mergeTemplates.add(mto);
				}
			}
			
			compiler.put("merges", mergeTemplates);
			compiler.put("remEmptySegs", cs.removeEmptySegments());
			compiler.put("format", U.b64e(cs.writeFormat()));
		}
		
		out.put("compiler", compiler);
		
		return out;
	}
	
	private JSONObject at() {
		return new JSONObject();
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject av() {
		JSONObject out = new JSONObject();
		out.put("visuStart", av.visuStart());
		out.put("visuEnd", av.visuEnd());
		return out;
	}
	
	public String save() {
		return saved;
	}
	
	public boolean valid() {
		return saved != null;
	}
}

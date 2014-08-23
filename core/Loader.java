package core;

import java.awt.Color;
import java.io.File;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import disp.AudioVisual;
import disp.AudioVisualController;
import disp.CompileSettings;
import disp.compile.MergeTemplate;

//TODO if audio file does not exist, try callback for request of new file
public class Loader {
	public static interface NewAudioCallback {
		File get();
	};
	
	private JSONObject data;
	private Audio audio;
	private AudioVisualController avc;
	private AudioTools at;
	private AudioVisual av;
	private String in;
	private Set<NewAudioCallback> newFileCallbacks;
	
	public Audio audio() {
		return audio;
	}
	
	public AudioVisualController avc() {
		return avc;
	}
	
	public AudioTools at() {
		return at;
	}
	
	public AudioVisual av() {
		return av;
	}
	
	public void alternativeAudioSource(NewAudioCallback src) {
		newFileCallbacks.add(src);
	}
	
	
	public Loader(String in) {
		newFileCallbacks = new HashSet<NewAudioCallback>();
		this.in = in;
	}
	
	public boolean process() {
		Object parse = JSONValue.parse(in);
		if(parse == null || !(parse instanceof JSONObject)) {
			data = null;
		}
		data = (JSONObject)parse;
		
		return pAudio() && pAt() && pAv() && pAvc();
	}
	
	private File alternative() {
		for(NewAudioCallback nac : newFileCallbacks) {
			File alt = nac.get();
			if(alt != null) {
				return alt;
			}
		}
		return null;
	}
	
	private boolean pAudio() {
		Object val = data.get("audio");

		try 
		{
			JSONObject oAud = (JSONObject)val;
			
			//check that audiofile exists
			String file = U.b64d((String)oAud.get("file"));
			if(!new File(file).exists()) {
				System.out.println("Does not exist.");
				File alt = alternative();
				if(alt != null) {
					file = alt.getAbsolutePath();
				}
				else {
					return false;
				}
			}
			
			Audio aud = new Audio(file);
			
			//types
			{
				SegmentTypeManager stm = aud.typeManager();
				stm.clear();
				for(Object oType : (JSONArray)oAud.get("types")) {
					JSONObject type = (JSONObject)oType;
					JSONArray col = (JSONArray)type.get("color");
					SegmentType st = new SegmentType(U.b64d((String)type.get("name")), 
							new Color(((Long)col.get(0))/255.0f, ((Long)col.get(1))/255.0f, ((Long)col.get(2))/255.0f));
					stm.define(U.b64d((String)type.get("decl")), st);
				}
			}
			
			//segments
			{
				Set<Segment> segments = aud.segments();
				JSONArray arr = (JSONArray)oAud.get("segments");
				
				for(Object obj : arr) {
					JSONObject segment = (JSONObject)obj;
					Segment seg = new Segment((Double)segment.get("start"), (Double)segment.get("end"), U.b64d((String)segment.get("type")));
					seg.transcript(U.b64d((String)segment.get("transcript")));
					segments.add(seg);
				}
			}
			
			//selection range
			{
				aud.setSelectedRegion((Double)oAud.get("selectStart"), (Double)oAud.get("selectEnd"));
			}
			
			audio = aud;
			return true;
			
		}
		catch(Exception e) {
			e.printStackTrace();
			audio = null;
			return false;
		}
	}
	
	private boolean pAt() {
		this.at = new AudioTools(audio);
		return true;
	}
	
	private boolean pAv() {
		try {
			AudioVisual av = new AudioVisual(audio);
			JSONObject oav = (JSONObject)data.get("av");
			av.visuStart((Double)oav.get("visuStart"));
			av.visuEnd((Double)oav.get("visuEnd"));
			this.av = av;
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			this.av = null;
			return false;
		}
	}
	
	private boolean pAvc() {
		try {
			AudioVisualController avc = new AudioVisualController(av, at);
			
			JSONObject o = (JSONObject)data.get("avc");
			avc.selectedSegmentType(U.b64d((String)o.get("selected")));
			
			CompileSettings settings = avc.settings;
			JSONObject compiler = (JSONObject)o.get("compiler");
			if(compiler != null) {
				JSONArray mergeTemplates = (JSONArray)compiler.get("merges");
				DefaultListModel<MergeTemplate> mtl = settings.mtListModel();
				for(Object mtoo : mergeTemplates) {
					JSONObject mto = (JSONObject)mtoo;
					MergeTemplate mt = new MergeTemplate();
					mt.removeNonAlphanumAtJoin = (Boolean)mto.get("rNAAJ");
					mt.alphaNumEndsOnly = (Boolean)mto.get("aNEO");
					mt.mergeText = U.b64d((String)mto.get("mergeText"));
					mt.minDist = (Double)mto.get("minDist");
					mt.maxDist = (Double)mto.get("maxDist");
					mtl.add(mtl.size(), mt);
				}
			
				settings.removeEmptySegments((Boolean)compiler.get("remEmptySegs"));
				settings.writeFormat(U.b64d((String)compiler.get("format")));
			}
			
			this.avc = avc;
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			this.avc = null;
			return false;
		}
	}
	
	public boolean valid() {
		return data != null 
			&& av != null 
			&& avc != null 
			&& audio != null 
			&& at != null;
	}
}

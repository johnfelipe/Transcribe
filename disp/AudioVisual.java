package disp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.font.LineMetrics;

import javax.swing.JPanel;

import com.sun.javafx.geom.Point2D;

import compile.SymbolDefinitions;
import core.Audio;
import core.Segment;

public class AudioVisual extends JPanel {
	private String audioFile;
	
	//start/end in ms
	private double visuStart;
	private double visuEnd;
	private boolean displayPreviewText;
	
	private Audio aud;
	
	public AudioVisual(Audio a) {
		aud = a;
		displayPreviewText = true;
	}
	
	public AudioVisual(Audio a, boolean displayPreviewText) {
		aud = a;
		this.displayPreviewText = displayPreviewText;
	}
	
	public Audio getAudio() {
		return aud;
	}
	
	public double visuStart() {
		return visuStart;
	}
	
	public double visuEnd() {
		return visuEnd;
	}
	
	public void visuStart(double visuStart) {
		this.visuStart = visuStart;
	}
	
	public void visuEnd(double visuEnd) {
		this.visuEnd = visuEnd;
	}

	//amp => -1.0  to  1.0
	public int mapY(double amp) {
		return (int)((1.0-amp)/2 * this.getHeight());
	}
	
	public int mapX(double ms) {
		return (int)((ms - visuStart)/(visuEnd - visuStart) * getWidth()); 
	}
	
	//get ms of x pos
	public double unmapX(double x) {
		return (x/this.getWidth() * (visuEnd-visuStart)) + visuStart;
	}
	
	
	public static final Color PEAK_COLOR = new Color(35,35,160);
	public static final Color AVG_COLOR = new Color(100,100,220);
	public static final Color TICK_COLOR = Color.GRAY;
	public static final Color BG = new Color(192,192,192);
	public static final Color SELECTED_SHADE_COLOR = new Color(30,30,30,50);
	public static final Color SELECTED_OUTLINE_COLOR = new Color(30,30,30,150);
	public static final Color BEAT_COLOR = new Color(50, 88, 50);
	public static final Color PLAYBACK_COLOR = new Color(180, 0, 0);
	
	private static final Font PREVIEW_FONT = new Font("Consolas", 13, 13);
	
	private String segmentPreviewText(Graphics2D g2, Segment seg) {
		int wid = mapX(seg.end())-mapX(seg.start())-3;
		if(wid < 30) {
			return "";
		}
		
		FontMetrics fm = g2.getFontMetrics(PREVIEW_FONT);
		String str1 = seg.transcript();
		if(fm.getStringBounds(str1, g2).getWidth() <= wid) {
			return str1;
		}
		
		String str2 = str1.substring(str1.length()/2, str1.length());
		str1 = str1.substring(0, str1.length()/2);
		
		try {
			while(fm.getStringBounds(str1+".."+str2, g2).getWidth() > wid) {
				str1 = str1.substring(0, str1.length()-1);
				str2 = str2.substring(1, str2.length());
			}
		}
		catch(StringIndexOutOfBoundsException e) {
			return "";
		}
		
		return str1+".."+str2;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		float[] frame = new float[2];
		aud.getFrame(visuStart, frame);
	
		g.setColor(BG);
		g.fillRect(0, 0, getWidth(), getHeight());

//		double dashIvl = 100.0;
//		double lmod = 0.0;
		
		//draw waveform
		double msOffset = (visuEnd-visuStart)/700;
		double msIvl = msOffset/160;
	
		for(int x = 0; x < this.getWidth(); x++) {
			double ms = unmapX(x);
//			double cmod = ms % dashIvl;
//			if(cmod < lmod) {
//				g.setColor(TICK_COLOR);
//				g.drawLine(x, getHeight(), x, getHeight()-10);
//				g.drawLine(x, 0, x, 10);
//			}
//			lmod = cmod;
			
			float peak = 0;
			for(double msoffs = ms; msoffs < ms+msOffset; msoffs+=msIvl) {
				aud.getFrame(msoffs, frame);
				float abs = Math.abs(frame[0]);
				if(abs > peak) {
					peak = abs;
				}
			}
			
			float ampAvg = 0;
			{
				int count = 0;
				
				for(double msoffs = ms; msoffs < ms+msOffset; msoffs+=msIvl) {
					aud.getFrame(msoffs, frame);
					ampAvg += Math.abs(frame[0]); //average stereo channels
					count++;
				}
				ampAvg /= count;
				
				//don't let the average amplitude exceed a threshold (nicer visual)
				ampAvg = (ampAvg*3+(peak/1.5f))/4;
			}
			
			int mappedY0 = mapY(0);
			
			g.setColor(PEAK_COLOR);
			g.drawLine(x, mappedY0, x, mapY(peak));
			g.drawLine(x, mappedY0, x, mapY(-peak));
			
			g.setColor(AVG_COLOR);
			g.drawLine(x, mappedY0, x, mapY(ampAvg));
			g.drawLine(x, mappedY0, x, mapY(-ampAvg));
		}
		
		//shade selected region
		if(aud.hasSelection()) {
			int startSelX = mapX(aud.getSelectedRegionStart());
			int endSelX = mapX(aud.getSelectedRegionEnd());
			
			g.setColor(SELECTED_SHADE_COLOR);
			g.fillRect(startSelX, 0, endSelX-startSelX, getHeight());
			g.setColor(SELECTED_OUTLINE_COLOR);
			g.drawRect(startSelX, 0, endSelX-startSelX, getHeight());
		}
		
		if(aud.playing() && aud.time() >= aud.playStart()) {
			int point = mapX(aud.time());
			g.setColor(PLAYBACK_COLOR);
			g.drawLine(point, 0, point, getHeight());
		}
		
		Stroke str = g2.getStroke();
		g2.setColor(BEAT_COLOR);
		g2.setStroke(new BasicStroke(2));
		
		//draw segments
		{
			double time = aud.time();
			double vs = visuStart(), ve = visuEnd();
			for(Segment seg : aud.segments()) {
				//don't render off-screen
				if((seg.start() < vs && seg.end() < vs)
						||
					(seg.start() > ve && seg.end() > ve)) 
				{
					continue;
				}
				
				Color col = aud.typeManager().get(seg.segmentType()).color();
				col = new Color(col.getRed(), col.getGreen(), col.getBlue(), 80);
				
				if(time >= seg.start() && time <= seg.end())
					col = col.darker();
				
				g.setColor(col);
				g.fillRect(mapX(seg.start()), 0, mapX(seg.end())-mapX(seg.start()), getHeight());
				
				col = new Color(col.getRed(), col.getGreen(), col.getBlue(), 200);
				g.setColor(col);
				g.drawRect(mapX(seg.start()), 0, mapX(seg.end())-mapX(seg.start()), getHeight());
				
				//visualize breaks
				if(seg.transcript().trim().endsWith(SymbolDefinitions.SYMB_BREAK)) {
					int ex = mapX(seg.end());
					g.setColor(new Color(255-col.getRed(), 255-col.getGreen(), 255-col.getBlue(), 150).darker());
					g.fillRect(ex-2, 0, 3, getHeight());
				}
				
				if(displayPreviewText && !seg.transcript().isEmpty()) {
					String prev = segmentPreviewText((Graphics2D)g, seg);
					if(!prev.isEmpty()) {
						g.setColor(Color.DARK_GRAY);
						g.setFont(PREVIEW_FONT);
						g.drawString(prev, mapX(seg.start())+3, 15);
					}
				}
			}
		}
		
		g2.setStroke(str);
	}
}

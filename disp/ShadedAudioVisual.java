package disp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JScrollBar;

import core.Audio;

public class ShadedAudioVisual extends AudioVisual implements MouseListener, MouseMotionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6595268216660940196L;
	private BufferedImage bi;
	private double endTime;
	
	//selected range start (ms), end (ms)
	private double rStart, rEnd;
	
	private Runnable rangeChangeCallback;
	
	public ShadedAudioVisual(Audio a) {
		super(a, false);
		super.visuStart(0);
		super.visuEnd(a.length());
		
		endTime = a.length();
		bi = null;
		
		rStart = endTime/4;
		rEnd = endTime/3;
		
		movingStart = false;
		movingEnd = false;
		movingWhole = false;
		rangeChangeCallback = null;
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public double getRangeStart() {
		return rStart;
	}
	
	public double getRangeEnd() {
		return rEnd;
	}
	
	//cannot change range
	@Override
	public void visuStart(double visuStart) {}
	@Override
	public void visuEnd(double visuEnd) {}
	
	
	public static final Color SHADE_OVERLAY = new Color(0,0,0,140);
	public static final Color BOX_OUTLINE = new Color(120,0,0,240);
	
	@Override
	public void paintComponent(Graphics g) {
		if(bi == null || bi.getWidth() != getWidth() || bi.getHeight() != getHeight()) {
			bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		}
		
		super.paintComponent(bi.getGraphics());
		g.drawImage(bi, 0, 0, null);
		
		g.setColor(SHADE_OVERLAY);
		g.fillRect(0, 0, mapX(rStart), getHeight()); //left
		g.fillRect(mapX(rEnd), 0, getWidth()-mapX(rEnd), getHeight());
		
		Graphics2D g2 = (Graphics2D) g;
		Stroke old = g2.getStroke();
		g2.setColor(BOX_OUTLINE);
		g2.setStroke(new BasicStroke(2));
		g2.drawRect(mapX(rStart), 1, mapX(rEnd)-mapX(rStart), getHeight()-2);
		g2.setStroke(old);
	}
	
	public void setRangeChangeCallback(Runnable callback) {
		this.rangeChangeCallback = callback;
	}
	
	public void setRange(double msStart, double msEnd) {
		this.rStart = msStart;
		this.rEnd = msEnd;
		
		if(this.rangeChangeCallback != null)
			this.rangeChangeCallback.run();
		
		repaint();
	}
	
	
	private boolean movingStart;
	private boolean movingEnd;
	private boolean movingWhole;
	private Point origMouse = null;
	private int origStartX = -1;
	private int origEndX = -1;
	
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		int startX = mapX(rStart);
		int endX = mapX(rEnd);
		int mx = e.getPoint().x;
	
		origMouse = e.getPoint();
		origStartX = startX;
		origEndX = endX;
		/*
		if(Math.abs(startX - mx) < 5) {
			movingStart = true;
		}
		else if(Math.abs(endX - mx) < 5) {
			movingEnd = true;
		}
		else if(mx > startX && mx < endX) {
			movingWhole = true;
		}
		*/
		if(mx >= startX-5 && mx <= endX+5)
			movingWhole = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		movingStart = false;
		movingEnd = false;
		movingWhole = false;
		origMouse = null;
		origStartX = -1;
		origEndX = -1;
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(movingStart && (e.getPoint().x < mapX(rEnd)-3 && e.getPoint().x > 0)) {
			rStart = unmapX(e.getPoint().x);
			
			if(rangeChangeCallback != null) {
				rangeChangeCallback.run();
			}
			
			repaint();
		}
		else if(movingEnd && (e.getPoint().x > mapX(rStart)+3 && e.getPoint().x < getWidth())) {
			rEnd = unmapX(e.getPoint().x);
			
			if(rangeChangeCallback != null) {
				rangeChangeCallback.run();
			}
			
			repaint();
		}
		else if(movingWhole) {
			int offs = e.getPoint().x - origMouse.x;
			
			if(origStartX + offs > 0 && origEndX + offs < getWidth()) {
				rStart = unmapX(origStartX + offs);
				rEnd = unmapX(origEndX + offs);
			
				if(rangeChangeCallback != null) {
					rangeChangeCallback.run();
				}
				
				repaint();
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {}
}

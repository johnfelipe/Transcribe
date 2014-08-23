package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.iharder.dnd.FileDrop;
import disp.AudioVisual;
import disp.AudioVisualController;

public class Main {
	private static final String BASE_TITLE = "Transcribe";
	
	private static JFrame j;
	private static Audio aud;
	private static AudioVisual av;
	private static AudioTools at;
	private static AudioVisualController avc;
	
	private static File currentFile = null;
	public static File activeFile() {
		return currentFile;
	}
	public static void activeFile(File file) {
		if(file != null) {
			j.setTitle(BASE_TITLE + " : " + file.getName());
		}
		else {
			j.setTitle(BASE_TITLE);
		}
		
		currentFile = file;
	}
	
	public static boolean load(String data) {
		Loader loader = new Loader(data);
		
		loader.alternativeAudioSource(new Loader.NewAudioCallback() {
			@Override
			public File get() {
				JFileChooser jfc = new JFileChooser();
				jfc.setDialogTitle("Audio source file not found, please locate the audio source file manually.");
				
				if(jfc.showSaveDialog(j) == JFileChooser.APPROVE_OPTION) {
					return jfc.getSelectedFile();
				}
				else {
					return null;
				}
			}
		});
		
		if(loader.process()) {
			j.remove(avc);
			aud = loader.audio();
			av = loader.av();
			at = loader.at();
			avc = loader.avc();
			j.add(avc);
			j.validate();
			return true;
		}
		else {
			return false;
		}
	}
	
	public static String save() {
		Saver saver = new Saver(aud, avc, at, av);
		if(!saver.valid()) {
			return null;
		}
		return saver.save();
	}
	
	
	public static void main(String[] args) {
		aud = new Audio("chip.wav");
		av = new AudioVisual(aud);
		at = new AudioTools(aud);
		avc = new AudioVisualController(av, at);
		
		 try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (UnsupportedLookAndFeelException e) {}
	    catch (ClassNotFoundException e) {}
	    catch (InstantiationException e) {}
	    catch (IllegalAccessException e) {}
		
		j = new JFrame();
		j.setBounds(0, 0, 900, 400);
		j.add(avc);
		j.setVisible(true);
		j.setDefaultCloseOperation(3);
		j.setTitle(BASE_TITLE);
	
		new FileDrop(j, new FileDrop.Listener() {
			@Override
			public void filesDropped(File[] files) {
				//prioritize save files
				for(File file : files) {
					if(file.getAbsolutePath().endsWith(".json")) {
						activeFile(null);
						
						try {
							String data = "";
							Scanner read = new Scanner(new FileReader(file));
							while(read.hasNext())
								data += read.next();
							read.close();
							load(data);
							activeFile(file);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(j, "Error loading: " + e.getMessage());
						}
						
						return;
					}
				}

				j.remove(avc);
				aud = new Audio(files[0].getAbsolutePath());
				av = new AudioVisual(aud);
				at = new AudioTools(aud);
				avc = new AudioVisualController(av, at);
				j.add(avc);
				j.validate();
			}
		});
	}
}

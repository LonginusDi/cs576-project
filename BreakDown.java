import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

public class BreakDown{

	String filename;
	String audio;
	InputStream is;
	private InputStream waveStream;
	AudioInputStream audioInputStream = null;
	int frameCount = 0;
	int width = 480;
	int height = 270;
	long len = width*height*3;
	int audioLen = 3200;
	int readBytes = 0;
	byte[] previousImg = new byte[(int)len];
	byte[] currentImg = new byte[(int)len];
	byte[] prevideo = new byte[audioLen];
	byte[] currentvideo = new byte[audioLen];
	long totalLength;// = len * 720;

	ArrayList<Section> sectionList = new ArrayList<Section>();


	public BreakDown(String filename, String audio){
		this.filename = filename;
		this.audio = audio;
	}

	public void initialize() {
		//read video
		try {
			File file = new File(filename);
			is = new FileInputStream(file);
			totalLength = (long)file.length();
			//is.skip(1491*len);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long audiolength;
		//read audio
		FileInputStream i;
    	try {
    		File f = new File(audio);
    		audiolength = (long)f.length();
    		i = new FileInputStream(audio);
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		return;
    	}

    	this.waveStream = new BufferedInputStream(i);

    	  try {
    		audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);
    	} catch (UnsupportedAudioFileException e1) {
    		//throw new PlayWaveException(e1);
    		e1.printStackTrace();
    	} catch (IOException e1) {
    		//throw new PlayWaveException(e1);
    		e1.printStackTrace();
    	}


		MotionDetect motionDetect = new MotionDetect();


		// compare the current and previous frame
		
		//frameCount = 1491;
		boolean newSection = true;
		// if it is not the end of video, continue
		while(hasNext()){
			frameCount ++;

			System.out.println("Frame: "+frameCount);
			// start a new section, no comparsion here.
			if(newSection){
				newSection = false;
				Section currentSection = new Section(frameCount);
				sectionList.add(currentSection);

				System.out.println("Start new section ");

				try {
				int o = 0;
						int nr = 0;
						while (o < previousImg.length && (nr=is.read(previousImg, o, previousImg.length-o)) >= 0) {
							o += nr;
						}

					readBytes = audioInputStream.read(prevideo, 0,
    				prevideo.length); 
					}
					catch (IOException e){}


			}
			// continue the section, compare current frame with previous frame
			else{
				System.out.println("Continue section ");
				// read current image
				try {
					int offset = 0;
					int numRead = 0;
					while (offset < currentImg.length && (numRead=is.read(currentImg, offset, currentImg.length-offset)) >= 0) {
						offset += numRead;
					}
					// read current audio
					readBytes = audioInputStream.read(currentvideo, 0,
    				currentvideo.length);
				}
				catch (IOException e){}

				// compare with motion vector and color

				// color 10000- 20000, change sceen 17, sound maybe whatever
				boolean breakPoint = motionDetect.checkMotion(previousImg, currentImg, 200 , 30000 * 506); 
				// compare audio frame
				boolean audioBreak = audiobreak(prevideo, currentvideo, 1000000);

				if(breakPoint ||  audioBreak){
					sectionList.get(sectionList.size()-1).setEnd(frameCount);
					newSection = true;
				}
				if(!hasNext()){
					sectionList.get(sectionList.size()-1).setEnd(frameCount);
				}


				previousImg = currentImg.clone();
				prevideo = currentvideo.clone();
			}
		}

		for(Section s: sectionList){
			System.out.println(s.getTotalFrame());
		}
	}

	// check if it is the end of the video
	boolean hasNext(){
		if(frameCount * len < totalLength)
			return true;

		return false;
	}

	// compare value from two audio frame with threshold
	boolean audiobreak(byte[] pre, byte[] current, int threshold){
		int leng = current.length;
		int totalpre = 0;
		int totalcurrent = 0;
		// add all values
		for(int i = 0; i < leng; i++){
			byte p = pre[i];
            short sp = (short)(p & 0xff);
            int pp = sp;
			totalcurrent += pp;

			byte c = current[i];
            short sc = (short)(c & 0xff);
            int cc = sc;
			totalpre += cc;
		}
		// get difference and compare
		int diff = Math.abs(totalcurrent - totalpre);
		System.out.println("sound diff: "+diff);
		if( diff > threshold)
			return true;

		return false;
	}


}
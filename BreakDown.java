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
import javax.imageio.ImageIO;

public class BreakDown{

	String filename;
	String audio;
	String inputimg;
	InputStream is;
	private InputStream waveStream;
	AudioInputStream audioInputStream = null;
	int frameCount = 1;
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
	byte[] newBytes = new byte[(int)len];

	int selectedFrame = 0;

	ArrayList<Section> sectionList = new ArrayList<Section>();


	public BreakDown(String filename, String audio, String inputimg){
		this.filename = filename;
		this.audio = audio;
		this.inputimg = inputimg;
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

		File in = new File(inputimg);
		showimage(in);



		MotionDetect motionDetect = new MotionDetect();


		// compare the current and previous frame
		
		//frameCount = 1491;
		//boolean newSection = true;


		Section currentSection = new Section(frameCount);
		sectionList.add(currentSection);

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

		// if it is not the end of video, continue
		while(hasNext()){
			frameCount ++;

			System.out.println("Frame: "+frameCount);

				//System.out.println("Continue section ");
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

				// color 10000- 20000, change sceen 13- 17, sound maybe whatever
			boolean breakPoint = motionDetect.checkMotion(previousImg, currentImg, newBytes, 14.5 , 8000, frameCount-1); 
				// compare audio frame
			boolean audioBreak = audiobreak(prevideo, currentvideo, 30000);

			if(breakPoint ||  audioBreak){
				sectionList.get(sectionList.size()-1).setEnd(frameCount-1);
				Section c = new Section(frameCount);
				sectionList.add(c);

			}
			if(!hasNext()){
				sectionList.get(sectionList.size()-1).setEnd(frameCount);

			}



			previousImg = currentImg.clone();
			prevideo = currentvideo.clone();

		}

		selectedFrame = motionDetect.frame;

		for(Section s: sectionList){
			
			System.out.println("Starting: "+ s.startingFrame + "  Ending: " + s.endingFrame + "  Total: "+s.getTotalFrame());
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


	private void convert(File file) {
		BufferedImage img = null;

		try 
		{
		    img = ImageIO.read(file); // eventually C:\\ImageTest\\pic2.jpg
		    BufferedImage img1 = new BufferedImage(480, 270, BufferedImage.TYPE_INT_RGB);
		    Graphics gs = img1.createGraphics();
		   
			gs.drawImage(img, 0, 0, width, height, null);
			gs.dispose();	
			System.out.println(img1.getHeight() + "," + img1.getWidth());
		    int ind = 0;
			for(int y = 0; y < height; y++){

				for(int x = 0; x < width; x++){

					 
					newBytes[ind] =  (byte) (img1.getRGB(x, y) >> 16);
					newBytes[ind+width*height] = (byte) (img1.getRGB(x, y) >> 8);
					newBytes[ind+width*height*2] = (byte) (img1.getRGB(x, y)); 					
					ind++;
				}
			}
			
		    
		} 
		catch (IOException e) 
		{
		    e.printStackTrace();
		}
		
	}

	private void showimage(File file) {
		BufferedImage img1;
		BufferedImage img2;
		int width1 = 1280;
		int height1 = 720;
		img1 = new BufferedImage(width1, height1, BufferedImage.TYPE_INT_RGB);
		img2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		try {
			

			InputStream is1 = new FileInputStream(file);

			long len1 = file.length();

			byte[] bytes1 = new byte[(int)len1];
			int offset = 0;
			int numRead = 0;
			while (offset < bytes1.length && (numRead=is1.read(bytes1, offset, bytes1.length-offset)) >= 0) {
				offset += numRead;
			}
			int ind = 0;
			for(int y = 0; y < height1; y++){

				for(int x = 0; x < width1; x++){

					byte a = 0;
					byte r = bytes1[ind];
					byte g = bytes1[ind+height1*width1];
					byte b = bytes1[ind+height1*width1*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img1.setRGB(x,y,pix);
					ind++;
				}
			}

			Graphics gs = img2.createGraphics();
			gs.drawImage(img1, 0, 0, width, height, null);
			gs.dispose();			
			ind = 0;
			for(int y = 0; y < height; y++){

				for(int x = 0; x < width; x++){

					 
					newBytes[ind] =  (byte) (img2.getRGB(x, y) >> 16);
					newBytes[ind+width*height] = (byte) (img2.getRGB(x, y) >> 8);
					newBytes[ind+width*height*2] = (byte) (img2.getRGB(x, y)); 					
					ind++;
				}
			}
			
			//lbIm2.setIcon(new ImageIcon(img3));
			
			
	
		

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
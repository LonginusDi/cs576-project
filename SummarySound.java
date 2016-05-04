//package org.wikijava.sound.playWave;
import java.util.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class SummarySound implements Runnable{

	private InputStream waveStream;
	AudioInputStream audioInputStream = null;
    private final int EXTERNAL_BUFFER_SIZE = 3200; // 
    SourceDataLine dataLine = null;
       // int frame_pre_sec = 3200;
    byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
    int readBytes = 0;
    int start= 1;
    int end = 4500;
    int framecount = 1;
    int fps = 15;

    static ArrayList<Integer> breakFrame = new ArrayList<Integer>();
    int sframe = 0;
    int lowerBound = 62;
    int upperBound = 150;

    sync s;

    /**
     * CONSTRUCTOR
     */
    public SummarySound(String filename, sync sy, ArrayList<Integer> breakFrame, int lb, int ub) {

    	FileInputStream i;
    	try {
    		i = new FileInputStream(filename);
            
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		return;
    	}


    	this.waveStream = new BufferedInputStream(i);
    	this.s = sy;

        this.breakFrame = breakFrame;
        this.lowerBound = lb;
        this.upperBound = ub;
    }

    public void play() throws PlayWaveException {
    	


    	try {
    		audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);
    	} catch (UnsupportedAudioFileException e1) {
    		throw new PlayWaveException(e1);
    	} catch (IOException e1) {
    		throw new PlayWaveException(e1);
    	}

	// Obtain the information about the AudioInputStream
    	AudioFormat audioFormat = audioInputStream.getFormat();
    	Info info = new Info(SourceDataLine.class, audioFormat);

	// opens the audio channel

    	try {
    		dataLine = (SourceDataLine) AudioSystem.getLine(info);
    		dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
    	} catch (LineUnavailableException e1) {
    		throw new PlayWaveException(e1);
    	}

	// Starts the music :P
    	dataLine.start();

    	try {
           
    		readBytes = audioInputStream.read(audioBuffer, 0,
    			audioBuffer.length);
		// if (readBytes >= 0){
		//     dataLine.write(audioBuffer, 0, readBytes);
		// }
	 //    }
    	} catch (IOException e1) {
    		throw new PlayWaveException(e1);
    	} 

    }

    public boolean playOneSec() throws PlayWaveException {
    	for(int j = 0; j < fps; j++){
    	try{

    		if (readBytes >= 0){

                int diff = 0;
                if(sframe < breakFrame.size() - 1){
                    diff = breakFrame.get(sframe + 1) - breakFrame.get(sframe);
                }

                if(sframe < breakFrame.size()){

                    if(framecount == breakFrame.get(sframe)){
                        if(diff > lowerBound && diff < upperBound){
                            framecount++;
                            System.out.println(breakFrame.get(sframe) + " Don't Break!!");
                            System.out.println("Diff is " +  diff);


                            dataLine.write(audioBuffer, 0, readBytes);
                            readBytes = audioInputStream.read(audioBuffer, 0,
                            audioBuffer.length);

                        }else{

                            byte[] t = new byte[3200*diff];

                            framecount += diff;
                            int readBytes = audioInputStream.read(t, 0, t.length);

                            // while(framecount < breakFrame.get(sframe + 1) ){
                            //     int readBytes = audioInputStream.read(t, 0,
                            //     t.length);
                            //     framecount++;
                            // }

                            System.out.println(breakFrame.get(sframe) + " Break!!");
                            System.out.println("Diff is " +  diff);
                        }

                        if(sframe < breakFrame.size()){
                            sframe++;
                        }

                    }else{
                        // if(diff > 45 && diff < 150){
                        //  framecount++;
                        //  System.out.println(framecount+" - "+breakFrame[sframe]);
                        // }else{
                        //  is.skip(2*len); /// 1500 -2000 break;
                        //  framecount += 2;
                        //  System.out.println(breakFrame[sframe] + "Break!!!!");
                        //  System.out.println("Diff is " +  diff);
                        //  sframe++;
                        // }
                        framecount++;

                        dataLine.write(audioBuffer, 0, readBytes);
                        readBytes = audioInputStream.read(audioBuffer, 0,
                        audioBuffer.length);

                        System.out.print("-");
                    }
                }
            
    		}
    		else{
    			return false;
    		}
    	} catch (IOException e1) {
    		throw new PlayWaveException(e1);
    	}
        if(framecount > end){
            return false;
        }
        // framecount++;

    }

    	s.checkSync();
    	//System.out.println("audio one sec");

    	return true;
    }

    @Override
    public void run(){
    	try{
    		play();
            byte[] t = new byte[3200];

            while(framecount < start){
                int readBytes = audioInputStream.read(t, 0,
                t.length);
                framecount++;
            }

    		while(playOneSec()){}

    	}	catch (PlayWaveException e1) {
    		e1.printStackTrace();
    	} 
        catch (IOException e1) {
            e1.printStackTrace();
        }
    	finally {
	    // plays what's left and and closes the audioChannel
    		dataLine.drain();
    		dataLine.close();
    	}

    }

    public void setPos(int start, int end){
        this.start = start;
        this.end = end;
        



    }

}

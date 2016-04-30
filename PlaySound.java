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
public class PlaySound implements Runnable{

	private InputStream waveStream;
	AudioInputStream audioInputStream = null;
    private final int EXTERNAL_BUFFER_SIZE = 84288; // 128Kb
    SourceDataLine dataLine = null;
    byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
    int readBytes = 0;

    int[] pre;

    sync s;

    /**
     * CONSTRUCTOR
     */
    public PlaySound(String filename, sync sy) {

        pre = new int[24];
    	FileInputStream i;
    	try {
    		i = new FileInputStream(filename);
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		return;
    	}

    	this.waveStream = new BufferedInputStream(i);
    	this.s = sy;
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
	    // while (readBytes != -1) {
    		readBytes = audioInputStream.read(audioBuffer, 0,
    			audioBuffer.length);
		// if (readBytes >= 0){
		//     dataLine.write(audioBuffer, 0, readBytes);
		// }
	 //    }
            for(int i = 0; i < this.EXTERNAL_BUFFER_SIZE; i++){
                byte r = audioBuffer[i];
                short sr = (short)(r & 0xff);
                int rr = sr;
                pre[i/3512] += rr;
            }
    	} catch (IOException e1) {
    		throw new PlayWaveException(e1);
    	} 

    }

    public boolean playOneSec() throws PlayWaveException {
    	
    	try{

    		if (readBytes >= 0){
    			dataLine.write(audioBuffer, 0, readBytes);

            for(int i = 1; i < 24; i++){
                double diff = (pre[i] - pre[i-1]) * 1.000 / 3512.000;
                System.out.println(diff);
            }

    			readBytes = audioInputStream.read(audioBuffer, 0,
    				audioBuffer.length);
            for(int i = 0; i < 24; i++){
                pre[i] = 0;
            }

            for(int i = 0; i < this.EXTERNAL_BUFFER_SIZE; i++){
                byte r = audioBuffer[i];
                short sr = (short)(r & 0xff);
                int rr = sr;
                pre[i/3512] += rr;
            }
            
    		}
    		else{
    			return false;
    		}
    	} catch (IOException e1) {
    		throw new PlayWaveException(e1);
    	}
    	s.checkSync();
    	System.out.println("audio one sec");

    	return true;
    }

    @Override
    public void run(){
    	try{
    		play();
    		while(playOneSec()){}

    	}	catch (PlayWaveException e1) {
    		e1.printStackTrace();
    	} 
    	finally {
	    // plays what's left and and closes the audioChannel
    		dataLine.drain();
    		dataLine.close();
    	}

    }


}

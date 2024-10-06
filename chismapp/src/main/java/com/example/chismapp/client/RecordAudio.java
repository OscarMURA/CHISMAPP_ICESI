package com.example.chismapp.client;

import java.io.ByteArrayOutputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * The {@code RecordAudio} class is responsible for recording audio from the microphone using a specified
 * {@code AudioFormat} and writing the captured audio data into a {@code ByteArrayOutputStream}.
 * It implements the {@code Runnable} interface so it can be executed in a separate thread for continuous recording.
 */
public class RecordAudio implements Runnable {

    private AudioFormat format;
    private ByteArrayOutputStream out;
    private volatile boolean stop = false; // Field to control when to stop recording
    private TargetDataLine targetLine; // Declaration of the audio capture line

    /**
     * Constructs a new {@code RecordAudio} object with the specified audio format and output stream.
     *
     * @param format the {@code AudioFormat} that defines how audio should be captured.
     * @param out    the {@code ByteArrayOutputStream} where captured audio data will be written.
     */
    public RecordAudio(AudioFormat format, ByteArrayOutputStream out) {
        this.format = format;
        this.out = out;
    }

    /**
     * Stops the audio recording. This method sets the {@code stop} flag to true, which signals
     * the recording loop in the {@code run} method to stop capturing audio.
     */
    public void stopRecording() {
        stop = true;
    }

    /**
     * Continuously captures audio from the microphone using the specified {@code AudioFormat}
     * until the {@code stopRecording} method is called.
     * <p>
     * The captured audio data is written to the provided {@code ByteArrayOutputStream}.
     * </p>
     */
    @Override
    public void run() {
        int bytesRead;
        try {
            // Prepare the audio capture line with the specified format
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);

            // Start capturing audio
            targetLine.start();

            // Buffer to store audio data read from the microphone
            byte[] buffer = new byte[targetLine.getBufferSize() / 5];

            // Continue capturing audio until the stop flag is set
            while (!stop) {
                bytesRead = targetLine.read(buffer, 0, buffer.length);
                out.write(buffer, 0, bytesRead);  // Write the audio data to the output stream
            }

            // Stop and close the audio line once recording is done
            targetLine.stop();
            targetLine.close();

        } catch (Exception e) {
            // Handle any exceptions that occur during audio capture
            e.printStackTrace();
        }
    }
}

package com.example.chismapp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

/**
 * The `HistorialRecorder` class in Java is used to record different types of messages and generate a
 * historical text file with the recorded messages.
 */
public class HistorialRecorder{

    private StringBuffer registered;
    private DateTimeFormatter myFormatObj;

// The `public HistorialRecorder()` constructor in the `HistorialRecorder` class is initializing the
// `registered` variable with a new `StringBuffer` object and the `myFormatObj` variable with a
// `DateTimeFormatter` object that formats date and time in the pattern "dd-MM-yyyy HH_mm_ss". This
// constructor is called when an instance of the `HistorialRecorder` class is created, setting up
// initial values for these variables.
    public HistorialRecorder(){
        registered = new StringBuffer();
        myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH_mm_ss");
    }

/**
 * The function `addMessage` in Java appends a message with a specific type of record to a registered
 * log, handling different cases based on the type.
 * 
 * @param msg The `msg` parameter in the `addMessage` method represents the message content that you
 * want to add to the registered log. It could be a text message, audio file name, call details, group
 * information, or any other relevant information based on the `eTypeRecord` type specified.
 * @param type The `type` parameter in the `addMessage` method is an enum type called `eTypeRecord`. It
 * is used to determine the type of message being added to the `registered` object. The method uses a
 * switch statement to handle different cases based on the `type` of the message.
 * @return The method `addMessage` is returning a boolean value `false`.
 */
    public boolean addMessage(String msg, eTypeRecord type){
        String line;
        switch(type){
            case TEXT -> line = "Text:  "; 
            case AUDIO -> line = "Audio: ";
            case STARTED_CONNECTION -> line = "Initialicing connextion at time: " + LocalDateTime.now().format(myFormatObj) + ", with the username: ";
            case CALL -> line = "Call:  ";
            case GROUP -> line = "Group: ";
            case RECEIVED -> line = "";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        }
        registered.append(line);
        if(type != eTypeRecord.CALL || type != eTypeRecord.AUDIO) {
            registered.append(msg + "\n");
        }else{
            registered.append("\n");
        }
        return false;
    }

/**
 * The `generate` method creates a file named "Historical.txt" in the "target/History" directory,
 * writes some data to it, and prints a success message or an error message if writing the file fails.
 */
    public void generate() {
        String nombreArchivo = System.getProperty("user.dir") + File.separator + "target" + File.separator + "History" + File.separator + "Historical.txt";
        
        // Crear la carpeta si no existe
        File directory = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "History");
        if (!directory.exists()) {
            boolean dirCreated = directory.mkdirs();  // Crear directorios
        }

        // Escribir el archivo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
            writer.write("--------------------------------------------------------\n");
            writer.write(registered.toString());
            System.out.println("Archivo creado exitosamente en: " + nombreArchivo);
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo: " + nombreArchivo);
            e.printStackTrace();
        }
    }
}
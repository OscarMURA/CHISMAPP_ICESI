package com.example.chismapp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class HistorialRecorder{

    private StringBuffer registered;
    private DateTimeFormatter myFormatObj;

    public HistorialRecorder(){
        registered = new StringBuffer();
        myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH_mm_ss");
    }

    public boolean addMessage(String msg, eTypeRecord type){
        String line;
        switch(type){
            case TEXT -> line = "Text:  "; 
            case AUDIO -> line = "Audio:";
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
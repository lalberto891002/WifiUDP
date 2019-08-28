package com.dominando.android.myapplication;

import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ProgramarWifi {
    String path;

    public static int ESTADO;
    int pacotes;
    int size;
    BufferedReader br;
    ArrayList<String> lineas;
    ArrayList<String>paquetes_enviar;
    public ProgramarWifi(String path) {
        this.path = path;
        pacotes = 0;

        lineas = new ArrayList<>();
        paquetes_enviar=new ArrayList<>();
        String linea;

        File arq = new File(path, "fw.hex");
        try {
            br = new BufferedReader(new FileReader(arq));

            while((linea=br.readLine())!=null)
            {
                lineas.add(linea);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        size=lineas.size();
        StringBuilder builder=new StringBuilder();
        int contador=0;
        for (String s:lineas
             ) {

            builder.append(s+'z');
            contador+=s.length();
            if(contador>=2048)
            {
                paquetes_enviar.add(builder.toString());
                contador=0;
                builder=new StringBuilder();
            }
            if(s.contains("FF"))
                paquetes_enviar.add(builder.toString());
        }
    }


    public String programar()
    {
        switch (ESTADO) {
            case 0:
                pacotes=0;
                ESTADO++;
                return "$FW,C\r\n";
            case 1:
              if(paquetes_enviar.get(pacotes).contains("FF"))
                  ESTADO++;
              return "$FW,T"+paquetes_enviar.get(pacotes++)+",\r\n";

            case 2:
                return "$FW,F\r\n";
            default:
                return null;
        }
    }

}

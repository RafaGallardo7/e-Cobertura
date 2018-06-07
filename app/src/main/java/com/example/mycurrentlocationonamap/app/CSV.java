/*
Copyright (c) 2014, 2015 Jaramillo Juan Carlos, Gallardo Rafael

e-Cobertura is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        e-Cobertura is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with e-Cobertura.  If not, see <http://www.gnu.org/licenses/>.*/


package com.example.mycurrentlocationonamap.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class CSV extends Activity implements OnClickListener{

    public static final String EMAIL = "juan.jaramillov@outlook.com";
    public static PrintStream print;
    private Button mEnviar, mAtras;
    public MainActivity main;
    public static double latitud;
    public static LatLng regUbicacion[];
    public static int sizeRegUbi;
    public static String fecha[];
    public static String hora[];
    public static int regPotencia[];
    public static String regCellID[];
    public static String regTipoRed[];
    public static int regLac[];
    public static int regBer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.csv);
        main = new MainActivity();

        regUbicacion = new LatLng[10000];
        regPotencia = new int[10000];
        fecha = new String[10000];
        hora = new String[10000];
        regCellID = new String[10000];
        regTipoRed = new String[10000];
        regLac = new int[10000];

        View botonTXT = findViewById(R.id.escribirTXT);
        botonTXT.setOnClickListener(this);
    }

    double valorDado;

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.escribirTXT:
                valorDado = Math.floor(Math.random()*100000);
                guardarTXT(this, "Reporte"+valorDado+".txt", main);
                Toast toastTXT = Toast.makeText(getApplicationContext(), "Archivo .txt almacenado "+
                        "en Carpeta Files como: Reporte"+valorDado+".txt", Toast.LENGTH_SHORT);
                toastTXT.show();
                break;
        }
    }

    public void setPosicion(LatLng pos[]) {
        regUbicacion = pos;
        sizeRegUbi = regUbicacion.length;
    }

    public void setDateFecha(String dat[]) {
        fecha = dat;
    }

    public void setHora(String dat[]) {
        hora = dat;
    }

    public void setCellID(String cellID[]) {
        regCellID = cellID;
    }

    public void setTipoRed(String tipoRed1[]) {
        regTipoRed = tipoRed1;
    }

    public void setLac(int lac[]) {
        regLac = lac;
    }

    public void setPotencia (int pot[]) {
        regPotencia = pot;
    }

    public boolean guardarTXT(Context contexto, String nombreArchivo, MainActivity main) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            return false;
        }
        // Crea una ruta donde poner nuestro archivo
        File archivo = new File(contexto.getExternalFilesDir(null), nombreArchivo);
        print = null; // Declara un objeto print stream
        boolean exito = false;
        try {

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            String simOperadora = telephonyManager.getSimOperatorName();

            OutputStream os = new FileOutputStream(archivo);

            // Conecta print stream a output stream
            print = new PrintStream(os);
            for (int i=0; i<=sizeRegUbi; i++) {
                print.print("\n");
                print.print(i+","+hora[i]+","+fecha[i]+","+regUbicacion[i].latitude
                        +","+regUbicacion[i].longitude+","+regPotencia[i]+","+regCellID[i]+","+regTipoRed[i]+","+simOperadora+",");
            }

            exito = true;
        } catch (IOException e) {
        } catch (Exception e) {
        } finally {
            try {
                if (null != print)
                    print.close();
            } catch (Exception ex) {
            }
        }
        return exito;
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

}


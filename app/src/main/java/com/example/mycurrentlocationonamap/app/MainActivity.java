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
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.telephony.CellSignalStrengthGsm;
import  android.telephony.SignalStrength;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity  implements LocationListener, OnMarkerClickListener, Serializable{

    //Declaracion de Variables
    SharedPreferences pref;
    private static String CONSUMER_KEY = "PTbxbI2t2UPa5IGAWBTDNPwzo";
    private static String CONSUMER_SECRET = "LdL9IioLBidvmlO4PhDU0QsMAvzcAL2544MqyKvvnaILfwMoTG";

    GoogleMap googleMap;
    MapFragment mapFragment;
    //Diferentes Manager
    LocationManager locationManager;
    PhoneStateMonitor phoneStateListener;
    public TelephonyManager manager;

    //Clases para determinar parametros calidad en distintas tecnologias getASU
    public CellSignalStrengthGsm cellSignalGsm;

    //PAra Obtener BER GSM
    public SignalStrength sigStrength;
    public GsmCellLocation cellLocation;
    public GsmCellLocation cellID[];
    public LatLng currentPosition;
    public double longitud;
    public double latitud;

    //Boton Detalles
    private Button mDetalles;
    private Button mAcerca;
    private Button mEstadisticas;
    private Button mEnviar;
    //Boton Salir
    public Button salir;

    //Circulo
    public Circle circulo[];
    public LatLng posCircle[];
    public double radCircle[];

    //Incrementa al actualizar datos de posicion 1.2 seg
    public int cntActLoc = 0;
    //registra potencias y posiciones al actualizar datos de gps
    public int regPot[];
    public LatLng regPos[];
    public String tipoRed;
    public String regTipoRed[];
    public int regLac[];

    //Variables aux para recuperar informacion del marker seleccionado
    public int currentPotencia = 0;
    public String currentCellID;
    public Marker potMarker;

    //Registros en cada contPosPot
    public String idMarker[];
    public String idCell[];

    public static PrintStream auxPrint;

    //INstancia clase CSV
    public CSV csv;
    public Estadistica stat;

    //var para habilitar o deshabilitar captura de datos
    public boolean bandCapturar = false;

    //Clase utilizada para fecha y hora
    public String stringDate;
    public String hora;
    public String mes;
    public String año;
    public String dia;
    public String fecha;
    public String regFecha[];
    public String regHora[];

    //DATOS DEL TELEFONO
    public String imei;
    public int lac;
    public int gsmAsu;
    public boolean bandEnviar = false;

    public int cntVerde;
    public int cntAmarillo;
    public int cntRojo;
    public int cntNegro;
    public boolean bandStats = false;

    //Metodo generado al cargar el programa
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createMapView();
        detalles();
        salida();
        estadistica();
        enviar();
        notificacion();
        auxPrint = null;
        csv = new CSV();
        stat = new Estadistica();

        circulo = new Circle[10000];
        posCircle = new LatLng[10000];
        radCircle = new double[10000];
        regPot = new int[10000];

        //Registra posiciones cada vez que se acutaliza gps
        regPos = new LatLng[10000];
        idMarker = new String[10000];
        idCell = new String[10000];
        cellID = new GsmCellLocation[10000];

        regFecha = new String[10000];
        regHora = new String[10000];
        regTipoRed = new String[10000];
        regLac = new int[10000];

        // Toma el LocationManager desde el servicio de sistema LOCATION_SERVICE
        //Servicios de locacion
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Crea un objeto criteria necesario para retribuir al proveedor
        Criteria criteria = new Criteria();
        // Toma el nombre de la mejor variable proveedora
        String provider = locationManager.getBestProvider(criteria, true);
        // El proveedor manda una actividad GPS cada 1.2 segundos
        locationManager.requestLocationUpdates(provider, 1200, 0, this);

        //Creating the Object of PhoneStateMonitor (User Defined Subclass of android.telephony.PhoneStateListener)
        phoneStateListener = new PhoneStateMonitor(this);

        //Servicios telefono
        manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);//Tomando la instancia de TelephonyManager
        cellLocation = (GsmCellLocation) manager.getCellLocation();

        //Clase para obtener parametros como LAC CellID
        phoneStateListener.setCellLocation(cellLocation);

        //Zoom Inicial al arrancar el programa!
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));

        //Click Listener de Google Maps
//        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerClickListener(this);
    }

    //Metodo para obtener fecha y hora actual
    public void updateTime () {
        stringDate = new Date().toString();
        hora = stringDate.substring(11, 19);
        mes = stringDate.substring(4,7);
        getMes(mes);
        dia = stringDate.substring(8,10);
        año = stringDate.substring(30,34);
        fecha = dia+"/"+mes+"/"+año;
    }

    public void getParametrosCell() {
        tipoRed = getTipoRed(manager.getNetworkType());
        imei = manager.getDeviceId();
        lac = cellLocation.getLac();
    }

    //Tomar el tipo de red y convertirlo a string
    private String getTipoRed(int tipo){
        String stringTipoR = "DESCONOCIDO";
        switch(tipo)
        {
            case TelephonyManager.NETWORK_TYPE_GPRS:        stringTipoR = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:        stringTipoR = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:        stringTipoR = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:        stringTipoR = "HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:        stringTipoR = "HSPA+";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:        stringTipoR = "LTE";
                break;
            default:stringTipoR = "DESCONOCIDO"; break;
        }
        return stringTipoR;
    }

    //transformar los meses de letras a numeros
    public String getMes(String mess) {
        if (mes.equals("Ene")){
            mes = "01";
        }else if (mes.equals("Feb")) {
            mes = "02";
        }else if (mes.equals("Mar")) {
            mes = "03";
        }else if (mes.equals("Abr")) {
            mes = "04";
        }else if (mes.equals("May")) {
            mes = "05";
        }else if (mes.equals("Jun")) {
            mes = "06";
        }else if (mes.equals("Jul")) {
            mes = "07";
        }else if (mes.equals("Ago")) {
            mes = "08";
        }else if (mes.equals("Sep")) {
            mes = "09";
        }else if (mes.equals("Oct")) {
            mes = "10";
        }else if (mes.equals("Nov")) {
            mes = "11";
        }else if (mes.equals("Dic")) {
            mes = "12";
        }
        return mes;
    }

    //Metodo Detalles que llama a Clase Detalles. (BOTON)
    public void detalles(){
        mDetalles = (Button) findViewById(R.id.detalles);
        mDetalles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Comienza ActivityDetalles
                //Subclase y Layout
                Intent i = new Intent(MainActivity.this, Detalles.class);
                startActivity(i);
            }
        });
    }

    //Metodo para Alerta de Salida y Boton Salida
    public void salida(){
        salir = (Button) findViewById(R.id.salir);
        salir.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                alerta();
            }
        });
    }

    //Metodo para Alerta de Salida
    public void alerta(){
           //Dialogo Alerta Salida
            AlertDialog.Builder sal;
            sal = new AlertDialog.Builder(this);
            sal.setMessage("Desea Salir de la Aplicación?").setCancelable(false).setPositiveButton("SI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alerta = sal.create();
            //Titulo para AlertDialog
            alerta.setTitle("e-Cobertura");
            //Icono para AlertDialog
            alerta.show();
    }

    //Metodo para Estadisticas [Boton]
    public void estadistica(){
        mEstadisticas = (Button) findViewById(R.id.estadisticas);
        mEstadisticas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bandStats = true;
                Intent i = new Intent(MainActivity.this, stat.getClass());
                startActivity(i);
            }
        });
    }

    public void acerca() {
        mAcerca = (Button) findViewById(R.id.acerca);
        mAcerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Acerca.class);
                startActivity(i);
            }
        });
    }

    //Metodo para Enviar [Boton]
    public void enviar(){
        mEnviar = (Button) findViewById(R.id.enviar);
        mEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bandEnviar = true;
                final Chronometer myChronometer = (Chronometer)findViewById(R.id.chronometer);
                myChronometer.stop();
                Toast toastC = Toast.makeText(getApplicationContext(), "Tiempo Transcurrido: " + myChronometer.getText(), Toast.LENGTH_LONG);
                toastC.setGravity(Gravity.CENTER| Gravity.TOP, 0, 0);
                toastC.show();
                Intent i = new Intent(MainActivity.this, CSV.class);
                startActivity(i);
            }
        });
    }


    /////////////////////////////
    //Resumen, Pausa, Destry
    /////////////////////////////

    //Generado al Prender la pantalla del cell
    //Escucha de potencia de la senal
    @Override
    protected void onResume() {
        super.onResume();
        manager.listen(phoneStateListener, phoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    //Agrega un marker en pantalla cuando se obtienen datos sobre posicion
    private void drawMarkers(Location location) {
        currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        // Agrega un marker al mapa indicando la posicion actual
        int image = getIcon(phoneStateListener.potenciaSenal);
        potMarker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitud, longitud))
                        .title("Informacion:")
                        .icon(BitmapDescriptorFactory.fromResource(image))
        );
    }


    //Metodo responde al evento click sobre markers
    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng posPressMarker = marker.getPosition();
        String idMarker = marker.getId();

        //Llamar metodo que devuelve la potencia y cellId referente al punto seleccionado
        getPotencia(idMarker);
        getCellId(idMarker);

        //Agregamos cuadro de texto al presionar markers
        marker.setSnippet("Potencia: " + currentPotencia + "dBm" + "\n\n" + "Cell Id:" +currentCellID);
        marker.showInfoWindow();
        return false;
    }

    //Metodo se ejecuata cuando se actualizan los valores de la posicion 1.2 seg
    @Override
    public void onLocationChanged(Location location) {
        cntActLoc++;

        //Reinicia el Registro de Datos
        if (bandCapturar == true) {
            cntActLoc = 0;
            cntVerde=0;
            cntAmarillo=0;
            cntRojo=0;
            cntNegro=0;

            //Reiniciar Vectores
            regPos = new LatLng[10000];
            googleMap.clear();
            bandCapturar = false;
        }

        //Asignamos datos
        latitud = location.getLatitude();
        longitud = location.getLongitude();

      if (googleMap != null)
        {
            //Registro de posiciones[] y potencias[] --> regPos[1], regPot[1]
            regPos[cntActLoc] = new  LatLng(latitud, longitud);
            regPot[cntActLoc] = phoneStateListener.potenciaSenal;

            csv.setPosicion(regPos);
            csv.setPotencia(regPot);

            //Metodo obtiene datos de hora y fecha
            updateTime();
            getParametrosCell();

            regTipoRed[cntActLoc] = tipoRed;
            regLac[cntActLoc] = lac;
            csv.setTipoRed(regTipoRed);
            csv.setLac(regLac);

            //Guarda registros de Fecha y Hora en cada actualizacion de posicion
            regFecha[cntActLoc] = fecha;
            regHora[cntActLoc] = hora;

            csv.setDateFecha(regFecha);
            csv.setHora(regHora);

            if (bandEnviar == true) {
                for (int i=1; i<=cntActLoc; i++) {
                    if (regPot[i] <=0 && regPot[i] >= -85) {
                        cntVerde++;
                    } else if (regPot[i] < -85 && regPot[i] >= -98) {
                        cntAmarillo++;
                    } else if (regPot[i] < -98 && regPot[i] >= -110) {
                        cntRojo++;
                    } else if (regPot[i] < -110) {
                        cntNegro++;
                    }
                }

//                Enviamos valores a la clase Estadistica
                stat.cntVerde = cntVerde;
                stat.cntAmarillo = cntAmarillo;
                stat.cntRojo = cntRojo;
                stat.cntNegro = cntNegro;

                stat.regPotencias = regPot;
                stat.cntMuestras = cntActLoc;

                bandEnviar = false;

            }

            //Llama al metodo dibujar los puntos correspondientes a la potencia de la senal!
            drawMarkers(location);

            //Registro ids[]---> idMarker[1]
            idMarker[cntActLoc] = potMarker.getId();

            //registro cellIds[]
            idCell[cntActLoc] = String.valueOf(cellLocation.getCid());

            csv.setCellID(idCell);

            //Actualiza la posicion de la camara a la posicion actual!
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentPosition));


            if (bandStats == true) {
                 bandStats = false;
            }
        }

    }

    //Metodo que devuelve la potencia referente al id del marker seleccionado
    public int getPotencia (String idMark) {
        for (int i = 0; i <= cntActLoc; i++) {
            if (idMark.equals(idMarker[i])) {
                currentPotencia = regPot[i];
            }
        }
        return currentPotencia;
    }

    //Metodo que devuelve el cellID referente al id del marker seleccionado
    public String getCellId(String idMark) {
        for (int i = 0; i <= cntActLoc; i++) {
            if (idMark.equals(idMarker[i])) {
                currentCellID = idCell[i];
            }
        }
        return currentCellID;
    }

    //Metodo que establece un icono diferente dependiendo de la potencia recibida
    public int getIcon(int potencia)
    {
        int image = 0;
        if (potencia <= 0 && potencia >= -85){
            image = R.drawable.ve;
        } else if (potencia < -85 && potencia >= -98) {
            image = R.drawable.am;
        } else if (potencia < -98 && potencia >= -110) {
            image = R.drawable.ro;
        } else if (potencia < -110) {
            image = R.drawable.negro;
        }
        return image;
    }


    //Metodo que se encargar de escribir el archivo de la clase CSV
    public void getInfo(OutputStream os) {
        auxPrint = new PrintStream(os);
        auxPrint.println("ID      LATITUD " + latitud);
    }

    //Metodo utilizado para crear el mapa
    private void createMapView(){
        try {
            if(null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.map)).getMap();

                /**
                 * Si el mapa sigue siendo null despues de intentar la inicializacion
                 * smostrara un error al usuario
                 */
                if(null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creando mapa",Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Muestra el menu; esto agrega items a la barra de accion
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Metodo que crea la opciones del menu principal de apps de android
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Maneja la barra de accion cuando se hace click. La barra de accion
        // automaticamente manejara clicks en el button Home/Up
        switch (item.getItemId()) {
            case R.id.detalles:
//                detalles1();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }
    @Override
    public void onProviderEnabled(String s) {
    }
    @Override

    public void onProviderDisabled(String s) {
    }

    //Metodo para crear notificacion
    private final int NOTIFICATION_ID = 1010;
    private void triggerNotification(){

        String cellId1 = String.valueOf(cellLocation.getCid());
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.c, "e-Cobertura", System.currentTimeMillis());

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notificacion);
        contentView.setImageViewResource(R.id.notificacion, R.drawable.c);
        contentView.setTextViewText(R.id.txt_notificacion, "Potencia:   " + phoneStateListener.potenciaSenal + "dBm" + "\n" +
                                                           "Cell Id:      " + cellId1  + "\n" +
                                                           " LAC:         " + lac);
        notification.contentView = contentView;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    //Metodo para barra de notificacion/comenzar
    public void notificacion() {

        Button btn = (Button)findViewById(R.id.comenzar);
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Activa la bandera de capturar datos
                bandCapturar = true;
                Toast toast1 = Toast.makeText(getApplicationContext(), "Capturando datos...", Toast.LENGTH_SHORT);
                toast1.show();

                final Chronometer myChronometer = (Chronometer)findViewById(R.id.chronometer);
                myChronometer.setBase(SystemClock.elapsedRealtime());
                myChronometer.start();

                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        triggerNotification();
                    }
                };
                timer.schedule(timerTask, 3000);
            }
        });
    }
}
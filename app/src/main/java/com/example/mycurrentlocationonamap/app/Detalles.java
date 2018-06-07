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
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class Detalles extends Activity {

    public static final String EMAIL = "juan.jaramillov@outlook.com";

    public CSV csv;
    public String regTipoRed[];
    public int regLac[];
    public int cnt = 0;
    private boolean mDone = false;
    private TextView mText = null;
    public String mTextStr;
    private TelephonyManager mManager;

    private CellLocation mCellLocation;
    private SignalStrength mSignalStrength;

    private static final int ID_ESTADO_SERVICIO = 0;
    private static final int ID_ESTADO_LLAMADA = 1;
    private static final int ID_ESTADO_CONEXION = 2;
    private static final int ID_NIVEL_SEÑAL = 3;
    private static final int ID_INFO_NIVEL_SEÑAL = 4;
    private static final int ID_DIRECCION_DATA = 5;
    private static final int ID_INFO_DISPOSITIVO = 6;


    public double latitud;
    public double longitud;

    String stringTipoR = "DESCONOCIDO";

//    TelephonyManager manager;
//    GsmCellLocation cellLocation;

//    MainActivity main;


    //Para latitud y Longitud
    TextView textLatitud, textLongitud;
    LocationManager miManagerLocacion;
    String PROVIDER = LocationManager.GPS_PROVIDER;

    private static final String NOMBRE_APP = "Detalles";
    private static final int EXCELENTE_SEÑAL = 75;
    private static final int BUENA_SEÑAL = 50;
    private static final int SEÑAL_MODERADA = 25;
    private static final int BAJA_SEÑAL = 0;

    private static final int[] ids = {
            R.id.estadoServicio,
            R.id.estadoLlamada,
            R.id.estadoConexion,
            R.id.NivelSeñal,
            R.id.infoNivelSeñal,
            R.id.DireccionData,
            R.id.infoDispositivo
    };

     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle);
         csv = new CSV();
         regTipoRed = new String[10000];
         regLac = new int[10000];
         mManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

         //Lat y Long
         textLatitud = (TextView)findViewById(R.id.latitud);
         textLongitud = (TextView)findViewById(R.id.longitud);
         miManagerLocacion = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//         cellLocation = (GsmCellLocation) manager.getCellLocation();

         //Toma ultima locacion conocida, si es que hay
         Location location = miManagerLocacion.getLastKnownLocation(PROVIDER);
         mostrarMiLocacion(location);
         miLocacion(location);

         //Se llama a dos metodos
        listenerNivelSeñal();

         //Registrar el Listener con el telephony manager
         mManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                 PhoneStateListener.LISTEN_CELL_LOCATION);
    }

    //Listener para Signal Strength
    final PhoneStateListener mListener = new PhoneStateListener()
    {
        public void onCellLocationChanged(CellLocation mLocation)
        {
            if(mDone) return;
            mCellLocation = mLocation;
            update();
        }
        public void onSignalStrengthsChanged(SignalStrength sStrength)
        {
            if(mDone) return;
            mSignalStrength = sStrength;
           update();
        }
    };


    private class Reflection extends AsyncTask<Void, Void, Void> {
        protected void onProgressUpdate(Void... progress) {
        }
        @Override
        protected Void doInBackground(Void... params) {
            mTextStr = ("Información Dispositivo\n\n" +
                    "SDK: `" + Build.VERSION.SDK_INT +
                    "`\nVersión: `" + Build.VERSION.RELEASE +
                    "`\nFabricante: `" + Build.MANUFACTURER +
                    "`\nModelo: `" + Build.MODEL + "\n\n"
            );
            return null;
        }

    }


    public final void update()
    {
        if(mSignalStrength == null || mCellLocation == null) return;
        final Reflection mReflec = new Reflection();
        mReflec.execute();
    }


    //En resumen al volver vuelve a llamar al listener del Nivel de Señal
    @Override
    protected void onResume()
    {
        super.onResume();
        listenerNivelSeñal();
        miManagerLocacion.requestLocationUpdates(
                PROVIDER,
                0,       //minTiempo
                0,       //minDistancia
                miEscuchaLocacion); //LocationListener
    }
    //En Pausa para de escuchar
    @Override
    protected void onPause()
    {
        super.onPause();
        pararEscuchar();
        miManagerLocacion.removeUpdates(miEscuchaLocacion);
    }
    //En destroy para igual de Escuchar y acaba el programa
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        pararEscuchar();
    }

    //Mostrar Locacion para Lat y Long
    private void mostrarMiLocacion(Location locacion) {
        if (locacion == null) {
        } else {
            textLatitud.setText("" + locacion.getLatitude());
            textLongitud.setText("" + locacion.getLongitude());
        }
    }

    private void miLocacion(Location locacion) {
        CSV csv = new CSV();
        if (locacion == null) {
        } else {
            latitud = locacion.getLatitude();
            longitud = locacion.getLongitude();
        }
    }

    private LocationListener miEscuchaLocacion
            = new LocationListener(){
        @Override
        public void onLocationChanged(Location locacion) {
            cnt++;
            mostrarMiLocacion(locacion);
            cargarInfo();

        }
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }};


    //Setea el nivel de Señal
    private void setNivelSeñal(int i, int info, SignalStrength nivel){
        //Para mostrar en la barra de progreso
        int a = nivel.getGsmSignalStrength();
        int barra = (int) ((((float)a)/31.0) * 100);
        String nivelSeñal = tomarStringNivelSeñal(barra);

        ((ProgressBar)findViewById(i)).setProgress(barra);
        ((TextView)findViewById(info)).setText(nivelSeñal);

    }

    //Para setear el TextView
    private void cargarInfoTextView(int i, String txt) {
        ((TextView)findViewById(i)).setText(txt);
    }

   //Toma stringNivelSeñal para poner en el TextView que esta junto a la barra de progreso
    private String tomarStringNivelSeñal(int level) {
        String stringNivelSeñal = "Pobre";
        if(level > EXCELENTE_SEÑAL)             stringNivelSeñal = "Excelente";
        else if(level > BUENA_SEÑAL)            stringNivelSeñal = "Buena";
        else if(level > SEÑAL_MODERADA)         stringNivelSeñal = "Moderada";
        else if(level > BAJA_SEÑAL)             stringNivelSeñal = "Mala";
        return stringNivelSeñal;
    }

    //Para que el phoneStateListener del TelephonyManager deje de escuchar.
    public void pararEscuchar(){
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }


    //Listener para el nivel de la señal
    private void listenerNivelSeñal() {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        int eventos = PhoneStateListener.LISTEN_SIGNAL_STRENGTH |
                PhoneStateListener.LISTEN_DATA_ACTIVITY |
                PhoneStateListener.LISTEN_CELL_LOCATION |
                PhoneStateListener.LISTEN_CALL_STATE |
                PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS|
                PhoneStateListener.LISTEN_SERVICE_STATE;
        telephonyManager.listen(phoneStateListener, eventos);
    }


    private void infoDpndTipoRed() {
        String tipoRed = stringTipoR;
        String infoTipoRed = "";

        if (tipoRed.equals("GPRS") || tipoRed.equals("UTMS")) {
            infoTipoRed += ("RSSI: " + tipoRed + "\n");
        }
        if (tipoRed.equals("EDGE") || tipoRed.equals("HSPA") || tipoRed.equals("HSPA+")){
        }
        if (tipoRed.equals("LTE")){
        }
    }

    //Cargar Infor en TextView
    private void cargarInfo(){
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        GsmCellLocation locacion = (GsmCellLocation) telephonyManager.getCellLocation();
        int cellid = locacion.getCid();
        int lac = locacion.getLac();
        String codigoPaisSim = telephonyManager.getSimCountryIso();
        String idDispositivo = telephonyManager.getDeviceId();
        String nombreOperadora = telephonyManager.getNetworkOperatorName();
        String numeroSerieSIM = telephonyManager.getSimSerialNumber();
        String idSubscriptor = telephonyManager.getSubscriberId();
        String simOperadora = telephonyManager.getSimOperatorName();
        String tipoRed = getTipoRed(telephonyManager.getNetworkType());
        String versionSoftware = telephonyManager.getDeviceSoftwareVersion();
        String informacionDispositivo = "";

        informacionDispositivo += ("Nombre Operadora: " + nombreOperadora + "\n");
        informacionDispositivo += ("Tipo de Red: " + tipoRed + "\n");
        informacionDispositivo += ("CellID: " + cellid + "\n");
        informacionDispositivo += ("LAC: " + lac + "\n\n");
        informacionDispositivo += ("" + mTextStr);
        informacionDispositivo += ("Version Software: " + versionSoftware + "\n");
        informacionDispositivo += ("IMEI: " + idDispositivo + "\n\n");
        informacionDispositivo += ("SIM Operadora: " + simOperadora + "\n");
        informacionDispositivo += ("SIM Codigo Pais: " + codigoPaisSim + "\n");
        informacionDispositivo += ("SIM Numero Serie.: " + numeroSerieSIM + "\n");
        informacionDispositivo += ("ID Subscriber: " + idSubscriptor + "\n\n");
        informacionDispositivo += ("Celdas Vecinas:" + "\n\n" );
        List<NeighboringCellInfo> infoCelda = telephonyManager.getNeighboringCellInfo();

        if(null != infoCelda){
            for(NeighboringCellInfo info: infoCelda){
                int rssi = info.getRssi();
                //int dbm = -113 + rssi*2;
                informacionDispositivo += ("\nCellID: " + info.getCid() + "\t\t" +
                        ", RSSI: " + rssi + "dbm\n");
            }
        }
        cargarInfoTextView(ids[ID_INFO_DISPOSITIVO], informacionDispositivo);
    }

    //Tomar el tipo de red y convertirlo a string
    private String getTipoRed(int tipo){

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

    //PhoneStateListener
    private final PhoneStateListener phoneStateListener = new PhoneStateListener(){
        @Override
        public void onCallForwardingIndicatorChanged(boolean llamadaEntrante)
        {
            Log.i(NOMBRE_APP, "onCallForwardingIndicatorChanged " + llamadaEntrante);
            super.onCallForwardingIndicatorChanged(llamadaEntrante);
        }

        @Override
        public void onCallStateChanged(int estado, String numeroEntrante)
        {
            String estadoLlamada = "DESCONOCIDO";
            switch(estado)
            {
                case TelephonyManager.CALL_STATE_IDLE:          estadoLlamada = "IDLE";
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    estadoLlamada = "Recibiendo Llamada (" + numeroEntrante + ")";
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:       estadoLlamada = "Offhook";
                    break;
            }
            cargarInfoTextView(ids[ID_ESTADO_LLAMADA], estadoLlamada);
            super.onCallStateChanged(estado, numeroEntrante);
        }

        @Override
        public void onCellLocationChanged(CellLocation locacion)
        {
            String locacionS = locacion.toString();
            super.onCellLocationChanged(locacion);
        }

        @Override
        public void onDataActivity(int direccion)
        {
            String direccionS = "none";

            switch(direccion)
            {
                case TelephonyManager.DATA_ACTIVITY_IN:         direccionS = "IN";
                    break;
                case TelephonyManager.DATA_ACTIVITY_INOUT:      direccionS = "INOUT";
                    break;
                case TelephonyManager.DATA_ACTIVITY_OUT:        direccionS = "OUT";
                    break;
                case TelephonyManager.DATA_ACTIVITY_NONE:       direccionS = "NINGUNO";
                    break;
                default:
                    direccionS = "DESCONOCIDO: " + direccion;
                    break;
            }
            Log.i(NOMBRE_APP, "onDataActivity " + direccionS);
            super.onDataActivity(direccion);
        }

        @Override
        public void onDataConnectionStateChanged(int estado)
        {
            String estadoConexion = "DESCONOCIDO";

            switch(estado)
            {
                case TelephonyManager.DATA_CONNECTED:           estadoConexion = "CONECTADO";
                    break;
                case TelephonyManager.DATA_CONNECTING:          estadoConexion = "CONECTANDO";
                    break;
                case TelephonyManager.DATA_DISCONNECTED:        estadoConexion = "DESCONECTANDO";
                    break;
                case TelephonyManager.DATA_SUSPENDED:           estadoConexion = "SUSPENDIDO";
                    break;
                default:   estadoConexion = "DESCONOCIDO: " + estado;
                    break;
            }
            cargarInfoTextView(ids[ID_ESTADO_CONEXION], estadoConexion);
            super.onDataConnectionStateChanged(estado);
        }

        @Override
        public void onMessageWaitingIndicatorChanged(boolean mensajeEspera)
        {
            super.onMessageWaitingIndicatorChanged(mensajeEspera);
        }

        @Override
        public void onServiceStateChanged(ServiceState estadoServicio)
        {
            String estadoServicioS = "DESCONOCIDO";

            switch(estadoServicio.getState())
            {
                case ServiceState.STATE_IN_SERVICE:             estadoServicioS = "EN SERVICIO";
                    break;
                case ServiceState.STATE_EMERGENCY_ONLY:         estadoServicioS = "SOLO EMERGENCIA";
                    break;
                case ServiceState.STATE_OUT_OF_SERVICE:
                    estadoServicioS = "FUERA DE SERVICIO";
                    break;
                case ServiceState.STATE_POWER_OFF:              estadoServicioS = "APAGANDO";
                    break;
                default: estadoServicioS = "DESCONOCIDO";
                    break;
            }
            cargarInfoTextView(ids[ID_ESTADO_SERVICIO], estadoServicioS);
            super.onServiceStateChanged(estadoServicio);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            int potencia = signalStrength.getGsmSignalStrength();
            TextView tx = (TextView)findViewById(R.id.potencia);
            tx.setText(potencia + "dbm");
            setNivelSeñal(ids[ID_NIVEL_SEÑAL], ids[ID_INFO_NIVEL_SEÑAL], signalStrength);
            super.onSignalStrengthsChanged(signalStrength);

            String ssignal = signalStrength.toString();
            String[] parts = ssignal.split(" ");
            int rsrp = 0;
            int rsrq = 0;
            int rssnr = 0;
            int cqi = 0;
            int dbm = 0;

            if ( mManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){

                rsrp = Integer.parseInt(parts[9]);
                rsrq = Integer.parseInt(parts[10]);
                rssnr = Integer.parseInt(parts[11]);
                cqi = Integer.parseInt(parts[12]);
                dbm = Integer.parseInt(parts[8])*2-113;
            }

            TextView tx1 = (TextView)findViewById(R.id.rsrp);
            tx1.setText(rsrp + " dBm" );
            TextView tx2 = (TextView)findViewById(R.id.rsrq);
            tx2.setText(rsrq + " dBm" );
            TextView tx3 = (TextView)findViewById(R.id.rssnr);
            tx3.setText(rssnr + " dBm" );
            TextView tx4 = (TextView)findViewById(R.id.cqi);
            tx4.setText(cqi + " nivel" );
            TextView tx5 = (TextView)findViewById(R.id.dbm);
            tx5.setText(dbm + " dBm" );
        }
    };
}
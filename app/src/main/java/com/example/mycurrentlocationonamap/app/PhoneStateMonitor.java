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

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;

public class PhoneStateMonitor extends PhoneStateListener {

    private Context context;
    private String estado;
    private int senal;
    public int potenciaSenal;
    private GsmCellLocation cellLocation;

    public void setCellLocation(GsmCellLocation cellLocation) {
        this.cellLocation = cellLocation;
    }

    public PhoneStateMonitor(Context context) {
        super();
        // TODO Auto-generated constructor stub
        this.context=context;
        estado = "nulo";
        senal = -9999;
        potenciaSenal = 0;
    }


    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        senal = signalStrength.getGsmSignalStrength();

        potenciaSenal = 2 * senal - 113;

        String pot = String.valueOf(potenciaSenal);
        String cellId = String.valueOf(cellLocation.getCid());

        Toast toastPot = Toast.makeText(context, "Potencia: " + pot + "dBm" + "\n Cell Id:" +cellId , Toast.LENGTH_SHORT);
        toastPot.show();
    }


}
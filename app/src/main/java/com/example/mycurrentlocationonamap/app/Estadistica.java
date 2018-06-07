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
import android.graphics.Color;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import lecho.lib.hellocharts.model.ArcValue;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.ColumnValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PieChartView;

public class Estadistica extends Activity {

    public static int cntVerde;
    public static int cntAmarillo;
    public static int cntRojo;
    public static int cntNegro;
    public static int[] regPotencias;
    public static int cntMuestras;
    public ColumnChartData dataB;
    public ColumnChartView barr;
    public LineChartData dataL;
    public LineChartView linn;
    public PieChartData dataP;
    public PieChartView piee;

    //Caracteristicas Line
    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLines = true;
    private boolean hasPoints = true;
    private boolean isFilled = true;
    private ValueShape shape = ValueShape.SQUARE;
    private boolean hasLabels = false;
    private boolean hasLabelForSelected = true;

    //Caracteristicas Column
    private boolean hasAxesC = true;
    private boolean hasAxesNamesC = true;
    private boolean hasLabelsC = false;
    private boolean hasLabelForSelectedC = true;

    //Caracteristicas Pie
    private boolean hasLabelForSelectedP = true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.estadistica);
        barr = (ColumnChartView) findViewById(R.id.bar);
        linn = (LineChartView) findViewById(R.id.lin);
        piee = (PieChartView) findViewById(R.id.pie);
        graphLine();
        graphPie();
        graphBar();
    }

    public void graphBar(){
        List<ColumnValue> values = new ArrayList<ColumnValue>(4);
        values.add(new ColumnValue(cntVerde).setColor(Color.GREEN));
        values.add(new ColumnValue(cntAmarillo).setColor(Color.YELLOW));
        values.add(new ColumnValue(cntRojo).setColor(Color.RED));
        values.add(new ColumnValue(cntNegro).setColor(Color.BLACK));

        Column column = new Column(values);
        column.setHasLabelsOnlyForSelected(hasLabelForSelected);
        List<Column> columns = new ArrayList<Column>(1);
        columns.add(column);
        dataB = new ColumnChartData(columns);

        if (hasAxesC) {
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNamesC) {
              axisY.setName("Cantidad Puntos");
            }
              dataB.setAxisYLeft(axisY);
        } else {
            dataB.setAxisXBottom(null);
            dataB.setAxisYLeft(null);
        }
        barr.setColumnChartData(dataB);
    }

    public void graphPie(){

        List<ArcValue> values = new ArrayList<ArcValue>(4);
        values.add(new ArcValue(cntVerde).setColor(Color.GREEN));
        values.add(new ArcValue(cntAmarillo).setColor(Color.YELLOW));
        values.add(new ArcValue(cntRojo).setColor(Color.RED));
        values.add(new ArcValue(cntNegro).setColor(Color.BLACK));

        dataP = new PieChartData(values);
        dataP.setHasLabelsOnlyForSelected(hasLabelForSelectedP);
        piee.setPieChartData(dataP);

    }

    public void graphLine(){

        List<PointValue> values = new ArrayList<PointValue>();
        for (int i=0; i<cntMuestras; i++) {
            values.add(new PointValue(i,regPotencias[i]+110));
        }

        Line line = new Line(values).setColor(Color.WHITE).setCubic(true);
        List<Line> lines = new ArrayList<Line>(1);

        lines.add(line);
        line.setFilled(isFilled);
        line.setShape(shape);
        line.setHasLabels(hasLabels);
        line.setHasLabelsOnlyForSelected(hasLabelForSelected);
        line.setHasLines(hasLines);
        line.setHasPoints(hasPoints);

        dataL = new LineChartData(lines);
        linn.setLineChartData(dataL);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setName("Tiempo");
                axisY.setName("Potencia");
            }
            dataL.setAxisXBottom(axisX);
            dataL.setAxisYLeft(axisY);
        } else {
            dataL.setAxisXBottom(null);
            dataL.setAxisYLeft(null);
        }
    }
}

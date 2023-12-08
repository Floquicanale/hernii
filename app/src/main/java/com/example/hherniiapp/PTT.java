package com.example.hherniiapp;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.github.psambit9791.jdsp.filter.Butterworth;

public class PTT {
    public static float CalcularPTT(ArrayList<Integer> ECG, ArrayList<Integer> PPG,ArrayList<Integer> SCG ){
        try {

            double[] doubledatascg = convertArrayListToDoubleArray(SCG);
            double[] filtradascg = HP_IIR(doubledatascg, 2.0);
            ArrayList<Float> scgFloat = convertirDoubleArrayToArrayListFloat(filtradascg);
            ArrayList<Float> ECGFloat = IntToFloat(ECG);
            ArrayList<Float> PPGFloat = IntToFloat(PPG);
            //ArrayList<Float> scgFloat = IntToFloat(SCG);

            //ArrayList<Float> datos_offset = restarPromedio(ECGFloat);
            ArrayList<Integer> picos = new ArrayList<>();

            float maxOutput = (Collections.max(ECGFloat));
            System.out.println("El maximo del output es:" + maxOutput);
            float umbral = 0.8f*maxOutput;
            System.out.println("El umbral utilizado eso:" + umbral);

            ArrayList<Float> SCG_menos1 = FloatmultiplicarPorNegativo(scgFloat);

            picos = find_peaks(ECGFloat, umbral);
            System.out.println("Maximos ECG:" + picos);
            System.out.println("n Picos:"+picos.size());

            ArrayList<ArrayList<Float>> ventanasR_PPG = R_window(PPGFloat, picos);
            ArrayList<ArrayList<Float>> ventanasR_SCG = R_window(SCG_menos1, picos);
            System.out.println("Pase las ventanas");
            //ArrayList<ArrayList<Float>> ECGcut = R_window(ECGFloat, picos);

            //ArrayList<ArrayList<BigDecimal>> SCG_window = convertirListaDeListasDoubleAListaDeListasBigDecimal(ventanasR_SCG);
            ArrayList<Integer> picos_SCG = new ArrayList<>();
            for (ArrayList<Float> ventana_SCG : ventanasR_SCG) {
                System.out.println("Entre a la ventana SCG");
                int pico_ventana_SCG = find_peaks_SCG(ventana_SCG);

                picos_SCG.add(pico_ventana_SCG);
            }
            ArrayList<Integer> foots_PPG = new ArrayList<>();
            for (ArrayList<Float> ventana_PPG : ventanasR_PPG) {
                System.out.println("Entre a la ventana PPG");
                double interseccion = punto_mas_bajo(ventana_PPG);
                int foot = (int) round(interseccion);

                foots_PPG.add(foot);
            }

            ArrayList<Float> PulseTransitTime = PTT(picos_SCG, foots_PPG);

            PulseTransitTime = removeOutliersZScore(PulseTransitTime);
            float Final_PTT = (float) 0;

            for (int i= 0; i < PulseTransitTime.size(); i++){
                Final_PTT += PulseTransitTime.get(i);
            }
            Final_PTT = Final_PTT/PulseTransitTime.size();

            System.out.println("Picos de cada ventana SCG son: "+picos_SCG);
            System.out.println("Foots de cada ventana PPG son: "+foots_PPG);
            System.out.println("PTT de cada ventana PPG son: "+PulseTransitTime);
            System.out.println("PTT final: "+Final_PTT);

            return Final_PTT;

        } catch (Exception e){
            e.printStackTrace();
            return 0f;
        }
    }


    //CONVERSIONES DE TIPO DE DATO.
    public static double[] convertArrayListToDoubleArray(ArrayList<Integer> arrayList) {
        int size = arrayList.size();
        double[] doubleArray = new double[size];

        for (int i = 0; i < size; i++) {
            doubleArray[i] = arrayList.get(i);
        }

        return doubleArray;
    }
    private static ArrayList<Float> IntToFloat(ArrayList<Integer> intArray) {
        ArrayList<Float> floatList = new ArrayList<>();
        for (float value : intArray) {
            floatList.add(value);
        }
        return floatList;
    }
    private static ArrayList<Float> convertirDoubleArrayToArrayListFloat(double[] arrayDouble) {
        ArrayList<Float> listaBigDecimal = new ArrayList<>();
        for (double valor : arrayDouble) {
            float valorF = (float) valor ;
            listaBigDecimal.add(valorF);
        }
        return listaBigDecimal;
    }

    //FILTROS

    public static double[] HP_IIR (double[] signal, double lowF){
        int Fs = 200; //Sampling Frequency in Hz
        int order = 4;
        Butterworth flt = new Butterworth(Fs);
        double[] result = flt.highPassFilter(signal, order, lowF); //get the result after filtering

        return result;
    }


    //PROCESAMIENTO
    public static ArrayList<Float> restarPromedio(ArrayList<Float> datos){
        ArrayList<Float> salida = new ArrayList<>();
        float suma = 0;
        for (float dato : datos) {
            suma += dato;
        }
        // Calcular la media
        float media = suma/datos.size();

        for (float dato : datos) {
            float datoRestadoMedia = dato - media;
            salida.add(datoRestadoMedia);
        }

        return salida;
    }

    private static ArrayList<Float> FloatmultiplicarPorNegativo(ArrayList<Float> listaOriginal) {
        ArrayList<Float> resultado = new ArrayList<>();

        for (float elemento : listaOriginal) {
            resultado.add(elemento * -1);
        }

        return resultado;
    }

    public static ArrayList<Integer> find_peaks(ArrayList<Float> datos, float umbral) {
        ArrayList<Integer> picos = new ArrayList<>();
        ArrayList<Integer> salida = new ArrayList<>();

        for (int i = 1; i < datos.size()-1; i++) {
            if (datos.get(i) > umbral && (datos.get(i) - datos.get(i - 1) >= 0) && (datos.get(i) - datos.get(i + 1) >= 0)) {
                picos.add(i);
            }
        }

        int max = 0;
        for (int i = 0; i<picos.size()-1; i++){
            int dif = Math.abs(picos.get(i) - picos.get(i+1));
            if(dif<50){
                if(picos.get(i) - max > 0){
                    max = picos.get(i);
                }
            }
            else{
                if (max == 0) {
                    max = picos.get(i);
                }
                salida.add(max);
                max = 0;
            }

        }

        System.out.println("La salida de find_peaks: " + salida);
        return salida;
    }


    public static int find_peaks_SCG(ArrayList<Float> datos) {
        int save = 0;

        //System.out.println(datos);
        float max = 0;
        for (int i = 0; i < 40; i++) { //solo busca en los primero 40 valores porq si esta mas tarde full patologico
            if(datos.get(i) > max){
                max = datos.get(i);
                save=i;
            }


        }

        //System.out.println("La salida de find_peaks: " + picos);
        return save;
    }

    public static ArrayList<ArrayList<Float>> R_window (ArrayList<Float> data, ArrayList<Integer> R_peaks){
        ArrayList<ArrayList<Float>> segmentosList = new ArrayList<>();
        //ArrayList<ArrayList<Double>> Ventanas_recortadas = new ArrayList<>();
        System.out.println("Longitud del array:"+ data.size());
        int save_i = 0;
        int save_f = 0;
        for (int i = 0; i < R_peaks.size()-1; i++) {
            if (R_peaks.get(i+1) < data.size()){
                save_i = R_peaks.get(i);
                save_f = R_peaks.get(i+1);
                List<Float> subList = data.subList(save_i, save_f);
                // Convierte la sublista a un nuevo ArrayList si es necesario
                ArrayList<Float> ventana = new ArrayList<>(subList);

                //ventana = Arrays.copyOfRange(data,save_i,save_f);
                //System.out.println(ventana);
                segmentosList.add(ventana);
            }

        }

        return segmentosList;
    }

    //PPG - Foot
    public static float punto_mas_bajo(ArrayList<Float> ppgSignal){

        // Find the minimum point in the PPG signal
        int minIndex = findMinIndex(ppgSignal);

        // Find the maximum local gradient
        int maxGradientIndex = findMaxGradientIndex(ppgSignal);

        // Calculate the slope of the line tangent to the maximum local gradient
        float maxGradientSlope = calculateSlope(ppgSignal, maxGradientIndex);

        // Calculate the y-coordinate of the tangent line at the maximum gradient point
        float maxGradientY = ppgSignal.get(maxGradientIndex) - maxGradientSlope * maxGradientIndex;

        // Calculate the x-coordinate of the intersection point
        float intersectionX =  (ppgSignal.get(minIndex) - maxGradientY) / maxGradientSlope;

        // The intersection point is (intersectionX, ppgSignal[minIndex])
        //System.out.println("Intersection Point: (" + intersectionX + ", " + ppgSignal[minIndex] + ")");

        return intersectionX;
    }

    public static int findMinIndex(ArrayList<Float> data) {
        int minIndex = 0;
        for (int i = 40; i < data.size(); i++) {
            if (data.get(i) < data.get(minIndex)) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    // Find the index of the maximum local gradient
    public static int findMaxGradientIndex(ArrayList<Float> data) {
        int maxGradientIndex = 0;
        double maxGradient = 0;

        for (int i = 40; i < data.size() - 1; i++) {
            double gradient = (data.get(i + 1) - data.get(i - 1) );
            if (gradient > maxGradient) {
                maxGradient = gradient;
                maxGradientIndex = i;
            }
        }

        return maxGradientIndex;
    }

    // Calculate the slope of a line given two points
    public static float calculateSlope(ArrayList<Float> data, int index) {
        if (index > 0 && index < data.size() - 1) {
            float x1 = index - 1;
            float y1 = data.get(index - 1);
            float x2 = index + 1;
            float y2 = data.get(index + 1);
            return (y2 - y1) / (x2 - x1);
        } else {
            return 0; // Default slope if index is out of bounds
        }
    }

    public static ArrayList<Float> PTT (ArrayList<Integer> SCG, ArrayList<Integer> Foots) {
        ArrayList<Float> resta = new ArrayList<>();

        while (SCG.size() != Foots.size()) {
            if (Foots.size() > SCG.size()) {
                Foots.remove(Foots.size() - 1);
            }
            else{
                SCG.remove(SCG.size() - 1);
            }
        }

        for (int i = 0; i < SCG.size(); i++){
            float difference = (Foots.get(i).floatValue() - SCG.get(i).floatValue()) * 0.005f;
            if (difference < 0.65){
                resta.add(difference);
            }
        }
        //Sacamos los valores dudosos

        return resta;

    }

    public static ArrayList<Float> removeOutliersZScore(ArrayList<Float> data) {
        ArrayList<Float> filteredData = new ArrayList<>();

        // Calculate the mean and standard deviation
        float mean = calculateMean(data);
        float standardDeviation = calculateStandardDeviation(data, mean);

        // Remove outliers based on z-scores
        for (Float value : data) {
            float zScore = (value - mean) / standardDeviation;
            if (Math.abs(zScore) <= 2) {
                filteredData.add(value);
            }
        }

        return filteredData;
    }

    private static float calculateMean(ArrayList<Float> data) {
        float sum = 0;
        for (Float value : data) {
            sum += value;
        }
        return sum / data.size();
    }

    private static float calculateStandardDeviation(ArrayList<Float> data, float mean) {
        float sumOfSquares = 0;
        for (Float value : data) {
            float difference = value - mean;
            sumOfSquares += difference * difference;
        }
        return (float) Math.sqrt(sumOfSquares / (data.size() - 1));
    }

    public static int HR(ArrayList<Integer> ECG){
        ArrayList<Float> ECGFloat = IntToFloat(ECG);
        ArrayList<Integer> picos = new ArrayList<>();
        float maxOutput = (Collections.max(ECGFloat));
        System.out.println("El maximo del output es:" + maxOutput);
        float umbral = 0.9f*maxOutput;
        System.out.println("El umbral utilizado eso:" + umbral);

        picos = find_peaks(ECGFloat, umbral);
        System.out.println("PICOS R:" + picos.size());

        int HR = picos.size()*2;

        return HR;
    }

}


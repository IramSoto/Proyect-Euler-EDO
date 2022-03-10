/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.table.DefaultTableModel;
import org.nfunk.jep.JEP;

/**
 *
 * @author iramalexanderalfarosoto
 */
public class Calcular {
    private JEP jep;
    private double x, y;
    private Double[] respuestaEuler;
    private Double[] respuestaEulerMejorado;
    private Double[] respuestaErrorAbs;
    private Double[] respuestaErrorRel;
    private Double[] exacta;
    private Double h;  
    private List<Double> steps = new ArrayList<Double>(); 
    private String funcion;
    private int cual;
    private String operar;

    public Calcular(double x, double y, String funcion, String h, String steps, String exacta, int cual) {
        this.cual = cual;
        this.funcion = funcion;
        this.x = x;
        this.y = y;
        this.h = Double.parseDouble(h);
        String exac[] = exacta.split(",");
        
        int valor = 0;
        this.exacta = new Double[exac.length];
        for(String ex: exac){
            this.exacta[valor] = Double.parseDouble(ex);
            valor++;
        }
        
        String splitS[] = steps.split(",");
        for(int i =0; i<splitS.length;i++){
            this.steps.add(Double.parseDouble(splitS[i]));
        }
        
        this.respuestaEuler = new Double[this.steps.size()];
        this.respuestaEulerMejorado = new Double[this.steps.size()];
        
        this.jep = new JEP();
        this.jep.addStandardFunctions();
        this.jep.addStandardConstants();
        this.jep.addVariable("x", x);
        this.jep.addVariable("y", y);
        this.jep.addVariable("h", this.h);
        this.jep.addVariable("q", 0);
        if(cual == 0){
            Euler(x,y,0);
        }else{
            EulerMejorado(x,y,0);
        }
        this.respuestaErrorAbs = new Double[this.steps.size()];
        this.respuestaErrorRel = new Double[this.steps.size()];
        calError(0);
        
    }
    
    public Double[][] Tabla(){
        Double[][] tabla = new Double[this.exacta.length + 1][5];
        tabla[0][0] = this.x;
        tabla[0][1] = this.y;
        tabla[0][2] = 0.0;
        tabla[0][3] = 0.0;
        tabla[0][4] = this.y;
        
        for(int i = 0; i < this.exacta.length; i++){
            tabla[i+1][0] = this.steps.get(i);
            if(this.cual == 0 ){
                tabla[i+1][1] = this.respuestaEuler[i];
            }else{
                tabla[i+1][1] = this.respuestaEulerMejorado[i];
            }
            tabla[i+1][2] = this.respuestaErrorAbs[i];
            tabla[i+1][3] = this.respuestaErrorRel[i];
            tabla[i+1][4] = this.exacta[i];
        }
        return tabla;
    }
    
    private void Euler(double Xi, double Yi, int stepA){
        DecimalFormat df = new DecimalFormat("#.####");
        Xi = Double.parseDouble(df.format(Xi));
        Yi = Double.parseDouble(df.format(Yi));
        operar = "y+h*("+ this.funcion +")";
        operar = operar.replaceAll("x", "("+Xi+")");
        operar = operar.replaceAll("y", "("+Yi+")");
        
        this.jep.parseExpression(operar);
        Double valor = this.jep.getValue();
        
        if(Xi >= this.steps.get(stepA)-this.h || valor.isInfinite()){
            this.respuestaEuler[stepA] = valor;
            
            if(!(this.steps.get(stepA) == this.steps.get(this.steps.size()-1))){
                Euler(Xi+this.h,valor, stepA+1);
            }
        }else{
            Euler(Xi+this.h,valor, stepA);
        }
    }
    
    private void EulerMejorado(double Xi, double Yi, int stepA){
        DecimalFormat df = new DecimalFormat("#.####");
        Xi = Double.parseDouble(df.format(Xi));
        Yi = Double.parseDouble(df.format(Yi));
        
        double k1, k2;
        operar = ""+this.funcion;
        operar = operar.replaceAll("x", "("+Xi+")");
        operar = operar.replaceAll("y", "("+Yi+")");
        
        this.jep.parseExpression(operar);
        k1 = this.jep.getValue();
        String funcionAux = this.funcion;
        funcionAux = funcionAux.replaceAll("y", "("+Yi+"+h*"+k1+")");
        double newX = Xi+this.h;
        funcionAux = funcionAux.replaceAll("x", "("+newX+")");
        
        this.jep.parseExpression(funcionAux);
        k2 = this.jep.getValue();
        this.jep.parseExpression(Yi+"+(h/2)*("+k1+"+"+k2+")");
        Double valor = this.jep.getValue();
        
        if(Xi >= this.steps.get(stepA)-this.h || valor.isInfinite()){
            this.respuestaEulerMejorado[stepA] = valor;
            if(!(this.steps.get(stepA) == this.steps.get(this.steps.size()-1))){
                EulerMejorado(Xi+this.h,valor, stepA+1);
            }
        }else{
            EulerMejorado(Xi+this.h,valor, stepA);
        }
        
    }
    
    private void calError(int step){
        
        if(this.cual == 0){
            this.respuestaErrorAbs[step] = errorAbs(this.respuestaEuler[step],this.exacta[step]);
            this.respuestaErrorRel[step] = errorRel(this.respuestaEuler[step],this.exacta[step]);
        }else{
            this.respuestaErrorAbs[step] = errorAbs(this.respuestaEulerMejorado[step],this.exacta[step]);
            this.respuestaErrorRel[step] = errorRel(this.respuestaEulerMejorado[step],this.exacta[step]);
        }
        if(step < this.exacta.length -1 ){
            calError(step +1);
        }
    }
    private Double errorAbs(Double dato, Double exacto){
        Double respuesta = dato - exacto;
        if(respuesta < 0 ){
            respuesta = respuesta * -1;
        }
        return respuesta;
    }
    
    private Double errorRel(Double dato, Double exacto){
        return (errorAbs(dato, exacto)/exacto)*100;
    }

    public Double[] getRespuestaEuler() {
        return this.respuestaEuler;
    }

    public Double[] getRespuestaEulerMejorado() {
        return this.respuestaEulerMejorado;
    }

    public Double[] getExacta() {
        return this.exacta;
    }

    public List<Double> getSteps() {
        return steps;
    }
    
    
    
    
}

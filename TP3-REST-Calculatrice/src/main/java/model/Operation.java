package model;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Modèle (représentation) d'une opération : {@code {"a":5.0,"b":3.0,"op":"+","res":8.0}}.
 * <p>
 * Sérialisé en JSON par JSON-B (Yasson) ou en XML par JAXB ({@code <operation>...</operation>})
 * selon l'en-tête {@code Accept} envoyé par le client.
 * JSON-B et JAXB exigent un constructeur sans argument et des getters/setters publics.
 */
@XmlRootElement(name = "operation")
public class Operation {

    private double a;
    private double b;
    private String op;
    private double res;

    public Operation() {
    }

    public Operation(double a, double b, String op, double res) {
        this.a = a;
        this.b = b;
        this.op = op;
        this.res = res;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public double getRes() {
        return res;
    }

    public void setRes(double res) {
        this.res = res;
    }

    @Override
    public String toString() {
        return a + " " + op + " " + b + " = " + res;
    }
}

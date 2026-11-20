package service;

import jakarta.jws.WebService;

/**
 * Classe métier implémentant le contrat {@link ICalculatrice}.
 * <p>
 * {@code endpointInterface} sépare le contrat (interface) de l'implémentation :
 * le WSDL est généré à partir de l'interface {@code service.ICalculatrice}.
 * {@code serviceName} et {@code portName} fixent les noms des éléments
 * {@code <service>} et {@code <port>} du WSDL.
 */
@WebService(endpointInterface = "service.ICalculatrice",
        targetNamespace = "http://service/",
        serviceName = "CalculatriceWSService",
        portName = "CalculatriceWSPort")
public class Calculatrice implements ICalculatrice {

    @Override
    public double somme(double a, double b) {
        return a + b;
    }

    @Override
    public double soustraction(double a, double b) {
        return a - b;
    }

    @Override
    public double multiplication(double a, double b) {
        return a * b;
    }

    @Override
    public double division(double a, double b) throws DivisionParZeroException {
        if (b == 0) {
            throw new DivisionParZeroException("Division par zéro impossible : b doit être différent de 0");
        }
        return a / b;
    }
}

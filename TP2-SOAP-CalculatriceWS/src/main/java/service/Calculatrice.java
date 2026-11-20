package service;

import jakarta.jws.WebService;

/**
 * Classe métier implémentant le contrat {@link ICalculatrice}.
 * <p>
 * {@code endpointInterface} sépare le contrat (interface) de l'implémentation :
 * le WSDL est généré à partir de l'interface {@code service.ICalculatrice}.
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
}

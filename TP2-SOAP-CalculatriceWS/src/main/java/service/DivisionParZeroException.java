package service;

import jakarta.xml.ws.WebFault;

/**
 * Exception métier levée lors d'une division par zéro.
 * <p>
 * Étant une exception vérifiée déclarée dans le contrat, elle apparaît dans le WSDL
 * ({@code <fault name="DivisionParZeroException">}) et est renvoyée au client
 * sous la forme d'un SOAP Fault.
 */
@WebFault(name = "DivisionParZeroFault", targetNamespace = "http://service/")
public class DivisionParZeroException extends Exception {

    public DivisionParZeroException(String message) {
        super(message);
    }
}

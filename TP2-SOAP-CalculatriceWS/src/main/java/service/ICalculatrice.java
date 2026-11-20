package service;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * Contrat du service web SOAP CalculatriceWS.
 * <p>
 * L'interface est annotée avec {@code @WebService} : elle définit les opérations
 * exposées (portType du WSDL). L'implémentation est séparée dans {@link Calculatrice}.
 */
@WebService(name = "CalculatriceWS", targetNamespace = "http://service/")
public interface ICalculatrice {

    /** Opération somme : renvoie a + b. */
    @WebMethod(operationName = "somme")
    double somme(@WebParam(name = "a") double a,
                 @WebParam(name = "b") double b);
}

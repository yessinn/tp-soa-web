package service;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * Contrat du service web SOAP CalculatriceWS.
 * <p>
 * L'interface est annotée avec {@code @WebService} : elle définit les opérations
 * exposées (portType du WSDL). L'implémentation est séparée dans {@link Calculatrice}.
 * <ul>
 *     <li>{@code name} : nom du portType dans le WSDL ;</li>
 *     <li>{@code targetNamespace} : espace de noms du WSDL et du XSD généré.</li>
 * </ul>
 */
@WebService(name = "CalculatriceWS", targetNamespace = "http://service/")
public interface ICalculatrice {

    /** Opération somme : renvoie a + b. */
    @WebMethod(operationName = "somme")
    double somme(@WebParam(name = "a") double a,
                 @WebParam(name = "b") double b);

    /** Opération soustraction : renvoie a - b. */
    @WebMethod(operationName = "soustraction")
    double soustraction(@WebParam(name = "a") double a,
                        @WebParam(name = "b") double b);

    /** Opération multiplication : renvoie a * b. */
    @WebMethod(operationName = "multiplication")
    double multiplication(@WebParam(name = "a") double a,
                          @WebParam(name = "b") double b);

    /**
     * Opération division : renvoie a / b.
     *
     * @throws DivisionParZeroException si b vaut 0 (traduite en SOAP Fault, déclarée
     *                                  dans le WSDL comme {@code <fault>} de l'opération)
     */
    @WebMethod(operationName = "division")
    double division(@WebParam(name = "a") double a,
                    @WebParam(name = "b") double b) throws DivisionParZeroException;
}

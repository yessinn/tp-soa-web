package serveur;

import jakarta.xml.ws.Endpoint;
import service.Calculatrice;

/**
 * Publication autonome du service web (sans serveur d'application)
 * grâce à {@link Endpoint#publish(String, Object)}.
 */
public class ServeurJWS {

    public static final String URL = "http://localhost:8686/CalculatriceWS";

    public static void main(String[] args) {
        Endpoint endpoint = Endpoint.publish(URL, new Calculatrice());
        if (endpoint.isPublished()) {
            System.out.println("Service SOAP publié à : " + URL);
            System.out.println("WSDL                  : " + URL + "?wsdl");
            System.out.println("XSD généré            : " + URL + "?xsd=1");
            System.out.println("(Ctrl+C pour arrêter le serveur)");
        }
    }
}

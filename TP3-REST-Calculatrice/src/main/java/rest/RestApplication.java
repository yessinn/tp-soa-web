package rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Configuration JAX-RS : active Jersey et fixe l'URL de base des ressources REST.
 * <p>
 * L'annotation remplace la déclaration de la servlet Jersey dans web.xml.
 * Toutes les ressources sont accessibles sous {@code /api}, par exemple :
 * {@code http://localhost:8080/CalculatriceREST/api/calcul/somme?a=5&b=3}.
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    // L'annotation suffit : Jersey découvre automatiquement les classes annotées @Path
}

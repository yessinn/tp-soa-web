package rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import metier.Calculatrice;
import model.Operation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;

/**
 * Ressource REST « calcul ».
 *
 * <pre>
 * GET  /api/calcul/somme?a=5&amp;b=3          → {"a":5.0,"b":3.0,"op":"+","res":8.0}
 * GET  /api/calcul/soustraction?a=10&amp;b=4   → {"a":10.0,"b":4.0,"op":"-","res":6.0}
 * GET  /api/calcul/multiplication?a=6&amp;b=7  → {"a":6.0,"b":7.0,"op":"*","res":42.0}
 * GET  /api/calcul/division?a=15&amp;b=4       → {"a":15.0,"b":4.0,"op":"/","res":3.75}
 * POST /api/calcul  {"a":5,"b":3,"op":"+"}  → {"a":5.0,"b":3.0,"op":"+","res":8.0}
 * </pre>
 * Les réponses sont en JSON par défaut, ou en XML si le client envoie
 * {@code Accept: application/xml}. Les erreurs renvoient 400 Bad Request.
 */
@Path("/calcul")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CalculRest {

    private final Calculatrice calculatrice = new Calculatrice();

    @GET
    @Path("/somme")
    public Response somme(@QueryParam("a") Double a, @QueryParam("b") Double b) {
        return calculer("+", a, b, calculatrice::somme);
    }

    @GET
    @Path("/soustraction")
    public Response soustraction(@QueryParam("a") Double a, @QueryParam("b") Double b) {
        return calculer("-", a, b, calculatrice::soustraction);
    }

    @GET
    @Path("/multiplication")
    public Response multiplication(@QueryParam("a") Double a, @QueryParam("b") Double b) {
        return calculer("*", a, b, calculatrice::multiplication);
    }

    @GET
    @Path("/division")
    public Response division(@QueryParam("a") Double a, @QueryParam("b") Double b) {
        return calculer("/", a, b, calculatrice::division);
    }

    /**
     * Calcul à partir d'une représentation JSON envoyée dans le corps de la requête,
     * ex. {@code {"a": 5, "b": 3, "op": "+"}}. Le champ {@code res} est calculé par le serveur.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response calculer(Operation operation) {
        if (operation == null) {
            return erreur("Corps JSON manquant : {\"a\": ..., \"b\": ..., \"op\": \"+|-|*|/\"}");
        }
        try {
            double res = calculatrice.calculer(operation.getOp(), operation.getA(), operation.getB());
            return Response.ok(new Operation(operation.getA(), operation.getB(), operation.getOp().trim(), res))
                    .build();
        } catch (IllegalArgumentException | ArithmeticException e) {
            return erreur(e.getMessage());
        }
    }

    // ------------------------------------------------------------------

    /** Vérifie les paramètres, applique l'opération et construit la réponse. */
    private Response calculer(String op, Double a, Double b, DoubleBinaryOperator operation) {
        if (a == null || b == null) {
            return erreur("Les paramètres 'a' et 'b' sont obligatoires, ex. ?a=5&b=3");
        }
        try {
            double res = operation.applyAsDouble(a, b);
            return Response.ok(new Operation(a, b, op, res)).build();
        } catch (ArithmeticException e) {
            return erreur(e.getMessage());
        }
    }

    /** Réponse 400 Bad Request avec un message d'erreur au format JSON. */
    private Response erreur(String message) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("status", Response.Status.BAD_REQUEST.getStatusCode());
        corps.put("erreur", message);
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(corps)
                .build();
    }
}

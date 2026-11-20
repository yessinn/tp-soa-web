package tn.fss.tp1;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class CinemaXmlAppTest {

    private static Document document;

    private static Source ressource(String nom) {
        return new StreamSource(Objects.requireNonNull(
                CinemaXmlAppTest.class.getClassLoader().getResource(nom)).toExternalForm());
    }

    @BeforeAll
    static void chargerDocument() throws Exception {
        document = CinemaXmlApp.charger(ressource("cinema.xml").getSystemId());
    }

    // ---------------------- Validation ----------------------

    @Test
    void documentValideNeProduitAucuneErreur() throws Exception {
        List<String> erreurs = CinemaXmlApp.valider(ressource("cinema.xml"), ressource("cinema.xsd"));
        assertTrue(erreurs.isEmpty(), () -> "Erreurs inattendues : " + erreurs);
    }

    @Test
    void documentInvalideEstRejeteAvecToutesLesErreurs() throws Exception {
        List<String> erreurs = CinemaXmlApp.valider(ressource("cinema-invalide.xml"), ressource("cinema.xsd"));
        String toutes = String.join("\n", erreurs);
        assertFalse(erreurs.isEmpty());
        assertTrue(toutes.contains("Horreur"), "enumeration genre");
        assertTrue(toutes.contains("11.5"), "note > 10");
        assertTrue(toutes.contains("F01"), "motif de l'identifiant");
        assertTrue(toutes.contains("F999"), "keyref seance -> film");
        assertTrue(toutes.contains("actif"), "attribut obligatoire");
    }

    // ---------------------- XPath ----------------------

    @Test
    void titresDesFilms() throws Exception {
        assertEquals(List.of("Inception", "Interstellar", "Intouchables", "Le Roi Lion", "Parasite", "The Revenant"),
                CinemaXmlApp.valeurs(document, "/cinema/films/film/titre"));
    }

    @Test
    void utilisateursActifs() throws Exception {
        assertEquals(List.of("U01", "U03", "U04"),
                CinemaXmlApp.valeurs(document, "//utilisateur[@actif='true']/@id"));
    }

    @Test
    void filmsEnPromotion() throws Exception {
        assertEquals(List.of("Inception", "Intouchables"),
                CinemaXmlApp.valeurs(document,
                        "/cinema/films/film[@id = /cinema/seances/seance[@promotion='true']/@film]/titre"));
    }

    @Test
    void filmsDeLeonardoDiCaprio() throws Exception {
        assertEquals(List.of("Inception", "The Revenant"),
                CinemaXmlApp.valeurs(document, "//film[acteurs/acteur = 'Leonardo DiCaprio']/titre"));
    }

    @Test
    void fonctionsDAgregation() throws Exception {
        assertEquals(6.0, CinemaXmlApp.interroger(document,
                new CinemaXmlApp.Requete("", "count(//film)", XPathConstants.NUMBER)));
        assertEquals(8.0, CinemaXmlApp.interroger(document,
                new CinemaXmlApp.Requete("", "sum(//reservation/@places)", XPathConstants.NUMBER)));
        assertEquals("Salle Carthage", CinemaXmlApp.interroger(document,
                new CinemaXmlApp.Requete("", "//salle[not(@capacite < //salle/@capacite)]/@nom",
                        XPathConstants.STRING)));
    }

    @Test
    void filmsReservesParU01() throws Exception {
        assertEquals(List.of("Inception", "Parasite"),
                CinemaXmlApp.valeurs(document,
                        "//film[@id = //seance[@id = //reservation[@utilisateur='U01']/@seance]/@film]/titre"));
    }

    @Test
    void toutesLesRequetesDuProgrammeSontEvaluables() throws Exception {
        for (CinemaXmlApp.Requete r : CinemaXmlApp.REQUETES) {
            assertNotNull(CinemaXmlApp.interroger(document, r), r.description());
        }
    }
}

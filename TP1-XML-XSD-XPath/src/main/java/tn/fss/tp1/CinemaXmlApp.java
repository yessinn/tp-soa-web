package tn.fss.tp1;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TP1 - Modélisation XML et interrogation XPath.
 * <p>
 * Classe principale regroupant les deux fonctionnalités demandées :
 * <ol>
 *     <li>Validation d'un document XML par rapport à un schéma XSD
 *         ({@link SchemaFactory} + {@link Validator}) ;</li>
 *     <li>Interrogation du document avec XPath
 *         ({@link XPathFactory} + {@link XPathExpression}).</li>
 * </ol>
 * Utilisation : {@code mvn compile exec:java} (fichiers du classpath) ou
 * {@code mvn compile exec:java -Dexec.args="chemin/doc.xml chemin/schema.xsd"}.
 */
public class CinemaXmlApp {

    /** Une requête XPath : description, expression et type de résultat attendu. */
    public record Requete(String description, String expression, QName typeResultat) {
    }

    /** Requêtes XPath d'exemple sur le document cinema.xml. */
    public static final List<Requete> REQUETES = List.of(
            new Requete("1. Titres de tous les films",
                    "/cinema/films/film/titre", XPathConstants.NODESET),
            new Requete("2. Utilisateurs actifs (prénom nom)",
                    "/cinema/utilisateurs/utilisateur[@actif='true']", XPathConstants.NODESET),
            new Requete("3. Séances en promotion (identifiants)",
                    "/cinema/seances/seance[@promotion='true']/@id", XPathConstants.NODESET),
            new Requete("4. Titres des films projetés en promotion",
                    "/cinema/films/film[@id = /cinema/seances/seance[@promotion='true']/@film]/titre",
                    XPathConstants.NODESET),
            new Requete("5. Films de science-fiction",
                    "//film[@genre='Science-Fiction']/titre", XPathConstants.NODESET),
            new Requete("6. Films dont la note est strictement supérieure à 8.5",
                    "//film[note > 8.5]/titre", XPathConstants.NODESET),
            new Requete("7. Films dans lesquels joue Leonardo DiCaprio",
                    "//film[acteurs/acteur = 'Leonardo DiCaprio']/titre", XPathConstants.NODESET),
            new Requete("8. Nombre total de films",
                    "count(//film)", XPathConstants.NUMBER),
            new Requete("9. Durée moyenne des films (minutes)",
                    "sum(//film/duree) div count(//film)", XPathConstants.NUMBER),
            new Requete("10. Nom de la salle ayant la plus grande capacité",
                    "//salle[not(@capacite < //salle/@capacite)]/@nom", XPathConstants.STRING),
            new Requete("11. Nombre total de places réservées",
                    "sum(//reservation/@places)", XPathConstants.NUMBER),
            new Requete("12. Films réservés par l'utilisateur U01",
                    "//film[@id = //seance[@id = //reservation[@utilisateur='U01']/@seance]/@film]/titre",
                    XPathConstants.NODESET),
            new Requete("13. Abonnés Premium actifs (e-mail)",
                    "//utilisateur[@actif='true' and abonnement='Premium']/email", XPathConstants.NODESET),
            new Requete("14. Le cinéma contient-il un film de plus de 160 minutes ?",
                    "count(//film[duree > 160]) > 0", XPathConstants.BOOLEAN)
    );

    public static void main(String[] args) throws Exception {
        Source xml;
        Source xsd;
        if (args.length >= 2) {
            xml = new StreamSource(new File(args[0]));
            xsd = new StreamSource(new File(args[1]));
        } else {
            xml = ressource("cinema.xml");
            xsd = ressource("cinema.xsd");
        }

        // ================= 1. Validation XML / XSD =================
        System.out.println("==================================================");
        System.out.println(" 1. Validation XML/XSD");
        System.out.println("==================================================");
        System.out.println("Document : " + xml.getSystemId());
        System.out.println("Schéma   : " + xsd.getSystemId());

        List<String> erreurs = valider(xml, xsd);
        if (!erreurs.isEmpty()) {
            System.out.println("Résultat : document INVALIDE (" + erreurs.size() + " erreur(s))");
            erreurs.forEach(e -> System.out.println("  - " + e));
            System.out.println("L'interrogation XPath n'est effectuée que sur un document valide.");
            System.exit(1);
        }
        System.out.println("Résultat : document VALIDE par rapport au schéma XSD");

        // ================= 2. Interrogation XPath ==================
        System.out.println();
        System.out.println("==================================================");
        System.out.println(" 2. Interrogation XPath");
        System.out.println("==================================================");
        Document document = charger(xml.getSystemId());
        for (Requete requete : REQUETES) {
            System.out.println(requete.description());
            System.out.println("   XPath : " + requete.expression());
            Object resultat = interroger(document, requete);
            System.out.println("   => " + formater(resultat));
            System.out.println();
        }
    }

    // ------------------------------------------------------------------
    // Fonctionnalité 1 : validation XML / XSD
    // ------------------------------------------------------------------

    /**
     * Valide un document XML par rapport à un schéma XSD.
     *
     * @return la liste des erreurs détectées (vide si le document est valide)
     */
    public static List<String> valider(Source xml, Source xsd) throws SAXException, IOException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        // Sécurité : interdire l'accès aux DTD / schémas externes (prévention XXE)
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        Schema schema = factory.newSchema(xsd);
        Validator validator = schema.newValidator();
        validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        // On collecte toutes les erreurs au lieu de s'arrêter à la première
        List<String> erreurs = new ArrayList<>();
        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) {
                erreurs.add(message("Avertissement", e));
            }

            @Override
            public void error(SAXParseException e) {
                erreurs.add(message("Erreur", e));
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXParseException {
                erreurs.add(message("Erreur fatale", e));
                throw e; // le document n'est pas bien formé : impossible de continuer
            }
        });

        try {
            validator.validate(xml);
        } catch (SAXParseException e) {
            // déjà enregistrée par fatalError()
        }
        return erreurs;
    }

    private static String message(String niveau, SAXParseException e) {
        return niveau + " ligne " + e.getLineNumber() + ", colonne " + e.getColumnNumber()
                + " : " + e.getMessage();
    }

    // ------------------------------------------------------------------
    // Fonctionnalité 2 : interrogation XPath
    // ------------------------------------------------------------------

    /** Charge le document XML en arbre DOM. */
    public static Document charger(String systemId)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        return builder.parse(systemId);
    }

    /** Compile puis évalue une requête XPath sur le document. */
    public static Object interroger(Document document, Requete requete) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expression = xpath.compile(requete.expression());
        return expression.evaluate(document, requete.typeResultat());
    }

    /** Raccourci : évalue une expression XPath et renvoie les valeurs textuelles des nœuds. */
    public static List<String> valeurs(Document document, String expression) throws XPathExpressionException {
        NodeList noeuds = (NodeList) interroger(document,
                new Requete("", expression, XPathConstants.NODESET));
        List<String> valeurs = new ArrayList<>();
        for (int i = 0; i < noeuds.getLength(); i++) {
            valeurs.add(texte(noeuds.item(i)));
        }
        return valeurs;
    }

    // ------------------------------------------------------------------
    // Utilitaires
    // ------------------------------------------------------------------

    private static String formater(Object resultat) {
        if (resultat instanceof NodeList noeuds) {
            List<String> valeurs = new ArrayList<>();
            for (int i = 0; i < noeuds.getLength(); i++) {
                valeurs.add(texte(noeuds.item(i)));
            }
            return valeurs.isEmpty() ? "(aucun résultat)" : String.join(" | ", valeurs);
        }
        if (resultat instanceof Double nombre) {
            return nombre == Math.floor(nombre) && !nombre.isInfinite()
                    ? String.valueOf(nombre.longValue())
                    : String.format(Locale.ROOT, "%.2f", nombre);
        }
        return String.valueOf(resultat);
    }

    /** Texte lisible d'un nœud : valeur d'un attribut ou « nom prénom » pour un utilisateur. */
    private static String texte(Node noeud) {
        if ("utilisateur".equals(noeud.getNodeName())) {
            org.w3c.dom.Element e = (org.w3c.dom.Element) noeud;
            return e.getElementsByTagName("prenom").item(0).getTextContent() + " "
                    + e.getElementsByTagName("nom").item(0).getTextContent()
                    + " (" + e.getAttribute("id") + ")";
        }
        return noeud.getTextContent().trim();
    }

    private static Source ressource(String nom) {
        URL url = CinemaXmlApp.class.getClassLoader().getResource(nom);
        if (url == null) {
            throw new IllegalStateException("Ressource introuvable dans le classpath : " + nom);
        }
        return new StreamSource(url.toExternalForm());
    }
}

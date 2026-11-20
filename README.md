# TP : Architecture SOA et services web

**Faculté des Sciences de Sfax** · LSI3-GLSI · 2025-2026
Enseignantes : Corinne Amel ZAYANI, Hela HAKIM, Rim MOHAMED

Chaque TP est un projet Maven indépendant, dans son propre dossier.

| TP | Dossier | Contenu | Livrables |
|---|---|---|---|
| **TP1** : modélisation XML et interrogation XPath | [`TP1-XML-XSD-XPath`](TP1-XML-XSD-XPath) | Domaine cinéma : XML + XSD (types, restrictions, `key`/`keyref`). Validation avec `SchemaFactory`/`Validator`. 14 requêtes XPath avec `XPathFactory`/`XPathExpression` | `pom.xml`, `cinema.xml`, `cinema.xsd`, `CinemaXmlApp.java`, [document de modélisation](TP1-XML-XSD-XPath/README.md) |
| **TP2** : Jakarta XML Web Services (SOAP) | [`TP2-SOAP-CalculatriceWS`](TP2-SOAP-CalculatriceWS) | Service `CalculatriceWS` (somme, soustraction, multiplication, division). Contrat `ICalculatrice` + implémentation `Calculatrice`, publiés avec `Endpoint.publish`. SOAP Fault en cas de division par zéro. WSDL/XSD générés, requêtes SoapUI | [Rapport Word](TP2-SOAP-CalculatriceWS/docs/Rapport_TP2_CalculatriceWS.docx) |
| **TP3** : service REST avec JAX-RS (Jersey) | [`TP3-REST-Calculatrice`](TP3-REST-Calculatrice) | Ressource `/api/calcul` (4 opérations en GET, plus un POST JSON). Réponses JSON/XML, erreurs 400. `.war` déployé dans Tomcat 10.1, collection Postman | [`.war`](TP3-REST-Calculatrice/livrables/CalculatriceREST.war), [rapport PDF](TP3-REST-Calculatrice/docs/Rapport_TP3_REST_Calculatrice.pdf) |

## Prérequis

- JDK 17 ou plus (testé avec JDK 21)
- Maven 3.9
- IntelliJ IDEA
- SoapUI (TP2)
- Apache Tomcat **10.1** ou plus (TP3)
- Postman (TP3)

## Commandes rapides

```bash
# TP1 : tests + exécution (validation XSD puis requêtes XPath)
cd TP1-XML-XSD-XPath && mvn clean test && mvn compile exec:java

# TP2 : publier le service SOAP, puis WSDL sur http://localhost:8686/CalculatriceWS?wsdl
cd TP2-SOAP-CalculatriceWS && mvn compile exec:exec

# TP3 : construire le .war, puis le copier dans <tomcat>/webapps
cd TP3-REST-Calculatrice && mvn clean package
```

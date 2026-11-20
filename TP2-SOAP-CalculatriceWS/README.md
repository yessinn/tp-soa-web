# TP2 : Mise en œuvre de Jakarta XML Web Services : CalculatriceWS

**Matière :** Architecture SOA et services web · **Filière :** LSI3-GLSI · FSS, 2025-2026

Application Java **CalculatriceWS**. Elle expose un service web **SOAP** avec quatre opérations : `somme`, `soustraction`, `multiplication` et `division`. Le service est écrit avec **Jakarta XML Web Services** (ex JAX-WS), publié avec `Endpoint.publish` et testé avec **SoapUI**.

Le rapport Word est dans [`docs/Rapport_TP2_CalculatriceWS.docx`](docs/Rapport_TP2_CalculatriceWS.docx).

---

## Structure du projet

```
TP2-SOAP-CalculatriceWS/
├── pom.xml                                  ← dépendances Jakarta XML Web Services
├── src/main/java/
│   ├── service/
│   │   ├── ICalculatrice.java               ← contrat (interface @WebService)
│   │   ├── Calculatrice.java                ← classe métier (implémentation)
│   │   └── DivisionParZeroException.java    ← exception métier → SOAP Fault
│   └── serveur/
│       └── ServeurJWS.java                  ← publication via Endpoint.publish
├── soapui/                                  ← requêtes SOAP (à coller dans SoapUI) et réponses obtenues
└── docs/
    ├── CalculatriceWS.wsdl                  ← WSDL généré (copie de ?wsdl)
    ├── CalculatriceWS.xsd                   ← XSD généré (copie de ?xsd=1)
    └── Rapport_TP2_CalculatriceWS.docx      ← document Word
```

## Activité 1 : projet Maven et dépendances

Depuis Java 9, JAX-WS n'est plus inclus dans le JDK. Il faut donc ajouter les trois dépendances vues en cours :

| Dépendance | Version | Rôle |
|---|---|---|
| `jakarta.xml.ws:jakarta.xml.ws-api` | 4.0.0 | API : interfaces comme `Endpoint` |
| `com.sun.xml.ws:jaxws-rt` | 4.0.2 | Implémentation de référence (RI) : exécute le service, traite les requêtes SOAP, génère le WSDL |
| `jakarta.jws:jakarta.jws-api` | 3.0.0 | Annotations `@WebService`, `@WebMethod`, `@WebParam` |

Les packages `service` et `serveur` sont créés dans `src/main/java`.

## Activité 2 : classe métier

`Calculatrice` implémente `ICalculatrice`. Conformément à la consigne, on a d'abord implémenté **une seule opération (`somme`)**, puis on l'a publiée et testée. Les trois autres ont été ajoutées ensuite. L'historique Git montre ces deux étapes (commit « étape 1 » puis commit « étape 2 »).

Les opérandes sont de type `double` pour que la division donne un résultat exact (ex. 15 / 4 = 3.75). Une division par zéro lève l'exception métier `DivisionParZeroException`.

## Activité 3 : annotations

```java
@WebService(name = "CalculatriceWS", targetNamespace = "http://service/")
public interface ICalculatrice {
    @WebMethod(operationName = "somme")
    double somme(@WebParam(name = "a") double a, @WebParam(name = "b") double b);
    ...
    @WebMethod(operationName = "division")
    double division(@WebParam(name = "a") double a, @WebParam(name = "b") double b)
            throws DivisionParZeroException;
}

@WebService(endpointInterface = "service.ICalculatrice", targetNamespace = "http://service/",
            serviceName = "CalculatriceWSService", portName = "CalculatriceWSPort")
public class Calculatrice implements ICalculatrice { ... }
```

| Annotation / paramètre | Effet dans le WSDL |
|---|---|
| `@WebService(name = "CalculatriceWS")` | `<portType name="CalculatriceWS">` |
| `targetNamespace = "http://service/"` | `targetNamespace` du WSDL et du XSD, préfixe `tns` |
| `endpointInterface = "service.ICalculatrice"` | sépare le contrat de l'implémentation : le WSDL est construit à partir de l'interface |
| `serviceName` / `portName` | `<service name="CalculatriceWSService">` / `<port name="CalculatriceWSPort">` |
| `@WebMethod(operationName = ...)` | `<operation name="somme">`… |
| `@WebParam(name = "a")` | éléments `<a>` et `<b>` du type complexe de la requête (au lieu de `arg0`, `arg1`) |
| `throws DivisionParZeroException` + `@WebFault` | `<fault name="DivisionParZeroException">` dans l'opération `division` |

## Activité 4 : publication du service

```java
public class ServeurJWS {
    public static final String URL = "http://localhost:8686/CalculatriceWS";
    public static void main(String[] args) {
        Endpoint.publish(URL, new Calculatrice());
    }
}
```

**Lancer le serveur :**

- **IntelliJ IDEA** : exécuter `serveur.ServeurJWS.main()`.
- **Maven** : `mvn compile exec:exec`.

> Il faut utiliser `exec:exec` (JVM séparée) et non `exec:java`. Avec `exec:java`, les threads du serveur HTTP intégré ne voient pas l'implémentation JAXB, et la génération du SOAP Fault de la division par zéro échoue (`Implementation of Jakarta XML Binding-API has not been found`).

**URLs :**

- Service : <http://localhost:8686/CalculatriceWS>
- WSDL : <http://localhost:8686/CalculatriceWS?wsdl>
- XSD généré : <http://localhost:8686/CalculatriceWS?xsd=1>

### Correspondance classe métier / contrat WSDL

| Java | WSDL / XSD généré |
|---|---|
| interface `ICalculatrice` (`name = "CalculatriceWS"`) | `<portType name="CalculatriceWS">` |
| méthode `somme(double a, double b)` | `<operation name="somme">` + messages `somme` / `sommeResponse` |
| paramètres `a`, `b` | `<xs:complexType name="somme"><xs:sequence><xs:element name="a" type="xs:double"/><xs:element name="b" type="xs:double"/>` |
| valeur de retour `double` | `<xs:complexType name="sommeResponse">` avec `<xs:element name="return" type="xs:double"/>` |
| `DivisionParZeroException` | `<xs:complexType name="DivisionParZeroException">` (champ `message`) + `<message name="DivisionParZeroException">` |
| style par défaut | `<soap:binding style="document">` + `<soap:body use="literal"/>` (document/literal wrapped) |
| `Endpoint.publish(URL, …)` | `<soap:address location="http://localhost:8686/CalculatriceWS"/>` |

Les fichiers complets sont dans [`docs/CalculatriceWS.wsdl`](docs/CalculatriceWS.wsdl) et [`docs/CalculatriceWS.xsd`](docs/CalculatriceWS.xsd).

## Activité 5 : tests avec SoapUI

1. *File → New SOAP Project*. Initial WSDL : `http://localhost:8686/CalculatriceWS?wsdl`.
2. SoapUI analyse le WSDL et affiche le binding `CalculatriceWSPortBinding` avec ses 4 opérations.
3. Ouvrir *Request 1* d'une opération, remplacer les `?` par les valeurs, puis exécuter (▶).

Les requêtes sont dans [`soapui/`](soapui/). Résultats obtenus :

| Opération | Requête (a, b) | Réponse |
|---|---|---|
| somme | 5, 3 | `<return>8.0</return>` |
| soustraction | 10, 4 | `<return>6.0</return>` |
| multiplication | 6, 7 | `<return>42.0</return>` |
| division | 15, 4 | `<return>3.75</return>` |
| division | 10, 0 | **SOAP Fault** (HTTP 500) : `faultstring` = « Division par zéro impossible : b doit être différent de 0 » |

Exemple de requête et de réponse (somme) :

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://service/">
   <soapenv:Header/>
   <soapenv:Body>
      <ser:somme>
         <a>5</a>
         <b>3</b>
      </ser:somme>
   </soapenv:Body>
</soapenv:Envelope>
```

```xml
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
   <S:Body>
      <ns2:sommeResponse xmlns:ns2="http://service/">
         <return>8.0</return>
      </ns2:sommeResponse>
   </S:Body>
</S:Envelope>
```

Le service peut aussi être testé sans SoapUI, avec curl :

```bash
curl -X POST http://localhost:8686/CalculatriceWS -H "Content-Type: text/xml;charset=UTF-8" --data-binary @soapui/somme-request.xml
```

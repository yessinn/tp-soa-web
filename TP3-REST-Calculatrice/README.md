# TP3 : Service REST Calculatrice avec Jakarta EE (JAX-RS)

**Matière :** Architecture SOA et services web · **Filière :** LSI3 · FSS, 2025-2026

Service REST qui propose l'addition, la soustraction, la multiplication et la division. Il est écrit avec **JAX-RS** et l'implémentation **Jersey**, packagé en `.war` avec Maven, déployé dans **Tomcat 10.1** et testé avec **Postman** et un navigateur.

## Livrables

| Livrable | Emplacement |
|---|---|
| Projet Maven complet | ce dossier |
| Fichier `.war` | [`livrables/CalculatriceREST.war`](livrables/CalculatriceREST.war) |
| Rapport PDF : environnement, captures, description de l'API, schéma d'architecture | [`docs/Rapport_TP3_REST_Calculatrice.pdf`](docs/Rapport_TP3_REST_Calculatrice.pdf) |
| Collection Postman (8 requêtes, 16 assertions) | [`postman/CalculatriceREST.postman_collection.json`](postman/CalculatriceREST.postman_collection.json) |
| Résultat de l'exécution de la collection | [`postman/resultats-postman-cli.txt`](postman/resultats-postman-cli.txt) |

## Structure du projet

```
TP3-REST-Calculatrice/
├── pom.xml                                   ← packaging war, Jersey 3.1.4
└── src/
    ├── main/java/
    │   ├── metier/Calculatrice.java          ← classe métier
    │   ├── model/Operation.java              ← modèle JSON/XML {a, b, op, res}
    │   └── rest/
    │       ├── RestApplication.java          ← @ApplicationPath("/api")
    │       └── CalculRest.java               ← ressource @Path("/calcul")
    └── test/java/metier/CalculatriceTest.java
```

## Dépendances

| Dépendance | Rôle |
|---|---|
| `jakarta.ws.rs-api` 3.1.0 | API JAX-RS (annotations et interfaces uniquement) |
| `jersey-container-servlet` 3.1.4 | Servlet Jersey : pont entre Tomcat et les ressources REST |
| `jersey-hk2` 3.1.4 | Injection de dépendances, **obligatoire** avec Jersey 3 (sinon `InjectionManagerFactory not found`) |
| `jersey-media-json-binding` 3.1.4 | Support JSON (JSON-B) |
| `jersey-media-jaxb` 3.1.4 + `jaxb-runtime` 4.0.4 | Support XML (JAXB) |

## API

URL de base : `http://localhost:8080/CalculatriceREST/api/calcul`

| Méthode | URI | Exemple de réponse |
|---|---|---|
| GET | `/somme?a=5&b=3` | `{"a":5.0,"b":3.0,"op":"+","res":8.0}` |
| GET | `/soustraction?a=10&b=4` | `{"a":10.0,"b":4.0,"op":"-","res":6.0}` |
| GET | `/multiplication?a=6&b=7` | `{"a":6.0,"b":7.0,"op":"*","res":42.0}` |
| GET | `/division?a=15&b=4` | `{"a":15.0,"b":4.0,"op":"/","res":3.75}` |
| POST | `/` avec le corps `{"a":5,"b":3,"op":"*"}` | `{"a":5.0,"b":3.0,"op":"*","res":15.0}` |

- **Format de réponse.** JSON par défaut, XML si le client envoie `Accept: application/xml` (exemple : `<operation><a>5.0</a><b>3.0</b><op>+</op><res>8.0</res></operation>`).
- **Erreurs (400 Bad Request).** Une division par zéro, un paramètre manquant ou un opérateur inconnu renvoient `400 Bad Request` avec le corps `{"status":400,"erreur":"..."}`.

## Compiler, déployer, tester

```bash
mvn clean package                    # tests JUnit + target/CalculatriceREST.war
```

1. Copier `target/CalculatriceREST.war` dans le dossier `webapps/` de **Tomcat 10.1** (ou plus).
2. Démarrer Tomcat : `bin/startup.bat` ou `bin/catalina.bat run`.
3. Ouvrir <http://localhost:8080/CalculatriceREST/api/calcul/somme?a=5&b=3>.

> **Tomcat 9 ne convient pas.** Jersey 3 utilise `jakarta.*`, alors que Tomcat 9 implémente encore `javax.servlet`. L'application s'y déploierait sans erreur, mais toutes les URL `/api/*` renverraient 404.

**Tests Postman :**

- **Interface graphique.** *Import* → `postman/CalculatriceREST.postman_collection.json` → *Run collection*.
- **Ligne de commande** :

```bash
postman collection run postman/CalculatriceREST.postman_collection.json
```

Résultat obtenu : 8 requêtes, 16 assertions, 0 échec.

## Architecture

```
Client (Postman / navigateur)
   │  GET /CalculatriceREST/api/calcul/somme?a=5&b=3
   ▼
Tomcat 10.1 (connecteur HTTP :8080, contexte /CalculatriceREST)
   ▼
Jersey ServletContainer (/api/* ← @ApplicationPath)  ── routage @Path / @GET, conversion @QueryParam
   ▼
CalculRest (@Path("/calcul"))  ──►  Calculatrice (métier)
   ▼
Operation {a, b, op, res}  ──►  JSON (JSON-B) ou XML (JAXB) selon Accept  ──►  réponse HTTP
```

# TP1 : Modélisation XML et interrogation XPath avec Maven en Java

**Matière :** Architecture SOA et services web · **Filière :** LSI3 · FSS, 2025-2026

Ce document décrit la modélisation (structure, choix, contraintes) et les requêtes XPath. Il fait partie des livrables du TP.

---

## 1. Domaine choisi : gestion d'un cinéma

Le cinéma « Le Colisée » (Sfax) gère :

| Entité | Rôle | Identifiant |
|---|---|---|
| **Film** | œuvre projetée (titre, réalisateur, année, durée, note, acteurs, synopsis) | `F001`, `F002`… |
| **Salle** | salle de projection (nom, capacité, type Standard/3D/IMAX) | `S1`, `S2`… |
| **Séance** | projection d'un film dans une salle à une date/heure, avec un prix et un indicateur de promotion | `SE01`… |
| **Utilisateur** | client inscrit (nom, prénom, e-mail, abonnement), actif ou non | `U01`… |
| **Réservation** | réservation de *n* places par un utilisateur pour une séance | `R001`… |

Ce domaine couvre directement les exemples de requêtes demandés dans l'énoncé : titres de films, utilisateurs actifs, séances (produits) en promotion.

## 2. Structure du document XML

```
cinema (@nom, @ville)
├── films
│   └── film+ (@id, @genre, @langue?)
│       ├── titre, realisateur, annee, duree, note
│       ├── acteurs
│       │   └── acteur+
│       └── synopsis?
├── salles
│   └── salle+ (@id, @nom, @capacite, @type)            ← élément vide
├── seances
│   └── seance* (@id, @film → film/@id, @salle → salle/@id, @promotion?)
│       └── date, heure, prix
├── utilisateurs
│   └── utilisateur* (@id, @actif)
│       └── nom, prenom, email, abonnement
└── reservations
    └── reservation* (@id, @utilisateur → utilisateur/@id, @seance → seance/@id, @places)
```

`+` = 1..n, `*` = 0..n, `?` = optionnel, `→` = référence vérifiée par le schéma (`xs:keyref`).

### Choix de modélisation

1. **Éléments ou attributs.** Les données descriptives et potentiellement longues ou multiples (titre, acteurs, synopsis, e-mail…) sont des **éléments**. Les métadonnées courtes et atomiques (identifiants, genre, langue, état `actif`, `promotion`, capacité) sont des **attributs**. Une salle et une réservation ne contiennent que des métadonnées : ce sont des **éléments vides** avec attributs.
2. **Éléments conteneurs** (`films`, `salles`, `seances`, `utilisateurs`, `reservations`). Ils regroupent les entités par catégorie. Le document est plus lisible et les chemins XPath plus simples (`/cinema/films/film`).
3. **Pas de duplication (normalisation).** Une séance ne recopie pas le film : elle le **référence** par son identifiant (`film="F001"`), comme une clé étrangère en base de données. Les jointures se font ensuite en XPath (requêtes 4 et 12).
4. **Liste d'acteurs.** Un film a plusieurs acteurs. On utilise un élément `acteur` répété (`maxOccurs="unbounded"`) et non une chaîne séparée par des virgules, pour pouvoir interroger un acteur précis en XPath (requête 7).
5. **Pas d'espace de noms cible.** Le schéma est associé au document par `xsi:noNamespaceSchemaLocation="cinema.xsd"`, comme vu en cours (XML Schema : syntaxe). Les expressions XPath restent ainsi sans préfixe.

## 3. Schéma XSD (`src/main/resources/cinema.xsd`)

Le schéma utilise des **types nommés** réutilisables (`FilmType`, `SeanceType`…) et les mécanismes suivants :

| Mécanisme XSD | Utilisation |
|---|---|
| `xs:sequence` | ordre imposé des sous-éléments (ex. `titre`, `realisateur`, `annee`…) |
| `minOccurs` / `maxOccurs` | `synopsis` optionnel, au moins un `acteur`, séances/utilisateurs/réservations 0..n |
| `xs:attribute use="required"` / `default` | `id` obligatoire ; `langue` vaut `VF` et `promotion` vaut `false` par défaut |
| `xs:restriction` + `xs:enumeration` | `GenreType`, `LangueType`, `TypeSalleType`, `AbonnementType` |
| `xs:restriction` + `xs:pattern` | format des identifiants (`F[0-9]{3}`, `SE[0-9]{2}`…) et de l'e-mail |
| `minInclusive` / `maxInclusive` / `fractionDigits` | note entre 0 et 10 (1 décimale), durée ≤ 300 min, prix ≥ 0 (3 décimales, dinars), places ≤ 10 |
| Types prédéfinis | `xs:date`, `xs:time`, `xs:gYear`, `xs:decimal`, `xs:boolean`, `xs:positiveInteger` |
| `xs:key` | unicité des identifiants de chaque entité |
| `xs:unique` | unicité de l'e-mail des utilisateurs |
| `xs:keyref` | intégrité référentielle : séance → film/salle, réservation → utilisateur/séance |

## 4. Projet Maven

```
TP1-XML-XSD-XPath/
├── pom.xml
├── src/main/java/tn/fss/tp1/CinemaXmlApp.java   ← classe principale (validation + XPath)
├── src/main/resources/cinema.xml                ← document XML
├── src/main/resources/cinema.xsd                ← schéma XSD
├── src/test/java/tn/fss/tp1/CinemaXmlAppTest.java
└── src/test/resources/cinema-invalide.xml       ← document volontairement invalide
```

**Dépendances (`pom.xml`).** Les API utilisées (JAXP : `javax.xml.parsers`, `javax.xml.validation`, `javax.xml.xpath`) font partie du JDK (module `java.xml`). Aucune bibliothèque externe n'est donc requise pour la validation et XPath. Le `pom.xml` déclare :

- `junit-jupiter` (tests unitaires) ;
- `exec-maven-plugin` (lancer la classe principale avec Maven) ;
- Java 17 (`maven.compiler.release`).

### Fonctionnalité 1 : validation XML/XSD

```java
SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
Schema schema = factory.newSchema(xsd);
Validator validator = schema.newValidator();
validator.setErrorHandler(...);   // collecte TOUTES les erreurs avec ligne/colonne
validator.validate(xml);
```

- Un `ErrorHandler` personnalisé enregistre toutes les erreurs au lieu de s'arrêter à la première.
- L'accès aux DTD et schémas externes est désactivé (`ACCESS_EXTERNAL_DTD`, `ACCESS_EXTERNAL_SCHEMA`) pour prévenir les attaques XXE.
- L'interrogation XPath n'est lancée que si le document est valide.

### Fonctionnalité 2 : interrogation XPath

```java
XPath xpath = XPathFactory.newInstance().newXPath();
XPathExpression expression = xpath.compile("/cinema/films/film/titre");
NodeList titres = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
```

Le type de retour dépend de la requête : `NODESET`, `NUMBER`, `STRING` ou `BOOLEAN`.

## 5. Requêtes XPath et résultats obtenus

| # | Besoin | Expression XPath | Résultat |
|---|---|---|---|
| 1 | Titres de tous les films | `/cinema/films/film/titre` | Inception, Interstellar, Intouchables, Le Roi Lion, Parasite, The Revenant |
| 2 | **Utilisateurs actifs** | `/cinema/utilisateurs/utilisateur[@actif='true']` | Amine Ben Salah, Mehdi Gharbi, Nour Karray |
| 3 | **Séances en promotion** | `/cinema/seances/seance[@promotion='true']/@id` | SE01, SE03 |
| 4 | Titres des films projetés en promotion (jointure) | `/cinema/films/film[@id = /cinema/seances/seance[@promotion='true']/@film]/titre` | Inception, Intouchables |
| 5 | Films de science-fiction | `//film[@genre='Science-Fiction']/titre` | Inception, Interstellar |
| 6 | Films notés plus de 8.5 | `//film[note > 8.5]/titre` | Inception, Interstellar |
| 7 | Films avec Leonardo DiCaprio | `//film[acteurs/acteur = 'Leonardo DiCaprio']/titre` | Inception, The Revenant |
| 8 | Nombre de films | `count(//film)` | 6 |
| 9 | Durée moyenne (XPath 1.0 n'a pas `avg`) | `sum(//film/duree) div count(//film)` | 134.17 |
| 10 | Salle de plus grande capacité (maximum sans `max()`) | `//salle[not(@capacite < //salle/@capacite)]/@nom` | Salle Carthage |
| 11 | Total des places réservées | `sum(//reservation/@places)` | 8 |
| 12 | Films réservés par U01 (double jointure) | `//film[@id = //seance[@id = //reservation[@utilisateur='U01']/@seance]/@film]/titre` | Inception, Parasite |
| 13 | E-mails des abonnés Premium actifs | `//utilisateur[@actif='true' and abonnement='Premium']/email` | amine.bensalah@…, nour.karray@… |
| 14 | Existe-t-il un film de plus de 160 min ? | `count(//film[duree > 160]) > 0` | true |

**Remarques :**

- La séance `SE04` a `promotion="false"` explicitement et `SE02`/`SE05` utilisent la valeur par défaut. Seules `SE01` et `SE03` sont en promotion.
- Pour la requête 4, l'opérateur `=` entre un attribut et un *node-set* est vrai si **au moins un** nœud du node-set a la même valeur. C'est ce qui permet d'exprimer une jointure en XPath 1.0.
- Pour la requête 10, une salle est retenue si **aucune** autre salle n'a une capacité strictement supérieure. C'est l'équivalent d'un `max()` en XPath 1.0.

## 6. Exécution et tests

```bash
# Tests unitaires (validation du document valide/invalide + résultats XPath)
mvn clean test

# Exécution de la classe principale sur cinema.xml / cinema.xsd
mvn compile exec:java

# Exécution sur un autre document (ex. le document invalide)
mvn compile exec:java -Dexec.args="src/test/resources/cinema-invalide.xml src/main/resources/cinema.xsd"
```

Dans IntelliJ IDEA : ouvrir le dossier `TP1-XML-XSD-XPath` (projet Maven), puis exécuter `CinemaXmlApp.main()`.

**Validation du document invalide.** Le fichier `cinema-invalide.xml` contient volontairement 5 types d'erreurs. Toutes sont détectées :

```
Résultat : document INVALIDE (8 erreur(s))
  - Erreur ligne 14 : cvc-enumeration-valid: Value 'Horreur' is not facet-valid ... enumeration '[Action, Animation, ...]'
  - Erreur ligne 19 : cvc-maxInclusive-valid: Value '11.5' is not facet-valid with respect to maxInclusive '10.0' ...
  - Erreur ligne 24 : cvc-pattern-valid: Value 'F01' is not facet-valid with respect to pattern 'F[0-9]{3}' ...
  - Erreur ligne 46 : cvc-complex-type.4: Attribute 'actif' must appear on element 'utilisateur'.
  - Erreur ligne 54 : cvc-identity-constraint.4.3: Key 'refSeanceFilm' with value 'F999' not found ...
  (+ les erreurs cvc-attribute.3 / cvc-type.3.1.3 associées)
```

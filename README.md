# TP1 – Le Pass Mobilité en TDD strict

GreenMove est un opérateur de vélos et de trottinettes en libre-service, le fil rouge du cours.
La Métropole lance le « Pass Mobilité » le 1er septembre 2027 : pour un usager du Pass, le déverrouillage est offert
et 20 minutes sont incluses, sur les deux premiers trajets de la journée.
En binôme, vous développez **en TDD strict** le calcul du prix d'une location. Il n'y a qu'un seul objet du domaine à écrire,
`TarifPass`, avec trois ports à doubler dans vos tests. Pas d'adaptateur, pas de Spring, pas de base de données.

## Ce que contient le dépôt

| Élément | Rôle |
|---|---|
| `Montant` | Un montant en centimes, immuable, jamais négatif. C'est le code de la démonstration du cours, **il ne se modifie pas**. |
| `TypeVehicule` | `VELO` ou `TROTTINETTE`. |
| `Abonnements` | Port : `estAbonne(usager)` et `aUnPassActif(usager, jour)`. **Ne se modifie pas.** |
| `CompteurTrajets` | Port : `trajetsInclus(usager, jour)` et `enregistrerTrajetInclus(usager, jour)`. **Ne se modifie pas.** |
| `Horloge` | Port : `aujourdhui()`. **Ne se modifie pas.** |
| `MontantTest` | Les 8 tests de `Montant`, déjà verts. |

La classe `TarifPass` n'existe pas encore : c'est votre premier test rouge qui la fait naître.
Son constructeur reçoit les trois ports, `TarifPass(Abonnements, CompteurTrajets, Horloge)`, et sa méthode est
`Montant prix(String usager, TypeVehicule type, int minutes)`. Le domaine reste pur : il n'appelle jamais `LocalDate.now()`,
le jour vient de l'`Horloge`.

## Les règles

- **R1 Tarif normal** : déverrouillage 1,00 € puis 0,20 €/min vélo, 0,25 €/min trottinette, plafond 15,00 € par location.
  Ex. vélo 12 min : 3,40 €. Vélo 90 min : 15,00 €.
- **R2 Abonné GreenMove** : 20 % de réduction sur le total. Ex. vélo 12 min abonné : 2,72 €.
- **R3 Pass actif et trajet inclus (1er ou 2e du jour)** : déverrouillage offert, 20 premières minutes offertes,
  les suivantes au tarif à la minute. Ex. vélo 12 min : 0,00 €. Vélo 30 min : 2,00 €. Trottinette 25 min : 1,25 €.
- **R4 Pass actif, 3e trajet du jour et suivants** : tarif normal. Ex. vélo 12 min, 3e trajet : 3,40 €.
- **R5 Un trajet inclus est enregistré au compteur** (`enregistrerTrajetInclus` appelé une fois, y compris quand le prix est 0,00 €),
  un trajet non inclus ne l'est jamais.
- **R6 Abonné et Pass** : la réduction de 20 % s'applique au reste à payer. Ex. vélo 30 min, 1er trajet, abonné : 1,60 €.
- **R7 Le plafond de 15,00 € s'applique aussi aux trajets Pass.** Ex. vélo 120 min, 1er trajet : 15,00 €
  (100 min × 0,20 € = 20,00 €, plafonné).
- **R8 Durée négative refusée** (`IllegalArgumentException("durée négative")`). Le « jour » est celui donné par l'horloge
  (le fuseau Europe/Paris est l'affaire de l'adaptateur, hors TP).

Ordre de calcul : montant = (déverrouillage ou 0) + tarif minute × minutes facturées, plafonné à 15 €, puis −20 % si abonné.

## Le jeu d'essai

Ces dix lignes doivent toutes être couvertes par un test de votre suite.

| Usager | Véhicule, durée | Trajets inclus déjà faits | Prix attendu | Compteur |
|---|---|---|---|---|
| Sans Pass, non abonné | Vélo, 12 min | – | 3,40 € | rien |
| Sans Pass, abonné | Vélo, 12 min | – | 2,72 € | rien |
| Pass | Vélo, 12 min | 0 | 0,00 € | enregistré |
| Pass | Vélo, 30 min | 1 | 2,00 € | enregistré |
| Pass | Trottinette, 25 min | 0 | 1,25 € | enregistré |
| Pass | Vélo, 12 min | 2 | 3,40 € | rien |
| Pass et abonné | Vélo, 30 min | 0 | 1,60 € | enregistré |
| Pass | Vélo, 120 min | 0 | 15,00 € | enregistré |
| Sans Pass, non abonné | Vélo, 90 min | – | 15,00 € | rien |
| Sans Pass, non abonné | Vélo, −1 min | – | IllegalArgumentException | rien |

## Ce que vous devez faire

1. **Dressez la liste de vos tests avant de coder** : un fichier ou une section de votre dossier, dans l'ordre où vous
   comptez les écrire, du plus simple au plus riche. Elle évoluera, gardez une trace de ce qui change.
2. **Travaillez en cycle Red-Green-Refactor** : un test rouge, le code minimal pour le passer au vert, puis un refactoring
   quand le code ou les tests le demandent. Jamais de code de production sans test rouge qui le justifie.
3. **Faites un commit par cycle**, dont le message commence par `RED`, `GREEN` ou `REFACTOR`, suivi d'une phrase courte.
   Exemple : `GREEN un vélo de 12 minutes coûte 3,40 €`. Un commit RED contient le test rouge et le squelette vide
   nécessaire pour que le projet compile.
4. **Travaillez en ping-pong** : l'un écrit le test rouge, l'autre le fait passer au vert, puis on échange les rôles.
   **Les deux noms doivent apparaître dans l'historique git.**
5. **Doublez les trois ports dans vos tests** : `Abonnements` et `Horloge` avec des fakes ou des lambdas,
   `CompteurTrajets` avec un mock Mockito pour vérifier l'enregistrement des trajets inclus (R5).
6. **Écrivez le workflow GitHub Actions** (`.github/workflows/ci.yml`) qui lance `mvn -B verify` à chaque push,
   comme à l'Exo.3, et publie le rapport JaCoCo en artefact.
7. **Produisez le rapport de couverture JaCoCo** (`mvn verify`, rapport dans `target/site/jacoco/index.html`)
   et commentez-le dans votre dossier.
8. **Ne modifiez ni les interfaces ni `Montant`.** Si un port vous semble insuffisant, notez-le dans la partie « limites » du dossier.

## Le rendu

Une archive **`NOM1-NOM2-TP1-TDD.zip`** déposée sur Campus Connect au plus tard le **dimanche 8 novembre 2026 à 23h59**,
contenant :

- le code du projet, **sans le dossier `target`**,
- le PDF du dossier, de 3 à 5 pages : l'ordre de vos tests et pourquoi, un refactoring expliqué avant/après,
  la lecture de la couverture, les limites,
- le lien de votre dépôt GitHub (créé avec « Use this template » à partir de ce modèle),
- le rapport de couverture JaCoCo (le dossier `target/site/jacoco` ou une capture).

## Prérequis

- JDK 17 ou plus récent
- Maven 3.9 ou plus récent
- Git
- Un compte GitHub

Avant votre premier commit, vérifiez que `mvn test` est vert (les 8 tests de `Montant` passent) :

```
mvn test
```

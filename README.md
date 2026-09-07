# Simulation de propagation d'un feu de foret

Implementation Java, sans dependance externe, de l'exercice demande par Jennie Zavatra.

## Sommaire

- [Lancer le projet](#lancer-le-projet)
- [Format du fichier de configuration](#format-du-fichier-de-configuration)
- [Comment j'ai aborde le probleme](#comment-jai-aborde-le-probleme)
- [Architecture](#architecture)
- [Choix techniques et justifications](#choix-techniques-et-justifications)
- [Tests](#tests)
- [Limites connues et pistes d'evolution](#limites-connues-et-pistes-dévolution)
- [Points a mettre en avant en entretien](#points-a-mettre-en-avant-en-entretien)

## Lancer le projet

Aucune dependance externe, aucun outil de build necessaire : uniquement un JDK 17+.

```bash
# Compiler le code source principal
javac -d out $(find src/main -name "*.java")

# Compiler et lancer les tests
javac -cp out -d out-test $(find src/test -name "*.java")
java -cp out:out-test com.jennie.forestfire.TestRunner

# Lancer la simulation (config par defaut : config/forest.properties)
java -cp out com.jennie.forestfire.app.Main

# Ou avec une configuration specifique
java -cp out com.jennie.forestfire.app.Main config/forest-multi-fire.properties
```

Un script `run.sh` fait ces trois etapes en une commande : `./run.sh [chemin-config]`.

## Format du fichier de configuration

J'ai choisi le format `.properties` (natif au JDK, cle=valeur), car l'enonce
laisse le format libre et je voulais eviter d'introduire une dependance
(type Jackson/Gson pour du JSON/YAML) pour un besoin aussi simple.

```properties
grid.height=10
grid.width=15
propagation.probability=0.35
fire.initial=5,7
simulation.seed=42
```

- `grid.height` / `grid.width` : dimensions de la grille.
- `propagation.probability` : probabilite p qu'une case en feu enflamme une case adjacente saine.
- `fire.initial` : positions (ligne,colonne) initialement en feu, separees par `;`. Ex : `2,3;5,5`.
- `simulation.seed` (optionnel) : fixe la graine du generateur aleatoire pour rejouer une simulation a l'identique. A retirer pour de l'aleatoire pur.

Deux exemples fournis : `config/forest.properties` (un seul foyer) et
`config/forest-multi-fire.properties` (plusieurs foyers, probabilite plus forte).

## Comment j'ai aborde le probleme

1. **Reformulation du probleme.** Avant de coder, j'ai identifie les briques independantes de l'enonce :
   - une structure de donnees (la grille et l'etat de ses cases),
   - une regle de transition (comment on passe de l'etat t a t+1),
   - une source de configuration (parametres externes),
   - une facon d'observer le resultat (affichage).

   Ces quatre briques n'ont pas de raison de dependre les unes des autres au-dela du strict necessaire :
   c'est ce qui a guide le decoupage en packages (`model`, `engine`, `config`, `render`).

2. **Identification du point delicat.** La seule vraie subtilite de l'enonce est la simultaneite :
   toutes les transitions de l'etape t doivent se baser sur l'etat *au debut* de l'etape t, pas sur
   un etat en cours de modification. Sinon, une case enflammee pendant le calcul de l'etape t
   pourrait, par erreur, en enflammer une autre a la meme etape - avec un resultat qui dependrait de
   l'ordre de parcours de la grille (bug classique des automates cellulaires). J'ai traite ce point en
   calculant systematiquement le prochain etat a partir d'une copie immuable de l'etat courant.

3. **Ecriture incrementale, testee au fur et a mesure.** J'ai construit dans l'ordre : le modele (`Grid`),
   puis le moteur (`Simulation`) avec ses regles, puis la configuration, puis l'affichage. Chaque brique a
   ete testee independamment avant de passer a la suivante.

4. **Verification manuelle.** J'ai fait tourner la simulation avec p=0 (le feu doit s'eteindre sans se
   propager) et p=1 (le feu doit envahir toute la zone connexe) pour confirmer visuellement le
   comportement aux bornes, avant de le figer dans des tests automatises.

## Architecture

```
com.jennie.forestfire
├── model      -> structures de donnees pures (Grid, CellState, Position)
├── engine     -> regles metier de propagation (Simulation, RandomProvider)
├── config     -> lecture et validation du fichier de configuration
├── render     -> affichage console (ASCII), aucune connaissance du moteur
└── app        -> point d'entree, assemble les briques ci-dessus (composition root)
```

Le principe directeur est la **separation entre modele, logique metier et
presentation** (proche d'une architecture en couches / hexagonale simplifiee) :

- `model` ne connait ni les regles de propagation, ni la configuration, ni l'affichage.
- `engine` connait le modele mais ignore totalement comment la grille est affichee ou configuree.
- `render` connait le modele mais ignore le moteur (il se contente de lire l'etat d'une grille donnee).
- `app.Main` est le seul endroit qui connait tout le monde : c'est la ou tout est cable ensemble.

Consequence concrete : on pourrait ajouter demain un export JSON, une interface graphique, ou une
source de configuration en JSON, sans toucher une seule ligne de `engine` ou de `model`.

## Choix techniques et justifications

| Choix | Justification |
|---|---|
| Grille recalculee (copie) a chaque etape, jamais mutee en place | Garantit un resultat independant de l'ordre de parcours (cf. section ci-dessus) |
| `RandomProvider` injecte en interface plutot que `java.util.Random` en dur | Rend le moteur testable de maniere deterministe (voir `SimulationTest`, stubs `ALWAYS_IGNITE` / `NEVER_IGNITE`) et permet de fixer une seed pour rejouer une simulation |
| `Position` en classe dediee plutot que `int[]` | Lisibilite, securite (pas d'inversion ligne/colonne), `equals`/`hashCode` pour l'utiliser dans des collections |
| Fichier de configuration en `.properties` | Natif au JDK, zero dependance, format libre autorise par l'enonce |
| Pas de framework de test (JUnit) | Choix pragmatique : voir section [Tests](#tests) ci-dessous |
| `run(maxSteps, callback)` avec callback plutot que la simulation qui affiche elle-meme | Le moteur reste utilisable en dehors d'un contexte console (tests, futur GUI, traitement batch) |

## Tests

La suite de tests (`GridTest`, `SimulationTest`, `ConfigLoaderTest`) couvre :

- le modele : dimensions invalides, voisinage (coin/bord/centre), copie independante, acces hors limites ;
- le moteur : comportement aux bornes (p=0 -> extinction immediate, p=1 -> propagation totale sur une
  grille connexe), invariant "une case cendre ne rebrule jamais", terminaison de la simulation,
  validation des parametres ;
- la configuration : lecture nominale, positions multiples, parametres manquants ou invalides rejetes
  explicitement.

**Sur le choix de ne pas utiliser JUnit :** l'environnement de developpement dans lequel j'ai prepare cet
exercice n'avait pas acces a un depot Maven/Gradle pour telecharger les dependances. Plutot que de livrer
un projet qui ne compile pas chez vous sans acces reseau, j'ai ecrit une mini-suite d'assertions
(`Assert.java`, une trentaine de lignes) suffisante pour ce perimetre. Le code de test est deja structure
comme des tests JUnit (une classe par unite testee, une methode par cas, `assertEquals`/`assertThrows`) :
migrer vers JUnit 5 serait immediat (ajout des annotations `@Test`, remplacement de `Assert.` par les
imports statiques `org.junit.jupiter.api.Assertions.*`, ajout de la dependance dans un `pom.xml`). Je
suis a l'aise pour le faire en direct pendant l'entretien si vous le souhaitez.

## Limites connues et pistes d'evolution

- **Un seul type de terrain.** Toutes les cases sont inflammables de la meme maniere. On pourrait
  introduire un `CellType` (arbre, roche, eau, route coupe-feu...) qui module la probabilite de
  propagation par case, sans changer la structure globale (la regle de transition vivrait toujours
  dans `engine`, juste avec un parametre supplementaire).
- **Pas de vent ni de direction privilegiee.** Ajouter un biais directionnel reviendrait a remplacer la
  probabilite unique `p` par une fonction `p(source, cible)`.
- **Rendu console uniquement.** Le decoupage en `render` isole permet d'ajouter un export (JSON par
  etape, image PNG, page web) sans toucher au moteur.
- **Historique non conserve par defaut.** `Simulation` n'expose que l'etat courant ; si l'on veut
  rejouer/animer toutes les etapes a posteriori, il suffirait de faire consommer le callback de `run()`
  par une liste au lieu d'un affichage direct - deja prevu par la signature `Consumer<Grid>`.

## Points a mettre en avant en entretien

- Le raisonnement sur la **simultanite des transitions** (copie immuable de l'etat) : c'est le piege
  principal de l'exercice, et je peux montrer avec un exemple concret ce qui se passerait si on mutait
  la grille en place.
- L'**injection du generateur aleatoire** comme demonstration de testabilite d'un systeme stochastique.
- La facon dont le **decoupage en packages traduit des responsabilites metier** (pas juste "je range
  les fichiers"), et ce que ca permettrait de faire evoluer facilement.
- Le compromis assume sur l'absence de JUnit, et la preuve que la migration serait triviale.

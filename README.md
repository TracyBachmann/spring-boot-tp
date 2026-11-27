# API Recensement

API REST réalisée avec Spring Boot dans le cadre des TP Diginamic. Elle permet de consulter et de gérer des villes et des départements français.

Le projet utilise Java 21, Spring Boot 3.4.7, Spring Data JPA et MariaDB. Les réponses sont en JSON ; certains endpoints proposent aussi des exports CSV ou PDF.

## Préparer le projet

- Installer Java 21.
- Démarrer MySQL/MariaDB dans XAMPP (port 3306).
- Disposer de la base `recensement`.
- Vérifier les paramètres de connexion dans `src/main/resources/application.properties`. Par défaut : utilisateur `root`, mot de passe vide, pour l'installation XAMPP locale.

Pour utiliser d'autres identifiants de base de données, définir `DB_USER` et `DB_PASSWORD` dans l'environnement avant de démarrer l'application.

Depuis PowerShell, à la racine du projet :

```powershell
.\mvnw.cmd spring-boot:run
```

L'API répond sur `http://localhost:8080`.

## Authentification

La connexion s'effectue avec `POST /login` et un corps JSON :

```json
{
  "username": "demo-user",
  "password": "<mot de passe>"
}
```

La réponse est un JWT au format texte. Pour consulter une ressource protégée, envoyer ce jeton dans l'en-tête `Authorization: Bearer <JWT>`.

Les utilisateurs et leurs rôles sont stockés dans MariaDB. Les mots de passe sont hachés avec BCrypt. Les comptes de démonstration (`demo-user` et `demo-admin`) peuvent être initialisés une seule fois à l'aide des variables `SECURITY_SEED_ENABLED`, `SECURITY_USER_PASSWORD` et `SECURITY_ADMIN_PASSWORD`. L'initialisation est désactivée par défaut et ne remplace pas les comptes existants.

Les jetons expirent au bout de 15 minutes. Sans `JWT_SECRET`, une clé de signature est générée au démarrage : les anciens jetons cessent alors de fonctionner après un redémarrage. Pour conserver une clé entre les démarrages, fournir `JWT_SECRET` sous forme de chaîne Base64 représentant au moins 32 octets aléatoires. Ne jamais commiter cette valeur.

Les requêtes GET sont accessibles aux rôles USER et ADMIN. Les créations, modifications et suppressions sont réservées à ADMIN.

## Endpoints utiles

| Méthode | URL | Usage |
| --- | --- | --- |
| POST | `/login` | Obtenir un JWT |
| GET | `/villes?page=0&size=20` | Liste paginée des villes |
| GET | `/villes/{id}` | Détail d'une ville |
| GET | `/villes/recherche/nom?debut=Stras` | Recherche par nom |
| GET | `/villes/export/csv?min=100000` | Export CSV |
| GET | `/departements` | Liste des départements |
| GET | `/departements/{id}` | Détail d'un département |
| GET | `/departements/{code}/export/pdf` | Export PDF |

Les routes POST, PUT et DELETE des villes et des départements sont disponibles pour les administrateurs. Les autres recherches sont documentées dans les contrôleurs.

L'application conserve une Content Security Policy (CSP) et autorise les appels CORS provenant des pages de test locales sur les ports `localhost:4200` et `127.0.0.1:4200`. Cette configuration est prévue pour les TP, pas pour un déploiement public.

## Tests

```powershell
.\mvnw.cmd clean test
```

Les tests utilisent H2 en mémoire et ne modifient pas la base MariaDB `recensement`. Ils couvrent les services, la validation des contrôleurs et les principaux cas d'authentification JWT.

Les journaux applicatifs sont écrits dans `logs/`. Ce dossier est ignoré par Git.

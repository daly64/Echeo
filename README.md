# Echeo

Application Android de gestion d'abonnements (IPTV, Netflix, Alpha…) pour un gérant de boutique.

- Données 100 % locales (Room), un seul utilisateur.
- Compilation automatique sur GitHub à chaque envoi sur `main` : l'APK et les captures d'écran sont dans les artefacts de l'onglet **Actions**.
- Chaque version s'installe par-dessus la précédente (même clé de signature, numéro de version croissant, migrations de base sans perte de données).

## Signature

Pour l'instant, l'APK est signé avec la clé de développement `keystore/echeo-dev.jks`.
Avant la vente, ajouter dans *Settings → Secrets → Actions* :
`ECHEO_KEYSTORE_B64` (clé privée en base64) et `ECHEO_KEYSTORE_PASSWORD`.

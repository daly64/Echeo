# Echeo

Application Android de gestion d'abonnements (IPTV, Netflix, Alpha…) pour un gérant de boutique.

- Données 100 % locales (Room), un seul utilisateur.
- Compilation automatique sur GitHub à chaque envoi sur `main` : l'APK et les captures d'écran sont dans les artefacts de l'onglet **Actions**.
- Chaque version s'installe par-dessus la précédente (même clé de signature, numéro de version croissant, migrations de base sans perte de données).

## Signature

Pour l'instant, l'APK est signé avec la clé de développement `keystore/echeo-dev.jks`.
Avant la vente, ajouter dans *Settings → Secrets → Actions* :
`ECHEO_KEYSTORE_B64` (clé privée en base64) et `ECHEO_KEYSTORE_PASSWORD`.

## Licence

Echeo ne fonctionne pas sans clé d'activation (pas d'essai). La clé est au style numéro de série
(20 caractères, ex. `K7QM2-X9TRA-4WNBH-6ZPE3`), liée au code du téléphone (8 caractères) et renouvelable :
à l'expiration, l'app se bloque jusqu'à la saisie d'une nouvelle clé (les données sont conservées).

Les clés sont émises par l'app **Forge Licence**. Elle crée un secret pour Echeo ; il faut le copier une seule fois
dans *Settings → Secrets → Actions* sous le nom `ECHEO_LICENCE_SECRET`, puis relancer la compilation.
Sans ce secret, la version compilée refuse toutes les clés.

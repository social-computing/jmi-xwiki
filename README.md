# Intégration dans Eclipse

## JAXB

Une partie des sources est générée automatiquement par JAXB.
L'execution d'une phase maven lance un plugin qui produit le code source dans le répertoire
target/generated-sources à partir de la définition des classes située dans
src/main/resources/socialcomputing.xwiki.model.xsd
Le répertoire target/generated-sources est automatiquement inclus dans la phase
de compilation maven, mais ce n'est pas le cas pour eclipse. 

### Ajout du répertoire target/generated-sources dans eclipse

1. Aller dans le menu "projet" puis "properties"
2. Choisir dans le menu de gauche "Java Build Path"
3. Cliquer sur le bouton "Ajouter un repertoire..."
4. Dans l'arborescence sélectionner le répertoire "target/generated-sources/xjc"
5. Valider et cliquer sur "ok" pour fermer la fenêtre de propriétés

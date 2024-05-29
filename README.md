# Réseau de capteurs en BCM4Java

Ce projet est un prototype de système de supervision réparti, basé sur un réseau de capteurs, développé dans le cadre du cours Composants (CPS) 2024 à Sorbonne Université.


## Introduction

L'objectif de ce projet est de développer un système de supervision réparti utilisant des composants BCM4Java pour simuler un réseau de capteurs environnementaux. Ce système permet de collecter des données, de détecter des alertes (comme un départ de feu de forêt) et de propager des requêtes de manière décentralisée et asynchrone.

## Fonctionnalités

- **Réseau de capteurs décentralisé** : Utilisation de composants BCM4Java pour représenter les nœuds du réseau.
- **Collecte de données** : Exécution de requêtes pour recueillir les valeurs des capteurs.
- **Détection d'alertes** : Évaluation d'expressions booléennes sur les valeurs des capteurs pour détecter des conditions spécifiques.
- **Propagation des requêtes** : Transmission des requêtes de manière asynchrone entre les nœuds du réseau.
- **Gestion du parallélisme** : Optimisation de la performance par la parallélisation des composants du système.

## Installation

Pour cloner et installer ce projet, assurez-vous d'avoir Java et également avoir BCM4Java ( trouvable sur internet ).

```bash
# Cloner le dépôt
git clone https://github.com/Freecs11/CPS.git

# Accéder au répertoire du projet
cd CPS

```

Projet effectué en groupe, voici les membres du groupe:

    - Rachid BOUHMAD
    - Do Truong Thinh TRUONG
    - Elhadj Alseiny DIALLO

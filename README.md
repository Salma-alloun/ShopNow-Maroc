# 🛍️ ShopNow.ma - Plateforme E-Commerce

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring-5.3-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.0-yellow)
![Stripe](https://img.shields.io/badge/Stripe-API-purple)
![JUnit](https://img.shields.io/badge/JUnit-4.13-red)
![Mockito](https://img.shields.io/badge/Mockito-5.5-brightgreen)
### 🧪 Tests

![JUnit](https://img.shields.io/badge/JUnit-4.13-red)
![Mockito](https://img.shields.io/badge/Mockito-5.5-brightgreen)
![Spring Test](https://img.shields.io/badge/Spring%20Test-5.3-success)
![AssertJ](https://img.shields.io/badge/AssertJ-3.24-orange)

## 📋 Description

**ShopNow.ma** est une application e-commerce complète développée avec Spring MVC. Elle permet aux utilisateurs de parcourir un catalogue de produits, gérer leur panier, effectuer des paiements sécurisés via Stripe, et suivre leurs commandes. Un dashboard administrateur permet la gestion complète des produits, catégories et utilisateurs.

---

## ✨ Fonctionnalités

### 👤 Côté Client
| Fonctionnalité | Description |
|----------------|-------------|
| 🔐 **Authentification** | Inscription / Connexion sécurisée |
| 🛍️ **Catalogue** | Consultation des produits par catégorie |
| 🔍 **Recherche** | Recherche de produits par nom |
| 🛒 **Panier** | Ajout/suppression/modification des quantités |
| 💳 **Paiement** | Intégration Stripe (cartes bancaires) |
| 📦 **Commandes** | Historique et suivi des commandes |
| 👤 **Profil** | Modification des informations personnelles |
| 📱 **Responsive** | Interface adaptée mobile/tablette/desktop |

### 👨‍💼 Côté Administrateur
| Fonctionnalité | Description |
|----------------|-------------|
| 📊 **Dashboard** | Vue d'ensemble des statistiques |
| 🏷️ **Catégories** | CRUD des catégories |
| 📦 **Produits** | CRUD des produits (nom, prix, stock, image) |
| 👥 **Utilisateurs** | Gestion des comptes clients |
| 📋 **Commandes** | Suivi et mise à jour des statuts |

---

## 🛠️ Technologies utilisées

### Backend
| Technologie | Version | Rôle |
|-------------|---------|------|
| Java | 17 | Langage principal |
| Spring MVC | 5.3 | Framework web |
| Spring Data JPA | 2.7 | ORM et accès aux données |
| Hibernate | 5.6 | Mapping objet-relationnel |
| MySQL | 8.0 | Base de données |
| Maven | 3.8 | Gestion des dépendances |

### Frontend
| Technologie | Rôle |
|-------------|------|
| Thymeleaf | Moteur de templates |
| Bootstrap 5 | Framework CSS responsive |
| Font Awesome 6 | Icônes vectorielles |
| JavaScript | Interactions dynamiques |
| AOS.js | Animations au scroll |

### Paiement & Sécurité
| Technologie | Rôle |
|-------------|------|
| Stripe API | Paiement par carte bancaire |
| BCrypt | Hashage des mots de passe |
| Session Management | Gestion des sessions utilisateurs |
## 🧪 Tests

Le projet inclut des **tests unitaires** et des **tests d'intégration** complets.
---


---

## 🚀 Installation

### Prérequis

- JDK 17 ou supérieur
- MySQL 8.0
- Maven 3.8+
- Compte Stripe (pour les paiements)

### 1. Cloner le dépôt

```bash
git clone https://github.com/Salma-alloun/ShopNow-Maroc.git
cd ShopNow-Maroc

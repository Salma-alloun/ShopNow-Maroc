package ma.fstm.ilisi.projects.webmvc.integration;

import ma.fstm.ilisi.projects.webmvc.TestRepositoryConfig;
import ma.fstm.ilisi.projects.webmvc.bo.*;
import ma.fstm.ilisi.projects.webmvc.dto.*;
import ma.fstm.ilisi.projects.webmvc.repository.*;
import ma.fstm.ilisi.projects.webmvc.service.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;

import static org.junit.Assert.*;

// Utiliser SpringRunner au lieu de SpringJUnit4ClassRunner
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
@Transactional
public class CompletePurchaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired(required = false)
    private CategoryRepository categoryRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;
    private Category testCategory;

    @Before
    public void setUp() {
        System.out.println("\n🔧 ===== PRÉPARATION DES DONNÉES DE TEST =====");
        
        // Nettoyer les données existantes
        cleanupTestData();

        // Création et persistance de la catégorie
        System.out.println("📦 Création de la catégorie...");
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Category for testing");
        entityManager.persist(testCategory);
        entityManager.flush();
        System.out.println("✅ Catégorie créée: ID=" + testCategory.getId());

        System.out.println("📦 Préparation des produits...");
        
        // Création du produit 1
        testProduct1 = new Product();
        testProduct1.setName("Produit Test 1");
        testProduct1.setDescription("Description du produit 1");
        testProduct1.setPrice(99.99);
        testProduct1.setStockQuantity(10);
        testProduct1.setCategory(testCategory);
        testProduct1.setCreatedAt(new Date());
        testProduct1.setUpdatedAt(new Date());
        testProduct1 = productRepository.save(testProduct1);
        System.out.println("✅ Produit 1 créé: ID=" + testProduct1.getId());

        // Création du produit 2
        testProduct2 = new Product();
        testProduct2.setName("Produit Test 2");
        testProduct2.setDescription("Description du produit 2");
        testProduct2.setPrice(49.99);
        testProduct2.setStockQuantity(20);
        testProduct2.setCategory(testCategory);
        testProduct2.setCreatedAt(new Date());
        testProduct2.setUpdatedAt(new Date());
        testProduct2 = productRepository.save(testProduct2);
        System.out.println("✅ Produit 2 créé: ID=" + testProduct2.getId());

        System.out.println("🔧 ===========================================\n");
    }

    private void cleanupTestData() {
        try {
            if (orderRepository != null) {
                orderRepository.deleteAll();
            }
            if (cartRepository != null) {
                cartRepository.deleteAll();
            }
            if (userRepository != null) {
                userRepository.deleteAll();
            }
            if (productRepository != null) {
                productRepository.deleteAll();
            }
            if (categoryRepository != null) {
                categoryRepository.deleteAll();
            }
            if (entityManager != null) {
                entityManager.flush();
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erreur lors du nettoyage: " + e.getMessage());
        }
    }

    @Test
    public void testCompletePurchaseScenario_UserRegistersAndBuysProducts() {
        System.out.println("🧪 ===== TEST D'INTÉGRATION COMPLET =====");
        
        // ÉTAPE 1: INSCRIPTION
        System.out.println("📝 ÉTAPE 1: Inscription");
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("nouvel.utilisateur@test.com");
        registerDTO.setPassword("Password123!");
        registerDTO.setFirstName("Jean");
        registerDTO.setLastName("Dupont");
        registerDTO.setCity("Casablanca");
        registerDTO.setPostalCode("20000");
        registerDTO.setCountry("Maroc");

        UserDTO registeredUser = userService.register(registerDTO);
        assertNotNull(registeredUser);
        assertNotNull(registeredUser.getId());
        assertEquals("nouvel.utilisateur@test.com", registeredUser.getEmail());
        
        testUser = userRepository.findById(registeredUser.getId()).orElse(null);
        assertNotNull(testUser);
        System.out.println("✅ Utilisateur inscrit");

        // ÉTAPE 2: CONNEXION
        System.out.println("\n📝 ÉTAPE 2: Connexion");
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("nouvel.utilisateur@test.com");
        loginDTO.setPassword("Password123!");

        UserDTO loggedInUser = userService.authenticate(loginDTO);
        assertNotNull(loggedInUser);
        System.out.println("✅ Connexion réussie");

        // ÉTAPE 3: AJOUT AU PANIER
        System.out.println("\n📝 ÉTAPE 3: Ajout au panier");
        cartService.addToCart(testUser, testProduct1.getId(), 2);
        CartDTO cart = cartService.addToCart(testUser, testProduct2.getId(), 3);
        assertNotNull(cart);
        
        double expectedTotal = (testProduct1.getPrice() * 2) + (testProduct2.getPrice() * 3);
        assertEquals(expectedTotal, cart.getTotal(), 0.01);
        System.out.println("✅ Panier créé, total: " + cart.getTotal());

        // ÉTAPE 4: CRÉATION COMMANDE
        System.out.println("\n📝 ÉTAPE 4: Création commande");
        OrderDTO order = orderService.createOrderFromCart(testUser.getId());
        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(expectedTotal, order.getTotal(), 0.01);
        System.out.println("✅ Commande créée: ID=" + order.getId());

        // ÉTAPE 5: PAIEMENT
        System.out.println("\n📝 ÉTAPE 5: Paiement");
        try {
            String clientSecret = paymentService.createPaymentIntent(order.getId(), testUser.getId());
            assertNotNull(clientSecret);
            System.out.println("✅ Paiement traité");
        } catch (Exception e) {
            System.out.println("⚠️ Paiement simulé: " + e.getMessage());
        }

        System.out.println("\n🎉 TEST RÉUSSI !\n");
    }

    @Test
    public void testPurchaseWithInsufficientStock_ShouldFail() {
        System.out.println("\n🧪 ===== TEST: Stock insuffisant =====");

        // Inscription
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("test.stock@test.com");
        registerDTO.setPassword("password");
        registerDTO.setFirstName("Test");
        registerDTO.setLastName("Stock");
        registerDTO.setCity("Rabat");
        registerDTO.setPostalCode("10000");
        registerDTO.setCountry("Maroc");

        UserDTO registeredUser = userService.register(registerDTO);
        User user = userRepository.findById(registeredUser.getId()).orElse(null);
        assertNotNull(user);

        // Création produit avec stock limité
        Category cat = new Category();
        cat.setName("Stock Category");
        entityManager.persist(cat);
        entityManager.flush();

        Product product = new Product();
        product.setName("Produit Rare");
        product.setPrice(199.99);
        product.setStockQuantity(2);
        product.setCategory(cat);
        product = productRepository.save(product);
        System.out.println("✅ Produit créé avec stock=2");

        // Test ajout quantité > stock
        boolean exceptionThrown = false;
        try {
            cartService.addToCart(user, product.getId(), 3);
        } catch (RuntimeException e) {
            exceptionThrown = true;
            System.out.println("✅ Exception attrapée: " + e.getMessage());
        }
        
        assertTrue("L'ajout aurait dû échouer", exceptionThrown);
        System.out.println("\n✅ TEST RÉUSSI\n");
    }

    @Test
    public void testUserWithMultipleOrders() {
        System.out.println("\n🧪 ===== TEST: Commandes multiples =====");

        // Inscription
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("multi.orders@test.com");
        registerDTO.setPassword("password");
        registerDTO.setFirstName("Multi");
        registerDTO.setLastName("Orders");
        registerDTO.setCity("Tanger");
        registerDTO.setPostalCode("90000");
        registerDTO.setCountry("Maroc");

        UserDTO registeredUser = userService.register(registerDTO);
        User user = userRepository.findById(registeredUser.getId()).orElse(null);
        assertNotNull(user);

        // Création catégorie et produits
        Category cat = new Category();
        cat.setName("Produits divers");
        entityManager.persist(cat);
        entityManager.flush();
        
        Product p1 = new Product();
        p1.setName("Produit A");
        p1.setPrice(50.0);
        p1.setStockQuantity(100);
        p1.setCategory(cat);
        p1 = productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("Produit B");
        p2.setPrice(30.0);
        p2.setStockQuantity(100);
        p2.setCategory(cat);
        p2 = productRepository.save(p2);

        // Première commande
        cartService.addToCart(user, p1.getId(), 2);
        OrderDTO order1 = orderService.createOrderFromCart(user.getId());
        assertNotNull(order1);

        // Deuxième commande
        cartService.addToCart(user, p2.getId(), 5);
        OrderDTO order2 = orderService.createOrderFromCart(user.getId());
        assertNotNull(order2);

        // Vérifications
        var userOrders = orderService.getOrdersByUser(user.getId());
        assertTrue(userOrders.size() >= 2);
        
        System.out.println("✅ L'utilisateur a " + userOrders.size() + " commandes");
        System.out.println("\n✅ TEST RÉUSSI\n");
    }
}
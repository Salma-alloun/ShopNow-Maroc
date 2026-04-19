package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.bo.Cart;
import ma.fstm.ilisi.projects.webmvc.bo.CartItem;
import ma.fstm.ilisi.projects.webmvc.bo.Product;
import ma.fstm.ilisi.projects.webmvc.bo.User;
import ma.fstm.ilisi.projects.webmvc.dto.CartDTO;
import ma.fstm.ilisi.projects.webmvc.dto.CartItemDTO;
import ma.fstm.ilisi.projects.webmvc.repository.CartItemRepository;
import ma.fstm.ilisi.projects.webmvc.repository.CartRepository;
import ma.fstm.ilisi.projects.webmvc.repository.ProductRepository;
import ma.fstm.ilisi.projects.webmvc.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
@RunWith(MockitoJUnitRunner.Silent.class)
public class CartServiceTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CartServiceImpl cartService;
    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;
    @Before
    public void setUp() {
        System.out.println("\n🔧 ===== PRÉPARATION DES DONNÉES DE TEST =====");
        
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        System.out.println("👤 Utilisateur créé: ID=1, Email=test@test.com");
        testProduct = new Product();
        testProduct.setId(100);
        testProduct.setName("Test Product");
        testProduct.setPrice(99.99);
        testProduct.setStockQuantity(10);
        System.out.println("📦 Produit créé: ID=100, Nom=Test Product, Prix=99.99 DH, Stock=10");
        testCart = new Cart();
        testCart.setId(1);
        testCart.setUser(testUser);
        testCart.setStatus("ACTIVE");
        testCart.setTotal(0.0);
        testCart.setCartItems(new ArrayList<>());
        testCart.setCreatedAt(new Date());
        testCart.setUpdatedAt(new Date());
        System.out.println("🛒 Panier créé: ID=1, Statut=ACTIVE, Total=0.0 DH");
        testCartItem = new CartItem();
        testCartItem.setId(1);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setSubtotal(199.98);
        System.out.println("📝 Item de test créé: Quantité=2, Sous-total=199.98 DH");
        System.out.println("🔧 ===========================================\n");
    }
    // ==================== TEST getOrCreateCart ====================
    @Test
    public void testGetOrCreateCart_WhenActiveCartExists_ShouldReturnExistingCart() {
        System.out.println("🧪 ===== TEST: getOrCreateCart - Panier existant =====");
        System.out.println("🎯 Objectif: Vérifier que la méthode retourne le panier existant quand l'utilisateur a déjà un panier actif");
        
        // Arrange
        System.out.println("📋 Préparation: Simulation d'un panier existant en base de données");
        when(cartRepository.findByUserAndStatus(testUser, "ACTIVE"))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        // Act
        System.out.println("⚡ Exécution: Appel de getOrCreateCart(testUser)");
        Cart result = cartService.getOrCreateCart(testUser);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - ID du panier: " + result.getId() + " (attendu: 1)");
        System.out.println("   - Statut: " + result.getStatus() + " (attendu: ACTIVE)");
        System.out.println("   - Utilisateur: " + result.getUser().getEmail() + " (attendu: test@test.com)");
        
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(testUser, result.getUser());
        
        verify(cartRepository, times(1)).findByUserAndStatus(testUser, "ACTIVE");
        verify(cartRepository, times(1)).save(any(Cart.class));
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testGetOrCreateCart_WhenNoActiveCart_ShouldCreateNewCart() {
        System.out.println("🧪 ===== TEST: getOrCreateCart - Création nouveau panier =====");
        System.out.println("🎯 Objectif: Vérifier que la méthode crée un nouveau panier quand l'utilisateur n'en a pas");
        
        // Arrange
        System.out.println("📋 Préparation: Simulation d'aucun panier existant en base");
        when(cartRepository.findByUserAndStatus(testUser, "ACTIVE"))
                .thenReturn(Optional.empty());
        
        Cart newCart = new Cart();
        newCart.setId(2);
        newCart.setUser(testUser);
        newCart.setStatus("ACTIVE");
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
        System.out.println("📋 Simulation: Sauvegarde d'un nouveau panier avec ID=2");
        // Act
        System.out.println("⚡ Exécution: Appel de getOrCreateCart(testUser)");
        Cart result = cartService.getOrCreateCart(testUser);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - ID du nouveau panier: " + result.getId() + " (attendu: 2)");
        System.out.println("   - Statut: " + result.getStatus() + " (attendu: ACTIVE)");
        System.out.println("   - Panier vide: " + result.getCartItems().isEmpty());
        
        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(testUser, result.getUser());
        assertTrue(result.getCartItems().isEmpty());
        
        verify(cartRepository).findByUserAndStatus(testUser, "ACTIVE");
        verify(cartRepository).save(any(Cart.class));
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST getActiveCartWithItems ====================
    @Test
    public void testGetActiveCartWithItems_WhenCartExists_ShouldReturnCart() {
        System.out.println("🧪 ===== TEST: getActiveCartWithItems - Panier avec items =====");
        System.out.println("🎯 Objectif: Vérifier que la méthode retourne le panier avec ses items");
        
        // Arrange
        testCart.getCartItems().add(testCartItem);
        System.out.println("📋 Préparation: Ajout d'un item au panier (quantité=2)");
        when(cartRepository.findActiveCartWithItems(testUser.getId()))
                .thenReturn(Optional.of(testCart));
        // Act
        System.out.println("⚡ Exécution: Appel de getActiveCartWithItems(testUser)");
        Cart result = cartService.getActiveCartWithItems(testUser);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - Nombre d'items: " + result.getCartItems().size() + " (attendu: 1)");
        System.out.println("   - Quantité du premier item: " + result.getCartItems().get(0).getQuantity() + " (attendu: 2)");
        
        assertNotNull(result);
        assertEquals(1, result.getCartItems().size());
        assertEquals(2, result.getCartItems().get(0).getQuantity());
        
        verify(cartRepository).findActiveCartWithItems(testUser.getId());
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST addToCart ====================
    @Test
    public void testAddToCart_WithNewProduct_ShouldAddItem() {
        System.out.println("🧪 ===== TEST: addToCart - Ajout nouveau produit =====");
        System.out.println("🎯 Objectif: Vérifier l'ajout d'un nouveau produit au panier");
        
        // Arrange
        System.out.println("📋 Préparation: Simulation du produit (ID=100, prix=99.99, stock=10)");
        when(productRepository.findById(100)).thenReturn(Optional.of(testProduct));
        System.out.println("📋 Préparation: Simulation du panier vide (ID=1)");
        when(cartRepository.findActiveCartWithItems(testUser.getId()))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        // Act
        System.out.println("⚡ Exécution: addToCart(testUser, 100, 3) - Ajout de 3 articles");
        CartDTO result = cartService.addToCart(testUser, 100, 3);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - Nombre d'items après ajout: " + result.getCartItems().size());
        System.out.println("   - Le produit 100 devrait être ajouté avec quantité=3");
        
        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testAddToCart_WithExistingProduct_ShouldUpdateQuantity() {
        System.out.println("🧪 ===== TEST: addToCart - Mise à jour quantité produit existant =====");
        System.out.println("🎯 Objectif: Vérifier la mise à jour de quantité quand le produit est déjà dans le panier");
        
        // Arrange
        testCart.getCartItems().add(testCartItem);
        System.out.println("📋 Préparation: Panier contient déjà le produit (ID=100) avec quantité=2");
        when(productRepository.findById(100)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findActiveCartWithItems(testUser.getId()))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        // Act
        System.out.println("⚡ Exécution: addToCart(testUser, 100, 1) - Ajout de 1 article supplémentaire");
        CartDTO result = cartService.addToCart(testUser, 100, 1);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - La quantité devrait passer de 2 à 3");
        System.out.println("   - Le sous-total devrait passer de 199.98 à 299.97");
        
        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(testCartItem);
        verify(cartRepository, times(1)).save(testCart);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testAddToCart_WithInvalidProduct_ShouldThrowException() {
        System.out.println("🧪 ===== TEST: addToCart - Produit inexistant =====");
        System.out.println("🎯 Objectif: Vérifier que la méthode lance une exception quand le produit n'existe pas");
        
        // Arrange
        System.out.println("📋 Préparation: Simulation d'un produit inexistant (ID=999)");
        when(productRepository.findById(999)).thenReturn(Optional.empty());
        // Act & Assert
        System.out.println("⚡ Exécution: addToCart(testUser, 999, 1) - Tentative d'ajout d'un produit inexistant");
        try {
            cartService.addToCart(testUser, 999, 1);
            fail("Should throw RuntimeException");
            System.out.println("❌ ERREUR: L'exception aurait dû être lancée!");
        } catch (RuntimeException e) {
            System.out.println("✅ Exception attrapée: " + e.getMessage());
            assertEquals("Produit non trouvé", e.getMessage());
            System.out.println("✅ TEST RÉUSSI (l'exception était attendue)!\n");
        }
    }
    @Test
    public void testAddToCart_WithInsufficientStock_ShouldThrowException() {
        System.out.println("🧪 ===== TEST: addToCart - Stock insuffisant =====");
        System.out.println("🎯 Objectif: Vérifier que la méthode lance une exception quand le stock est insuffisant");
        
        // Arrange
        testProduct.setStockQuantity(2);
        System.out.println("📋 Préparation: Stock du produit limité à 2");
        when(productRepository.findById(100)).thenReturn(Optional.of(testProduct));
        // Act & Assert
        System.out.println("⚡ Exécution: addToCart(testUser, 100, 5) - Tentative d'ajout de 5 articles (stock=2)");
        try {
            cartService.addToCart(testUser, 100, 5);
            fail("Should throw RuntimeException");
            System.out.println("❌ ERREUR: L'exception aurait dû être lancée!");
        } catch (RuntimeException e) {
            System.out.println("✅ Exception attrapée: " + e.getMessage());
            assertEquals("Stock insuffisant", e.getMessage());
            System.out.println("✅ TEST RÉUSSI (l'exception était attendue)!\n");
        }
    }
    // ==================== TEST updateQuantity ====================
    @Test
    public void testUpdateQuantity_WithValidQuantity_ShouldUpdate() {
        System.out.println("🧪 ===== TEST: updateQuantity - Mise à jour quantité =====");
        System.out.println("🎯 Objectif: Vérifier la mise à jour de la quantité d'un produit");
        
        // Arrange
        testCart.getCartItems().add(testCartItem);
        System.out.println("📋 Préparation: Panier avec produit (ID=100) quantité=2");
        when(cartRepository.findActiveCartWithItems(testUser.getId()))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        // Act
        System.out.println("⚡ Exécution: updateQuantity(testUser, 100, 5) - Mise à jour à quantité=5");
        CartDTO result = cartService.updateQuantity(testUser, 100, 5);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Nouvelle quantité: " + testCartItem.getQuantity() + " (attendu: 5)");
        System.out.println("   - Nouveau sous-total: " + testCartItem.getSubtotal() + " (attendu: 499.95)");
        
        assertNotNull(result);
        assertEquals(5, testCartItem.getQuantity());
        assertEquals(499.95, testCartItem.getSubtotal(), 0.01);
        
        verify(cartItemRepository, times(1)).save(testCartItem);
        verify(cartRepository, times(1)).save(testCart);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testUpdateQuantity_WithZeroQuantity_ShouldRemoveItem() {
        System.out.println("🧪 ===== TEST: updateQuantity - Suppression d'un article =====");
        System.out.println("🎯 Objectif: Vérifier que l'article est supprimé quand la quantité est mise à 0");
        
        // Arrange
        testCart.getCartItems().add(testCartItem);
        System.out.println("📋 Préparation: Panier avec produit (ID=100) quantité=2");
        when(cartRepository.findActiveCartWithItems(testUser.getId()))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        // Act
        System.out.println("⚡ Exécution: updateQuantity(testUser, 100, 0) - Mise à quantité=0");
        CartDTO result = cartService.updateQuantity(testUser, 100, 0);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Panier vide après suppression: " + testCart.getCartItems().isEmpty());
        
        assertNotNull(result);
        assertTrue(testCart.getCartItems().isEmpty());
        
        verify(cartItemRepository, times(1)).delete(testCartItem);
        verify(cartRepository, times(1)).save(testCart);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST removeFromCart ====================
    @Test
    public void testRemoveFromCart_WithValidProduct_ShouldRemoveItem() {
        System.out.println("🧪 ===== TEST: removeFromCart - Retrait d'un article =====");
        System.out.println("🎯 Objectif: Vérifier le retrait d'un article du panier");
        
        // Arrange
        testCart.getCartItems().add(testCartItem);
        System.out.println("📋 Préparation: Panier avec produit (ID=100)");
        when(cartRepository.findActiveCartWithItems(testUser.getId()))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        // Act
        System.out.println("⚡ Exécution: removeFromCart(testUser, 100) - Retrait du produit 100");
        CartDTO result = cartService.removeFromCart(testUser, 100);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Panier vide après retrait: " + testCart.getCartItems().isEmpty());
        
        assertNotNull(result);
        assertTrue(testCart.getCartItems().isEmpty());
        
        verify(cartItemRepository, times(1)).delete(testCartItem);
        verify(cartRepository, times(1)).save(testCart);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST clearCart ====================
    @Test
    public void testClearCart_ShouldRemoveAllItems() {
        System.out.println("🧪 ===== TEST: clearCart - Vider le panier =====");
        System.out.println("🎯 Objectif: Vérifier que le panier est complètement vidé");
        
        // Arrange
        testCart.getCartItems().add(testCartItem);
        System.out.println("📋 Préparation: Panier avec 1 article");
        doNothing().when(cartItemRepository).deleteByCartId(testCart.getId());
        when(cartRepository.findActiveCartWithItems(testUser.getId()))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        // Act
        System.out.println("⚡ Exécution: clearCart(testUser) - Vidage du panier");
        cartService.clearCart(testUser);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Panier vide: " + testCart.getCartItems().isEmpty());
        System.out.println("   - Total remis à zéro: " + testCart.getTotal());
        
        assertTrue(testCart.getCartItems().isEmpty());
        assertEquals(0.0, testCart.getTotal(), 0.01);
        
        verify(cartItemRepository).deleteByCartId(testCart.getId());
        verify(cartRepository).save(testCart);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST getCartItemCount ====================
    @Test
    public void testGetCartItemCount_WithItems_ShouldReturnCount() {
        System.out.println("🧪 ===== TEST: getCartItemCount - Compter les articles =====");
        System.out.println("🎯 Objectif: Vérifier que le nombre d'articles est correct");
        
        // Arrange
        testCart.getCartItems().add(testCartItem);
        System.out.println("📋 Préparation: Panier avec 1 article");
        when(cartRepository.findActiveCartWithItems(testUser.getId()))
                .thenReturn(Optional.of(testCart));
        // Act
        System.out.println("⚡ Exécution: getCartItemCount(testUser) - Récupération du nombre d'articles");
        int result = cartService.getCartItemCount(testUser);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Nombre d'articles: " + result + " (attendu: 1)");
        
        assertEquals(1, result);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST getCartTotalQuantityForUser ====================
    @Test
    public void testGetCartTotalQuantityForUser_WithMultipleItems_ShouldReturnSum() {
        System.out.println("🧪 ===== TEST: getCartTotalQuantityForUser - Somme des quantités =====");
        System.out.println("🎯 Objectif: Vérifier que la somme des quantités de tous les articles est correcte");
        
        // Arrange
        testCart.getCartItems().add(testCartItem);
        
        Product product2 = new Product();
        product2.setId(101);
        CartItem item2 = new CartItem();
        item2.setQuantity(3);
        item2.setProduct(product2);
        testCart.getCartItems().add(item2);
        
        System.out.println("📋 Préparation: Panier avec 2 articles - quantités: 2 et 3");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(cartRepository.findActiveCartWithItems(1)).thenReturn(Optional.of(testCart));
        // Act
        System.out.println("⚡ Exécution: getCartTotalQuantityForUser(1) - Calcul de la quantité totale");
        int result = cartService.getCartTotalQuantityForUser(1);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Quantité totale: " + result + " (attendu: 5)");
        
        assertEquals(5, result);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST validateCart ====================
    @Test
    public void testValidateCart_WithSufficientStock_ShouldReturnTrue() {
        System.out.println("🧪 ===== TEST: validateCart - Stock suffisant =====");
        System.out.println("🎯 Objectif: Vérifier que le panier est valide quand le stock est suffisant");
        
        // Arrange
        testCart.getCartItems().add(testCartItem);
        testProduct.setStockQuantity(5);
        System.out.println("📋 Préparation: Article demande 2 unités, stock disponible=5");
        // Act
        System.out.println("⚡ Exécution: validateCart(testCart) - Validation du panier");
        boolean result = cartService.validateCart(testCart);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Panier valide: " + result + " (attendu: true)");
        
        assertTrue(result);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testValidateCart_WithInsufficientStock_ShouldReturnFalse() {
        System.out.println("🧪 ===== TEST: validateCart - Stock insuffisant =====");
        System.out.println("🎯 Objectif: Vérifier que le panier est invalide quand le stock est insuffisant");
        
        // Arrange
        testCart.getCartItems().add(testCartItem);
        testProduct.setStockQuantity(1);
        System.out.println("📋 Préparation: Article demande 2 unités, stock disponible=1");
        // Act
        System.out.println("⚡ Exécution: validateCart(testCart) - Validation du panier");
        boolean result = cartService.validateCart(testCart);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Panier valide: " + result + " (attendu: false)");
        
        assertFalse(result);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST getCartItemCountForUser ====================
    @Test
    public void testGetCartItemCountForUser_WithValidUser_ShouldReturnCount() {
        System.out.println("🧪 ===== TEST: getCartItemCountForUser - Nombre d'articles par ID =====");
        System.out.println("🎯 Objectif: Vérifier la récupération du nombre d'articles pour un utilisateur spécifique");
        
        // Arrange
        testCart.getCartItems().add(testCartItem);
        System.out.println("📋 Préparation: Utilisateur ID=1 avec 1 article dans son panier");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(cartRepository.findActiveCartWithItems(1)).thenReturn(Optional.of(testCart));
        // Act
        System.out.println("⚡ Exécution: getCartItemCountForUser(1) - Récupération du nombre d'articles");
        int result = cartService.getCartItemCountForUser(1);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Nombre d'articles: " + result + " (attendu: 1)");
        
        assertEquals(1, result);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
}
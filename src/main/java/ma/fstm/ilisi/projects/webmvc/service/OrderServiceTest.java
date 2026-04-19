package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.bo.*;
import ma.fstm.ilisi.projects.webmvc.dto.OrderDTO;
import ma.fstm.ilisi.projects.webmvc.repository.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@RunWith(MockitoJUnitRunner.Silent.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private OrderServiceImpl orderService;
    private User testUser;
    private Cart testCart;
    private CartItem testCartItem;
    private Product testProduct;
    private Order testOrder;
    private OrderItem testOrderItem;
    @Before
    public void setUp() {
        System.out.println("\n🔧 ===== PRÉPARATION DES DONNÉES DE TEST =====");
        // Création d'un utilisateur
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        System.out.println("👤 Utilisateur créé: ID=1, Email=test@test.com");
        // Création d'un produit
        testProduct = new Product();
        testProduct.setId(100);
        testProduct.setName("Test Product");
        testProduct.setPrice(99.99);
        testProduct.setStockQuantity(10);
        System.out.println("📦 Produit créé: ID=100, Stock=10, Prix=99.99 DH");
        // Création d'un panier
        testCart = new Cart();
        testCart.setId(1);
        testCart.setUser(testUser);
        testCart.setStatus("ACTIVE");
        testCart.setCartItems(new ArrayList<>());
        testCart.setTotal(199.98);
        System.out.println("🛒 Panier créé: ID=1, Total=199.98 DH");
        // Création d'un item de panier
        testCartItem = new CartItem();
        testCartItem.setId(1);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setSubtotal(199.98);
        testCart.getCartItems().add(testCartItem);
        System.out.println("📝 Item de panier créé: Quantité=2, Sous-total=199.98 DH");
        // Création d'une commande
        testOrder = new Order();
        testOrder.setId(1000);
        testOrder.setUser(testUser);
        testOrder.setStatus("PENDING");
        testOrder.setTotal(199.98);
        testOrder.setOrderDate(new Date());
        testOrder.setOrderItems(new ArrayList<>());
        System.out.println("📋 Commande créée: ID=1000, Total=199.98 DH, Statut=PENDING");
        // Création d'un item de commande
        testOrderItem = new OrderItem();
        testOrderItem.setId(1);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setSubtotal(199.98);
        testOrder.getOrderItems().add(testOrderItem);
        System.out.println("📝 Item de commande créé: Quantité=2, Sous-total=199.98 DH");
        System.out.println("🔧 ===========================================\n");
    }
    // ==================== TEST createOrderFromCart ====================
    @Test
    public void testCreateOrderFromCart_WithValidCart_ShouldCreateOrder() {
        System.out.println("🧪 ===== TEST: createOrderFromCart - Création de commande réussie =====");
        System.out.println("🎯 Objectif: Vérifier qu'une commande est créée à partir d'un panier valide");
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(cartRepository.findActiveCartWithItems(1)).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1000);
            return savedOrder;
        });
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        doNothing().when(cartItemRepository).deleteByCartId(1);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        // Act
        System.out.println("⚡ Exécution: createOrderFromCart(1)");
        OrderDTO result = orderService.createOrderFromCart(1);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - ID commande: " + result.getId() + " (attendu: 1000)");
        System.out.println("   - Statut: " + result.getStatus() + " (attendu: PENDING)");
        System.out.println("   - Nombre d'items: " + result.getOrderItems().size() + " (attendu: 1)");
        assertNotNull(result);
        assertEquals(1000, result.getId());
        assertEquals("PENDING", result.getStatus());
        assertEquals(1, result.getOrderItems().size());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(productRepository, times(1)).save(testProduct);
        verify(cartItemRepository, times(1)).deleteByCartId(1);
        verify(cartRepository, times(1)).save(testCart);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testCreateOrderFromCart_WithEmptyCart_ShouldThrowException() {
        System.out.println("🧪 ===== TEST: createOrderFromCart - Panier vide =====");
        System.out.println("🎯 Objectif: Vérifier que la création échoue si le panier est vide");
        // Arrange
        testCart.getCartItems().clear();
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(cartRepository.findActiveCartWithItems(1)).thenReturn(Optional.of(testCart));
        // Act & Assert
        System.out.println("⚡ Exécution: createOrderFromCart(1) avec panier vide");
        try {
            orderService.createOrderFromCart(1);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            System.out.println("✅ Exception attrapée: " + e.getMessage());
            assertEquals("Le panier est vide", e.getMessage());
        }
        verify(orderRepository, never()).save(any(Order.class));
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testCreateOrderFromCart_WithNoCart_ShouldThrowException() {
        System.out.println("🧪 ===== TEST: createOrderFromCart - Panier inexistant =====");
        System.out.println("🎯 Objectif: Vérifier que la création échoue si le panier n'existe pas");
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(cartRepository.findActiveCartWithItems(1)).thenReturn(Optional.empty());
        // Act & Assert
        System.out.println("⚡ Exécution: createOrderFromCart(1) sans panier");
        try {
            orderService.createOrderFromCart(1);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            System.out.println("✅ Exception attrapée: " + e.getMessage());
            assertEquals("Panier vide ou introuvable", e.getMessage());
        }
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST getOrderById ====================
    @Test
    public void testGetOrderById_WithValidId_ShouldReturnOrder() {
        System.out.println("🧪 ===== TEST: getOrderById - Récupération de commande =====");
        System.out.println("🎯 Objectif: Vérifier la récupération d'une commande par son ID");
        // Arrange
        when(orderRepository.findById(1000)).thenReturn(Optional.of(testOrder));
        // Act
        System.out.println("⚡ Exécution: getOrderById(1000)");
        OrderDTO result = orderService.getOrderById(1000);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - ID: " + result.getId() + " (attendu: 1000)");
        System.out.println("   - Statut: " + result.getStatus() + " (attendu: PENDING)");
        assertNotNull(result);
        assertEquals(1000, result.getId());
        assertEquals("PENDING", result.getStatus());
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testGetOrderById_WithInvalidId_ShouldThrowException() {
        System.out.println("🧪 ===== TEST: getOrderById - ID inexistant =====");
        System.out.println("🎯 Objectif: Vérifier que la recherche avec un ID invalide lance une exception");
        // Arrange
        when(orderRepository.findById(9999)).thenReturn(Optional.empty());
        // Act & Assert
        System.out.println("⚡ Exécution: getOrderById(9999)");
        try {
            orderService.getOrderById(9999);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            System.out.println("✅ Exception attrapée: " + e.getMessage());
            assertEquals("Commande non trouvée", e.getMessage());
        }
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST getOrdersByUser ====================
    @Test
    public void testGetOrdersByUser_WithValidUser_ShouldReturnOrders() {
        System.out.println("🧪 ===== TEST: getOrdersByUser - Commandes d'un utilisateur =====");
        System.out.println("🎯 Objectif: Vérifier la récupération des commandes d'un utilisateur");
        // Arrange
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);
        when(orderRepository.findByUserIdOrderByOrderDateDesc(1)).thenReturn(orders);
        // Act
        System.out.println("⚡ Exécution: getOrdersByUser(1)");
        List<OrderDTO> results = orderService.getOrdersByUser(1);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Nombre de commandes: " + results.size() + " (attendu: 1)");
        assertNotNull(results);
        assertEquals(1, results.size());
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST updateStatus ====================
    @Test
    public void testUpdateStatus_WithValidId_ShouldUpdateStatus() {
        System.out.println("🧪 ===== TEST: updateStatus - Mise à jour du statut =====");
        System.out.println("🎯 Objectif: Vérifier la mise à jour du statut d'une commande");
        // Arrange
        when(orderRepository.findById(1000)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        // Act
        System.out.println("⚡ Exécution: updateStatus(1000, 'PAID')");
        OrderDTO result = orderService.updateStatus(1000, "PAID");
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Nouveau statut: " + testOrder.getStatus() + " (attendu: PAID)");
        assertEquals("PAID", testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
}
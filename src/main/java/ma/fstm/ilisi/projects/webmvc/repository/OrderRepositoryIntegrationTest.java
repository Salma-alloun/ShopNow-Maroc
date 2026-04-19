package ma.fstm.ilisi.projects.webmvc.repository;
import ma.fstm.ilisi.projects.webmvc.bo.*;
import ma.fstm.ilisi.projects.webmvc.TestRepositoryConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
@Transactional
public class OrderRepositoryIntegrationTest {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    private User testUser1;
    private User testUser2;
    private Product testProduct;
    private Category testCategory;
    private Order order1;
    private Order order2;
    private Order order3;
    @Before
    public void setUp() {
        // Création d'une catégorie
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Category for testing");
        entityManager.persist(testCategory);
        entityManager.flush();
        // Création d'un produit
        testProduct = new Product();
        testProduct.setName("Produit Test");
        testProduct.setPrice(99.99);
        testProduct.setStockQuantity(100);
        testProduct.setCategory(testCategory);
        testProduct.setCreatedAt(new Date());
        testProduct.setUpdatedAt(new Date());
        entityManager.persist(testProduct);
        entityManager.flush();
        // Création d'utilisateurs
        testUser1 = new User();
        testUser1.setEmail("user1@test.com");
        testUser1.setPasswordHash("pass123");
        testUser1.setFirstName("User");
        testUser1.setLastName("One");
        testUser1.setRole("CLIENT");
        testUser1.setActive(true);
        testUser1.setCreatedAt(new Date());
        testUser1.setUpdatedAt(new Date());
        entityManager.persist(testUser1);
        entityManager.flush();
        testUser2 = new User();
        testUser2.setEmail("user2@test.com");
        testUser2.setPasswordHash("pass123");
        testUser2.setFirstName("User");
        testUser2.setLastName("Two");
        testUser2.setRole("CLIENT");
        testUser2.setActive(true);
        testUser2.setCreatedAt(new Date());
        testUser2.setUpdatedAt(new Date());
        entityManager.persist(testUser2);
        entityManager.flush();
        // Création de commandes
        order1 = new Order();
        order1.setUser(testUser1);
        order1.setOrderDate(new Date());
        order1.setStatus("PENDING");
        order1.setTotal(199.98);
        entityManager.persist(order1);
        entityManager.flush();
        order2 = new Order();
        order2.setUser(testUser1);
        order2.setOrderDate(new Date(System.currentTimeMillis() - 10000)); // date plus ancienne
        order2.setStatus("PAID");
        order2.setTotal(299.97);
        entityManager.persist(order2);
        entityManager.flush();
        order3 = new Order();
        order3.setUser(testUser2);
        order3.setOrderDate(new Date());
        order3.setStatus("PENDING");
        order3.setTotal(99.99);
        entityManager.persist(order3);
        entityManager.flush();
    }
    @Test
    public void testFindById_ShouldReturnOrder() {
        // When
        Optional<Order> found = orderRepository.findById(order1.getId());
        // Then
        assertTrue("La commande doit être trouvée", found.isPresent());
        assertEquals("L'ID doit correspondre", order1.getId(), found.get().getId());
        assertEquals("Le statut doit être PENDING", "PENDING", found.get().getStatus());
    }
    @Test
    public void testFindById_WithInvalidId_ShouldReturnEmpty() {
        // When
        Optional<Order> found = orderRepository.findById(9999);
        // Then
        assertFalse("Aucune commande ne doit être trouvée", found.isPresent());
    }
    @Test
    public void testFindByUserIdOrderByOrderDateDesc_ShouldReturnUserOrdersInDescOrder() {
        // When
        List<Order> userOrders = orderRepository.findByUserIdOrderByOrderDateDesc(testUser1.getId());
        // Then
        assertEquals("L'utilisateur 1 doit avoir 2 commandes", 2, userOrders.size());
        assertTrue("Les commandes doivent être triées par date descendante",
                userOrders.get(0).getOrderDate().compareTo(userOrders.get(1).getOrderDate()) >= 0);
    }
    @Test
    public void testFindByUserIdOrderByOrderDateDesc_WithUserWithoutOrders_ShouldReturnEmpty() {
        // Given - Créer un utilisateur sans commande
        User userWithoutOrders = new User();
        userWithoutOrders.setEmail("noorders@test.com");
        userWithoutOrders.setPasswordHash("pass");
        userWithoutOrders.setFirstName("No");
        userWithoutOrders.setLastName("Orders");
        userWithoutOrders.setRole("CLIENT");
        userWithoutOrders.setActive(true);
        userWithoutOrders.setCreatedAt(new Date());
        userWithoutOrders.setUpdatedAt(new Date());
        entityManager.persist(userWithoutOrders);
        entityManager.flush();
        // When
        List<Order> userOrders = orderRepository.findByUserIdOrderByOrderDateDesc(userWithoutOrders.getId());
        // Then
        assertTrue("La liste doit être vide", userOrders.isEmpty());
    }
    @Test
    public void testFindByStatus_ShouldReturnOrdersWithGivenStatus() {
        // When
        List<Order> pendingOrders = orderRepository.findByStatus("PENDING");
        List<Order> paidOrders = orderRepository.findByStatus("PAID");
        // Then
        assertEquals("Il doit y avoir 2 commandes PENDING", 2, pendingOrders.size());
        assertEquals("Il doit y avoir 1 commande PAID", 1, paidOrders.size());
    }
    @Test
    public void testFindByStatus_WithNonExistentStatus_ShouldReturnEmpty() {
        // When
        List<Order> shippedOrders = orderRepository.findByStatus("SHIPPED");
        // Then
        assertTrue("Aucune commande SHIPPED ne doit exister", shippedOrders.isEmpty());
    }
    @Test
    public void testSave_ShouldCreateNewOrder() {
        // Given
        Order newOrder = new Order();
        newOrder.setUser(testUser2);
        newOrder.setOrderDate(new Date());
        newOrder.setStatus("PENDING");
        newOrder.setTotal(149.98);
        // When
        Order savedOrder = orderRepository.save(newOrder);
        entityManager.flush();
        // Then
        assertNotNull("L'ID doit être généré", savedOrder.getId());
        assertEquals("Le statut doit être PENDING", "PENDING", savedOrder.getStatus());
        assertEquals("Le total doit être 149.98", 149.98, savedOrder.getTotal(), 0.01);
    }
    @Test
    public void testUpdate_ShouldModifyOrder() {
        // Given
        order1.setStatus("SHIPPED");
        order1.setTotal(249.98);
        order1.setOrderDate(new Date());
        // When
        Order updatedOrder = orderRepository.save(order1);
        entityManager.flush();
        entityManager.clear();
        // Then
        Order found = orderRepository.findById(order1.getId()).orElse(null);
        assertNotNull("La commande doit exister", found);
        assertEquals("Le statut doit être mis à jour", "SHIPPED", found.getStatus());
        assertEquals("Le total doit être mis à jour", 249.98, found.getTotal(), 0.01);
    }
    @Test
    public void testDelete_ShouldRemoveOrder() {
        // Given
        Integer orderId = order3.getId();
        // When
        orderRepository.delete(order3);
        entityManager.flush();
        // Then
        Optional<Order> found = orderRepository.findById(orderId);
        assertFalse("La commande doit être supprimée", found.isPresent());
    }
}
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
import java.util.Optional;
import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
@Transactional
public class CartRepositoryIntegrationTest {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    private User testUser;
    private Product testProduct1;
    private Product testProduct2;
    private Cart testCart;
    private CartItem testCartItem;
    private Category testCategory;
    @Before
    public void setUp() {
        // Création d'une catégorie
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Category for testing");
        entityManager.persist(testCategory);
        entityManager.flush();
        // Création d'un utilisateur
        testUser = new User();
        testUser.setEmail("cartuser@test.com");
        testUser.setPasswordHash("pass123");
        testUser.setFirstName("Cart");
        testUser.setLastName("User");
        testUser.setRole("CLIENT");
        testUser.setActive(true);
        testUser.setCreatedAt(new Date());
        testUser.setUpdatedAt(new Date());
        entityManager.persist(testUser);
        entityManager.flush();
        // Création de produits
        testProduct1 = new Product();
        testProduct1.setName("Produit Panier 1");
        testProduct1.setPrice(99.99);
        testProduct1.setStockQuantity(10);
        testProduct1.setCategory(testCategory);
        testProduct1.setCreatedAt(new Date());
        testProduct1.setUpdatedAt(new Date());
        entityManager.persist(testProduct1);
        entityManager.flush();
        testProduct2 = new Product();
        testProduct2.setName("Produit Panier 2");
        testProduct2.setPrice(49.99);
        testProduct2.setStockQuantity(20);
        testProduct2.setCategory(testCategory);
        testProduct2.setCreatedAt(new Date());
        testProduct2.setUpdatedAt(new Date());
        entityManager.persist(testProduct2);
        entityManager.flush();
        // Création d'un panier
        testCart = new Cart();
        testCart.setUser(testUser);
        testCart.setStatus("ACTIVE");
        testCart.setCreatedAt(new Date());
        testCart.setUpdatedAt(new Date());
        testCart.setTotal(0.0);
        entityManager.persist(testCart);
        entityManager.flush();
        // Création d'un item de panier
        testCartItem = new CartItem();
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct1);
        testCartItem.setQuantity(2);
        testCartItem.setSubtotal(199.98);
        entityManager.persist(testCartItem);
        entityManager.flush();
        // Ajout de l'item au panier
        testCart.getCartItems().add(testCartItem);
        testCart.setTotal(199.98);
        entityManager.merge(testCart);
        entityManager.flush();
    }
    @Test
    public void testFindByUserAndStatus_ShouldReturnCart() {
        // When
        Optional<Cart> found = cartRepository.findByUserAndStatus(testUser, "ACTIVE");
        // Then
        assertTrue("Le panier doit être trouvé", found.isPresent());
        assertEquals("L'ID du panier doit correspondre", testCart.getId(), found.get().getId());
        assertEquals("Le statut doit être ACTIVE", "ACTIVE", found.get().getStatus());
    }
    @Test
    public void testFindByUserAndStatus_WithWrongStatus_ShouldReturnEmpty() {
        // When
        Optional<Cart> found = cartRepository.findByUserAndStatus(testUser, "ABANDONED");
        // Then
        assertFalse("Aucun panier ne doit être trouvé", found.isPresent());
    }
    @Test
    public void testFindActiveCartWithItems_ShouldReturnCartWithItems() {
        // When
        Optional<Cart> found = cartRepository.findActiveCartWithItems(testUser.getId());
        // Then
        assertTrue("Le panier doit être trouvé", found.isPresent());
        Cart cart = found.get();
        assertNotNull("Les items doivent être chargés", cart.getCartItems());
        assertFalse("Le panier doit avoir des items", cart.getCartItems().isEmpty());
        assertEquals("Un item doit être présent", 1, cart.getCartItems().size());
        assertEquals("La quantité doit être 2", 2, cart.getCartItems().get(0).getQuantity());
    }
    @Test
    public void testFindActiveCartWithItems_WithInvalidUser_ShouldReturnEmpty() {
        // When
        Optional<Cart> found = cartRepository.findActiveCartWithItems(9999);
        // Then
        assertFalse("Aucun panier ne doit être trouvé", found.isPresent());
    }
    @Test
    public void testExistsByUserAndStatus_ShouldReturnTrue_WhenExists() {
        // When
        boolean exists = cartRepository.existsByUserAndStatus(testUser, "ACTIVE");
        // Then
        assertTrue("Le panier doit exister", exists);
    }
    @Test
    public void testExistsByUserAndStatus_WithWrongStatus_ShouldReturnFalse() {
        // When
        boolean exists = cartRepository.existsByUserAndStatus(testUser, "ABANDONED");
        // Then
        assertFalse("Le panier ne doit pas exister", exists);
    }
    @Test
    public void testDeleteAbandonedCarts_ShouldDeleteOldCarts() {
        // Given - Créer un panier abandonné ancien
        Cart abandonedCart = new Cart();
        abandonedCart.setUser(testUser);
        abandonedCart.setStatus("ABANDONED");
        abandonedCart.setCreatedAt(new Date(System.currentTimeMillis() - 15 * 24 * 60 * 60 * 1000L));
        abandonedCart.setUpdatedAt(new Date(System.currentTimeMillis() - 15 * 24 * 60 * 60 * 1000L));
        abandonedCart.setTotal(0.0);
        entityManager.persist(abandonedCart);
        entityManager.flush();
        // When
        Date cutoffDate = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L);
        int deletedCount = cartRepository.deleteAbandonedCarts(cutoffDate);
        // Then
        assertEquals("Un panier abandonné doit être supprimé", 1, deletedCount);
    }
    @Test
    public void testSave_ShouldCreateNewCart() {
        // Given
        Cart newCart = new Cart();
        newCart.setUser(testUser);
        newCart.setStatus("ACTIVE");
        newCart.setCreatedAt(new Date());
        newCart.setUpdatedAt(new Date());
        newCart.setTotal(0.0);
        // When
        Cart savedCart = cartRepository.save(newCart);
        entityManager.flush();
        // Then
        assertNotNull("L'ID doit être généré", savedCart.getId());
        assertEquals("Le statut doit être ACTIVE", "ACTIVE", savedCart.getStatus());
        assertEquals("Le total doit être 0", 0.0, savedCart.getTotal(), 0.01);
    }
    @Test
    public void testUpdate_ShouldModifyCart() {
        // Given
        testCart.setStatus("CONVERTED");
        testCart.setTotal(299.97);
        testCart.setUpdatedAt(new Date());
        // When
        Cart updatedCart = cartRepository.save(testCart);
        entityManager.flush();
        entityManager.clear();
        // Then
        Cart found = cartRepository.findById(testCart.getId()).orElse(null);
        assertNotNull("Le panier doit exister", found);
        assertEquals("Le statut doit être mis à jour", "CONVERTED", found.getStatus());
        assertEquals("Le total doit être mis à jour", 299.97, found.getTotal(), 0.01);
    }
    @Test
    public void testDelete_ShouldRemoveCart() {
        // Given
        Integer cartId = testCart.getId();
        // When
        cartRepository.delete(testCart);
        entityManager.flush();
        // Then
        Optional<Cart> found = cartRepository.findById(cartId);
        assertFalse("Le panier doit être supprimé", found.isPresent());
    }
}
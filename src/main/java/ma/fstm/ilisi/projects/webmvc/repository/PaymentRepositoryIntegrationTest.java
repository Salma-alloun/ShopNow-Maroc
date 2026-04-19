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
public class PaymentRepositoryIntegrationTest {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    private User testUser;
    private Order testOrder;
    private Payment testPayment;
    @Before
    public void setUp() {
        System.out.println("\n🔧 ===== PRÉPARATION DES DONNÉES DE TEST =====");
        
        // Nettoyer les données existantes
        try {
            paymentRepository.deleteAll();
            orderRepository.deleteAll();
            userRepository.deleteAll();
            entityManager.flush();
            System.out.println("✅ Nettoyage effectué");
        } catch (Exception e) {
            System.out.println("⚠️ Aucune donnée à nettoyer");
        }
        
        // Création d'un utilisateur
        testUser = new User();
        testUser.setEmail("paymentuser@test.com");
        testUser.setPasswordHash("pass123");
        testUser.setFirstName("Payment");
        testUser.setLastName("User");
        testUser.setRole("CLIENT");
        testUser.setActive(true);
        testUser.setCreatedAt(new Date());
        testUser.setUpdatedAt(new Date());
        entityManager.persist(testUser);
        entityManager.flush();
        System.out.println("👤 Utilisateur créé: ID=" + testUser.getId());
        // Création d'une commande
        testOrder = new Order();
        testOrder.setUser(testUser);
        testOrder.setOrderDate(new Date());
        testOrder.setStatus("PENDING");
        testOrder.setTotal(299.97);
        entityManager.persist(testOrder);
        entityManager.flush();
        System.out.println("📋 Commande créée: ID=" + testOrder.getId());
        // Création d'un paiement
        testPayment = new Payment();
        testPayment.setOrder(testOrder);
        testPayment.setStripePaymentIntentId("pi_test_123456789");
        testPayment.setAmount(299.97);
        testPayment.setCurrency("MAD");
        testPayment.setStatus("PENDING");
        testPayment.setPaymentMethod("CARD");
        testPayment.setPaymentDate(new Date());
        entityManager.persist(testPayment);
        entityManager.flush();
        System.out.println("💳 Paiement créé: ID=" + testPayment.getId() + ", Intent=pi_test_123456789");
        
        System.out.println("🔧 ===========================================\n");
    }
    @Test
    public void testFindById_ShouldReturnPayment() {
        System.out.println("🧪 ===== TEST: findById - Récupération par ID =====");
        
        Optional<Payment> found = paymentRepository.findById(testPayment.getId());
        assertTrue("Le paiement doit être trouvé", found.isPresent());
        assertEquals("L'ID doit correspondre", testPayment.getId(), found.get().getId());
        assertEquals("Le statut doit être PENDING", "PENDING", found.get().getStatus());
        
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testFindById_WithInvalidId_ShouldReturnEmpty() {
        System.out.println("🧪 ===== TEST: findById - ID invalide =====");
        
        Optional<Payment> found = paymentRepository.findById(9999);
        assertFalse("Aucun paiement ne doit être trouvé", found.isPresent());
        
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testFindByOrderId_ShouldReturnPayment() {
        System.out.println("🧪 ===== TEST: findByOrderId - Récupération par commande =====");
        
        Optional<Payment> found = paymentRepository.findByOrderId(testOrder.getId());
        assertTrue("Le paiement doit être trouvé", found.isPresent());
        assertEquals("L'ID de la commande doit correspondre", testOrder.getId(), found.get().getOrder().getId());
        assertEquals("Le montant doit correspondre", 299.97, found.get().getAmount(), 0.01);
        
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testFindByOrderId_WithOrderWithoutPayment_ShouldReturnEmpty() {
        System.out.println("🧪 ===== TEST: findByOrderId - Commande sans paiement =====");
        
        Order orderWithoutPayment = new Order();
        orderWithoutPayment.setUser(testUser);
        orderWithoutPayment.setOrderDate(new Date());
        orderWithoutPayment.setStatus("PENDING");
        orderWithoutPayment.setTotal(99.99);
        entityManager.persist(orderWithoutPayment);
        entityManager.flush();
        System.out.println("📋 Commande sans paiement créée: ID=" + orderWithoutPayment.getId());
        Optional<Payment> found = paymentRepository.findByOrderId(orderWithoutPayment.getId());
        assertFalse("Aucun paiement ne doit être trouvé", found.isPresent());
        
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testFindByStripePaymentIntentId_ShouldReturnPayment() {
        System.out.println("🧪 ===== TEST: findByStripePaymentIntentId - Récupération par Intent ID =====");
        
        Optional<Payment> found = paymentRepository.findByStripePaymentIntentId("pi_test_123456789");
        assertTrue("Le paiement doit être trouvé", found.isPresent());
        assertEquals("L'intent ID doit correspondre", "pi_test_123456789", found.get().getStripePaymentIntentId());
        
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testFindByStripePaymentIntentId_WithInvalidIntent_ShouldReturnEmpty() {
        System.out.println("🧪 ===== TEST: findByStripePaymentIntentId - Intent invalide =====");
        
        Optional<Payment> found = paymentRepository.findByStripePaymentIntentId("invalid_intent");
        assertFalse("Aucun paiement ne doit être trouvé", found.isPresent());
        
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testSave_ShouldCreateNewPayment() {
        System.out.println("🧪 ===== TEST: save - Création d'un nouveau paiement =====");
        
        Payment newPayment = new Payment();
        newPayment.setOrder(testOrder);
        newPayment.setStripePaymentIntentId("pi_test_new_123");
        newPayment.setAmount(199.98);
        newPayment.setCurrency("MAD");
        newPayment.setStatus("PENDING");
        newPayment.setPaymentMethod("CARD");
        newPayment.setPaymentDate(new Date());
        System.out.println("📝 Nouveau paiement: Intent=pi_test_new_123, Montant=199.98 MAD");
        Payment savedPayment = paymentRepository.save(newPayment);
        entityManager.flush();
        assertNotNull("L'ID doit être généré", savedPayment.getId());
        assertEquals("Le statut doit être PENDING", "PENDING", savedPayment.getStatus());
        assertEquals("L'intent ID doit être correct", "pi_test_new_123", savedPayment.getStripePaymentIntentId());
        
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testUpdate_ShouldModifyPayment() {
        System.out.println("🧪 ===== TEST: update - Mise à jour d'un paiement =====");
        
        testPayment.setStatus("SUCCEEDED");
        testPayment.setStripePaymentIntentId("pi_test_updated");
        System.out.println("📝 Paiement modifié: Status=SUCCEEDED, Intent=pi_test_updated");
        paymentRepository.save(testPayment);
        entityManager.flush();
        entityManager.clear();
        Payment found = paymentRepository.findById(testPayment.getId()).orElse(null);
        assertNotNull("Le paiement doit exister", found);
        assertEquals("Le statut doit être mis à jour", "SUCCEEDED", found.getStatus());
        assertEquals("L'intent ID doit être mis à jour", "pi_test_updated", found.getStripePaymentIntentId());
        
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testDelete_ShouldRemovePayment() {
        System.out.println("🧪 ===== TEST: delete - Suppression d'un paiement =====");
        
        Integer paymentId = testPayment.getId();
        System.out.println("📝 Paiement à supprimer: ID=" + paymentId);
        paymentRepository.delete(testPayment);
        entityManager.flush();
        Optional<Payment> found = paymentRepository.findById(paymentId);
        assertFalse("Le paiement doit être supprimé", found.isPresent());
        
        System.out.println("✅ TEST RÉUSSI!\n");
    }
}
package ma.fstm.ilisi.projects.webmvc.repository;
import ma.fstm.ilisi.projects.webmvc.bo.Address;
import ma.fstm.ilisi.projects.webmvc.bo.User;
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
public class UserRepositoryIntegrationTest {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    private User user1;
    private User user2;
    private User user3;
    private Address address1;
    private Address address2;
    @Before
    public void setUp() {
        // Adresse 1
        address1 = new Address();
        address1.setCity("Casablanca");
        address1.setPostalCode("20000");
        address1.setCountry("Maroc");
        entityManager.persist(address1);
        entityManager.flush();
        // Adresse 2
        address2 = new Address();
        address2.setCity("Rabat");
        address2.setPostalCode("10000");
        address2.setCountry("Maroc");
        entityManager.persist(address2);
        entityManager.flush();
        // Utilisateur 1 (CLIENT, actif, Maroc)
        user1 = new User();
        user1.setEmail("ahmed@test.com");
        user1.setPasswordHash("pass123");
        user1.setFirstName("Ahmed");
        user1.setLastName("Alaoui");
        user1.setRole("CLIENT");
        user1.setActive(true);
        user1.setCreatedAt(new Date());
        user1.setUpdatedAt(new Date());
        user1.setFailedLoginAttempts(0);
        user1.setAddress(address1);
        entityManager.persist(user1);
        entityManager.flush();
        // Utilisateur 2 (ADMIN, actif, Maroc)
        user2 = new User();
        user2.setEmail("admin@test.com");
        user2.setPasswordHash("admin123");
        user2.setFirstName("Admin");
        user2.setLastName("System");
        user2.setRole("ADMIN");
        user2.setActive(true);
        user2.setCreatedAt(new Date());
        user2.setUpdatedAt(new Date());
        user2.setFailedLoginAttempts(0);
        user2.setAddress(address2);
        entityManager.persist(user2);
        entityManager.flush();
        // Utilisateur 3 (CLIENT, inactif, France)
        user3 = new User();
        user3.setEmail("youssef@test.com");
        user3.setPasswordHash("pass123");
        user3.setFirstName("Youssef");
        user3.setLastName("El Amrani");
        user3.setRole("CLIENT");
        user3.setActive(false);
        user3.setCreatedAt(new Date());
        user3.setUpdatedAt(new Date());
        user3.setFailedLoginAttempts(3);
        
        Address address3 = new Address();
        address3.setCity("Paris");
        address3.setPostalCode("75001");
        address3.setCountry("France");
        entityManager.persist(address3);
        entityManager.flush();
        user3.setAddress(address3);
        entityManager.persist(user3);
        entityManager.flush();
    }
    @Test
    public void testFindByEmail_ShouldReturnUser() {
        // When
        Optional<User> found = userRepository.findByEmail("ahmed@test.com");
        // Then
        assertTrue("L'utilisateur doit être trouvé", found.isPresent());
        assertEquals("Le prénom doit être Ahmed", "Ahmed", found.get().getFirstName());
        assertEquals("Le nom doit être Alaoui", "Alaoui", found.get().getLastName());
    }
    @Test
    public void testFindByEmail_ShouldReturnEmpty_WhenEmailNotFound() {
        // When
        Optional<User> found = userRepository.findByEmail("inexistant@test.com");
        // Then
        assertFalse("Aucun utilisateur ne doit être trouvé", found.isPresent());
    }
    @Test
    public void testFindByEmailIgnoreCase_ShouldReturnUser() {
        // When
        Optional<User> found = userRepository.findByEmailIgnoreCase("AHMED@TEST.COM");
        // Then
        assertTrue("L'utilisateur doit être trouvé", found.isPresent());
        assertEquals("L'email doit correspondre", "ahmed@test.com", found.get().getEmail());
    }
    @Test
    public void testExistsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // When
        boolean exists = userRepository.existsByEmail("admin@test.com");
        // Then
        assertTrue("L'email doit exister", exists);
    }
    @Test
    public void testExistsByEmail_ShouldReturnFalse_WhenEmailNotExists() {
        // When
        boolean exists = userRepository.existsByEmail("unknown@test.com");
        // Then
        assertFalse("L'email ne doit pas exister", exists);
    }
    @Test
    public void testFindByFirstNameContainingIgnoreCase_ShouldReturnMatchingUsers() {
        List<User> results = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Ahmed", "XXXXXX");
        assertEquals("Un seul utilisateur doit correspondre", 1, results.size());
        assertEquals("Le prénom doit être Ahmed", "Ahmed", results.get(0).getFirstName());
    }
    @Test
    public void testFindByLastNameContainingIgnoreCase_ShouldReturnMatchingUsers() {
        List<User> results = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("XXXXXX", "Alaoui");
        assertEquals("Un seul utilisateur doit correspondre", 1, results.size());
        assertEquals("Le nom doit être Alaoui", "Alaoui", results.get(0).getLastName());
    }
    @Test
    public void testFindByFirstNameOrLastName_ShouldReturnMultipleMatches() {
        List<User> results = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("a", "a");
        assertEquals("La recherche large doit retourner plusieurs résultats", 3, results.size());
    }
    @Test
    public void testFindByRole_ShouldReturnUsersWithGivenRole() {
        // When
        List<User> clients = userRepository.findByRole("CLIENT");
        List<User> admins = userRepository.findByRole("ADMIN");
        // Then
        assertEquals("Il doit y avoir 2 clients", 2, clients.size());
        assertEquals("Il doit y avoir 1 admin", 1, admins.size());
        assertEquals("L'email de l'admin doit être admin@test.com", "admin@test.com", admins.get(0).getEmail());
    }
    @Test
    public void testFindByIsActive_ShouldReturnActiveUsers() {
        // When
        List<User> activeUsers = userRepository.findByIsActive(true);
        List<User> inactiveUsers = userRepository.findByIsActive(false);
        // Then
        assertEquals("Il doit y avoir 2 utilisateurs actifs", 2, activeUsers.size());
        assertEquals("Il doit y avoir 1 utilisateur inactif", 1, inactiveUsers.size());
        assertEquals("L'email de l'utilisateur inactif doit être youssef@test.com", 
                    "youssef@test.com", inactiveUsers.get(0).getEmail());
    }
    @Test
    public void testFindByCountry_ShouldReturnUsersFromCountry() {
        // When
        List<User> moroccanUsers = userRepository.findByCountry("Maroc");
        // Then
        assertEquals("Il doit y avoir 2 utilisateurs marocains", 2, moroccanUsers.size());
        assertTrue("Les emails doivent contenir ahmed@test.com", 
                   moroccanUsers.stream().anyMatch(u -> u.getEmail().equals("ahmed@test.com")));
        assertTrue("Les emails doivent contenir admin@test.com", 
                   moroccanUsers.stream().anyMatch(u -> u.getEmail().equals("admin@test.com")));
    }
    @Test
    public void testFindByCity_ShouldReturnUsersFromCity() {
        // When
        List<User> casablancaUsers = userRepository.findByCity("Casablanca");
        // Then
        assertEquals("Il doit y avoir 1 utilisateur à Casablanca", 1, casablancaUsers.size());
        assertEquals("L'email doit être ahmed@test.com", "ahmed@test.com", casablancaUsers.get(0).getEmail());
    }
    @Test
    public void testSearchUsers_WithMultipleCriteria_ShouldReturnFilteredUsers() {
        // When - Recherche des clients marocains actifs
        List<User> results = userRepository.searchUsers(null, null, null, "CLIENT", true, "Maroc");
        // Then
        assertEquals("Un seul client marocain actif", 1, results.size());
        assertEquals("L'email doit être ahmed@test.com", "ahmed@test.com", results.get(0).getEmail());
    }
    @Test
    public void testSearchUsers_WithAllNull_ShouldReturnAllUsers() {
        // When
        List<User> results = userRepository.searchUsers(null, null, null, null, null, null);
        // Then
        assertEquals("Tous les utilisateurs doivent être retournés", 3, results.size());
    }
    @Test
    public void testFindByFailedLoginAttemptsGreaterThan_ShouldReturnUsers() {
        // When
        List<User> users = userRepository.findByFailedLoginAttemptsGreaterThan(2);
        // Then
        assertEquals("Un utilisateur doit avoir plus de 2 tentatives", 1, users.size());
        assertEquals("L'email doit être youssef@test.com", "youssef@test.com", users.get(0).getEmail());
    }
    @Test
    public void testCountUsersByRole_ShouldReturnCountPerRole() {
        // When
        List<Object[]> counts = userRepository.countUsersByRole();
        // Then
        assertEquals("Il doit y avoir 2 rôles différents", 2, counts.size());
        
        for (Object[] row : counts) {
            String role = (String) row[0];
            Long count = (Long) row[1];
            
            if ("CLIENT".equals(role)) {
                assertEquals("Il doit y avoir 2 clients", Long.valueOf(2), count);
            } else if ("ADMIN".equals(role)) {
                assertEquals("Il doit y avoir 1 admin", Long.valueOf(1), count);
            }
        }
    }
    @Test
    public void testCountUsersByCountry_ShouldReturnCountPerCountry() {
        // When
        List<Object[]> counts = userRepository.countUsersByCountry();
        // Then
        assertEquals("Il doit y avoir 2 pays différents", 2, counts.size());
        
        for (Object[] row : counts) {
            String country = (String) row[0];
            Long count = (Long) row[1];
            
            if ("Maroc".equals(country)) {
                assertEquals("Il doit y avoir 2 utilisateurs au Maroc", Long.valueOf(2), count);
            } else if ("France".equals(country)) {
                assertEquals("Il doit y avoir 1 utilisateur en France", Long.valueOf(1), count);
            }
        }
    }
    @Test
    public void testUpdateUserActiveStatus_ShouldModifyStatus() {
        // When
        int updatedCount = userRepository.updateUserActiveStatus(user1.getId(), false);
        entityManager.flush();
        entityManager.clear();
        // Then
        assertEquals("Une ligne doit être mise à jour", 1, updatedCount);
        
        User updatedUser = userRepository.findById(user1.getId()).orElse(null);
        assertNotNull("L'utilisateur doit exister", updatedUser);
        assertFalse("L'utilisateur doit être inactif", updatedUser.isActive());
    }
    @Test
    public void testUpdateLastLogin_ShouldUpdateDate() {
        // When
        int updatedCount = userRepository.updateLastLogin(user1.getId());
        entityManager.flush();
        entityManager.clear();
        // Then
        assertEquals("Une ligne doit être mise à jour", 1, updatedCount);
        
        User updatedUser = userRepository.findById(user1.getId()).orElse(null);
        assertNotNull("L'utilisateur doit exister", updatedUser);
        assertNotNull("La date de dernière connexion doit être définie", updatedUser.getLastLogin());
        assertEquals("Les tentatives doivent être réinitialisées", 0, updatedUser.getFailedLoginAttempts());
    }
    @Test
    public void testIncrementFailedLoginAttempts_ShouldIncreaseCounter() {
        // Given
        int initialAttempts = user1.getFailedLoginAttempts();
        // When
        int updatedCount = userRepository.incrementFailedLoginAttempts(user1.getId());
        entityManager.flush();
        entityManager.clear();
        // Then
        assertEquals("Une ligne doit être mise à jour", 1, updatedCount);
        
        User updatedUser = userRepository.findById(user1.getId()).orElse(null);
        assertNotNull("L'utilisateur doit exister", updatedUser);
        assertEquals("Les tentatives doivent augmenter de 1", initialAttempts + 1, updatedUser.getFailedLoginAttempts());
    }
    @Test
    public void testResetFailedLoginAttempts_ShouldResetToZero() {
        // Given
        user1.setFailedLoginAttempts(5);
        entityManager.merge(user1);
        entityManager.flush();
        // When
        int updatedCount = userRepository.resetFailedLoginAttempts(user1.getId());
        entityManager.flush();
        entityManager.clear();
        // Then
        assertEquals("Une ligne doit être mise à jour", 1, updatedCount);
        
        User updatedUser = userRepository.findById(user1.getId()).orElse(null);
        assertNotNull("L'utilisateur doit exister", updatedUser);
        assertEquals("Les tentatives doivent être à 0", 0, updatedUser.getFailedLoginAttempts());
    }
    @Test
    public void testHasAddress_ShouldReturnTrue_WhenUserHasAddress() {
        // When
        boolean hasAddress = userRepository.hasAddress(user1.getId());
        // Then
        assertTrue("L'utilisateur doit avoir une adresse", hasAddress);
    }
    @Test
    public void testHasAddress_ShouldReturnFalse_WhenUserHasNoAddress() {
        // Given - créer un utilisateur sans adresse
        User userWithoutAddress = new User();
        userWithoutAddress.setEmail("noaddress@test.com");
        userWithoutAddress.setPasswordHash("pass");
        userWithoutAddress.setFirstName("No");
        userWithoutAddress.setLastName("Address");
        userWithoutAddress.setRole("CLIENT");
        userWithoutAddress.setActive(true);
        userWithoutAddress.setCreatedAt(new Date());
        userWithoutAddress.setUpdatedAt(new Date());
        entityManager.persist(userWithoutAddress);
        entityManager.flush();
        // When
        boolean hasAddress = userRepository.hasAddress(userWithoutAddress.getId());
        // Then
        assertFalse("L'utilisateur ne doit pas avoir d'adresse", hasAddress);
    }
    @Test
    public void testCountAdmins_ShouldReturnNumberOfAdmins() {
        // When
        long adminCount = userRepository.countAdmins();
        // Then
        assertEquals("Il doit y avoir 1 admin", 1, adminCount);
    }
    @Test
    public void testCountClients_ShouldReturnNumberOfClients() {
        // When
        long clientCount = userRepository.countClients();
        // Then
        assertEquals("Il doit y avoir 2 clients", 2, clientCount);
    }
    @Test
    public void testCountVisitors_ShouldReturnZero() {
        // When
        long visitorCount = userRepository.countVisitors();
        // Then
        assertEquals("Il ne doit pas y avoir de visiteurs", 0, visitorCount);
    }
}
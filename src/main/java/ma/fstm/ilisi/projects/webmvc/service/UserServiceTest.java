package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.bo.User;
import ma.fstm.ilisi.projects.webmvc.bo.Address;
import ma.fstm.ilisi.projects.webmvc.dto.UserDTO;
import ma.fstm.ilisi.projects.webmvc.dto.RegisterDTO;
import ma.fstm.ilisi.projects.webmvc.dto.LoginDTO;
import ma.fstm.ilisi.projects.webmvc.repository.UserRepository;
import ma.fstm.ilisi.projects.webmvc.repository.AddressRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.Date;
import java.util.Optional;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@RunWith(MockitoJUnitRunner.Silent.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressRepository addressRepository;
    @InjectMocks
    private UserService userService;
    private User testUser;
    private UserDTO testUserDTO;
    private RegisterDTO testRegisterDTO;
    private Address testAddress;
    @Before
    public void setUp() {
        System.out.println("\n🔧 ===== PRÉPARATION DES DONNÉES DE TEST =====");
        
        // Création d'une adresse de test
        testAddress = new Address();
        testAddress.setId(1);
        testAddress.setCity("Casablanca");
        testAddress.setPostalCode("20000");
        testAddress.setCountry("Maroc");
        System.out.println("📍 Adresse créée: Casablanca, 20000, Maroc");
        // Création d'un utilisateur de test
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@test.com");
        testUser.setPasswordHash("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole("CLIENT");
        testUser.setActive(true);
        testUser.setCreatedAt(new Date());
        testUser.setUpdatedAt(new Date());
        testUser.setAddress(testAddress);
        System.out.println("👤 Utilisateur créé: ID=1, Email=test@test.com, Rôle=CLIENT");
        // Création d'un DTO utilisateur
        testUserDTO = new UserDTO();
        testUserDTO.setId(1);
        testUserDTO.setEmail("test@test.com");
        testUserDTO.setFirstName("Test");
        testUserDTO.setLastName("User");
        testUserDTO.setRole("CLIENT");
        testUserDTO.setActive(true);
        System.out.println("📄 UserDTO créé");
        // Création d'un DTO d'inscription
        testRegisterDTO = new RegisterDTO();
        testRegisterDTO.setEmail("new@test.com");
        testRegisterDTO.setPassword("newpass123");
        testRegisterDTO.setFirstName("New");
        testRegisterDTO.setLastName("User");
        testRegisterDTO.setCity("Rabat");
        testRegisterDTO.setPostalCode("10000");
        testRegisterDTO.setCountry("Maroc");
        System.out.println("📝 RegisterDTO créé: Email=new@test.com");
        
        System.out.println("🔧 ===========================================\n");
    }
    // ==================== TEST CONVERSION ====================
    @Test
    public void testToUserDTO_WithValidUser_ShouldConvert() {
        System.out.println("🧪 ===== TEST: toUserDTO - Conversion User → UserDTO =====");
        System.out.println("🎯 Objectif: Vérifier la conversion d'un User en UserDTO");
        // Act
        UserDTO result = userService.toUserDTO(testUser);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - Email: " + result.getEmail() + " (attendu: test@test.com)");
        System.out.println("   - Prénom: " + result.getFirstName() + " (attendu: Test)");
        System.out.println("   - Mot de passe null (sécurité): " + (result.getPassword() == null));
        System.out.println("   - Adresse présente: " + (result.getAddress() != null));
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFirstName(), result.getFirstName());
        assertEquals(testUser.getLastName(), result.getLastName());
        assertNull(result.getPassword());
        assertNotNull(result.getAddress());
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testToUser_WithValidDTO_ShouldConvert() {
        System.out.println("🧪 ===== TEST: toUser - Conversion UserDTO → User =====");
        System.out.println("🎯 Objectif: Vérifier la conversion d'un UserDTO en User");
        // Act
        User result = userService.toUser(testUserDTO);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - Email: " + result.getEmail() + " (attendu: test@test.com)");
        System.out.println("   - Rôle forcé à CLIENT: " + result.getRole() + " (attendu: CLIENT)");
        assertNotNull(result);
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        assertEquals("CLIENT", result.getRole()); // ✅ toUser() force toujours CLIENT
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST AUTHENTIFICATION ====================
    @Test
    public void testAuthenticate_WithValidCredentials_ShouldReturnUser() {
        System.out.println("🧪 ===== TEST: authenticate - Connexion réussie =====");
        System.out.println("🎯 Objectif: Vérifier qu'un utilisateur peut se connecter avec les bons identifiants");
        // Arrange
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(testUser));
        // Act
        System.out.println("⚡ Exécution: authenticate('test@test.com', 'password123')");
        UserDTO result = userService.authenticate("test@test.com", "password123");
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - Email: " + (result != null ? result.getEmail() : "null") + " (attendu: test@test.com)");
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class)); // lastLogin mis à jour
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testAuthenticate_WithInvalidPassword_ShouldReturnNull() {
        System.out.println("🧪 ===== TEST: authenticate - Mot de passe incorrect =====");
        System.out.println("🎯 Objectif: Vérifier que la connexion échoue avec un mauvais mot de passe");
        // Arrange
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(testUser));
        // Act
        System.out.println("⚡ Exécution: authenticate('test@test.com', 'wrongpass')");
        UserDTO result = userService.authenticate("test@test.com", "wrongpass");
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat null (attendu): " + (result == null));
        assertNull(result);
        verify(userRepository, times(1)).save(any(User.class)); // failedAttempts incrémenté
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testAuthenticate_WithInactiveUser_ShouldThrowException() {
        System.out.println("🧪 ===== TEST: authenticate - Compte désactivé =====");
        System.out.println("🎯 Objectif: Vérifier qu'un compte désactivé ne peut pas se connecter");
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(testUser));
        // Act & Assert
        System.out.println("⚡ Exécution: Tentative de connexion avec compte désactivé");
        try {
            userService.authenticate("test@test.com", "password123");
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            System.out.println("✅ Exception attrapée: " + e.getMessage());
            assertEquals("Compte désactivé. Contactez l'administrateur.", e.getMessage());
        }
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testAuthenticate_WithNonExistentEmail_ShouldReturnNull() {
        System.out.println("🧪 ===== TEST: authenticate - Email inexistant =====");
        System.out.println("🎯 Objectif: Vérifier qu'un email inconnu retourne null");
        // Arrange
        when(userRepository.findByEmailIgnoreCase("unknown@test.com")).thenReturn(Optional.empty());
        // Act
        System.out.println("⚡ Exécution: authenticate('unknown@test.com', 'pass')");
        UserDTO result = userService.authenticate("unknown@test.com", "pass");
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat null (attendu): " + (result == null));
        assertNull(result);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST INSCRIPTION ====================
    @Test
    public void testRegister_WithValidData_ShouldCreateUser() {
        System.out.println("🧪 ===== TEST: register - Inscription réussie =====");
        System.out.println("🎯 Objectif: Vérifier qu'un nouvel utilisateur peut s'inscrire");
        // Arrange
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        
        User savedUser = new User();
        savedUser.setId(2);
        savedUser.setEmail("new@test.com");
        savedUser.setFirstName("New");
        savedUser.setLastName("User");
        savedUser.setRole("CLIENT");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        // Act
        System.out.println("⚡ Exécution: register(new@test.com)");
        UserDTO result = userService.register(testRegisterDTO);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - Email: " + (result != null ? result.getEmail() : "null") + " (attendu: new@test.com)");
        System.out.println("   - Rôle: " + (result != null ? result.getRole() : "null") + " (attendu: CLIENT)");
        assertNotNull(result);
        assertEquals("new@test.com", result.getEmail());
        assertEquals("CLIENT", result.getRole()); // ✅ Toujours CLIENT à l'inscription
        verify(userRepository, times(1)).save(any(User.class));
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testRegister_WithExistingEmail_ShouldThrowException() {
        System.out.println("🧪 ===== TEST: register - Email déjà utilisé =====");
        System.out.println("🎯 Objectif: Vérifier que l'inscription échoue si l'email existe déjà");
        // Arrange
        when(userRepository.existsByEmail("new@test.com")).thenReturn(true);
        // Act & Assert
        System.out.println("⚡ Exécution: register avec email existant");
        try {
            userService.register(testRegisterDTO);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            System.out.println("✅ Exception attrapée: " + e.getMessage());
            assertEquals("Email déjà utilisé", e.getMessage());
        }
        verify(userRepository, never()).save(any(User.class));
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST CRUD ====================
    @Test
    public void testGetUserById_WithValidId_ShouldReturnUser() {
        System.out.println("🧪 ===== TEST: getUserById - Recherche par ID =====");
        System.out.println("🎯 Objectif: Vérifier la récupération d'un utilisateur par son ID");
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        // Act
        System.out.println("⚡ Exécution: getUserById(1)");
        UserDTO result = userService.getUserById(1);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - ID: " + (result != null ? result.getId() : "null") + " (attendu: 1)");
        System.out.println("   - Email: " + (result != null ? result.getEmail() : "null") + " (attendu: test@test.com)");
        assertNotNull(result);
        assertEquals(1, result.getId());   
        assertEquals("test@test.com", result.getEmail());
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testGetUserById_WithInvalidId_ShouldReturnNull() {
        System.out.println("🧪 ===== TEST: getUserById - ID inexistant =====");
        System.out.println("🎯 Objectif: Vérifier que la recherche avec un ID invalide retourne null");
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());
        // Act
        System.out.println("⚡ Exécution: getUserById(999)");
        UserDTO result = userService.getUserById(999);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat null (attendu): " + (result == null));
        assertNull(result);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testUpdateUser_WithValidData_ShouldUpdateUser() {
        System.out.println("🧪 ===== TEST: updateUser - Mise à jour utilisateur =====");
        System.out.println("🎯 Objectif: Vérifier la mise à jour des informations d'un utilisateur");
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        
        UserDTO updateDTO = new UserDTO();
        updateDTO.setId(1);
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("Name");
        updateDTO.setEmail("updated@test.com");
        updateDTO.setRole("CLIENT");
        updateDTO.setActive(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Act
        System.out.println("⚡ Exécution: updateUser(updated@test.com)");
        UserDTO result = userService.updateUser(updateDTO);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat non null: " + (result != null));
        System.out.println("   - Nouveau prénom: " + (result != null ? result.getFirstName() : "null") + " (attendu: Updated)");
        System.out.println("   - Nouvel email: " + (result != null ? result.getEmail() : "null") + " (attendu: updated@test.com)");
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("updated@test.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testDeleteUser_WithValidId_ShouldReturnTrue() {
        System.out.println("🧪 ===== TEST: deleteUser - Suppression utilisateur =====");
        System.out.println("🎯 Objectif: Vérifier la suppression d'un utilisateur");
        // Arrange
        when(userRepository.existsById(1)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1);
        // Act
        System.out.println("⚡ Exécution: deleteUser(1)");
        boolean result = userService.deleteUser(1);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat: " + result + " (attendu: true)");
        assertTrue(result);
        verify(userRepository, times(1)).deleteById(1);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    // ==================== TEST ADMINISTRATION ====================
    @Test
    public void testLockUser_WithValidId_ShouldLockAccount() {
        System.out.println("🧪 ===== TEST: lockUser - Verrouillage de compte =====");
        System.out.println("🎯 Objectif: Vérifier qu'un administrateur peut verrouiller un compte");
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        // Act
        System.out.println("⚡ Exécution: lockUser(1)");
        boolean result = userService.lockUser(1);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat: " + result + " (attendu: true)");
        System.out.println("   - Compte actif: " + testUser.isActive() + " (attendu: false)");
        assertTrue(result);
        assertFalse(testUser.isActive());
        verify(userRepository, times(1)).save(testUser);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testUnlockUser_WithValidId_ShouldUnlockAccount() {
        System.out.println("🧪 ===== TEST: unlockUser - Déverrouillage de compte =====");
        System.out.println("🎯 Objectif: Vérifier qu'un administrateur peut déverrouiller un compte");
        // Arrange
        testUser.setActive(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        // Act
        System.out.println("⚡ Exécution: unlockUser(1)");
        boolean result = userService.unlockUser(1);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Résultat: " + result + " (attendu: true)");
        System.out.println("   - Compte actif: " + testUser.isActive() + " (attendu: true)");
        System.out.println("   - Tentatives échouées: " + testUser.getFailedLoginAttempts() + " (attendu: 0)");
        assertTrue(result);
        assertTrue(testUser.isActive());
        assertEquals(0, testUser.getFailedLoginAttempts());
        verify(userRepository, times(1)).save(testUser);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testPromoteToAdmin_WithValidClient_ShouldPromote() {
        System.out.println("🧪 ===== TEST: promoteToAdmin - Promotion en Admin =====");
        System.out.println("🎯 Objectif: Vérifier qu'un client peut être promu administrateur");
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        // Act
        System.out.println("⚡ Exécution: promoteToAdmin(1)");
        userService.promoteToAdmin(1);
        // Assert
        System.out.println("✅ Vérification des résultats:");
        System.out.println("   - Nouveau rôle: " + testUser.getRole() + " (attendu: ADMIN)");
        assertEquals("ADMIN", testUser.getRole());
        verify(userRepository, times(1)).save(testUser);
        System.out.println("✅ TEST RÉUSSI!\n");
    }
    @Test
    public void testPromoteToAdmin_WithExistingAdmin_ShouldThrowException() {
        System.out.println("🧪 ===== TEST: promoteToAdmin - Promotion d'un admin existant =====");
        System.out.println("🎯 Objectif: Vérifier qu'on ne peut pas promouvoir un admin déjà admin");
        // Arrange
        testUser.setRole("ADMIN");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        // Act & Assert
        System.out.println("⚡ Exécution: promoteToAdmin(1) (utilisateur déjà admin)");
        try {
            userService.promoteToAdmin(1);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            System.out.println("✅ Exception attrapée: " + e.getMessage());
            assertEquals("Cet utilisateur est déjà Admin !", e.getMessage());
        }
        verify(userRepository, never()).save(any(User.class));
        System.out.println("✅ TEST RÉUSSI!\n");
    }
}
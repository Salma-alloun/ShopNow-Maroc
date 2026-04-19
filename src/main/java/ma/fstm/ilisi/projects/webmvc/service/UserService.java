package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.bo.User;
import ma.fstm.ilisi.projects.webmvc.bo.Address;
import ma.fstm.ilisi.projects.webmvc.dto.UserDTO;
import ma.fstm.ilisi.projects.webmvc.dto.AddressDTO;
import ma.fstm.ilisi.projects.webmvc.dto.LoginDTO;
import ma.fstm.ilisi.projects.webmvc.dto.RegisterDTO;
import ma.fstm.ilisi.projects.webmvc.repository.UserRepository;
import ma.fstm.ilisi.projects.webmvc.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;
    // ==================== CONVERSION ====================
    public UserDTO toUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLastLogin(user.getLastLogin());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setPassword(null); // ✅ Jamais exposer le mot de passe
        if (user.getAddress() != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setId(user.getAddress().getId());
            addressDTO.setCity(user.getAddress().getCity());
            addressDTO.setPostalCode(user.getAddress().getPostalCode());
            addressDTO.setCountry(user.getAddress().getCountry());
            addressDTO.setUserId(user.getId());
            addressDTO.setUserFullName(user.getFullName());
            dto.setAddress(addressDTO);
        }
        return dto;
    }
    public User toUser(UserDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole("CLIENT"); // ✅ toUser() force toujours CLIENT
        user.setActive(dto.isActive());
        user.setCreatedAt(dto.getCreatedAt());
        user.setUpdatedAt(dto.getUpdatedAt());
        user.setLastLogin(dto.getLastLogin());
        user.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        return user;
    }
    // ==================== AUTHENTIFICATION ====================
    public UserDTO authenticate(String email, String password) {
        if (email == null || password == null) return null;
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // ✅ Vérifier si le compte est actif avant tout
            if (!user.isActive()) {
                throw new RuntimeException("Compte désactivé. Contactez l'administrateur.");
            }
            if (password.equals(user.getPasswordHash())) {
                user.setLastLogin(new Date());
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
                return toUserDTO(user);
            } else {
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                userRepository.save(user);
            }
        }
        return null;
    }
    public UserDTO authenticate(LoginDTO loginDTO) {
        return authenticate(loginDTO.getEmail(), loginDTO.getPassword());
    }
    // ==================== INSCRIPTION ====================
    public UserDTO register(RegisterDTO registerDTO) {
        System.out.println("=== INSCRIPTION ===");
        System.out.println("Email: " + registerDTO.getEmail());
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPasswordHash(registerDTO.getPassword());
        user.setFirstName(registerDTO.getFirstName());
        user.setLastName(registerDTO.getLastName());
        // ✅ TOUJOURS CLIENT à l'inscription publique — jamais modifiable
        user.setRole("CLIENT");
        System.out.println("Rôle enregistré: CLIENT");
        user.setActive(true);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setFailedLoginAttempts(0);
        Address address = new Address();
        address.setCity(registerDTO.getCity());
        address.setPostalCode(registerDTO.getPostalCode());
        address.setCountry(registerDTO.getCountry());
        user.setAddress(address);
        User savedUser = userRepository.save(user);
        return toUserDTO(savedUser);
    }
    // ==================== CRUD ====================
    public UserDTO createUser(UserDTO userDTO) {
        User user = toUser(userDTO);
        user.setPasswordHash(userDTO.getPassword() != null ? userDTO.getPassword() : "default123");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setFailedLoginAttempts(0);
        if (userDTO.getAddress() != null) {
            Address address = new Address();
            address.setCity(userDTO.getAddress().getCity());
            address.setPostalCode(userDTO.getAddress().getPostalCode());
            address.setCountry(userDTO.getAddress().getCountry());
            user.setAddress(address);
        }
        User savedUser = userRepository.save(user);
        return toUserDTO(savedUser);
    }
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }
    public UserDTO getUserById(int id) {
        return userRepository.findById(id)
                .map(this::toUserDTO)
                .orElse(null);
    }
    public UserDTO getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(this::toUserDTO)
                .orElse(null);
    }
    public UserDTO updateUser(UserDTO userDTO) {
        Optional<User> userOpt = userRepository.findById(userDTO.getId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setUpdatedAt(new Date());
            // ✅ Seul CLIENT ou ADMIN autorisé — jamais VISITOR
            String role = userDTO.getRole();
            if ("CLIENT".equals(role) || "ADMIN".equals(role)) {
                user.setRole(role);
            }
            user.setActive(userDTO.isActive());
            // ✅ Changer le mot de passe seulement si fourni
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                user.setPasswordHash(userDTO.getPassword());
            }
            if (userDTO.getAddress() != null) {
                if (user.getAddress() == null) {
                    user.setAddress(new Address());
                }
                user.getAddress().setCity(userDTO.getAddress().getCity());
                user.getAddress().setPostalCode(userDTO.getAddress().getPostalCode());
                user.getAddress().setCountry(userDTO.getAddress().getCountry());
            }
            User updatedUser = userRepository.save(user);
            return toUserDTO(updatedUser);
        }
        return null;
    }
    public boolean deleteUser(int id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    // ==================== RECHERCHE ====================
    public List<UserDTO> searchUsers(String email, String firstName, String lastName,
                                     String role, Boolean isActive, String country) {
        return userRepository.searchUsers(email, firstName, lastName, role, isActive, country)
                .stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }
    public List<UserDTO> getUsersByRole(String role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }
    public List<UserDTO> getActiveUsers() {
        return userRepository.findByIsActive(true)
                .stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }
    public List<UserDTO> getInactiveUsers() {
        return userRepository.findByIsActive(false)
                .stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }
    public List<UserDTO> getUsersByCountry(String country) {
        return userRepository.findByCountry(country)
                .stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }
    // ==================== ADMINISTRATION ====================
    public void createDefaultAdmin() {
        // ✅ Création directe sans passer par register() qui force CLIENT
        if (userRepository.countAdmins() == 0) {
            User admin = new User();
            admin.setEmail("admin@ecommerce.com");
            admin.setPasswordHash("admin123");
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setRole("ADMIN"); // ✅ Direct
            admin.setActive(true);
            admin.setCreatedAt(new Date());
            admin.setUpdatedAt(new Date());
            admin.setFailedLoginAttempts(0);
            Address address = new Address();
            address.setCity("Casablanca");
            address.setPostalCode("20000");
            address.setCountry("Maroc");
            admin.setAddress(address);
            userRepository.save(admin);
            System.out.println("✅ Admin par défaut créé : admin@ecommerce.com / admin123");
        }
    }
    public boolean lockUser(int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    public boolean unlockUser(int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(true);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    public void promoteToAdmin(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Cet utilisateur est déjà Admin !");
        }
        user.setRole("ADMIN");
        user.setUpdatedAt(new Date());
        userRepository.save(user);
    }
    public void demoteToClient(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if ("CLIENT".equals(user.getRole())) {
            throw new RuntimeException("Cet utilisateur est déjà Client !");
        }
        user.setRole("CLIENT");
        user.setUpdatedAt(new Date());
        userRepository.save(user);
    }
    public boolean isSuperAdmin(UserDTO user) {
        return "admin@ecommerce.com".equals(user.getEmail());
    }
    public void createUserByAdmin(String firstName, String lastName,
                                   String email, String password, String role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Cet email est déjà utilisé !");
        }
        // ✅ Validation rôle : seulement CLIENT ou ADMIN
        if (!"CLIENT".equals(role) && !"ADMIN".equals(role)) {
            role = "CLIENT";
        }
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }
    
    // ==================== STATISTIQUES ====================
    public long getTotalUsers() { return userRepository.count(); }
    public long countAdmins()   { return userRepository.countAdmins(); }
    public long countClients()  { return userRepository.countClients(); }
    public long countVisitors() { return userRepository.countVisitors(); }
    public List<Object[]> getUsersCountByRole()    { return userRepository.countUsersByRole(); }
    public List<Object[]> getUsersCountByCountry() { return userRepository.countUsersByCountry(); }
}
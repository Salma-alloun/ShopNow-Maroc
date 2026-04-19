package ma.fstm.ilisi.projects.webmvc.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ma.fstm.ilisi.projects.webmvc.bo.User;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Recherche par email (exact)
    Optional<User> findByEmail(String email);
    
    // Recherche par email (ignore casse)
    Optional<User> findByEmailIgnoreCase(String email);
    
    // Vérifier si un email existe déjà
    boolean existsByEmail(String email);
    
    // Recherche par nom (prénom ou nom)
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    
    // Recherche par rôle
    List<User> findByRole(String role);
    
    // Recherche par statut actif/inactif
    List<User> findByIsActive(boolean isActive);
    
    // Recherche par pays (via adresse)
    @Query("SELECT u FROM User u WHERE u.address.country = :country")
    List<User> findByCountry(@Param("country") String country);
    
    // Recherche par ville (via adresse)
    @Query("SELECT u FROM User u WHERE u.address.city = :city")
    List<User> findByCity(@Param("city") String city);
    
    // Recherche avancée multi-critères
    @Query("SELECT u FROM User u WHERE " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive) AND " +
           "(:country IS NULL OR u.address.country = :country)")
    List<User> searchUsers(@Param("email") String email,
                          @Param("firstName") String firstName,
                          @Param("lastName") String lastName,
                          @Param("role") String role,
                          @Param("isActive") Boolean isActive,
                          @Param("country") String country);
    
    // Trouver les utilisateurs avec des tentatives de connexion échouées > seuil
    List<User> findByFailedLoginAttemptsGreaterThan(int maxAttempts);
    
    // Trouver les utilisateurs qui n'ont pas de panier
    @Query("SELECT u FROM User u WHERE u.cart IS NULL")
    List<User> findUsersWithoutCart();
    
    // Trouver les utilisateurs avec un panier actif
    @Query("SELECT u FROM User u WHERE u.cart IS NOT NULL AND u.cart.status = 'ACTIVE'")
    List<User> findUsersWithActiveCart();
    
    // Compter le nombre d'utilisateurs par rôle
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();
    
    // Compter le nombre d'utilisateurs par pays
    @Query("SELECT u.address.country, COUNT(u) FROM User u WHERE u.address IS NOT NULL GROUP BY u.address.country")
    List<Object[]> countUsersByCountry();
    
    // Trouver les utilisateurs qui ont des commandes
    @Query("SELECT DISTINCT u FROM User u JOIN u.orders o")
    List<User> findUsersWithOrders();
    
    // Trouver les utilisateurs qui n'ont jamais commandé
    @Query("SELECT u FROM User u WHERE u.orders IS EMPTY")
    List<User> findUsersWithoutOrders();
    
    // Trouver les utilisateurs avec un total de commandes > montant
    @Query("SELECT u FROM User u JOIN u.orders o GROUP BY u HAVING SUM(o.total) > :amount")
    List<User> findUsersWithTotalOrdersGreaterThan(@Param("amount") double amount);
    
    // Mise à jour du statut actif
    @Modifying
    @Query("UPDATE User u SET u.isActive = :isActive WHERE u.id = :userId")
    int updateUserActiveStatus(@Param("userId") int userId, @Param("isActive") boolean isActive);
    
    // Mise à jour de la date de dernière connexion
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = CURRENT_TIMESTAMP, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    int updateLastLogin(@Param("userId") int userId);
    
    // Incrémenter les tentatives de connexion échouées
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    int incrementFailedLoginAttempts(@Param("userId") int userId);
    
    // Réinitialiser les tentatives de connexion échouées
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0 WHERE u.id = :userId")
    int resetFailedLoginAttempts(@Param("userId") int userId);
    
    // Trouver les utilisateurs inactifs depuis une certaine date
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date OR u.lastLogin IS NULL")
    List<User> findUsersInactiveSince(@Param("date") java.util.Date date);
    
    // Vérifier si un utilisateur a une adresse
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.id = :userId AND u.address IS NOT NULL")
    boolean hasAddress(@Param("userId") int userId);
    
    // Trouver les utilisateurs avec une adresse incomplète
    @Query("SELECT u FROM User u WHERE u.address IS NOT NULL AND " +
           "(u.address.city IS NULL OR u.address.postalCode IS NULL OR u.address.country IS NULL)")
    List<User> findUsersWithIncompleteAddress();
    
    // Compter les admins (comme dans votre exemple)
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN'")
    long countAdmins();
    
    // Compter les clients
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'CLIENT'")
    long countClients();
    
    // Compter les visiteurs
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'VISITOR'")
    long countVisitors();
}
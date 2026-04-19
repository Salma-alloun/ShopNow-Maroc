package ma.fstm.ilisi.projects.webmvc.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ma.fstm.ilisi.projects.webmvc.bo.Address;
import java.util.List;
import java.util.Optional;
@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    
    // ==================== FIND BY FIELD METHODS ====================
    
    Optional<Address> findByUserId(int userId);
    
    List<Address> findByCity(String city);
    
    List<Address> findByCountry(String country);
    
    List<Address> findByCityAndCountry(String city, String country);
    
    List<Address> findByPostalCode(String postalCode);
    
    // ==================== SEARCH METHODS ====================
    
    // Recherche avancée d'adresses (la méthode qui manquait)
    @Query("SELECT a FROM Address a WHERE " +
           "(:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:country IS NULL OR LOWER(a.country) LIKE LOWER(CONCAT('%', :country, '%'))) AND " +
           "(:postalCode IS NULL OR a.postalCode = :postalCode)")
    List<Address> searchAddresses(@Param("city") String city,
                                  @Param("country") String country,
                                  @Param("postalCode") String postalCode);
    
    // ==================== STATISTICS METHODS ====================
    
    // Compter les adresses par pays (la méthode qui manquait)
    @Query("SELECT a.country, COUNT(a) FROM Address a GROUP BY a.country")
    List<Object[]> countAddressesByCountry();
    
    // Compter les adresses par ville
    @Query("SELECT a.city, COUNT(a) FROM Address a GROUP BY a.city")
    List<Object[]> countAddressesByCity();
    
    // Compter les adresses par code postal
    @Query("SELECT a.postalCode, COUNT(a) FROM Address a GROUP BY a.postalCode")
    List<Object[]> countAddressesByPostalCode();
    
    // ==================== ASSIGNMENT METHODS ====================
    
    // Vérifier si une adresse est assignée à un utilisateur (la méthode qui manquait)
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Address a WHERE a.id = :addressId AND a.user IS NOT NULL")
    boolean isAddressAssignedToUser(@Param("addressId") int addressId);
    
    // Trouver les adresses sans utilisateur associé (la méthode qui manquait)
    @Query("SELECT a FROM Address a WHERE a.user IS NULL")
    List<Address> findUnassignedAddresses();
    
    // Trouver les adresses assignées à un utilisateur
    @Query("SELECT a FROM Address a WHERE a.user IS NOT NULL")
    List<Address> findAssignedAddresses();
    
    // ==================== ADDITIONAL USEFUL METHODS ====================
    
    // Trouver par ville et code postal
    List<Address> findByCityAndPostalCode(String city, String postalCode);
    
    // Trouver par pays et code postal
    List<Address> findByCountryAndPostalCode(String country, String postalCode);
    
    // Compter le nombre d'adresses dans une ville
    long countByCity(String city);
    
    // Compter le nombre d'adresses dans un pays
    long countByCountry(String country);
    
    // Vérifier si un utilisateur a une adresse
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Address a WHERE a.user.id = :userId")
    boolean existsByUserId(@Param("userId") int userId);
    
    // Supprimer les adresses orphelines (non assignées depuis plus de X jours - nécessite un champ date)
    // @Query("DELETE FROM Address a WHERE a.user IS NULL AND a.createdAt < :date")
    // @Modifying
    // void deleteUnassignedAddressesOlderThan(@Param("date") Date date);
}
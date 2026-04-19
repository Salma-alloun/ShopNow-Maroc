package ma.fstm.ilisi.projects.webmvc.repository;
import ma.fstm.ilisi.projects.webmvc.bo.Cart;
import ma.fstm.ilisi.projects.webmvc.bo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    
    // Trouver le panier actif d'un utilisateur
    Optional<Cart> findByUserAndStatus(User user, String status);
    
    // Trouver le panier d'un utilisateur avec tous ses items chargés
    @Query("SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    Optional<Cart> findActiveCartWithItems(@Param("userId") int userId);
    
    // Vérifier si un utilisateur a un panier actif
    boolean existsByUserAndStatus(User user, String status);
    
    // Supprimer les paniers abandonnés (plus anciens que x jours)
    @Modifying
    @Transactional
    @Query("DELETE FROM Cart c WHERE c.status = 'ABANDONED' AND c.updatedAt < :date")
    int deleteAbandonedCarts(@Param("date") java.util.Date date);
}
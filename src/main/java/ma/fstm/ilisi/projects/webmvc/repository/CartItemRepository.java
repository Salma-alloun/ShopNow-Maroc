package ma.fstm.ilisi.projects.webmvc.repository;
import ma.fstm.ilisi.projects.webmvc.bo.CartItem;
import ma.fstm.ilisi.projects.webmvc.bo.Cart;
import ma.fstm.ilisi.projects.webmvc.bo.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    
    // Trouver un item spécifique dans un panier
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    // Supprimer tous les items d'un panier
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") int cartId);
    
    // Compter le nombre d'items dans un panier
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    int countItemsInCart(@Param("cartId") int cartId);
    
    // Obtenir le total d'un panier
    @Query("SELECT SUM(ci.subtotal) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Double getCartTotal(@Param("cartId") int cartId);
}
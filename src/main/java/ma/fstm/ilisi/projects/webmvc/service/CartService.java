package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.bo.Cart;
import ma.fstm.ilisi.projects.webmvc.bo.User;
import ma.fstm.ilisi.projects.webmvc.dto.CartDTO;
import java.util.List;
import java.util.Map;
public interface CartService {
    
    // Obtenir ou créer le panier d'un utilisateur
    Cart getOrCreateCart(User user);
    
    // Obtenir le panier actif d'un utilisateur avec ses items
    Cart getActiveCartWithItems(User user);
    
    // Ajouter un produit au panier
    CartDTO addToCart(User user, int productId, int quantity);
    
    // Mettre à jour la quantité d'un produit
    CartDTO updateQuantity(User user, int productId, int quantity);
    
    // Retirer un produit du panier
    CartDTO removeFromCart(User user, int productId);
    
    // Vider le panier
    void clearCart(User user);
    
    // Obtenir le nombre d'items (articles distincts) dans le panier
    int getCartItemCount(User user);
    
    // Obtenir le nombre d'items pour un utilisateur par son ID (articles distincts)
    int getCartItemCountForUser(int userId);
    
    // NOUVELLE MÉTHODE : Obtenir la QUANTITÉ TOTALE de produits dans le panier
    int getCartTotalQuantityForUser(int userId);
    
    // Convertir Cart en CartDTO
    CartDTO convertToDTO(Cart cart);
    
    CartDTO mergeGuestCartAndGet(int userId, List<Map<String, Integer>> guestItems);
    
    // Valider le panier avant commande
    boolean validateCart(Cart cart);
    CartDTO addToCartForUser(int userId, int productId, int quantity);
    CartDTO updateQuantityForUser(int userId, int productId, int quantity);
    CartDTO removeFromCartForUser(int userId, int productId);
    CartDTO getCartForUser(int userId);
}
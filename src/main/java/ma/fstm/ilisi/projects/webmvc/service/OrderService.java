package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.dto.OrderDTO;
import java.util.List;
public interface OrderService {
    // Créer une commande depuis le panier du user
    OrderDTO createOrderFromCart(int userId);
    // Récupérer une commande par ID
    OrderDTO getOrderById(int orderId);
    // Récupérer toutes les commandes d'un user
    List<OrderDTO> getOrdersByUser(int userId);
    // Mettre à jour le statut (PENDING → PAID → SHIPPED...)
    OrderDTO updateStatus(int orderId, String status);
}
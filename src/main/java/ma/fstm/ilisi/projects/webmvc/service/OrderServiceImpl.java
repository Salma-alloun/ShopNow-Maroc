package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.bo.*;
import ma.fstm.ilisi.projects.webmvc.dto.*;
import ma.fstm.ilisi.projects.webmvc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired private OrderRepository     orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private CartRepository      cartRepository;
    @Autowired private CartItemRepository  cartItemRepository;
    @Autowired private UserRepository      userRepository;
    @Autowired private ProductRepository   productRepository;
    @Override
    public OrderDTO createOrderFromCart(int userId) {
        // 1. Récupérer l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        // 2. Récupérer le panier actif avec ses items
        Cart cart = cartRepository.findActiveCartWithItems(userId)
                .orElseThrow(() -> new RuntimeException("Panier vide ou introuvable"));
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Le panier est vide");
        }
        // 3. Créer la commande
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus("PENDING");
        order.setTotal(cart.getTotal());
        order = orderRepository.save(order);
        // 4. Créer les OrderItems depuis les CartItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(cartItem.getProduct());
            oi.setQuantity(cartItem.getQuantity());
            oi.calculateSubtotal();
            orderItems.add(orderItemRepository.save(oi));
            // Décrémenter le stock du produit
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }
        order.setOrderItems(orderItems);
     // 5. Supprimer les CartItems ET passer le Cart en ORDERED
     // Alternative
     // 5. Vider le panier et le remettre ACTIVE pour prochaine commande
        cartItemRepository.deleteByCartId(cart.getId());
        cart.getCartItems().clear();
        cart.setStatus("ACTIVE");
        cart.setTotal(0.0);
        cart.setUpdatedAt(new Date());
        cartRepository.save(cart);
        return convertToDTO(order);
    }
    @Override
    public OrderDTO getOrderById(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        return convertToDTO(order);
    }
    @Override
    public List<OrderDTO> getOrdersByUser(int userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    @Override
    public OrderDTO updateStatus(int orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        order.setStatus(status);
        return convertToDTO(orderRepository.save(order));
    }
    // ── Conversion BO → DTO ──────────────────────────────────────────────────
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus());
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUserFullName(order.getUser().getFullName());
        }
        if (order.getOrderItems() != null) {
            List<OrderItemDTO> itemDTOs = order.getOrderItems()
                    .stream().map(this::convertItemToDTO).collect(Collectors.toList());
            dto.setOrderItems(itemDTOs);
        } else {
            dto.setOrderItems(new ArrayList<>());
        }
        return dto;
    }
    private OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());
        if (item.getOrder()   != null) dto.setOrderId(item.getOrder().getId());
        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
            dto.setProductPrice(item.getProduct().getPrice());
        }
        return dto;
    }
}
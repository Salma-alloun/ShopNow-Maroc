package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.bo.Cart;
import java.util.ArrayList;
import ma.fstm.ilisi.projects.webmvc.bo.Cart;
import ma.fstm.ilisi.projects.webmvc.bo.CartItem;
import ma.fstm.ilisi.projects.webmvc.bo.Product;
import ma.fstm.ilisi.projects.webmvc.bo.User;
import ma.fstm.ilisi.projects.webmvc.dto.CartDTO;
import ma.fstm.ilisi.projects.webmvc.dto.CartItemDTO;
import ma.fstm.ilisi.projects.webmvc.repository.CartItemRepository;
import ma.fstm.ilisi.projects.webmvc.repository.CartRepository;
import ma.fstm.ilisi.projects.webmvc.repository.ProductRepository;
import ma.fstm.ilisi.projects.webmvc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
@Service
@Transactional
public class CartServiceImpl implements CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public Cart getOrCreateCart(User user) {
        Optional<Cart> existingCart = cartRepository.findByUserAndStatus(user, "ACTIVE");
        
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.setUpdatedAt(new Date());
            return cartRepository.save(cart);
        } else {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setCreatedAt(new Date());
            newCart.setUpdatedAt(new Date());
            newCart.setStatus("ACTIVE");
            newCart.setTotal(0.0);
            newCart.setCartItems(new ArrayList<>());
            return cartRepository.save(newCart);
        }
    }
    
    @Override
    public Cart getActiveCartWithItems(User user) {
        Optional<Cart> cart = cartRepository.findActiveCartWithItems(user.getId());
        if (cart.isPresent()) {
            Cart existingCart = cart.get();
            // IMPORTANT: S'assurer que la liste des items est initialisée
            if (existingCart.getCartItems() == null) {
                existingCart.setCartItems(new ArrayList<>());
            }
            return existingCart;
        }
        return getOrCreateCart(user);
    }
    @Override
    public CartDTO addToCart(User user, int productId, int quantity) {
        // Vérifier le produit et le stock
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Stock insuffisant");
        }
        
        // Obtenir ou créer le panier (MAINTENANT AVEC LISTE INITIALISÉE)
        Cart cart = getActiveCartWithItems(user);
        
        // DEBUG
        System.out.println("========== AJOUT AU PANIER ==========");
        System.out.println("Cart ID: " + cart.getId());
        System.out.println("Cart items avant: " + (cart.getCartItems() != null ? cart.getCartItems().size() : "null"));
        
        // Vérifier si le produit existe déjà dans le panier
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Mettre à jour la quantité
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            
            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Stock insuffisant pour augmenter la quantité");
            }
            
            item.setQuantity(newQuantity);
            item.calculateSubtotal();
            cartItemRepository.save(item);
            System.out.println("Quantité mise à jour: " + newQuantity);
        } else {
            // Créer un nouvel item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.calculateSubtotal();
            cartItemRepository.save(newItem);
            
            // Ajouter à la liste du panier
            cart.getCartItems().add(newItem);
            System.out.println("Nouvel item ajouté");
        }
        
        // Mettre à jour le panier
        cart.setUpdatedAt(new Date());
        cart.calculateTotal();
        cartRepository.save(cart);
        
        System.out.println("Cart items après: " + cart.getCartItems().size());
        System.out.println("Total panier: " + cart.getTotal());
        System.out.println("======================================");
        
        return convertToDTO(cart);
    }
    
    @Override
    public CartDTO updateQuantity(User user, int productId, int quantity) {
        Cart cart = getActiveCartWithItems(user);
        
        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getProduct().getId() == productId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Produit non trouvé dans le panier"));
        
        if (quantity <= 0) {
            // Retirer l'item si quantité <= 0
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            // Vérifier le stock
            Product product = item.getProduct();
            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException("Stock insuffisant");
            }
            
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        
        cart.setUpdatedAt(new Date());
        cart.calculateTotal();
        cartRepository.save(cart);
        
        return convertToDTO(cart);
    }
    
    @Override
    public CartDTO removeFromCart(User user, int productId) {
        Cart cart = getActiveCartWithItems(user);
        
        cart.getCartItems().removeIf(item -> {
            if (item.getProduct().getId() == productId) {
                cartItemRepository.delete(item);
                return true;
            }
            return false;
        });
        
        cart.setUpdatedAt(new Date());
        cart.calculateTotal();
        cartRepository.save(cart);
        
        return convertToDTO(cart);
    }
    
    @Override
    public void clearCart(User user) {
        Cart cart = getActiveCartWithItems(user);
        cartItemRepository.deleteByCartId(cart.getId());
        cart.getCartItems().clear();
        cart.setTotal(0.0);
        cart.setUpdatedAt(new Date());
        cartRepository.save(cart);
    }
    
    @Override
    public int getCartItemCount(User user) {
        Cart cart = getActiveCartWithItems(user);
        return cart.getCartItems() != null ? cart.getCartItems().size() : 0;
    }
    
    @Override
    public int getCartItemCountForUser(int userId) {
        // Cette méthode retourne le NOMBRE D'ARTICLES DISTINCTS
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return 0;
        }
        
        User user = userOpt.get();
        Optional<Cart> cartOpt = cartRepository.findActiveCartWithItems(userId);
        
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            return cart.getCartItems() != null ? cart.getCartItems().size() : 0;
        }
        
        return 0;
    }
    
    // NOUVELLE MÉTHODE : Retourne la QUANTITÉ TOTALE (somme des quantités)
    @Override
    public int getCartTotalQuantityForUser(int userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return 0;
            }
            
            User user = userOpt.get();
            Optional<Cart> cartOpt = cartRepository.findActiveCartWithItems(userId);
            
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                    // Somme des quantités de tous les articles
                    return cart.getCartItems().stream()
                            .mapToInt(CartItem::getQuantity)
                            .sum();
                }
            }
            
            return 0;
            
        } catch (Exception e) {
            System.err.println("Erreur dans getCartTotalQuantityForUser: " + e.getMessage());
            return 0;
        }
    }
    
    @Override
    public boolean validateCart(Cart cart) {
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return false;
        }
        
        // Vérifier que tous les produits sont encore disponibles
        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        dto.setStatus(cart.getStatus());
        dto.setTotal(cart.getTotal());
        
        if (cart.getUser() != null) {
            dto.setUserId(cart.getUser().getId());
            dto.setUserFullName(cart.getUser().getFullName());
        }
        
        if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            List<CartItemDTO> itemDTOs = cart.getCartItems().stream()
                    .map(this::convertItemToDTO)
                    .collect(Collectors.toList());
            dto.setCartItems(itemDTOs);
        } else {
            dto.setCartItems(new ArrayList<>());
        }
        
        return dto;
    }
    
    private CartItemDTO convertItemToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());
        
        if (item.getCart() != null) {
            dto.setCartId(item.getCart().getId());
        }
        
        if (item.getProduct() != null) {
            Product product = item.getProduct();
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setProductPrice(product.getPrice());
            dto.setProductImage(product.getImageUrl());
            
            if (product.getCategory() != null) {
                dto.setCategoryName(product.getCategory().getName());
            } else {
                dto.setCategoryName("Non catégorisé");
            }
        }
        
        return dto;
    }
    
    @Override
    public CartDTO addToCartForUser(int userId, int productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return addToCart(user, productId, quantity);
    }
    @Override
    public CartDTO updateQuantityForUser(int userId, int productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return updateQuantity(user, productId, quantity);
    }
    @Override
    public CartDTO removeFromCartForUser(int userId, int productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return removeFromCart(user, productId);
    }
    @Override
    public CartDTO getCartForUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return convertToDTO(getActiveCartWithItems(user));
    }
    
    // =====================================================================
    // FUSION : panier visiteur (localStorage) → panier utilisateur (BDD)
    // Appelée juste après la connexion, depuis le JavaScript de la page login
    // =====================================================================
    @Override
    public CartDTO mergeGuestCartAndGet(int userId, List<Map<String, Integer>> guestItems) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if (guestItems != null && !guestItems.isEmpty()) {
            for (Map<String, Integer> item : guestItems) {
                Integer productId = item.get("productId");
                Integer quantity  = item.get("quantity");
                if (productId != null && quantity != null && quantity > 0) {
                    try {
                        addToCart(user, productId, quantity);
                    } catch (RuntimeException e) {
                        System.err.println("Fusion ignorée pour produit " + productId + ": " + e.getMessage());
                    }
                }
            }
        }
        return convertToDTO(getActiveCartWithItems(user));
    }
}
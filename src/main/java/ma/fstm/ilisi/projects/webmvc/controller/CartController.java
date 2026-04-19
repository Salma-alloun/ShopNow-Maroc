package ma.fstm.ilisi.projects.webmvc.controller;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ma.fstm.ilisi.projects.webmvc.dto.UserDTO;
import ma.fstm.ilisi.projects.webmvc.dto.AddToCartRequest;
import ma.fstm.ilisi.projects.webmvc.dto.CartDTO;
import ma.fstm.ilisi.projects.webmvc.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ma.fstm.ilisi.projects.webmvc.dto.CartItemDTO;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
@Controller
@RequestMapping("/panier")
public class CartController {
    @Autowired
    private CartService cartService;
    // Méthode utilitaire pour calculer la quantité totale
    private int calculateTotalQuantity(CartDTO cart) {
        if (cart == null || cart.getCartItems() == null) return 0;
        return cart.getCartItems().stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();
    }
    // =====================================================================
    // FUSION : panier visiteur (localStorage) → panier utilisateur (BDD)
    // Appelé en AJAX juste après la connexion, avant la redirection
    // Body attendu : [{ "productId": 1, "quantity": 2 }, ...]
    // =====================================================================
    @PostMapping("/fusionner")
    @ResponseBody
    public ResponseEntity<?> mergeGuestCart(
            @RequestBody List<Map<String, Integer>> guestItems,
            HttpSession session) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Non connecté"));
            }
            CartDTO cart = cartService.mergeGuestCartAndGet(user.getId(), guestItems);
            int totalQuantity = calculateTotalQuantity(cart);
            session.setAttribute("cartCount", totalQuantity);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cartCount", totalQuantity,
                "cartTotal", cart.getTotal()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    // AJAX: Ajouter au panier
    @PostMapping("/ajouter")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, HttpSession session) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Veuillez vous connecter pour ajouter au panier");
                response.put("redirect", "/login");
                response.put("visitor", true);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            CartDTO cart = cartService.addToCartForUser(user.getId(), request.getProductId(), request.getQuantity());
            int totalQuantity = calculateTotalQuantity(cart);
            session.setAttribute("cartCount", totalQuantity);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Produit ajouté au panier avec succès");
            response.put("cartCount", totalQuantity);
            response.put("cartTotal", cart.getTotal());
            return ResponseEntity.ok(response);
        } catch (ClassCastException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur de session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de l'ajout au panier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // AJAX: Mettre à jour la quantité
    @PostMapping("/mettre-a-jour")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(@RequestBody AddToCartRequest request, HttpSession session) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Veuillez vous connecter"));
            }
            CartDTO cart = cartService.updateQuantityForUser(user.getId(), request.getProductId(), request.getQuantity());
            int totalQuantity = calculateTotalQuantity(cart);
            session.setAttribute("cartCount", totalQuantity);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cartCount", totalQuantity,
                "cartTotal", cart.getTotal()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    // AJAX: Retirer du panier
    @DeleteMapping("/retirer/{productId}")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable int productId, HttpSession session) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Veuillez vous connecter"));
            }
            CartDTO cart = cartService.removeFromCartForUser(user.getId(), productId);
            int totalQuantity = calculateTotalQuantity(cart);
            session.setAttribute("cartCount", totalQuantity);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cartCount", totalQuantity,
                "cartTotal", cart.getTotal()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    // AJAX: Obtenir le nombre d'items (QUANTITÉ TOTALE)
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> getCartCount(HttpSession session) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.ok(Map.of("count", 0));
            }
            int totalQuantity = cartService.getCartTotalQuantityForUser(user.getId());
            session.setAttribute("cartCount", totalQuantity);
            return ResponseEntity.ok(Map.of("count", totalQuantity));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
    }
    // Page du panier
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) {
                CartDTO emptyCart = new CartDTO();
                emptyCart.setCartItems(new ArrayList<>());
                emptyCart.setTotal(0.0);
                model.addAttribute("cart", emptyCart);
                return "panier";
            }
            CartDTO cart = cartService.getCartForUser(user.getId());
            int totalQuantity = calculateTotalQuantity(cart);
            session.setAttribute("cartCount", totalQuantity);
            if (cart.getCartItems() == null) {
                cart.setCartItems(new ArrayList<>());
            }
            model.addAttribute("cart", cart);
            return "panier";
        } catch (Exception e) {
            CartDTO emptyCart = new CartDTO();
            emptyCart.setCartItems(new ArrayList<>());
            emptyCart.setTotal(0.0);
            model.addAttribute("cart", emptyCart);
            model.addAttribute("error", "Erreur lors du chargement du panier: " + e.getMessage());
            return "panier";
        }
    }
    // Endpoint AJAX pour obtenir les données du panier
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<?> getCartData(HttpSession session) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.ok(Map.of("cartItems", new ArrayList<>(), "total", 0.0));
            }
            CartDTO cart = cartService.getCartForUser(user.getId());
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/debug")
    @ResponseBody
    public ResponseEntity<?> debugCart(HttpSession session) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.ok(Map.of("message", "Non connecté", "cart", new CartDTO()));
            }
            CartDTO cart = cartService.getCartForUser(user.getId());
            int totalQuantity = calculateTotalQuantity(cart);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("cartId", cart.getId());
            response.put("cartItems", cart.getCartItems());
            response.put("total", cart.getTotal());
            response.put("totalQuantity", totalQuantity);
            response.put("sessionCartCount", session.getAttribute("cartCount"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
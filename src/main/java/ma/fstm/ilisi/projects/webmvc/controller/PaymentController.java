package ma.fstm.ilisi.projects.webmvc.controller;
import ma.fstm.ilisi.projects.webmvc.dto.OrderDTO;
import ma.fstm.ilisi.projects.webmvc.dto.UserDTO;
import ma.fstm.ilisi.projects.webmvc.service.OrderService;
import ma.fstm.ilisi.projects.webmvc.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.Map;
import ma.fstm.ilisi.projects.webmvc.AppConfig;
@Controller
@RequestMapping("/paiement")
public class PaymentController {
	private String stripePublicKey = AppConfig.STRIPE_PUBLIC_KEY;
    @Autowired private OrderService   orderService;
    @Autowired private PaymentService paymentService;
    // ── Page de paiement ─────────────────────────────────────────────────────
    @GetMapping("/{orderId}")
    public String showPaymentPage(@PathVariable int orderId,
                                  HttpSession session,
                                  Model model) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) return "redirect:/users/login";
        try {
            OrderDTO order = orderService.getOrderById(orderId);
            // Sécurité : la commande appartient au user connecté
            if (order.getUserId() != user.getId()) {
                return "redirect:/panier";
            }
            // Si déjà payée, aller directement à la confirmation
            if ("PAID".equals(order.getStatus())) {
                return "redirect:/commande/confirmation/" + orderId;
            }
            // Si pas PENDING, retour au panier
            if (!"PENDING".equals(order.getStatus())) {
                return "redirect:/panier";
            }
            model.addAttribute("order", order);
            model.addAttribute("stripePublicKey", stripePublicKey);
            return "paiement";
        } catch (Exception e) {
            return "redirect:/panier";
        }
    }
    // ── AJAX : Créer le PaymentIntent Stripe ─────────────────────────────────
    @PostMapping("/creer-intent/{orderId}")
    @ResponseBody
    public ResponseEntity<?> createPaymentIntent(@PathVariable int orderId,
                                                  HttpSession session) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Non connecté"));
            String clientSecret = paymentService.createPaymentIntent(orderId, user.getId());
            return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
        } catch (Exception e) {
            // Afficher l'erreur complète dans la console
            e.printStackTrace();
            // ET retourner le message au navigateur pour déboguer
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage(), 
                                 "cause", e.getCause() != null ? e.getCause().getMessage() : "null"));
        }
    }
    // ── AJAX : Confirmer le paiement après succès Stripe ─────────────────────
    @PostMapping("/confirmer/{orderId}")
    @ResponseBody
    public ResponseEntity<?> confirmPayment(@PathVariable int orderId,
                                             @RequestBody Map<String, String> body,
                                             HttpSession session) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Non connecté"));
            }
            String paymentIntentId = body.get("paymentIntentId");
            if (paymentIntentId == null || paymentIntentId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "PaymentIntent ID manquant"));
            }
            paymentService.confirmPayment(orderId, paymentIntentId);
            // Mettre à jour le compteur panier dans la session
            session.setAttribute("cartCount", 0);
            return ResponseEntity.ok(Map.of(
                "success",     true,
                "redirectUrl", "/commande/confirmation/" + orderId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
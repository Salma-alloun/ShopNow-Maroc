package ma.fstm.ilisi.projects.webmvc.controller;
import ma.fstm.ilisi.projects.webmvc.dto.OrderDTO;
import ma.fstm.ilisi.projects.webmvc.dto.UserDTO;
import ma.fstm.ilisi.projects.webmvc.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpSession;
@Controller
@RequestMapping("/commande")
public class OrderController {
    @Autowired
    private OrderService orderService;
    // ── Étape 1 : Créer la commande depuis le panier ─────────────────────────
    // Appelé quand le client clique sur "Procéder au paiement"
    @PostMapping("/creer")
    public String createOrder(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) {
                // Sauvegarder l'URL pour rediriger après login
                session.setAttribute("redirectAfterLogin", "/panier");
                return "redirect:/users/login";
            }
            // Créer la commande → remplit orders + order_items
            OrderDTO order = orderService.createOrderFromCart(user.getId());
            // Mettre en session pour l'étape Stripe
            session.setAttribute("pendingOrderId", order.getId());
            session.setAttribute("cartCount", 0);
            // → Étape 2 : page de paiement Stripe
            return "redirect:/paiement/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/panier";
        }
    }
    // ── Mes commandes ─────────────────────────────────────────────────────────
    @GetMapping("/mes-commandes")
    public String mesCommandes(HttpSession session, Model model) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) return "redirect:/users/login";
        model.addAttribute("commandes", orderService.getOrdersByUser(user.getId()));
        return "mes-commandes";
    }
    // ── Détail d'une commande ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public String detailCommande(@PathVariable int id, HttpSession session, Model model) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) return "redirect:/users/login";
        OrderDTO order = orderService.getOrderById(id);
        // Sécurité : un user ne peut voir que ses propres commandes
        if (order.getUserId() != user.getId() && !"ADMIN".equals(user.getRole())) {
            return "redirect:/commande/mes-commandes";
        }
        model.addAttribute("commande", order);
        return "detail-commande";
    }
    
    @GetMapping("/confirmation/{orderId}")
    public String confirmation(@PathVariable int orderId,
                               HttpSession session,
                               Model model) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) return "redirect:/users/login";
        OrderDTO order = orderService.getOrderById(orderId);
        if (order.getUserId() != user.getId()) return "redirect:/";
        model.addAttribute("commande", order);
        return "confirmation";
    }
}
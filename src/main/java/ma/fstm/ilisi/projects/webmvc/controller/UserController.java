package ma.fstm.ilisi.projects.webmvc.controller;
import ma.fstm.ilisi.projects.webmvc.dto.UserDTO;
import ma.fstm.ilisi.projects.webmvc.dto.AddressDTO;
import ma.fstm.ilisi.projects.webmvc.dto.LoginDTO;
import ma.fstm.ilisi.projects.webmvc.dto.RegisterDTO;
import ma.fstm.ilisi.projects.webmvc.service.UserService;
import ma.fstm.ilisi.projects.webmvc.service.AddressService;
import ma.fstm.ilisi.projects.webmvc.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpSession;
import java.util.List;
@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private CartService cartService;
    // ==================== PAGE DE CONNEXION ====================
    
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "login";
    }
    // ==================== TRAITEMENT DE LA CONNEXION ====================
    
    @PostMapping("/login")
    public String processLogin(@ModelAttribute LoginDTO loginDTO, 
                               HttpSession session, 
                               RedirectAttributes redirectAttributes) {
        try {
            UserDTO user = userService.authenticate(loginDTO);
            if (user != null) {
                session.setAttribute("user", user);
                
                int totalQuantity = cartService.getCartTotalQuantityForUser(user.getId());
                session.setAttribute("cartCount", totalQuantity);
                
                System.out.println("Utilisateur connecté - Quantité totale dans panier: " + totalQuantity);
                
                String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
                session.removeAttribute("redirectAfterLogin");
                
                if ("ADMIN".equals(user.getRole())) {
                    return "redirect:/users/admin/dashboard";
                } else if (redirectUrl != null && !redirectUrl.isEmpty()) {
                    return "redirect:" + redirectUrl;
                } else {
                    return "redirect:/catalogue";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Email ou mot de passe incorrect");
                return "redirect:/users/login?error=true";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur de connexion: " + e.getMessage());
            return "redirect:/users/login?error=true";
        }
    }
    // ==================== PAGE D'INSCRIPTION ====================
    
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register";
    }
    @PostMapping("/register")
    public String processRegister(@ModelAttribute RegisterDTO registerDTO,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas");
                return "redirect:/users/register";
            }
            
            UserDTO newUser = userService.register(registerDTO);
            session.setAttribute("user", newUser);
            session.setAttribute("cartCount", 0);
            
            redirectAttributes.addFlashAttribute("success", "Inscription réussie ! Bienvenue !");
            return "redirect:/catalogue";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/register";
        }
    }
    // ==================== DÉCONNEXION ====================
    
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Déconnexion réussie");
        return "redirect:/";
    }
    // ==================== PAGE DE PROFIL ====================
    
    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, Model model) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) return "redirect:/users/login";
        
        UserDTO updatedUser = userService.getUserById(user.getId());
        model.addAttribute("user", updatedUser);
        return "profile";
    }
    // ==================== MISE À JOUR DU PROFIL ====================
    
@PostMapping("/profile/update")
public String updateProfile(@RequestParam String firstName,
                            @RequestParam String lastName,
                            @RequestParam String email,
                            @RequestParam(required = false) String city,
                            @RequestParam(required = false) String postalCode,
                            @RequestParam(required = false) String country,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
    try {
        // Vérifier que l'utilisateur est connecté
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null) return "redirect:/users/login";
        // Construire le DTO avec les nouvelles valeurs
        UserDTO userDTO = new UserDTO();
        userDTO.setId(currentUser.getId());
        userDTO.setFirstName(firstName);
        userDTO.setLastName(lastName);
        userDTO.setEmail(email);
        userDTO.setRole(currentUser.getRole());
        userDTO.setActive(currentUser.isActive());
        // Adresse si fournie
        if (city != null || postalCode != null || country != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setCity(city);
            addressDTO.setPostalCode(postalCode);
            addressDTO.setCountry(country);
            userDTO.setAddress(addressDTO);
        }
        // Sauvegarder et mettre à jour la session
        UserDTO updated = userService.updateUser(userDTO);
        session.setAttribute("user", updated);
        redirectAttributes.addFlashAttribute("success", "Profil mis à jour !");
        return "redirect:/users/profile";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        return "redirect:/users/profile";
    }
}
    // ==================== ADMINISTRATION ====================
    
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/users/login";
        }
        
        model.addAttribute("totalUsers", userService.getTotalUsers());
        model.addAttribute("totalAdmins", userService.countAdmins());
        model.addAttribute("totalClients", userService.countClients());
        model.addAttribute("totalVisitors", userService.countVisitors());
        model.addAttribute("activeUsers", userService.getActiveUsers().size());
        model.addAttribute("inactiveUsers", userService.getInactiveUsers().size());
        
        return "admin/dashboard";
    }
    @GetMapping("/admin/list")
    public String listUsers(HttpSession session, Model model) {
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) return "redirect:/users/login";
        
        List<UserDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user-list";
    }
    @GetMapping("/admin/edit/{id}")
    public String editUser(@PathVariable int id, HttpSession session, Model model) {
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) return "redirect:/users/login";
        
        model.addAttribute("user", userService.getUserById(id));
        return "admin/user-edit";
    }
    @PostMapping("/admin/update")
    public String adminUpdateUser(@ModelAttribute UserDTO userDTO,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            UserDTO currentUser = (UserDTO) session.getAttribute("user");
            if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) return "redirect:/users/login";
            
            userService.updateUser(userDTO);
            redirectAttributes.addFlashAttribute("success", "Utilisateur mis à jour !");
            return "redirect:/users/admin/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/admin/edit/" + userDTO.getId();
        }
    }
    @GetMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable int id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            UserDTO currentUser = (UserDTO) session.getAttribute("user");
            if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) return "redirect:/users/login";
            
            if (currentUser.getId() == id) {
                redirectAttributes.addFlashAttribute("error", "Vous ne pouvez pas supprimer votre propre compte !");
                return "redirect:/users/admin/list";
            }
            
            boolean deleted = userService.deleteUser(id);
            if (deleted) redirectAttributes.addFlashAttribute("success", "Utilisateur supprimé !");
            return "redirect:/users/admin/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/admin/list";
        }
    }
    @GetMapping("/admin/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable int id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            UserDTO currentUser = (UserDTO) session.getAttribute("user");
            if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) return "redirect:/users/login";
            
            UserDTO user = userService.getUserById(id);
            if (user.isActive()) {
                userService.lockUser(id);
                redirectAttributes.addFlashAttribute("success", "Utilisateur désactivé !");
            } else {
                userService.unlockUser(id);
                redirectAttributes.addFlashAttribute("success", "Utilisateur activé !");
            }
            return "redirect:/users/admin/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/admin/list";
        }
    }
    @GetMapping("/admin/create")
    public String showCreateUserPage(HttpSession session, Model model) {
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null || !currentUser.isAdmin()) return "redirect:/users/login";
        return "admin/user-create";
    }
    @PostMapping("/admin/create")
    public String processCreateUser(@RequestParam String firstName,
                                    @RequestParam String lastName,
                                    @RequestParam String email,
                                    @RequestParam String password,
                                    @RequestParam String confirmPassword,
                                    @RequestParam(defaultValue = "CLIENT") String role,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            UserDTO currentUser = (UserDTO) session.getAttribute("user");
            if (currentUser == null || !currentUser.isAdmin()) return "redirect:/users/login";
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas !");
                return "redirect:/users/admin/create";
            }
            if ("ADMIN".equals(role) && !userService.isSuperAdmin(currentUser)) {
                redirectAttributes.addFlashAttribute("error", "Seul le Super Admin peut créer un administrateur !");
                return "redirect:/users/admin/create";
            }
            userService.createUserByAdmin(firstName, lastName, email, password, role);
            redirectAttributes.addFlashAttribute("success", "Utilisateur créé avec succès !");
            return "redirect:/users/admin/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/admin/create";
        }
    }
    
    
    @GetMapping("/setup")
    @ResponseBody
    public String setupAdmin() {
        userService.createDefaultAdmin();
        return "Admin créé !";
    }
    
 // ==================== PAGE MODIFICATION PROFIL ====================
    @GetMapping("/profile/edit")
    public String showEditProfilePage(HttpSession session, Model model) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "profile-edit";
    }
    
    
}
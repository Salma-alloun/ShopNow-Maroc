package ma.fstm.ilisi.projects.webmvc.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/index")
    public String index() {
        return "index";
    }
    
    // AJOUTEZ CES MÉTHODES
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    // Pages statiques
    @GetMapping("/favoris")
    public String favoris() {
        return "favoris";
    }
    
    @GetMapping("/profil")
    public String profil() {
        return "profil";
    }
    
    @GetMapping("/commandes")
    public String commandes() {
        return "commandes";
    }
    
    @GetMapping("/promotions") 
    public String promotions() { 
        return "promotions"; 
    }
    
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
package ma.fstm.ilisi.projects.webmvc.controller;
import ma.fstm.ilisi.projects.webmvc.dto.CategoryDTO;
import ma.fstm.ilisi.projects.webmvc.dto.UserDTO;
import ma.fstm.ilisi.projects.webmvc.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpSession;
import java.util.List;
@Controller
@RequestMapping("/admin/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    // ==================== PAGE PRINCIPALE ====================
    
    @GetMapping
    public String showCategoriesPage(HttpSession session, Model model) {
        // Vérifier si l'utilisateur est admin
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/users/login";
        }
        
        List<CategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        
        return "admin/category";
    }
    // ==================== API REST POUR LES REQUÊTES AJAX ====================
    
    @GetMapping("/{id}")
    @ResponseBody
    public CategoryDTO getCategory(@PathVariable int id) {
        return categoryService.getCategoryById(id);
    }
    // ==================== AJOUTER UNE CATÉGORIE ====================
    
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute CategoryDTO categoryDTO,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        // Vérifier si l'utilisateur est admin
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/users/login";
        }
        
        try {
            if (categoryDTO.getId() > 0) {
                // Modification
                CategoryDTO updated = categoryService.updateCategory(categoryDTO);
                if (updated != null) {
                    redirectAttributes.addFlashAttribute("success", "Catégorie modifiée avec succès !");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Catégorie non trouvée !");
                }
            } else {
                // Création
                CategoryDTO created = categoryService.createCategory(categoryDTO);
                redirectAttributes.addFlashAttribute("success", "Catégorie créée avec succès !");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
    // ==================== SUPPRIMER UNE CATÉGORIE ====================
    
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable int id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        // Vérifier si l'utilisateur est admin
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/users/login";
        }
        
        try {
            boolean deleted = categoryService.deleteCategory(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Catégorie supprimée avec succès !");
            } else {
                redirectAttributes.addFlashAttribute("error", "Catégorie non trouvée !");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer cette catégorie : " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
}
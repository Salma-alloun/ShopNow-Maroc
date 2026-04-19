package ma.fstm.ilisi.projects.webmvc.controller;
import ma.fstm.ilisi.projects.webmvc.dto.ProductDTO;
import ma.fstm.ilisi.projects.webmvc.dto.UserDTO;
import ma.fstm.ilisi.projects.webmvc.service.ProductService;
import ma.fstm.ilisi.projects.webmvc.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
@Controller
@RequestMapping("/admin/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    // ✅ Chemin absolu vers src/main/resources/static/uploads/
    private String getUploadDir() {
        // Chemin relatif au répertoire de travail du projet
        String uploadDir = System.getProperty("user.dir")
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "resources"
                + File.separator + "static"
                + File.separator + "uploads"
                + File.separator;
        // Créer le dossier s'il n'existe pas
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return uploadDir;
    }
    @GetMapping
    public String showProductsPage(HttpSession session, Model model) {
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/users/login";
        }
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product";
    }
    @GetMapping("/{id}")
    @ResponseBody
    public ProductDTO getProduct(@PathVariable int id) {
        return productService.getProductById(id);
    }
    // ✅ SOLUTION : @ModelAttribute au lieu de @RequestParam
    //    Spring mappe automatiquement tous les champs du formulaire
    //    multipart vers le DTO sans problème
    @PostMapping("/save")
    public String saveProduct(
            @ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/users/login";
        }
        try {
            // ✅ Gestion image
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = UUID.randomUUID().toString()
                        + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get(getUploadDir());
                Files.copy(imageFile.getInputStream(),
                        uploadPath.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING);
                // ✅ Stocker UNIQUEMENT le nom du fichier
                productDTO.setImageUrl(fileName);
            }
            if (productDTO.getId() > 0) {
                productService.updateProduct(productDTO);
                redirectAttributes.addFlashAttribute("success",
                        "Produit modifié avec succès !");
            } else {
                productService.createProduct(productDTO);
                redirectAttributes.addFlashAttribute("success",
                        "Produit créé avec succès !");
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur upload image : " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable int id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/users/login";
        }
        try {
            if (productService.deleteProduct(id)) {
                redirectAttributes.addFlashAttribute("success", "Produit supprimé !");
            } else {
                redirectAttributes.addFlashAttribute("error", "Produit non trouvé !");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de supprimer : " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
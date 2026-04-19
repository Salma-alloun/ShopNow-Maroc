package ma.fstm.ilisi.projects.webmvc.controller;
import ma.fstm.ilisi.projects.webmvc.dto.UserDTO;
import ma.fstm.ilisi.projects.webmvc.dto.ProductDTO;
import ma.fstm.ilisi.projects.webmvc.dto.CategoryDTO;
import ma.fstm.ilisi.projects.webmvc.service.CartService;
import ma.fstm.ilisi.projects.webmvc.service.ProductService;
import ma.fstm.ilisi.projects.webmvc.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
@Controller
@RequestMapping("/catalogue")
public class CatalogueController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CartService cartService;
    
    // Nombre de produits par page
    private static final int PAGE_SIZE = 6;
    @GetMapping
    public String showCatalogue(
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String sort,
            Model model, 
            HttpSession session) {
        System.out.println("========== CatalogueController.showCatalogue ==========");
        System.out.println("Catégorie: " + categorie);
        System.out.println("Page: " + page);
        System.out.println("Tri: " + sort);
        
        // Récupérer tous les produits
        List<ProductDTO> allProducts = productService.getAllProducts();
        System.out.println("Nombre total de produits en base: " + allProducts.size());
        
        // Récupérer toutes les catégories depuis la base de données
        List<CategoryDTO> allCategories = categoryService.getAllCategories();
        List<String> categoryNames = allCategories.stream()
                .map(CategoryDTO::getName)
                .collect(Collectors.toList());
        System.out.println("Catégories trouvées: " + categoryNames);
        
        // Appliquer le filtre par catégorie si spécifié
        List<ProductDTO> filteredProducts = allProducts;
        if (categorie != null && !categorie.trim().isEmpty()) {
            filteredProducts = allProducts.stream()
                    .filter(p -> categorie.equals(p.getCategoryName()))
                    .collect(Collectors.toList());
            System.out.println("Produits après filtre catégorie '" + categorie + "': " + filteredProducts.size());
        }
        
        // Appliquer le tri
        if (sort != null && !sort.isEmpty()) {
            filteredProducts = sortProducts(filteredProducts, sort);
        }
        
        // Grouper les produits par catégorie avec pagination
        Map<String, List<ProductDTO>> productsByCategory = new HashMap<>();
        Map<String, Integer> totalPagesByCategory = new HashMap<>();
        Map<String, Integer> currentPageByCategory = new HashMap<>();
        
        // Si une catégorie spécifique est sélectionnée, ne grouper que celle-ci
        if (categorie != null && !categorie.trim().isEmpty()) {
            List<ProductDTO> categoryProducts = filteredProducts;
            
            // Pagination pour cette catégorie
            int currentPage = (page != null && page >= 0) ? page : 0;
            int totalProducts = categoryProducts.size();
            int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);
            
            // Extraire les produits de la page courante
            int start = currentPage * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, totalProducts);
            
            if (start < totalProducts) {
                List<ProductDTO> paginatedProducts = categoryProducts.subList(start, end);
                productsByCategory.put(categorie, paginatedProducts);
                totalPagesByCategory.put(categorie, totalPages);
                currentPageByCategory.put(categorie, currentPage);
            } else {
                productsByCategory.put(categorie, new ArrayList<>());
                totalPagesByCategory.put(categorie, totalPages);
                currentPageByCategory.put(categorie, currentPage);
            }
        } else {
            // Grouper toutes les catégories avec pagination
            Map<String, List<ProductDTO>> allProductsByCategory = filteredProducts.stream()
                    .collect(Collectors.groupingBy(
                        p -> p.getCategoryName() != null ? p.getCategoryName() : "Non catégorisé",
                        Collectors.toList()
                    ));
            
            // Pour chaque catégorie, appliquer la pagination
            for (Map.Entry<String, List<ProductDTO>> entry : allProductsByCategory.entrySet()) {
                String catName = entry.getKey();
                List<ProductDTO> catProducts = entry.getValue();
                
                int currentPage = (page != null && page >= 0) ? page : 0;
                int totalProducts = catProducts.size();
                int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);
                
                // Extraire les produits de la page courante
                int start = currentPage * PAGE_SIZE;
                int end = Math.min(start + PAGE_SIZE, totalProducts);
                
                if (start < totalProducts) {
                    List<ProductDTO> paginatedProducts = catProducts.subList(start, end);
                    productsByCategory.put(catName, paginatedProducts);
                } else {
                    productsByCategory.put(catName, new ArrayList<>());
                }
                
                totalPagesByCategory.put(catName, totalPages);
                currentPageByCategory.put(catName, currentPage);
            }
        }
        // Gestion de l'utilisateur connecté
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user != null) {
            try {
                int cartCount = cartService.getCartItemCountForUser(user.getId());
                session.setAttribute("cartCount", cartCount);
                System.out.println("Cart count pour l'utilisateur " + user.getEmail() + ": " + cartCount);
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération du panier: " + e.getMessage());
                session.setAttribute("cartCount", 0);
            }
        } else {
            session.setAttribute("cartCount", 0);
        }
        // Ajouter les attributs au modèle
        model.addAttribute("productsByCategory", productsByCategory);
        model.addAttribute("totalPagesByCategory", totalPagesByCategory);
        model.addAttribute("currentPageByCategory", currentPageByCategory);
        model.addAttribute("allCategories", categoryNames);
        model.addAttribute("totalProducts", filteredProducts.size());
        model.addAttribute("currentCategory", categorie);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", page != null ? page : 0);
        model.addAttribute("pageSize", PAGE_SIZE);
        System.out.println("Catégories avec produits: " + productsByCategory.keySet());
        System.out.println("==================================================");
        
        return "catalogue";
    }
    @GetMapping("/recherche")
    public String searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) Integer page,
            Model model,
            HttpSession session) {
        System.out.println("========== CatalogueController.searchProducts ==========");
        System.out.println("Mot-clé: " + keyword);
        System.out.println("Type de recherche: " + searchType);
        System.out.println("Page: " + page);
        List<ProductDTO> searchResults = new ArrayList<>();
        int totalResults = 0;
        int currentPage = (page != null && page >= 0) ? page : 0;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Récupérer tous les produits
            List<ProductDTO> allProducts = productService.getAllProducts();
            String searchLower = keyword.toLowerCase().trim();
            // Filtrer selon le type de recherche
            if (searchType == null || "all".equals(searchType)) {
                // Recherche dans nom, catégorie et description
                searchResults = allProducts.stream()
                        .filter(p -> p.getName().toLowerCase().contains(searchLower) ||
                                    (p.getCategoryName() != null && p.getCategoryName().toLowerCase().contains(searchLower)) ||
                                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchLower)))
                        .collect(Collectors.toList());
            } else if ("name".equals(searchType)) {
                // Recherche uniquement dans le nom
                searchResults = allProducts.stream()
                        .filter(p -> p.getName().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
            } else if ("category".equals(searchType)) {
                // Recherche uniquement dans la catégorie
                searchResults = allProducts.stream()
                        .filter(p -> p.getCategoryName() != null && 
                                    p.getCategoryName().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
            } else if ("description".equals(searchType)) {
                // Recherche uniquement dans la description
                searchResults = allProducts.stream()
                        .filter(p -> p.getDescription() != null && 
                                    p.getDescription().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
            }
            totalResults = searchResults.size();
            System.out.println("Résultats trouvés: " + totalResults);
            // Pagination des résultats
            int start = currentPage * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, totalResults);
            if (start < totalResults) {
                searchResults = searchResults.subList(start, end);
            } else {
                searchResults = new ArrayList<>();
            }
        }
        // Récupérer toutes les catégories pour les filtres
        List<CategoryDTO> allCategories = categoryService.getAllCategories();
        List<String> categoryNames = allCategories.stream()
                .map(CategoryDTO::getName)
                .collect(Collectors.toList());
        // Gestion de l'utilisateur connecté
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user != null) {
            try {
                int cartCount = cartService.getCartItemCountForUser(user.getId());
                session.setAttribute("cartCount", cartCount);
            } catch (Exception e) {
                session.setAttribute("cartCount", 0);
            }
        } else {
            session.setAttribute("cartCount", 0);
        }
        // Ajouter les attributs au modèle
        model.addAttribute("searchPerformed", true);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("totalSearchResults", totalResults);
        model.addAttribute("searchCurrentPage", currentPage);
        model.addAttribute("searchTotalPages", (int) Math.ceil((double) totalResults / PAGE_SIZE));
        model.addAttribute("allCategories", categoryNames);
        model.addAttribute("pageSize", PAGE_SIZE);
        System.out.println("==================================================");
        return "catalogue";
    }
    @GetMapping("/produit/{id}")
    public String showProductDetails(@PathVariable int id, Model model, HttpSession session) {
        ProductDTO product = productService.getProductById(id);
        
        if (product == null) {
            return "redirect:/catalogue";
        }
        // Produits similaires (même catégorie)
        List<ProductDTO> similarProducts = new ArrayList<>();
        if (product.getCategoryId() != null) {
            similarProducts = productService.getProductsByCategory(product.getCategoryId());
            // Exclure le produit courant
            similarProducts = similarProducts.stream()
                    .filter(p -> p.getId() != id)
                    .limit(4)
                    .collect(Collectors.toList());
        }
        // Gestion de l'utilisateur connecté
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user != null) {
            try {
                int cartCount = cartService.getCartItemCountForUser(user.getId());
                session.setAttribute("cartCount", cartCount);
            } catch (Exception e) {
                session.setAttribute("cartCount", 0);
            }
        } else {
            session.setAttribute("cartCount", 0);
        }
        model.addAttribute("product", product);
        model.addAttribute("similarProducts", similarProducts);
        
        return "produit-details";
    }
    // Méthode utilitaire pour le tri
    private List<ProductDTO> sortProducts(List<ProductDTO> products, String sort) {
        if (sort == null || sort.isEmpty()) {
            return products;
        }
        List<ProductDTO> sortedList = new ArrayList<>(products);
        
        switch (sort) {
            case "price-asc":
                sortedList.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
                break;
            case "price-desc":
                sortedList.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                break;
            case "name-asc":
                sortedList.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
                break;
            case "name-desc":
                sortedList.sort((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
                break;
            case "stock-asc":
                sortedList.sort((p1, p2) -> Integer.compare(p1.getStockQuantity(), p2.getStockQuantity()));
                break;
            case "stock-desc":
                sortedList.sort((p1, p2) -> Integer.compare(p2.getStockQuantity(), p1.getStockQuantity()));
                break;
            default:
                // Tri par défaut (par ID ou date d'ajout)
                sortedList.sort((p1, p2) -> Integer.compare(p2.getId(), p1.getId()));
                break;
        }
        
        return sortedList;
    }
}
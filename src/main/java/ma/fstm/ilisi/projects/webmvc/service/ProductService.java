package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.bo.Product;
import ma.fstm.ilisi.projects.webmvc.bo.Category;
import ma.fstm.ilisi.projects.webmvc.dto.ProductDTO;
import ma.fstm.ilisi.projects.webmvc.repository.ProductRepository;
import ma.fstm.ilisi.projects.webmvc.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@Transactional
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    // ==================== CONVERSION METHODS ====================
    
    public ProductDTO toProductDTO(Product product) {
        if (product == null) return null;
        
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        return dto;
    }
    public Product toProduct(ProductDTO dto) {
        if (dto == null) return null;
        
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());  // Vérifiez que cette ligne est bien exécutée
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrl(dto.getImageUrl());
        product.setCreatedAt(dto.getCreatedAt());
        product.setUpdatedAt(dto.getUpdatedAt());
        
        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId()).ifPresent(product::setCategory);
        }
        
        return product;
    }
    // ==================== CRUD METHODS ====================
    
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
    }
    public ProductDTO getProductById(int id) {
        return productRepository.findById(id)
                .map(this::toProductDTO)
                .orElse(null);
    }
    public ProductDTO createProduct(ProductDTO productDTO) {
        System.out.println("========== ProductService.createProduct ==========");
        System.out.println("DTO reçu - ID: " + productDTO.getId());
        System.out.println("DTO reçu - Nom: '" + productDTO.getName() + "'");
        System.out.println("DTO reçu - Prix: " + productDTO.getPrice());
        System.out.println("DTO reçu - Stock: " + productDTO.getStockQuantity());
        System.out.println("DTO reçu - CategoryId: " + productDTO.getCategoryId());
        
        // Vérification explicite
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new RuntimeException("Le nom du produit ne peut pas être null");
        }
        
        Product product = toProduct(productDTO);
        System.out.println("Entity après conversion - Nom: '" + product.getName() + "'");
        
        product.setCreatedAt(new Date());
        product.setUpdatedAt(new Date());
        
        Product savedProduct = productRepository.save(product);
        System.out.println("✅ Produit sauvegardé avec ID: " + savedProduct.getId());
        System.out.println("==================================================");
        
        return toProductDTO(savedProduct);
    }
    public ProductDTO updateProduct(ProductDTO productDTO) {
        Optional<Product> productOpt = productRepository.findById(productDTO.getId());
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            product.setStockQuantity(productDTO.getStockQuantity());
            if (productDTO.getImageUrl() != null) {
                product.setImageUrl(productDTO.getImageUrl());
            }
            product.setUpdatedAt(new Date());
            
            if (productDTO.getCategoryId() != null) {
                categoryRepository.findById(productDTO.getCategoryId()).ifPresent(product::setCategory);
            } else {
                product.setCategory(null);
            }
            
            Product updatedProduct = productRepository.save(product);
            return toProductDTO(updatedProduct);
        }
        
        return null;
    }
    public boolean deleteProduct(int id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
    // ==================== SEARCH METHODS ====================
    
    public List<ProductDTO> searchProducts(String name, Integer categoryId, 
                                           Double minPrice, Double maxPrice, Boolean inStock) {
        return productRepository.searchProducts(name, categoryId, minPrice, maxPrice, inStock).stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
    }
    
 // Ajoutez ces méthodes à votre ProductService existant
    public List<ProductDTO> getProductsWithFilters(String keyword, Integer categoryId, Double maxPrice) {
        List<ProductDTO> allProducts = getAllProducts();
        
        return allProducts.stream()
                .filter(product -> keyword == null || 
                        product.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        (product.getDescription() != null && product.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .filter(product -> categoryId == null || 
                        (product.getCategoryId() != null && product.getCategoryId().equals(categoryId)))
                .filter(product -> maxPrice == null || product.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }
    // Méthode avec pagination
    public List<ProductDTO> getProductsPaginated(List<ProductDTO> products, int page, int size) {
        int start = page * size;
        int end = Math.min(start + size, products.size());
        
        if (start >= products.size()) {
            return List.of();
        }
        
        return products.subList(start, end);
    }
    
    
 // Dans votre ProductService.java existant, ajoutez ces méthodes :
    public List<ProductDTO> searchProducts(String keyword, Integer categoryId, Double maxPrice) {
        List<ProductDTO> allProducts = getAllProducts();
        
        return allProducts.stream()
                .filter(product -> keyword == null || 
                        product.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        (product.getDescription() != null && product.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .filter(product -> categoryId == null || 
                        (product.getCategoryId() != null && product.getCategoryId().equals(categoryId)))
                .filter(product -> maxPrice == null || product.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }
    public List<ProductDTO> getProductsByCategory(int categoryId) {
        return getAllProducts().stream()
                .filter(product -> product.getCategoryId() != null && product.getCategoryId() == categoryId)
                .collect(Collectors.toList());
    }
    public List<ProductDTO> getNewProducts() {
        return getAllProducts().stream()
                .sorted((p1, p2) -> {
                    if (p1.getCreatedAt() == null) return 1;
                    if (p2.getCreatedAt() == null) return -1;
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                })
                .limit(10)
                .collect(Collectors.toList());
    } 
}
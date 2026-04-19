package ma.fstm.ilisi.projects.webmvc.repository;
import ma.fstm.ilisi.projects.webmvc.bo.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    // Recherche par nom (ignore casse)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Recherche par catégorie
    List<Product> findByCategoryId(int categoryId);
    
    // Recherche par prix entre min et max
    List<Product> findByPriceBetween(double minPrice, double maxPrice);
    
    // Recherche par stock
    List<Product> findByStockQuantityLessThan(int threshold);
    
    // Recherche par stock > 0 (produits disponibles)
    List<Product> findByStockQuantityGreaterThan(int minStock);
    
    // Recherche avancée multi-critères
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:inStock IS NULL OR (:inStock = true AND p.stockQuantity > 0) OR (:inStock = false AND p.stockQuantity = 0))")
    List<Product> searchProducts(@Param("name") String name,
                                 @Param("categoryId") Integer categoryId,
                                 @Param("minPrice") Double minPrice,
                                 @Param("maxPrice") Double maxPrice,
                                 @Param("inStock") Boolean inStock);
    
    // Compter les produits par catégorie
    @Query("SELECT p.category.name, COUNT(p) FROM Product p GROUP BY p.category.name")
    List<Object[]> countProductsByCategory();
    
    // Produits les plus vendus (via orderItems)
    @Query("SELECT p, SUM(oi.quantity) as totalSold FROM Product p JOIN p.orderItems oi GROUP BY p.id ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts();
    
    // Produits en rupture de stock
    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();
    
    // Produits avec stock faible (< seuil)
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
}
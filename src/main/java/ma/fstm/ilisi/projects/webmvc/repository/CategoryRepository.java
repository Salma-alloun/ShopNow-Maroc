package ma.fstm.ilisi.projects.webmvc.repository;
import ma.fstm.ilisi.projects.webmvc.bo.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    // Recherche par nom (exact)
    Optional<Category> findByName(String name);
    
    // Recherche par nom (ignore casse)
    Optional<Category> findByNameIgnoreCase(String name);
    
    // Vérifier si une catégorie existe par nom
    boolean existsByName(String name);
    
    // Recherche par nom contenant (ignore casse)
    List<Category> findByNameContainingIgnoreCase(String name);
    
    // Recherche avancée multi-critères
    @Query("SELECT c FROM Category c WHERE " +
           "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:description IS NULL OR LOWER(c.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    List<Category> searchCategories(@Param("name") String name,
                                    @Param("description") String description);
    
    // Compter le nombre de produits par catégorie
    @Query("SELECT c.name, COUNT(p) FROM Category c LEFT JOIN c.products p GROUP BY c.id, c.name")
    List<Object[]> countProductsByCategory();
    
    // Trouver les catégories avec au moins un produit
    @Query("SELECT c FROM Category c WHERE c.products IS NOT EMPTY")
    List<Category> findCategoriesWithProducts();
    
    // Trouver les catégories sans produit
    @Query("SELECT c FROM Category c WHERE c.products IS EMPTY")
    List<Category> findCategoriesWithoutProducts();
}
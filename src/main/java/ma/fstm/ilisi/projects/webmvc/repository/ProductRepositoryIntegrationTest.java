package ma.fstm.ilisi.projects.webmvc.repository;
import ma.fstm.ilisi.projects.webmvc.bo.Category;
import ma.fstm.ilisi.projects.webmvc.bo.Product;
import ma.fstm.ilisi.projects.webmvc.TestRepositoryConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
@Transactional
public class ProductRepositoryIntegrationTest {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ProductRepository productRepository;
    private Category category1;
    private Category category2;
    private Product product1;
    private Product product2;
    private Product product3;
    @Before
    public void setUp() {
        // Catégorie 1 (Électronique)
        category1 = new Category();
        category1.setName("Électronique");
        category1.setDescription("Produits électroniques");
        entityManager.persist(category1);
        entityManager.flush();
        // Catégorie 2 (Livres)
        category2 = new Category();
        category2.setName("Livres");
        category2.setDescription("Livres et magazines");
        entityManager.persist(category2);
        entityManager.flush();
        // Produit 1 (Smartphone, Électronique, 599.99 DH, stock 10)
        product1 = new Product();
        product1.setName("Smartphone X");
        product1.setDescription("Dernier modèle de smartphone");
        product1.setPrice(599.99);
        product1.setStockQuantity(10);
        product1.setCategory(category1);
        product1.setCreatedAt(new Date());
        product1.setUpdatedAt(new Date());
        product1.setImageUrl("/images/phone.jpg");
        entityManager.persist(product1);
        entityManager.flush();
        // Produit 2 (Tablette, Électronique, 299.99 DH, stock 5)
        product2 = new Product();
        product2.setName("Tablette Pro");
        product2.setDescription("Tablette haute performance");
        product2.setPrice(299.99);
        product2.setStockQuantity(5);
        product2.setCategory(category1);
        product2.setCreatedAt(new Date());
        product2.setUpdatedAt(new Date());
        product2.setImageUrl("/images/tablet.jpg");
        entityManager.persist(product2);
        entityManager.flush();
        // Produit 3 (Livre Java, Livres, 45.50 DH, stock 20)
        product3 = new Product();
        product3.setName("Java pour les nuls");
        product3.setDescription("Apprendre Java facilement");
        product3.setPrice(45.50);
        product3.setStockQuantity(20);
        product3.setCategory(category2);
        product3.setCreatedAt(new Date());
        product3.setUpdatedAt(new Date());
        product3.setImageUrl("/images/java-book.jpg");
        entityManager.persist(product3);
        entityManager.flush();
    }
    @Test
    public void testFindByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
        // When - Recherche par nom partiel
        List<Product> results = productRepository.findByNameContainingIgnoreCase("smart");
        // Then
        assertEquals("Un seul produit doit correspondre", 1, results.size());
        assertEquals("Le nom doit être Smartphone X", "Smartphone X", results.get(0).getName());
    }
    @Test
    public void testFindByNameContainingIgnoreCase_WithMultipleMatches_ShouldReturnAll() {
        // When - Recherche avec 'a' (présent dans plusieurs noms)
        List<Product> results = productRepository.findByNameContainingIgnoreCase("a");
        // Then - Devrait trouver tous les produits (tous ont un 'a' dans leur nom)
        assertEquals("Tous les produits doivent correspondre", 3, results.size());
    }
    @Test
    public void testFindByNameContainingIgnoreCase_WithNoMatch_ShouldReturnEmpty() {
        // When - Recherche avec un terme inexistant
        List<Product> results = productRepository.findByNameContainingIgnoreCase("xyz123");
        // Then
        assertTrue("Aucun produit ne doit être trouvé", results.isEmpty());
    }
    @Test
    public void testFindByCategoryId_ShouldReturnProductsFromCategory() {
        // When - Produits de la catégorie Électronique
        List<Product> electronics = productRepository.findByCategoryId(category1.getId());
        // Then
        assertEquals("Il doit y avoir 2 produits électroniques", 2, electronics.size());
        assertTrue("Les produits doivent inclure Smartphone X",
                electronics.stream().anyMatch(p -> p.getName().equals("Smartphone X")));
        assertTrue("Les produits doivent inclure Tablette Pro",
                electronics.stream().anyMatch(p -> p.getName().equals("Tablette Pro")));
    }
    @Test
    public void testFindByCategoryId_WithInvalidId_ShouldReturnEmpty() {
        // When
        List<Product> results = productRepository.findByCategoryId(9999);
        // Then
        assertTrue("Aucun produit ne doit être trouvé", results.isEmpty());
    }
    @Test
    public void testFindByPriceBetween_ShouldReturnProductsInPriceRange() {
        // When - Produits entre 200 et 600 DH
        List<Product> results = productRepository.findByPriceBetween(200.0, 600.0);
        // Then
        assertEquals("Il doit y avoir 2 produits dans cette fourchette", 2, results.size());
        assertTrue("Smartphone X (599.99) doit être inclus",
                results.stream().anyMatch(p -> p.getName().equals("Smartphone X")));
        assertTrue("Tablette Pro (299.99) doit être incluse",
                results.stream().anyMatch(p -> p.getName().equals("Tablette Pro")));
    }
    @Test
    public void testFindByPriceBetween_WithLowRange_ShouldReturnOnlyLowPricedProducts() {
        // When - Produits entre 0 et 100 DH
        List<Product> results = productRepository.findByPriceBetween(0.0, 100.0);
        // Then
        assertEquals("Un seul produit doit être dans cette fourchette", 1, results.size());
        assertEquals("Le livre Java doit être inclus", "Java pour les nuls", results.get(0).getName());
    }
    @Test
    public void testFindByStockQuantityLessThan_ShouldReturnLowStockProducts() {
        // When - Produits avec stock < 10
        List<Product> results = productRepository.findByStockQuantityLessThan(10);
        // Then
        assertEquals("Un seul produit a un stock < 10", 1, results.size());
        assertEquals("Tablette Pro (stock 5) doit être retournée", "Tablette Pro", results.get(0).getName());
    }
    @Test
    public void testFindByStockQuantityGreaterThan_ShouldReturnAvailableProducts() {
        // When - Produits avec stock > 0 (disponibles)
        List<Product> results = productRepository.findByStockQuantityGreaterThan(0);
        // Then
        assertEquals("Tous les produits ont un stock > 0", 3, results.size());
    }
    @Test
    public void testSearchProducts_WithMultipleCriteria_ShouldReturnFilteredProducts() {
        // When - Recherche de produits électroniques entre 200 et 600 DH
        List<Product> results = productRepository.searchProducts(
                null, category1.getId(), 200.0, 600.0, null);
        // Then
        assertEquals("2 produits électroniques dans cette fourchette", 2, results.size());
    }
    @Test
    public void testSearchProducts_WithNameAndCategory_ShouldReturnSpecificProduct() {
        // When - Recherche du smartphone
        List<Product> results = productRepository.searchProducts(
                "smartphone", category1.getId(), null, null, null);
        // Then
        assertEquals("Un seul produit doit correspondre", 1, results.size());
        assertEquals("Smartphone X", results.get(0).getName());
    }
    @Test
    public void testSearchProducts_WithInStockTrue_ShouldReturnAvailableProducts() {
        // When - Produits en stock (tous le sont)
        List<Product> results = productRepository.searchProducts(null, null, null, null, true);
        // Then
        assertEquals("Tous les produits sont en stock", 3, results.size());
    }
    @Test
    public void testSearchProducts_WithAllNull_ShouldReturnAllProducts() {
        // When - Tous les critères sont null
        List<Product> results = productRepository.searchProducts(null, null, null, null, null);
        // Then
        assertEquals("Tous les produits doivent être retournés", 3, results.size());
    }
    @Test
    public void testCountProductsByCategory_ShouldReturnCountPerCategory() {
        // When
        List<Object[]> counts = productRepository.countProductsByCategory();
        // Then
        assertEquals("Il doit y avoir 2 catégories", 2, counts.size());
        for (Object[] row : counts) {
            String categoryName = (String) row[0];
            Long count = (Long) row[1];
            if ("Électronique".equals(categoryName)) {
                assertEquals("2 produits en Électronique", Long.valueOf(2), count);
            } else if ("Livres".equals(categoryName)) {
                assertEquals("1 produit en Livres", Long.valueOf(1), count);
            }
        }
    }
    @Test
    public void testFindOutOfStockProducts_ShouldReturnEmpty() {
        // When - Tous les produits ont du stock
        List<Product> results = productRepository.findOutOfStockProducts();
        // Then
        assertTrue("Aucun produit en rupture de stock", results.isEmpty());
    }
    @Test
    public void testFindOutOfStockProducts_WhenProductOutOfStock_ShouldReturnIt() {
        // Given - Mettre un produit en rupture de stock
        product1.setStockQuantity(0);
        entityManager.merge(product1);
        entityManager.flush();
        // When
        List<Product> results = productRepository.findOutOfStockProducts();
        // Then
        assertEquals("Un produit en rupture de stock", 1, results.size());
        assertEquals("Smartphone X", results.get(0).getName());
    }
    @Test
    public void testFindLowStockProducts_ShouldReturnProductsBelowThreshold() {
        // When - Seuil de stock bas = 10
        List<Product> results = productRepository.findLowStockProducts(10);
        // Then - Tablette Pro a stock 5 (< 10)
        assertEquals("Un produit a un stock < 10", 1, results.size());
        assertEquals("Tablette Pro", results.get(0).getName());
    }
    @Test
    public void testFindTopSellingProducts_ShouldReturnOrderedBySales() {
        // Note: Ce test nécessite des OrderItems, donc nous n'avons pas de données
        // On vérifie juste que la requête ne plante pas
        List<Object[]> results = productRepository.findTopSellingProducts();
        assertNotNull("La requête doit retourner un résultat (même vide)", results);
        // Si des données étaient présentes, on pourrait vérifier l'ordre
    }
}
package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.bo.Category;
import ma.fstm.ilisi.projects.webmvc.dto.CategoryDTO;
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
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    // ==================== CONVERSION METHODS ====================
    
    public CategoryDTO toCategoryDTO(Category category) {
        if (category == null) return null;
        
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        
        return dto;
    }
    public Category toCategory(CategoryDTO dto) {
        if (dto == null) return null;
        
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setCreatedAt(dto.getCreatedAt());
        
        return category;
    }
    // ==================== CRUD METHODS ====================
    
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
    }
    public CategoryDTO getCategoryById(int id) {
        return categoryRepository.findById(id)
                .map(this::toCategoryDTO)
                .orElse(null);
    }
    public CategoryDTO getCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .map(this::toCategoryDTO)
                .orElse(null);
    }
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Vérifier si une catégorie avec le même nom existe déjà
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà");
        }
        
        Category category = toCategory(categoryDTO);
        category.setCreatedAt(new Date());
        
        Category savedCategory = categoryRepository.save(category);
        return toCategoryDTO(savedCategory);
    }
    public CategoryDTO updateCategory(CategoryDTO categoryDTO) {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryDTO.getId());
        
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            
            // Vérifier si le nouveau nom n'est pas déjà utilisé par une autre catégorie
            if (!category.getName().equalsIgnoreCase(categoryDTO.getName()) &&
                categoryRepository.existsByName(categoryDTO.getName())) {
                throw new RuntimeException("Une catégorie avec ce nom existe déjà");
            }
            
            category.setName(categoryDTO.getName());
            category.setDescription(categoryDTO.getDescription());
            
            Category updatedCategory = categoryRepository.save(category);
            return toCategoryDTO(updatedCategory);
        }
        
        return null;
    }
    public boolean deleteCategory(int id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
    // ==================== SEARCH METHODS ====================
    
    public List<CategoryDTO> searchCategories(String name, String description) {
        return categoryRepository.searchCategories(name, description).stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
    }
    public List<CategoryDTO> getCategoriesByNameContaining(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
    }
    // ==================== STATISTICS METHODS ====================
    
    public List<Object[]> countProductsByCategory() {
        return categoryRepository.countProductsByCategory();
    }
    public List<CategoryDTO> getCategoriesWithProducts() {
        return categoryRepository.findCategoriesWithProducts().stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
    }
    public List<CategoryDTO> getCategoriesWithoutProducts() {
        return categoryRepository.findCategoriesWithoutProducts().stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
    }
    public long getTotalCategories() {
        return categoryRepository.count();
    }
}
package ma.fstm.ilisi.projects.webmvc.dto;
import java.util.Date;
import java.util.List;
public class CategoryDTO {
    
    private int id;
    private String name;
    private String description;
    private Date createdAt;
    
    // Relations
    private List<ProductDTO> products;
    
    public CategoryDTO() {}
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public List<ProductDTO> getProducts() { return products; }
    public void setProducts(List<ProductDTO> products) { this.products = products; }
}
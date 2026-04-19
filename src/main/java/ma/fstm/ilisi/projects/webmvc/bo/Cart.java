package ma.fstm.ilisi.projects.webmvc.bo;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;  // Ajoutez cet import
import javax.persistence.*;
@Entity
@Table(name = "carts")
public class Cart implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
    
    private String status; // Valeurs: "ACTIVE", "ABANDONED", "CONVERTED"
    
    private double total;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();  // Initialisez avec une liste vide
    
    public Cart() {}
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public List<CartItem> getCartItems() { 
        if (cartItems == null) {
            cartItems = new ArrayList<>();  // Protection contre null
        }
        return cartItems; 
    }
    
    public void setCartItems(List<CartItem> cartItems) { 
        this.cartItems = cartItems; 
    }
    
    // Méthode utilitaire pour calculer le total
    public void calculateTotal() {
        if (cartItems != null && !cartItems.isEmpty()) {
            this.total = cartItems.stream()
                    .mapToDouble(CartItem::getSubtotal)
                    .sum();
        } else {
            this.total = 0.0;
        }
    }
    
    // Méthode utilitaire pour ajouter un item
    public void addCartItem(CartItem item) {
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        cartItems.add(item);
        item.setCart(this);
        calculateTotal();
    }
    
    // Méthode utilitaire pour retirer un item
    public void removeCartItem(CartItem item) {
        if (cartItems != null) {
            cartItems.remove(item);
            calculateTotal();
        }
    }
}
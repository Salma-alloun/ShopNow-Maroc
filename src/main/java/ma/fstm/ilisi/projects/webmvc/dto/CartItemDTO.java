package ma.fstm.ilisi.projects.webmvc.dto;
public class CartItemDTO {
    
    private int id;
    private int quantity;
    private double subtotal;
    
    // Relations
    private Integer cartId;
    private Integer productId;
    private String productName;
    private Double productPrice;
 // CHAMPS MANQUANTS - AJOUTEZ CES LIGNES
    private String productImage;    // Pour l'image du produit
    private String categoryName;     // Pour la catégorie du produit
    public CartItemDTO() {}
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getProductPrice() { return productPrice; }
    public void setProductPrice(Double productPrice) { this.productPrice = productPrice; }
 // NOUVEAUX GETTERS/SETTERS
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
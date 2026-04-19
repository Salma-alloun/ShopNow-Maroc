package ma.fstm.ilisi.projects.webmvc.dto;
import java.util.Date;
import java.util.List;
public class UserDTO {
    
    private int id;
    private String email;
    private String password;  // AJOUTER CETTE LIGNE
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;
    private Date lastLogin;
    private int failedLoginAttempts;
    
    // Relations
    private AddressDTO address;
    private CartDTO cart;
    private List<OrderDTO> orders;
    private List<PaymentDTO> payments;
    
    public UserDTO() {}
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }  // AJOUTER CE GETTER
    public void setPassword(String password) { this.password = password; }  // AJOUTER CE SETTER
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    public AddressDTO getAddress() { return address; }
    public void setAddress(AddressDTO address) { this.address = address; }
    public CartDTO getCart() { return cart; }
    public void setCart(CartDTO cart) { this.cart = cart; }
    public List<OrderDTO> getOrders() { return orders; }
    public void setOrders(List<OrderDTO> orders) { this.orders = orders; }
    public List<PaymentDTO> getPayments() { return payments; }
    public void setPayments(List<PaymentDTO> payments) { this.payments = payments; }
    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }
}
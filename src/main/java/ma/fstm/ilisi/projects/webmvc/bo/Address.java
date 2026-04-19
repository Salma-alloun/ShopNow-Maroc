package ma.fstm.ilisi.projects.webmvc.bo;
import java.io.Serializable;
import javax.persistence.*;
@Entity
@Table(name = "addresses")
public class Address implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String city;
    
    @Column(name = "postal_code")
    private String postalCode;
    
    private String country;
    
    // Relation bidirectionnelle optionnelle (si besoin d'accéder à l'user depuis l'adresse)
    @OneToOne(mappedBy = "address")
    private User user;
    
    public Address() {}
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
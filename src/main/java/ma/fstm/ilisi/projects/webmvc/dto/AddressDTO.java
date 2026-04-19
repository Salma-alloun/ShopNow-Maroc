package ma.fstm.ilisi.projects.webmvc.dto;
public class AddressDTO {
    
    private int id;
    private String city;
    private String postalCode;
    private String country;
    
    // Optionnel: pour avoir l'information de l'user associé
    private Integer userId;
    private String userFullName;
    
    public AddressDTO() {}
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
}
package ma.fstm.ilisi.projects.webmvc.dto;
import java.util.Date;
public class OrderArchiveDTO {
    
    private int id;
    private int clientId;
    private String clientFullName;
    private Date orderDate;
    private double total;
    private String status;
    private Date archiveDate;
    
    public OrderArchiveDTO() {}
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public String getClientFullName() { return clientFullName; }
    public void setClientFullName(String clientFullName) { this.clientFullName = clientFullName; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getArchiveDate() { return archiveDate; }
    public void setArchiveDate(Date archiveDate) { this.archiveDate = archiveDate; }
}
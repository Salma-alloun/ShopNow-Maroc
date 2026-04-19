package ma.fstm.ilisi.projects.webmvc.dto;

public class StatistiqueMensuelleDTO {
    
    private int mois;
    private int annee;
    private String moisNom;
    private int nombreCommandes;
    private double totalVentes;
    private int nombreClients;
    
    // Constructeurs
    public StatistiqueMensuelleDTO() {}
    
    public StatistiqueMensuelleDTO(int mois, int annee, String moisNom, int nombreCommandes, 
                                   double totalVentes, int nombreClients) {
        this.mois = mois;
        this.annee = annee;
        this.moisNom = moisNom;
        this.nombreCommandes = nombreCommandes;
        this.totalVentes = totalVentes;
        this.nombreClients = nombreClients;
    }
    
    // Getters et Setters
    public int getMois() { return mois; }
    public void setMois(int mois) { this.mois = mois; }
    
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
    
    public String getMoisNom() { return moisNom; }
    public void setMoisNom(String moisNom) { this.moisNom = moisNom; }
    
    public int getNombreCommandes() { return nombreCommandes; }
    public void setNombreCommandes(int nombreCommandes) { this.nombreCommandes = nombreCommandes; }
    
    public double getTotalVentes() { return totalVentes; }
    public void setTotalVentes(double totalVentes) { this.totalVentes = totalVentes; }
    
    public int getNombreClients() { return nombreClients; }
    public void setNombreClients(int nombreClients) { this.nombreClients = nombreClients; }
}
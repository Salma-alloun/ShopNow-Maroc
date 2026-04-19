package ma.fstm.ilisi.projects.webmvc.dto;

public class StatistiqueDTO {
    
    private String label;
    private double valeur;
    private int nombre;
    
    // Constructeurs
    public StatistiqueDTO() {}
    
    public StatistiqueDTO(String label, double valeur) {
        this.label = label;
        this.valeur = valeur;
    }
    
    public StatistiqueDTO(String label, int nombre) {
        this.label = label;
        this.nombre = nombre;
    }
    
    // Getters et Setters
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public double getValeur() { return valeur; }
    public void setValeur(double valeur) { this.valeur = valeur; }
    
    public int getNombre() { return nombre; }
    public void setNombre(int nombre) { this.nombre = nombre; }
}
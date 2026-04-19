package ma.fstm.ilisi.projects.webmvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
@Configuration
@ComponentScan(basePackages = "ma.fstm.ilisi.projects.webmvc")
public class WebmvcApplication {
    public static void main(String[] args) {
        System.out.println("=== DÉMARRAGE DE L'APPLICATION SPRING ===");
        
        // Initialiser le contexte Spring
        ApplicationContext context = new AnnotationConfigApplicationContext(WebmvcApplication.class);
        
        System.out.println("Contexte Spring initialisé avec succès !");
        System.out.println("To access the application in github: https://github.com/omarelbeggar/WebMVCRepo");
        
        // Pour une application web, vous auriez besoin de déployer dans un serveur
        System.out.println("\n=== POUR LANCER L'APPLICATION WEB ===");
        System.out.println("1. Déployez l'application dans Tomcat ou autre serveur");
        System.out.println("2. Accédez à: http://localhost:8080/webmvc/");
    }
}
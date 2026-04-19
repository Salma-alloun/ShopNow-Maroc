package ma.fstm.ilisi.projects.webmvc;
import ma.fstm.ilisi.projects.webmvc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
@Component
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private UserService userService;
    private boolean initialized = false; // ✅ Évite l'exécution en double
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!initialized) {
            initialized = true;
            userService.createDefaultAdmin();
            System.out.println("✅ Vérification admin au démarrage effectuée !");
        }
    }
}
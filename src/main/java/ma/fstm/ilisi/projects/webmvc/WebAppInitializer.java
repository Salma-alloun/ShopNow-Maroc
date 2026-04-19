package ma.fstm.ilisi.projects.webmvc;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;  // ← Changé de jakarta à javax
import javax.servlet.ServletException;  // ← Changé de jakarta à javax
import javax.servlet.ServletRegistration;  // ← Changé de jakarta à javax

public class WebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class);

        // Ajouter le listener
        servletContext.addListener(new ContextLoaderListener(context));

        // DispatcherServlet utilise le même contexte
        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
        ServletRegistration.Dynamic registration = servletContext.addServlet("dispatcher", dispatcherServlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/");
    }
}
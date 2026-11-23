package fr.diginamic.hello;

import fr.diginamic.hello.services.DepartementService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialisationDonnees implements CommandLineRunner {

    private final DepartementService departementService;

    public InitialisationDonnees(
            DepartementService departementService) {

        this.departementService = departementService;
    }

    /**
     * Initializes department names when the application starts.
     *
     * The service checks the application.init configuration property
     * before contacting the external API.
     *
     * @param args application startup arguments
     */
    @Override
    public void run(String... args) throws Exception {
        departementService.initData();
    }
}

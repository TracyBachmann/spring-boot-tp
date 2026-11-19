package fr.diginamic.hello.controleurs;

import fr.diginamic.hello.modeles.Ville;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/villes")
public class VilleControleur {

    private final List<Ville> villes = new ArrayList<>();

    @PostConstruct
    public void initData() {
        villes.add(new Ville("Nice", 342000));
        villes.add(new Ville("Carcassonne", 47800));
        villes.add(new Ville("Narbonne", 53400));
        villes.add(new Ville("Lyon", 496000));
    }

    @GetMapping
    public List<Ville> getVilles() {
        return villes;
    }

    @PostMapping
    public ResponseEntity<String> ajouterVille(@RequestBody Ville ville) {

        for (Ville villeExistante : villes) {
            if (villeExistante.getNom()
                    .equalsIgnoreCase(ville.getNom())) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("La ville existe déjà");
            }
        }

        villes.add(ville);

        return ResponseEntity
                .ok("Ville insérée avec succès");
    }
}
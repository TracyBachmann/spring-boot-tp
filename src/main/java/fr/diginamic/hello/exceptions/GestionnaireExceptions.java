package fr.diginamic.hello.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GestionnaireExceptions {

    @ExceptionHandler(VilleException.class)
    public ResponseEntity<String> gererVilleException(
            VilleException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> gererValidation(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(erreur -> erreur.getDefaultMessage())
                .orElse("Données invalides");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> gererResponseStatusException(
            ResponseStatusException exception) {

        String message = exception.getReason();

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(message != null ? message : "Erreur lors de la requête");
    }
}

package pe.edu.blenkir.ssacb;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> bad(IllegalArgumentException e){ return ResponseEntity.badRequest().body(Map.of("message",e.getMessage())); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e){
        String detail=e.getBindingResult().getFieldErrors().stream().findFirst()
           .map(err->"Campo "+err.getField()+": "+err.getDefaultMessage()).orElse("Datos inválidos");
        return ResponseEntity.badRequest().body(Map.of("message",detail));
    }
    @ExceptionHandler(java.util.NoSuchElementException.class)
    ResponseEntity<?> missing(java.util.NoSuchElementException e){return ResponseEntity.status(404).body(Map.of("message","Registro no encontrado"));}
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflict(DataIntegrityViolationException e){ return ResponseEntity.status(409).body(Map.of("message","El registro ya existe o está relacionado con otros datos.")); }
}

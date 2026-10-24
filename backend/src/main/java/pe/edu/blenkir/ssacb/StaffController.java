package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class StaffController {
    private final SchoolStaffRepository staff;
    private final AuditService audit;
    public StaffController(SchoolStaffRepository staff,AuditService audit){this.staff=staff;this.audit=audit;}

    public record StaffRequest(@NotBlank @Pattern(regexp="\\d{8}") String dni,
       @NotBlank String nombreCompleto,@NotBlank String cargo,String correo,String telefono,boolean activo){}
    public record StaffDto(Long id,String dni,String nombreCompleto,String cargo,String correo,String telefono,boolean activo){}
    static StaffDto dto(SchoolStaff s){return new StaffDto(s.getId(),s.getDni(),s.getNombreCompleto(),s.getCargo(),s.getCorreo(),s.getTelefono(),s.isActivo());}

    @GetMapping @Transactional(readOnly=true)
    public List<StaffDto> list(){return staff.findAll().stream().map(StaffController::dto).toList();}

    @PostMapping @Transactional
    public StaffDto create(@Valid @RequestBody StaffRequest r,Authentication a){
        var s=staff.save(SchoolStaff.builder().dni(r.dni()).nombreCompleto(r.nombreCompleto())
           .cargo(r.cargo()).correo(r.correo()).telefono(r.telefono()).activo(true).build());
        audit.log(a.getName(),"CREAR","PERSONAL",s.getId());return dto(s);
    }

    @PutMapping("/{id}") @Transactional
    public StaffDto update(@PathVariable Long id,@Valid @RequestBody StaffRequest r,Authentication a){
        var s=staff.findById(id).orElseThrow(()->new IllegalArgumentException("Personal no encontrado"));
        s.setDni(r.dni());s.setNombreCompleto(r.nombreCompleto());s.setCargo(r.cargo());
        s.setCorreo(r.correo());s.setTelefono(r.telefono());s.setActivo(r.activo());
        audit.log(a.getName(),"ACTUALIZAR","PERSONAL",id);return dto(staff.save(s));
    }
}

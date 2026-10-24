package pe.edu.blenkir.ssacb;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/exports")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SECRETARIA')")
public class ExportController {
    private final StudentRepository students;
    private final EnrollmentRepository enrollments;
    private final CompetencyResultRepository results;
    public ExportController(StudentRepository students,EnrollmentRepository enrollments,CompetencyResultRepository results){
        this.students=students;this.enrollments=enrollments;this.results=results;
    }
    private static String csv(Object x){
        String value=x==null?"":String.valueOf(x);
        // Evita que Excel interprete contenido de origen externo como fórmula.
        if(value.matches("(?s)^\\s*[=+@\\-].*"))value="'"+value;
        return "\""+value.replace("\"","\"\"")+"\"";
    }
    private static void row(StringBuilder b,Object... cells){
        for(int i=0;i<cells.length;i++){if(i>0)b.append(',');b.append(csv(cells[i]));}b.append("\r\n");
    }
    private ResponseEntity<byte[]> file(String name,StringBuilder data){
        byte[] bytes=("\uFEFF"+data).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
           .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+name+"\"").body(bytes);
    }

    @GetMapping("/siagie/students.csv") @Transactional(readOnly=true)
    public ResponseEntity<byte[]> studentCsv(){
        var b=new StringBuilder();row(b,"DNI","CODIGO_ESTUDIANTE","APELLIDOS","NOMBRES","FECHA_NACIMIENTO","GRADO","SECCION","ACTIVO");
        for(var s:students.findAll())row(b,s.getDni(),s.getCodigo(),s.getApellidos(),s.getNombres(),s.getFechaNacimiento(),s.getGrado(),s.getSeccion(),s.isActivo());
        return file("blenkir_padron_siagie_preparacion.csv",b);
    }
    @GetMapping("/siagie/enrollments.csv") @Transactional(readOnly=true)
    public ResponseEntity<byte[]> enrollmentCsv(){
        var b=new StringBuilder();row(b,"DNI","CODIGO_CURSO","AREA","GRADO","SECCION","PERIODO","MATRICULA_ACTIVA");
        for(var e:enrollments.findAll()){var s=e.getStudent();var c=e.getCourse();row(b,s.getDni(),c.getCodigo(),c.getNombre(),c.getGrado(),c.getSeccion(),c.getPeriodo(),e.isActivo());}
        return file("blenkir_matriculas_siagie_preparacion.csv",b);
    }
    @GetMapping("/siagie/competencies.csv") @Transactional(readOnly=true)
    public ResponseEntity<byte[]> competencyCsv(){
        var b=new StringBuilder();row(b,"DNI","AREA","COMPETENCIA","PERIODO","NIVEL","CONCLUSION_DESCRIPTIVA");
        for(var r:results.findAll())row(b,r.getStudent().getDni(),r.getCompetency().getCourse().getNombre(),r.getCompetency().getNombre(),r.getPeriodo(),r.getNivel(),r.getConclusion());
        return file("blenkir_competencias_siagie_preparacion.csv",b);
    }
}

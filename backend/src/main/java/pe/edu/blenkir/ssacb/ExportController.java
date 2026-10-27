package pe.edu.blenkir.ssacb;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private ResponseEntity<byte[]> excel(String name,String sheetName,String[] headers,List<Object[]> data){
        try(var workbook=new XSSFWorkbook();var output=new ByteArrayOutputStream()){
            var sheet=workbook.createSheet(sheetName);
            var headerStyle=workbook.createCellStyle();
            var headerFont=workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            var header=sheet.createRow(0);
            for(int i=0;i<headers.length;i++){
                var cell=header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            for(int rowIndex=0;rowIndex<data.size();rowIndex++){
                var row=sheet.createRow(rowIndex+1);
                for(int columnIndex=0;columnIndex<headers.length;columnIndex++){
                    var value=data.get(rowIndex)[columnIndex];
                    var cell=row.createCell(columnIndex);
                    if(value instanceof Boolean bool)cell.setCellValue(bool);
                    else if(value instanceof Number number)cell.setCellValue(number.doubleValue());
                    else if(value!=null)cell.setCellValue(String.valueOf(value));
                }
            }
            sheet.createFreezePane(0,1);
            sheet.setAutoFilter(new CellRangeAddress(0,data.size(),0,headers.length-1));
            for(int i=0;i<headers.length;i++)sheet.autoSizeColumn(i);
            workbook.write(output);
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+name+"\"")
                .body(output.toByteArray());
        }catch(IOException exception){
            throw new IllegalStateException("No se pudo generar el archivo Excel",exception);
        }
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

    @GetMapping("/siagie/students.xlsx") @Transactional(readOnly=true)
    public ResponseEntity<byte[]> studentExcel(){
        var data=students.findAll().stream()
            .map(s->new Object[]{s.getDni(),s.getCodigo(),s.getApellidos(),s.getNombres(),s.getFechaNacimiento(),s.getGrado(),s.getSeccion(),s.isActivo()})
            .toList();
        return excel("padron_estudiantes.xlsx","Padron",new String[]{"DNI","CODIGO_ESTUDIANTE","APELLIDOS","NOMBRES","FECHA_NACIMIENTO","GRADO","SECCION","ACTIVO"},data);
    }

    @GetMapping("/siagie/enrollments.xlsx") @Transactional(readOnly=true)
    public ResponseEntity<byte[]> enrollmentExcel(){
        var data=enrollments.findAll().stream()
            .map(e->{var s=e.getStudent();var c=e.getCourse();return new Object[]{s.getDni(),c.getCodigo(),c.getNombre(),c.getGrado(),c.getSeccion(),c.getPeriodo(),e.isActivo()};})
            .toList();
        return excel("matriculas_por_curso.xlsx","Matriculas",new String[]{"DNI","CODIGO_CURSO","AREA","GRADO","SECCION","PERIODO","MATRICULA_ACTIVA"},data);
    }

    @GetMapping("/siagie/competencies.xlsx") @Transactional(readOnly=true)
    public ResponseEntity<byte[]> competencyExcel(){
        var data=results.findAll().stream()
            .map(r->new Object[]{r.getStudent().getDni(),r.getCompetency().getCourse().getNombre(),r.getCompetency().getNombre(),r.getPeriodo(),r.getNivel(),r.getConclusion()})
            .toList();
        return excel("logros_por_competencias.xlsx","Logros",new String[]{"DNI","AREA","COMPETENCIA","PERIODO","NIVEL","CONCLUSION_DESCRIPTIVA"},data);
    }
}

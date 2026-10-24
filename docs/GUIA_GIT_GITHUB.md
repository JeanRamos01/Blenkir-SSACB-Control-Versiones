# Reconstrucción progresiva de Blenkir SIGA con Git y GitHub

## Alcance y honestidad del historial

Este repositorio **reconstruye por etapas** el contenido del ZIP de referencia. Los commits registran la incorporación progresiva de archivos ya existentes y su integración por módulos. **No representan las fechas ni las decisiones originales de desarrollo**, y no deben presentarse como tales. El punto inicial contiene el prototipo heredado; cada rama temática agrega una capa del proyecto. Algunas etapas intermedias son pedagógicas y solo se espera que el sistema completo funcione tras la integración final.

## Ramas creadas

| Rama | Contenido reconstruido |
| --- | --- |
| `main` | Punto inicial y versión final integrada |
| `develop` | Integración progresiva |
| `feature/backend-spring` | Spring Boot, JWT y configuración |
| `feature/postgresql-jpa` | PostgreSQL, JPA, Hibernate y repositorios |
| `feature/crud-academico` | Controladores CRUD y servicios |
| `feature/validaciones` | DNI, errores y pruebas |
| `feature/frontend-angular` | Interfaz Angular |
| `docs/arquitectura` | Documento de adaptación académica |
| `docs/control-versiones` | Esta guía y evidencias |

## Recorrido conceptual

1. `git switch develop` y `git switch -c feature/nueva-mejora` crean una rama de trabajo.
2. Edita un archivo real, por ejemplo `backend/src/main/java/pe/edu/blenkir/ssacb/StudentController.java`.
3. `git status` muestra archivos modificados. `git diff` muestra modificaciones aún no preparadas.
4. `git add RUTA_DEL_ARCHIVO` mueve el cambio a **Staging Area**. `git diff --cached` muestra exactamente lo preparado.
5. `git commit -m "fix: describir cambio real"` guarda la versión preparada.
6. `git push -u origin feature/nueva-mejora` publica la rama.
7. En GitHub, crea un **Pull Request** con base `develop` y compara la rama de mejora. Revisa y fusiona.
8. En una publicación, fusiona `develop` en `main` y crea la etiqueta de versión.

### Verificar el historial incluido

```bash
git status
git branch -a
git log --graph --all --decorate --oneline
git show --stat feature/postgresql-jpa
git show --stat feature/validaciones
```

### Publicar en tu propio GitHub

Descomprime **incluyendo la carpeta oculta `.git`**. Abre una terminal dentro de `Blenkir_SIGA_Modular`. Antes de publicar, revisa que no haya contraseñas, secretos, datos personales ni archivos sensibles.

1. Crea en github.com un repositorio **vacío** (sin README, licencia ni `.gitignore` inicial), por ejemplo `Blenkir-SIGA-Control-Versiones`.
2. En el directorio del proyecto:

```bash
git remote add origin https://github.com/TU_USUARIO/Blenkir-SIGA-Control-Versiones.git
git push -u origin main
git push -u origin develop
git push origin feature/backend-spring feature/postgresql-jpa feature/crud-academico feature/validaciones feature/frontend-angular docs/arquitectura docs/control-versiones
git push origin v1.0.0
```

Si `origin` ya existe, inspecciona `git remote -v` antes de cambiarlo. Si GitHub requiere autenticación, completa su flujo autorizado; no coloques tokens en archivos del proyecto.

> **Nota sobre Pull Requests:** Los merges pedagógicos ya se guardaron localmente para preservar el gráfico de Git. Publicar este historial completo NO crea retroactivamente Pull Requests en GitHub. Para evidenciar uno, haz una nueva mejora auténtica en otra rama basada en `develop`, publícala y ábrela desde GitHub.

### Evidencia práctica nueva del Staging Area

Ejemplo: agrega una explicación real a esta guía o mejora una validación existente, y toma capturas en cada punto:

```bash
git switch develop
git switch -c feature/evidencia-staging
# Modificar realmente un archivo del proyecto
git status
git diff
git add docs/GUIA_GIT_GITHUB.md
git status
git diff --cached
git commit -m "docs: ampliar evidencia del staging area"
git push -u origin feature/evidencia-staging
```

El ejemplo `git add docs/GUIA_GIT_GITHUB.md` solo es aplicable si modificaste esa guía; si editaste otro archivo, usa su ruta real.

## Lista de capturas recomendada

- `git status` antes del `git add`.
- `git status` después del `git add`, donde aparece *Changes to be committed*.
- `git diff --cached` y la confirmación de `git commit`.
- `git branch -a` con las ramas publicadas.
- `git log --graph --all --oneline --decorate` con merges.
- Pestaña **Commits** en GitHub.
- Pestaña **Branches** y un Pull Request nuevo con estado **Merged**.
- Código final en la rama `main` y etiqueta `v1.0.0`.

## Consideraciones

- No publiques `.env`, contraseñas reales, datos de alumnos ni respaldos de la base de datos.
- El archivo `.gitignore` incluido ayuda a excluir secretos y artefactos generados, pero revisa igualmente lo que está bajo seguimiento mediante `git ls-files`.
- Los commits reconstruidos demuestran ramas, Staging Area, commits y merges; los nuevos Pull Requests demuestran revisión e integración colaborativa en GitHub.

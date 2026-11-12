# Publicar LOGISTPULSE en GitHub desde Visual Studio Code (Windows)

> **Importante:** esta guía usa **Visual Studio Code (VS Code)**, no Visual Studio 2022. Son programas diferentes. Para proyectos Java, VS Code con Extension Pack for Java suele ser la opción más sencilla.

## A. Preparar Windows

1. Instala Java **JDK 17 o posterior** (Temurin u otra distribución compatible).
2. Instala **Visual Studio Code**: <https://code.visualstudio.com/>.
3. Instala **Git para Windows**: <https://git-scm.com/downloads/win>.
4. En VS Code abre Extensions (`Ctrl+Shift+X`) e instala **Extension Pack for Java** de Microsoft.
5. Abre una terminal (`Terminal` > `New Terminal`) y comprueba:

```powershell
java -version
javac -version
git --version
```

Si Maven está instalado, prueba también `mvn -version`. Para trabajar con la consola y el script PowerShell no es obligatorio tener Maven, aunque sí para las pruebas JUnit.

## B. Abrir el proyecto correctamente

1. Descarga y descomprime el ZIP `logistpulse-smart-inventory-java.zip`.
2. En VS Code elige **File > Open Folder** y selecciona la carpeta **`logistpulse-smart-inventory-java`**, no `src`, no `java` ni la carpeta padre.
3. Comprueba que ves `pom.xml`, `README.md`, `src`, `docs` y `.github`.
4. Abre `src/main/java/ec/edu/logistpulse/App.java` y haz clic en **Run** junto a `main`. Introduce las opciones en la terminal.

Si no funciona `Run`, puedes ejecutarlo desde la terminal PowerShell de VS Code así:

```powershell
.\scripts\ejecutar.ps1
```

Si el sistema bloquea scripts PowerShell, usa estos comandos directos (sin cambios de configuración):

```powershell
New-Item -ItemType Directory -Force out | Out-Null
$fuentes = @(Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object FullName)
javac -encoding UTF-8 -d out $fuentes
java -cp out ec.edu.logistpulse.App
```

## C. Primera subida a GitHub: método VISUAL (sin memorizar comandos)

1. En VS Code abre la carpeta principal del proyecto.
2. Abre **Source Control** en el menú lateral (`Ctrl+Shift+G`).
3. Si aparece **Initialize Repository**, pulsa ahí (esto ejecuta `git init`).
4. En la sección Changes, verifica que estén el código, `pom.xml`, `.github`, `docs` y `README.md`, pero **no** carpetas compiladas `out` ni `target` gracias a `.gitignore`.
5. Pulsa el símbolo **+** de Changes (Stage All Changes).
6. En la caja de mensaje escribe: `feat: implementa Command y Chain para reservas`.
7. Pulsa **Commit**. Si Git pide identidad, configura tu nombre y correo de autor (véase sección D).
8. Abre la paleta con `Ctrl+Shift+P` y busca **Publish to GitHub**. También puede aparecer el botón **Publish Branch** o **Publish to GitHub** en Source Control; elige **Publish to GitHub** para un repositorio nuevo.
9. Inicia sesión en GitHub si VS Code te lo solicita.
10. Nombra el repositorio **`logistpulse-smart-inventory-java`** y elige **Public** (para una entrega pública) o **Private** (si aún no lo compartirás).
11. Si pide incluir archivos, marca los del proyecto y publica.
12. Abre GitHub, confirma que aparecen `README.md`, `pom.xml`, `src` y `.github`.
13. En GitHub ve a la pestaña **Actions**: el workflow `Java CI` intentará ejecutar `mvn test` automáticamente en cada push/PR.

**No crees manualmente el repositorio en GitHub si utilizarás esta opción:** `Publish to GitHub` crea el remoto por ti.

Documentación oficial: <https://code.visualstudio.com/docs/sourcecontrol/repos-remotes>.

## D. Método alternativo: crear primero el repositorio en GitHub y subir por terminal

1. Entra a <https://github.com/new> y crea `logistpulse-smart-inventory-java`.
2. Deja el repositorio **vacío**: NO selecciones `Add a README`, `.gitignore` o licencia, porque este proyecto ya los incluye.
3. Abre el proyecto en VS Code; después `Terminal` > `New Terminal`.
4. Ejecuta en orden:

```powershell
git init
git branch -M main
git add .
git commit -m "feat: implementa Command y Chain para reservas"
git remote add origin https://github.com/TU-USUARIO/logistpulse-smart-inventory-java.git
git push -u origin main
```

Sustituye **TU-USUARIO** por tu nombre de usuario real en GitHub. Cuando se abra el navegador para autenticarse, inicia sesión y autoriza a Git Credential Manager o VS Code según el caso.

Si Git dice `Author identity unknown`, ejecuta:

```powershell
git config --global user.name "Tu Nombre"
git config --global user.email "tu-email-de-github@example.com"
```

Y repite el comando `git commit`.

**No combines los métodos C y D**: si ya publicaste desde VS Code, no agregues otra vez el remoto `origin`.

Documentación oficial GitHub: <https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github>.

## E. Cómo subir cambios posteriores

Cada vez que modifiques código:

1. Guarda los archivos (`Ctrl+S`).
2. En `Source Control`, revisa los cambios.
3. Haz **Stage**, escribe un mensaje descriptivo y pulsa **Commit**.
4. Pulsa **Sync Changes** o **Push**, según lo que aparezca.

O usa estos comandos desde la carpeta raíz:

```powershell
git status
git add .
git commit -m "test: agrega casos de validacion de lotes"
git push
```

## F. Verificar antes de entregar

- En el repositorio de GitHub se ven las carpetas `src/main/java` y `src/test/java`.
- `README.md` explica el negocio y los dos patrones.
- `mvn test` (si tienes Maven) muestra 12 pruebas correctas.
- En GitHub Actions el workflow `Java CI` aparece verde (si todas las descargas funcionan).
- Guarda el enlace real `https://github.com/TU-USUARIO/logistpulse-smart-inventory-java` para tu docente.

## G. Errores habituales

| Mensaje | Qué significa / corrección |
|---|---|
| `git is not recognized` | Instala Git y reinicia VS Code. |
| `java/javac is not recognized` | Instala el JDK y revisa PATH/JAVA_HOME. |
| `mvn is not recognized` | Instala Maven o ejecuta `App.java` con el Extension Pack for Java. |
| `remote origin already exists` | El remoto ya existe. Usa `git remote -v`; no repitas `remote add`. |
| `src refspec main does not match any` | Aún no hiciste un commit o la rama no existe. Haz el commit y `git branch -M main`. |
| `rejected non-fast-forward` | El remoto tiene commits que no están localmente; no fuerces el push. Trae e integra cambios antes de intentar de nuevo. |
| `Authentication failed` | Inicia sesión mediante navegador/gestor de credenciales; GitHub no acepta contraseña de cuenta directamente para HTTPS. |
| Errores de `package ... does not exist` | Abre la **raíz** del proyecto y espera a que Java cargue `pom.xml`; no compiles un solo archivo aislado. |

**Precaución:** no subas contraseñas, tokens, `.env`, claves de API ni secretos.

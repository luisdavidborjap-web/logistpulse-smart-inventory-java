# Actualizar el repositorio LOGISTPULSE existente desde GitHub Codespaces

**Repositorio:** `https://github.com/luisdavidborjap-web/logistpulse-smart-inventory-java`

Esta es una **actualización**. No crees otro repositorio ni vuelvas a ejecutar `git init`. El ZIP actualizado contiene sus archivos **en la raíz**, sin una carpeta duplicada.

## A. Actualizar el código

1. Abre el repositorio de GitHub → **Code → Codespaces** → abre tu espacio de trabajo en el navegador.
2. Descarga `logistpulse-strategy-actualizacion-codespaces.zip` a tu computadora.
3. En el editor de Codespaces, arrastra el ZIP al panel **Explorer** dentro del directorio `/workspaces/logistpulse-smart-inventory-java`.
4. Abre una terminal (Terminal → New Terminal). Confirma la ubicación y el ZIP:

```bash
pwd
ls -la
```

Deberías ver el ZIP al lado de `pom.xml`. Comprueba **el nombre exacto** si se descargó con un sufijo (`(1)` por ejemplo).

5. Extrae la actualización (se sobrescribirán los archivos del **proyecto** que cambiaron, incluido `README.md`; la carpeta `.git` no está dentro del ZIP):

```bash
unzip -o logistpulse-strategy-actualizacion-codespaces.zip
```

6. Comprueba que `pom.xml`, `README.md` y `src` siguen en la raíz:

```bash
ls -la
ls src/main/java/ec/edu/logistpulse/estrategia/
```

Debe aparecer `picking/` y `reposicion/`. Cuando hayas comprobado la extracción, elimina el ZIP:

```bash
rm logistpulse-strategy-actualizacion-codespaces.zip
```

> Si aparece `cannot find or open ZIP`, todavía no está subido a Codespaces o tiene otro nombre. Ejecuta `ls -la` y utiliza el nombre real. Si el ZIP queda en un subdirectorio, muévelo a la raíz antes de extraer.

## B. Compilar y probar

```bash
mvn test
mvn -q -DskipTests package
java -cp target/classes ec.edu.logistpulse.App
```

- El primero ejecuta **34 pruebas JUnit**.
- El segundo compila la aplicación (sin repetir las pruebas).
- El tercero inicia la interfaz de consola.

**Demostración rápida**: opción `6` → `2` (FIFO) → `TODOS`; opción `2` → SKU `MED-002`, lote `AUTO`, cantidad `1`, solicitante `docente`. El sistema debe asignar `L-203`. Cambia a `LIFO` y solicita de nuevo con `AUTO`; debe asignar `L-204` si hay capacidad y stock suficientes.

Para reposición, opción `7` → `3` (EOQ) → `TODOS`; opción `8` → SKU `TEC-001`. Debe sugerir `12` unidades de ejemplo.

## C. Guardar los cambios en el mismo repositorio de GitHub

Cuando las pruebas estén correctas, sal del programa con `0` y ejecuta:

```bash
git status
git add .
git commit -m "feat: agregar patron Strategy para picking y reposicion"
git push origin main
```

No agregues `git init`, ni vuelvas a configurar `origin`; ya estás trabajando en tu repositorio. Si `git status` indica `nothing to commit, working tree clean`, significa que no hay cambios pendientes de guardar. Si el `git push` es rechazado por cambios remotos, no fuerces `--force`; primero resuelve la sincronización.

## D. Verificar en GitHub

- Abre `https://github.com/luisdavidborjap-web/logistpulse-smart-inventory-java`.
- Comprueba que existe `src/main/java/ec/edu/logistpulse/estrategia/` y que `README.md` menciona los **tres patrones**.
- Ve a la pestaña **Actions** y comprueba el último proceso **Java CI**.

**Nota:** Codespaces puede suspenderse; los cambios no están publicados hasta ejecutar `git push`. El programa solo conserva datos en memoria durante su ejecución.

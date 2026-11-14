# Actualizar el repositorio actual desde GitHub Codespaces

Repositorio: `https://github.com/luisdavidborjap-web/logistpulse-smart-inventory-java`

**No crees otro repositorio.** Este ZIP incluye archivos **en la raíz**, sin carpeta duplicada.
La operación conserva tu historial Git; los archivos originales quedarán disponibles
en `main` mientras pruebes los cambios en una rama nueva.

## 1. Verificar estado y crear una rama de corrección

Abre **Code → Codespaces → tu Codespace → Terminal → New Terminal**:

```bash
pwd
git status --short
```

Si aparecen cambios pendientes, **guárdalos mediante commit o stash antes de continuar**.
Con el directorio limpio:

```bash
git switch main
git pull --ff-only origin main
git switch -c fix/luis-review-solid-english
```

## 2. Subir el ZIP y retirar las rutas viejas

Descarga `logistpulse-refactor-luis-v3-codespaces.zip` y arrástralo al panel
**Explorer** de Codespaces, hasta que aparezca en la raíz.

Confirma:

```bash
ls -lh logistpulse-refactor-luis-v3-codespaces.zip
```

Las rutas anteriores están en español y **deben desaparecer** de la nueva rama,
porque dejarlas provoca duplicación de clases y compilaciones ambiguas. En la raíz
`/workspaces/logistpulse-smart-inventory-java`, ejecuta:

```bash
rm -rf src/main/java/ec/edu/logistpulse/comando
rm -rf src/main/java/ec/edu/logistpulse/validacion
rm -rf src/main/java/ec/edu/logistpulse/estrategia
rm -rf src/main/java/ec/edu/logistpulse/dominio
rm -rf src/main/java/ec/edu/logistpulse/servicio
rm -rf src/main/java/ec/edu/logistpulse/auditoria
rm -rf src/main/java/ec/edu/logistpulse/bootstrap
rm -f src/test/java/ec/edu/logistpulse/SistemaTest.java
rm -f src/test/java/ec/edu/logistpulse/StrategyTest.java
rm -f src/smoke/java/ec/edu/logistpulse/PruebaRapida.java
rm -f src/smoke/java/ec/edu/logistpulse/PruebaStrategyRapida.java
rm -f scripts/ejecutar.ps1 scripts/verificar.ps1
rm -f docs/ACTUALIZAR_CODESPACES.md
unzip -o logistpulse-refactor-luis-v3-codespaces.zip
rm logistpulse-refactor-luis-v3-codespaces.zip
```

**Importante:** NO ejecutes `rm -rf .git` ni elimines el repositorio principal.
Los comandos eliminan exclusivamente las carpetas de paquetes anteriores y las pruebas/
scripts antiguos que venían en el ZIP v2. Si añadiste clases o pruebas propias después,
revisa `git status` y conserva esas modificaciones desde la rama original.
La carpeta `bootstrap` se elimina porque el ZIP trae una versión nueva en inglés
(`SampleData`); la nueva se restaura al descomprimir.

## 3. Compilar y probar

```bash
mvn clean test
mvn -q -DskipTests package
java -cp target/classes ec.edu.logistpulse.App
```

Maven debería ejecutar **53 pruebas JUnit**. No continúes con la publicación si
`mvn clean test` termina en `BUILD FAILURE`.

La prueba adicional, usando solamente el compilador Java del Codespace, es:

```bash
bash scripts/smoke.sh
```

## 4. Guardar los cambios en GitHub (sin modificar main directamente)

Al salir del programa con `0`:

```bash
git status --short
git add -A
git commit -m "refactor: english packages, numbered commands and SOLID improvements"
git push -u origin fix/luis-review-solid-english
```

En GitHub abre **Compare & pull request**, selecciona base `main` y comparación
`fix/luis-review-solid-english`; revisa la pestaña **Files changed** (debería
mostrar `comando`/`validacion`/`estrategia` como eliminados y `command`/`chain`/
`strategy` como añadidos) y verifica que **Actions** esté en verde.
Después de la revisión, haz **Merge pull request**.

## 5. Presentación al profesor

Muestra: `CMD-000001` y `RES-000001`, una reserva repartida entre `LOT-200` y
`LOT-203`, rechazo de `mallory` al liberar una reserva de `ana`, ajuste/transferencia
de un supervisor, `undo()`, cambio FEFO/FIFO/LIFO y resultados de JUnit/Actions.

**Si aparece un error de rama existente**, utiliza otro nombre, por ejemplo
`fix/luis-review-solid-english-2`. No fuerces un push sobre `main`.

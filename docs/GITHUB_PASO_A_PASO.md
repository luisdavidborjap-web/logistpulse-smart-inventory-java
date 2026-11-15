# LOGISTPULSE — Uso de GitHub Codespaces

El proyecto se desarrolla y ejecuta en **GitHub Codespaces**. No es necesario instalar JDK, Maven o Git en Windows si el Codespace ya contiene esas herramientas.

## Primera apertura

1. Abre tu repositorio de GitHub.
2. Pulsa **Code → Codespaces → Create codespace on main**.
3. Se abrirá un editor con terminal integrada.
4. Verifica el entorno: `java -version`, `mvn -version`, `git status`.

## Incorporar los archivos preparados

1. Arrastra el archivo ZIP proporcionado al panel **Explorer**, en la carpeta principal del repositorio.
2. Ejecuta `ls -lh` para conocer el **nombre exacto** del ZIP.
3. Descomprime con `unzip -o NOMBRE_EXACTO.zip`.
4. Comprueba que `README.md`, `src/` y `pom.xml` estén directamente en la raíz, sin carpeta adicional.
5. Elimina el ZIP con `rm NOMBRE_EXACTO.zip`.

## Probar y publicar

```bash
mvn clean test
bash scripts/smoke.sh
mvn -q -DskipTests package
java -cp target/classes ec.edu.logistpulse.App
```

Sal del menú escribiendo `0`. Después:

```bash
git add -A
git commit -m "feat: implement five behavioral design patterns"
git push
```

Si trabajas en una rama nueva, establece el upstream con `git push -u origin NOMBRE_RAMA`, abre una Pull Request y fusiona solamente después de que las pruebas de GitHub Actions hayan terminado con éxito.

## Precauciones

- No elimines `.git/`: contiene la configuración del repositorio.
- No incluyas el ZIP original en el commit.
- No reemplaces código propio sin revisar primero `git diff`.
- Los datos de la consola se pierden cuando se reinicia la aplicación, pero el código se conserva en GitHub después del push.

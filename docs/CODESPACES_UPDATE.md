# Actualizar LOGISTPULSE desde GitHub Codespaces

Trabaja en una rama para poder revisar los cambios antes de integrarlos en `main`.

1. En tu Codespace, comprueba `git status --short`. Si tienes cambios propios sin guardar, haz commit o conserva una copia antes de continuar.
2. Cambia a `main`, actualiza y crea una rama:

```bash
git switch main
git pull --ff-only origin main
git switch -c feat/state-observer
```

3. Descarga `logistpulse-state-observer-codespaces.zip` y arrástralo al **Explorer** de Codespaces. Confirma el nombre exacto con `ls -l`.
4. En la raíz `/workspaces/logistpulse-smart-inventory-java`, descomprime:

```bash
unzip -o logistpulse-state-observer-codespaces.zip
rm logistpulse-state-observer-codespaces.zip
```

El ZIP contiene archivos directamente en la raíz (sin carpeta contenedora adicional). La carpeta `.git` no está incluida ni se modifica. Si el proyecto principal ya era una versión corregida con paquetes en inglés, no es necesario borrar esos paquetes.

5. Ejecuta las pruebas:

```bash
mvn clean test
bash scripts/smoke.sh
mvn -q -DskipTests package
java -cp target/classes ec.edu.logistpulse.App
```

6. Cuando todo funcione:

```bash
git add -A
git commit -m "feat: implement State and Observer with diagrams and tests"
git push -u origin feat/state-observer
```

7. En GitHub, crea un Pull Request `feat/state-observer → main`. Comprueba que GitHub Actions termine con éxito, revisa los archivos modificados y pulsa **Merge pull request**. Solo después elimina la rama y el Codespace si ya no los necesitas.

**Si tu `main` todavía es una versión anterior a LOGISTPULSE 3.0** (carpetas `comando`, `validacion`, etc.), no fusiones los dos diseños mezclados. El paquete actual se basa en la v3 de paquetes ingleses; deberías actualizar primero a esa base o revisar cuidadosamente la diferencia en la Pull Request.

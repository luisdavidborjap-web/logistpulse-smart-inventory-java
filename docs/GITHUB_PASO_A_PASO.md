# Guía de GitHub y Codespaces — LOGISTPULSE v3

El repositorio existe. **No es necesario crearlo otra vez.**

1. Entra al repositorio y abre **Code → Codespaces**.
2. Abre el Codespace existente con acceso a la rama `main`.
3. Sigue [`CODESPACES_UPDATE.md`](CODESPACES_UPDATE.md) para crear una rama,
   reemplazar los paquetes antiguos, validar y subir el cambio con una pull request.
4. En la pestaña `Actions` verifica el resultado del flujo **Java CI**.

Las acciones `git add`, `git commit` y `git push` ocurren dentro del Codespace,
pero los archivos no estarán visibles en GitHub hasta realizar **push**.

Consulta la guía oficial de GitHub:
https://docs.github.com/en/codespaces/developing-in-a-codespace/using-source-control-in-your-codespace

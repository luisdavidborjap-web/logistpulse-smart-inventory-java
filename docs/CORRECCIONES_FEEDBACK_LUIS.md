# Respuesta a las observaciones dirigidas a Luis

Fuente: cuatro capturas compartidas por el estudiante (comparación de repositorios, SOLID y comentarios a Luis).
Este documento **no evalúa** a los otros estudiantes. Algunas observaciones de la última captura están cortadas;
solo se implementaron los puntos legibles y directamente aplicables al repositorio de Luis.

| Observación del profesor | Cambio implementado | Dónde comprobar |
|---|---|---|
| Carpetas de patrones en español | `comando` → `command`; `validacion` → `chain`; `estrategia` → `strategy`; también `domain`, `service`, `audit`, `security` | `src/main/java/ec/edu/logistpulse/` |
| Command sin objetos claramente etiquetados y numerados | `CMD-000001` para comando, `RES-000001` para reserva y `LOT-T-000001` para lote transferido; los objetos ya existentes conservan SKU/lot/warehouse IDs | `IdSequence`, `Command`, `AuditEntry`, consola |
| Faltaba `undo()` | Interfaz `Command` incluye `execute()`/`undo()`; las cuatro implementaciones ofrecen compensación segura | `command/` |
| Faltan `Adjust` y `Transfer` | Se agregan clases de comando, servicio y validaciones de rol/stock/capacidad | `AdjustCommand`, `TransferCommand`, `StockMutationService` |
| Chain solo aplicada a Reserve | `OperationRequest` representa las cuatro operaciones; el receptor las envía a Chain | `chain/`, `ReservationService`, `StockMutationService` |
| Mallory liberaba la reserva de Ana | Autorización `owner OR supervisor` antes de liberar y antes de restaurar; se comprueba permiso al deshacer comandos | `AuthorizationHandler`, `CommandInvoker` |
| Reserva limitada a un lote | Strategy crea `List<Allocation>` y la reserva puede distribuirse en múltiples lotes | `PickingContext`, `Reservation`, `ReservationService` |
| Strategy mezclada con Chain | Strategy crea el plan antes de Chain; `LotHandler` ya no invoca políticas | `PickingContext`, `ReservationService`, `LotHandler` |
| `ServicioInventario` sobrecargado (S/SOLID) | Separación entre `ReservationService`, `StockMutationService` y la fachada `StockService` | `service/` |
| Chain creada dentro del constructor del servicio (O/D/SOLID) | Cadena compuesta en `SampleData` e inyectada a los receptores | `bootstrap/SampleData.java` |
| Comandos dependían de clase concreta (D/SOLID) | Dependen de la interfaz `StockOperations` | `command/`, `service/StockOperations` |
| Políticas arrojaban excepciones no contempladas (L/SOLID) | `PickingPolicy` solo ordena; fallas de plan se expresan con `ValidationResult`; se documenta contrato | `strategy/picking`, `chain/ValidationResult` |
| Idempotencia | Se mantiene ID más huella; conflicto de ID/payload se rechaza y audita | `CommandInvoker` |
| Tests, CI y documentación | 53 métodos JUnit, 36 aserciones rápidas, CI Maven/JDK 17 y docs actualizadas | `src/test/`, `src/smoke/`, `.github/workflows/ci.yml` |

## Precauciones importantes

1. Un comando de `undo()` **no equivale** a revertir cualquier historial arbitrariamente: puede ser rechazado si afecta stock ya reservado, o viola otras invariantes. Es una compensación segura.
2. Numeración y auditoría son **en memoria**: para producción se requiere DB y transacciones.
3. Los actores y roles se simulan; un sistema real requiere autenticación y control de sesión.
4. El picking automático se limita al SKU y bodega solicitados; no distribuye entre diferentes bodegas.
5. `Adjust` representa entradas, mermas o corrección de conteos; `Transfer` traslada stock de una bodega a otra.
6. Como las capturas no contienen el documento original completo de la evaluación, la revisión no puede garantizar que sean todos los requisitos del curso.

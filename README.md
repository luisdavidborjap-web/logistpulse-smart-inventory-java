# LOGISTPULSE Smart Inventory — Java v3.0

Proyecto académico de **inventario inteligente** con tres patrones de comportamiento:
**Command**, **Chain of Responsibility** y **Strategy**.

Esta revisión responde específicamente a las observaciones recibidas para **Luis**:
paquetes en inglés, comandos numerados, `execute()`/`undo()`, cuatro operaciones,
identidad del solicitante y permisos, distribución entre lotes y mejor separación SOLID.

## Requisitos y ejecución en GitHub Codespaces

No necesitas instalar Java ni Maven en tu computador: abre el repositorio desde **Code → Codespaces**.
En la terminal:

```bash
mvn test
mvn -q -DskipTests package
java -cp target/classes ec.edu.logistpulse.App
```

Prueba rápida adicional sin Maven (solo JDK 17+):

```bash
bash scripts/smoke.sh
```

## Carpetas en inglés

```text
src/main/java/ec/edu/logistpulse/
  App.java
  command/              Command, invoker, Reserve, Release, Adjust, Transfer
  chain/                ValidationHandler + seis validadores
  strategy/
    picking/             PickingPolicy, PickingContext, FEFO/FIFO/LIFO
    replenishment/       ReplenishmentPolicy, ROP/MinMax/EOQ
  domain/               Product, Warehouse, StockLot, Reservation, Allocation, Inventory
  service/              StockOperations, StockService, ReservationService, StockMutationService
  security/             ActorDirectory, Role, DemoActorDirectory
  audit/                AuditLedger, AuditEntry, InMemoryAuditLedger
  bootstrap/            SampleData (composición e inyección)
src/test/               53 métodos de pruebas JUnit 5
src/smoke/              36 aserciones de verificación funcional
.github/workflows/ci.yml
```

## Identificación: qué objeto tiene qué número

| Elemento | Ejemplo | Finalidad |
|---|---|---|
| **Comando** | `CMD-000001` | Identifica una petición única para idempotencia y auditoría |
| **Reserva** | `RES-000001` | Identifica la asignación a uno o varios lotes |
| **Lote** | `LOT-200` | Identifica una partida existente |
| **Lote por transferencia** | `LOT-T-000001` | Identifica la nueva partida creada en la bodega de destino |
| **Producto** | `MED-002` | SKU de negocio |
| **Bodega** | `WH-01`, `WH-02` | Identifica origen/destino y su capacidad |

El invocador registra: `commandId`, tipo, actor, objeto objetivo, estado,
validador que rechazó, mensaje y `Instant`. La numeración es **en memoria y se reinicia**
al cerrar: producción requeriría secuencias persistentes/identificadores globalmente únicos.

## Las cuatro operaciones Command

- `ReserveCommand`: reservar existencias en uno o varios lotes.
- `ReleaseCommand`: liberar **solo** si el solicitante es propietario o supervisor.
- `AdjustCommand`: corregir existencias con delta positivo/negativo; **solo supervisor**.
- `TransferCommand`: trasladar unidades no reservadas entre dos bodegas; **solo supervisor**.

Todos soportan `execute()` y `undo()`. `undo()` es una **compensación condicionada**:
puede rechazarse si, desde la ejecución original, se reservaron unidades,
caducaron lotes o ya no hay capacidad suficiente. Nunca fuerza un estado inválido.

`CommandInvoker` memoriza (ID, huella del payload). Si llega el mismo ID con
una petición distinta, rechaza el conflicto; si es un reintento exacto, **no repite**
la operación. No es idempotencia durable entre reinicios.

## Chain of Responsibility

El orden, compuesto en `SampleData`, es:

**Identity → Resource/SKU → Authorization → Stock → Lot → Capacity**.

Los cuatro tipos de operación pasan por esta cadena. Cada eslabón retorna
un `ValidationResult` tipado, que incluye `rejectedBy` y el motivo.

## Strategy — independiente de Chain

`PickingContext` ejecuta **FEFO**, **FIFO** o **LIFO** **antes** de invocar la cadena.
La estrategia genera un **plan de asignación**, no valida la solicitud ni modifica stock.
Chain valida ese plan y luego el receptor ejecuta la operación.

Para la selección automática escribe `AUTO`; se puede repartir una cantidad
entre varios lotes elegibles del **mismo SKU y bodega**. Alternativamente, escribe
un ID de lote (`LOT-200`) para seleccionar manualmente uno solo.

En paralelo, `ReplenishmentContext` ofrece **Reorder Point**, **Min-Max** y **EOQ**:
recomienda compras; **no** registra órdenes ni cambia existencias.

## Demostración en el menú

Actores registrados en el ejemplo (solo para pruebas):

| Actor | Rol |
|---|---|
| `ana` | OPERATOR |
| `mallory` | OPERATOR |
| `supervisor` | SUPERVISOR |

1. Opción **2**: actor `ana`, SKU `MED-002`, bodega `WH-01`, cantidad `7`, lote `AUTO`.
   Se crea `CMD-000001`, y la reserva `RES-000001` usa `LOT-200` (5) + `LOT-203` (2).
2. Opción **3**: actor `mallory`, reserva `RES-000001` → rechazo `AUTHORIZATION`.
3. Opción **3**: actor `ana`, reserva `RES-000001` → liberación aceptada.
4. Opción **11**: ID del comando de liberación, actor `ana` → intenta restaurarla.
5. Opción **4** y **5** con actor `supervisor` para demostrar ajustes y transferencias.
6. Opción **7** para mostrar el historial etiquetado.

Las selecciones FEFO/FIFO/LIFO se modifican desde la opción **8**,
y las recomendaciones de reposición desde las opciones **9** y **10**.

## Alcance y límites

Es un **prototipo educativo en memoria**, sin base de datos, API, autenticación real,
transacciones SQL, control distribuido de concurrencia ni persistencia al reiniciar.
El directorio de roles es de demostración; escribir `supervisor` en la consola
NO representa autenticación segura en un sistema real. Las operaciones se serializan
por instancia del receptor, pero no sustituyen transacciones de base de datos.

No se ha agregado un cuarto patrón de diseño. Se han mejorado los tres existentes.

## Más documentación

- [`docs/CORRECCIONES_FEEDBACK_LUIS.md`](docs/CORRECCIONES_FEEDBACK_LUIS.md): cambios por observación del profesor.
- [`docs/DISENO_DEL_SISTEMA.md`](docs/DISENO_DEL_SISTEMA.md): arquitectura, flujo y SOLID.
- [`docs/CODESPACES_UPDATE.md`](docs/CODESPACES_UPDATE.md): actualización segura en el repositorio existente.

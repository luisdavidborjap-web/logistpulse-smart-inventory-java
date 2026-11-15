# LOGISTPULSE — Diseño del sistema

## Descripción funcional

Prototipo por consola de una empresa logística que almacena lotes de productos identificados por SKU y controla las reservas, el despacho, las transferencias, el almacenamiento de partidas recibidas y las restricciones de calidad. Todos los cambios de inventario pasan por operaciones validadas; se registran las decisiones de los comandos y se emiten eventos después de mutaciones exitosas.

## Responsabilidades y SOLID

| Responsabilidad | Componente |
|---|---|
| Interacción con usuario | `App` |
| Composición / inyección de dependencias | `SampleData` |
| Encapsular y auditar comandos | `Command`, `CommandInvoker`, `AuditLedger` |
| Comprobar acceso y reglas | `chain.ValidationHandler` y eslabones |
| Planificar lotes | `strategy.picking.PickingContext` + políticas |
| Calcular recomendaciones | `strategy.replenishment.ReplenishmentContext` + políticas |
| Mantener invariantes de cantidades y estados | `domain.StockLot`, `state.LotState` y estados concretos |
| Reservas y despacho | `ReservationService` |
| Movimientos de stock y calidad | `StockMutationService` |
| Orquestar la aplicación | `StockService`, mediante `StockOperations` |
| Emitir hechos inmutables | `observer.InventoryEventCoordinator`, `InventoryEventBus` |
| Conservar hechos y reaccionar | `InventoryEventJournal`, `ReplenishmentAlertListener`, `PulseFleetListener` |

El sistema aplica **SRP** separando validación, política, mutación y publicación; **OCP** permitiendo nuevas estrategias y suscriptores sin reescribir el flujo; **LSP** definiendo un comportamiento conocido para cada estado; **ISP** separando contratos de comando, operaciones, políticas, eventos y listeners; **DIP** haciendo depender los comandos de `StockOperations` y el coordinador de `InventoryEventPublisher`.

## Detalles de State

`StockLot` es el *contexto* del patrón State. Su campo privado `LotState` se inicializa como `STORED` para inventario existente, `QUARANTINED` para productos inmovilizados, o `RECEIVED` mediante la factoría `StockLot.received(...)`. No es una sola enumeración con un `switch` central: cada estado implementa sus transiciones permitidas y rechaza las ilegales.

- **RECEIVED → STORED** por put-away (`StoreLotCommand`), solo supervisor.
- **STORED → RESERVED** al reservar. En un lote con múltiples reservas, seguirá reservado mientras quede al menos una.
- **RESERVED → STORED** tras liberar el último apartado o completar un despacho parcial respecto al stock total.
- **RESERVED → SHIPPED** al despachar las últimas unidades físicas del lote. SHIPPED es terminal.
- **RECEIVED/STORED ↔ QUARANTINED** mediante inmovilización y aprobación de calidad; no admite reservas activas.

`ReservationStatus` es otra noción: una reserva se encuentra `ACTIVE`, `RELEASED` o `SHIPPED`. El estado de la reserva no es igual al estado del lote.

### Invariantes

- `0 <= reserved <= onHand` en cada `StockLot`.
- La cantidad preparada `staged` no supera `stagingCapacity`.
- Un lote caducado no es elegible, aun cuando un supervisor lo saque de cuarentena.
- El ajuste y traslado no consumen cantidades reservadas.
- Al despachar una reserva se descuenta `qty` tanto de `reserved` como de `onHand`; al liberar solo disminuye `reserved`.
- Solo se altera el estado o los contadores después de comprobar precondiciones; no se avanza ante errores de validación.

## Detalles de Observer

`InventoryEventCoordinator` toma instantáneas del inventario antes/después de una ejecución aprobada y compara los cambios observables. Publica objetos inmutables a través de `InventoryEventPublisher`. `InventoryEventBus` es el publicador concreto con `subscribe` y `unsubscribe`.

- `StockChangedEvent`: `lotId`, `sku`, `warehouseId`, cantidad física y reservada anterior y nueva.
- `LotStateChangedEvent`: anterior y siguiente estado del lote.
- `ReservationChangedEvent`: anterior y siguiente `ReservationStatus`.
- `LotRemovedEvent`: eliminación segura de un lote transferido cuando se revierte la transferencia.

Las notificaciones están en el *pasado* (hechos), no son instrucciones de modificación. El historial de Command y el historial de eventos tienen propósitos diferentes y se muestran por separado.

### Contrato de entrega

- Se publican eventos **solo** para operaciones exitosas y con datos ya modificados.
- Un reintento idempotente no reejecuta `StockOperations`, por lo tanto tampoco vuelve a publicar.
- Un consumidor que falla no altera el resultado de una mutación confirmada: el bus registra un fallo de entrega y continúa notificando al resto.
- La entrega es **en memoria, síncrona y no durable**; no existe entrega *exactly once* ante caída del proceso ni una transacción que abarque publicación y base de datos.

## Decisiones intencionales

- `ShipCommand.undo()` rechaza deshacer una salida física de mercancía. La devolución real exige un proceso separado para preservar trazabilidad.
- `StoreLotCommand.undo()` solo regresa a RECEIVED si el lote se almacenó y nadie volvió a tocarlo. Si hubo cambios, la cadena rechaza la compensación por seguridad.
- Las políticas de selección operan antes de Chain; la cadena puede rechazar el plan, pero la política no valida autorizaciones.
- La reimplementación para producción requeriría persistencia de `commandId`, transacciones, autorización real, outbox de eventos, observabilidad, concurrencia distribuida y manejo de inventario por movimiento/ubicación.

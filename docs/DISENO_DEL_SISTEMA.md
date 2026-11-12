# Documento de diseño — LOGISTPULSE Smart Inventory

## 1. Contexto y problema

LOGISTPULSE es una propuesta de inventario inteligente orientada al seguimiento de productos por SKU y lote. Su problema principal en este prototipo es impedir **reservas inválidas**, registrar las decisiones y desacoplar el programa que recibe la petición de la lógica de inventario.

**Base documental:** presentación adjunta *Patrones de comportamiento en el Smart Inventory de LOGISTPULSE*, especialmente:

- Diapositiva 1: flujo de `POST /reservations` y patrones del ecosistema completo.
- Diapositiva 2: **Command**; `Reserve`, `Release`, `Adjust`, `Transfer`, ledger, idempotencia y compensaciones.
- Diapositiva 3: **Chain of Responsibility**; validaciones `SKU → stock → lote → capacidad`.

**Selección de alcance:** este repositorio implementa únicamente **Reserve** y **Release** de Command y los cuatro manejadores de Chain. No implementa Adjust/Transfer, API REST ni los otros cuatro patrones de la presentación.

## 2. Objetivos y requisitos

**Objetivo:** permitir reservar y liberar stock con reglas verificables, historial auditable e idempotencia en una demostración de consola.

Requisitos funcionales:

1. Listar productos, lotes, unidades disponibles y reservadas.
2. Crear solicitud con SKU, ID de lote, cantidad y solicitante.
3. Validar existencia del SKU, cantidad positiva y actor no vacío.
4. Validar stock reservable sumado sobre lotes habilitados del SKU.
5. Validar pertenencia, existencia, fecha de vencimiento, cuarentena y disponibilidad del lote solicitado.
6. Validar espacio en la **zona de preparación**. Este concepto es una regla propuesta para dar sentido operativo a la validación de capacidad; reservar no aumenta el espacio físico usado en bodega.
7. Si todas pasan, descontar unidades disponibles, incrementar reservadas y ocupar preparación.
8. Permitir liberación total por UUID de reserva activa; devolver unidades y desocupar preparación.
9. Registrar en el ledger aprobaciones y rechazos; impedir dos ejecuciones del mismo comando con el mismo ID.

## 3. Arquitectura de capas

- `App`: interfaz de consola y **invocador**, no implementa reglas de reserva.
- `Comando`: interfaz del patrón Command con `execute()`.
- `ProcesadorComandos`: ejecutor y guardián de la idempotencia; captura resultados y registra el ledger.
- `ServicioInventario`: **receptor** de los comandos; configura la cadena de validaciones y modifica el dominio.
- `ManejadorReserva`: interfaz común por herencia para las validaciones.
- `Inventario`, `Lote`, `Reserva`, `Producto`, `Almacen`: entidades de negocio.
- `LibroAuditoria`: registro en memoria de resultados.

## 4. Patrón Command

**Problema:** si cada botón/API modifica directamente el inventario, se duplican reglas, resulta difícil auditar decisiones y reintentos pueden descontar dos veces las unidades.

**Solución:** cada acción es un objeto:

- Interfaz `Comando` → `execute()`, ID, tipo, actor y huella.
- `ReservarStockCommand` → encapsula los datos y solicita una reserva al receptor.
- `LiberarStockCommand` → revierte el efecto de una reserva activa en una operación nueva.
- `ProcesadorComandos` → invoca, detecta reintentos por ID, guarda el resultado y registra auditoría.
- `ServicioInventario` → receptor.

**Idempotencia:** durante la vida del proceso, reenviar un comando con igual UUID e igual contenido devuelve el resultado original sin repetir cambios ni duplicar el asiento. Reutilizar UUID con otra solicitud se rechaza. Un comando nuevo es una operación diferente.

**Compensación:** `LiberarStockCommand` devuelve la cantidad reservada al lote y libera capacidad; no es un `undo` general de toda la aplicación.

## 5. Patrón Chain of Responsibility

Cada manejador puede rechazar con `ValidacionException`, propagando una razón explícita:

1. `ValidarSku`: SKU registrado, cantidad positiva y solicitante.
2. `ValidarStock`: stock reservable global del SKU (no vencido/no cuarentena) suficiente.
3. `ValidarLote`: lote existe, pertenece al SKU, no vencido, no cuarentena y tiene unidades.
4. `ValidarCapacidad`: la zona de preparación admite la cantidad solicitada.

La cadena se construye en `ServicioInventario` usando `enlazar(...)`. Si no se lanza excepción, todas pasaron; la reserva se confirma.

**Importante:** hay dos controles de cantidades distintos: stock agregado por SKU y stock del lote concreto. Por ejemplo, el SKU puede disponer de 10 unidades en total pero el lote seleccionado solo de 2; una reserva de 3 debe rechazarse en `ValidarLote`.

## 6. Datos iniciales de demostración

| SKU | Lote | Disponible | Estado | Vencimiento |
|---|---|---:|---|---|
| MED-001 | L-100 | 10 | Habilitado | 2099-12-31 |
| MED-002 | L-200 | 6 | Habilitado | 2099-12-31 |
| MED-002 | L-201 | 5 | Cuarentena | 2099-12-31 |
| MED-002 | L-202 | 4 | Vencido | 2020-01-01 |
| TEC-001 | L-300 | 3 | Habilitado | 2099-12-31 |

Capacidad de preparación: **8 unidades**. Son datos ficticios para demostrar validaciones.

## 7. Invariantes y consideraciones de diseño

- `Lote.disponible >= 0`, `Lote.reservado >= 0`.
- `ocupaciónPreparación` entre 0 y `capacidadPreparacion`.
- La reserva aprobada consume stock del lote exacto; reserva y liberación son operaciones de unidades enteras.
- No se puede liberar dos veces una reserva activa con IDs de comando diferentes.
- Fecha de vencimiento igual a hoy se considera válida para esta demostración; el lote está vencido cuando `vencimiento < hoy`.
- Los identificadores de lote son únicos dentro del inventario.
- Solo se aplica reserva de lote **seleccionado manualmente**; FEFO no está implementado.

## 8. Limitaciones y cómo evolucionar

- **Persistencia:** producto, reservas, resultados idempotentes y auditoría viven solo en memoria y desaparecen al reiniciar.
- **Atomicidad:** para varias instancias/API y concurrencia real se necesita una transacción de BD que englobe actualizar lote, capacidad, reserva, resultados y auditoría. El `synchronized` del procesador solo protege las llamadas que pasan por esa instancia.
- **Control de usuarios:** el campo solicitante es descriptivo, no verifica identidades.
- **Cadena de validación:** sin API, cada usuario selecciona un lote explícitamente; no hay algoritmo de selección automática.
- **Retención del ledger:** en esta implementación los resultados se almacenan temporalmente, no cumplen requisitos de auditoría regulada.
- **Fechas:** las comprobaciones se realizan al momento de la reserva, no de forma programada.

**Evolución recomendada:** Spring Boot + PostgreSQL + endpoints `GET /inventory`, `POST /reservations` y `POST /reservations/{id}/release`; control transaccional; pruebas de concurrencia; seguridad; y después políticas FEFO/FIFO como Strategy, si el curso permite ampliar patrones.

## 9. Argumento para la defensa oral

> Elegimos Command porque convierte cada operación en una entidad ejecutable, repetible de forma segura y auditable; y Chain of Responsibility porque la reserva depende de validaciones independientes que deben ejecutarse en orden y detenerse en el primer incumplimiento. Juntos forman una ruta que permite registrar por qué el sistema aprobó o rechazó una reserva, sin mezclar esas reglas con la interfaz de usuario.

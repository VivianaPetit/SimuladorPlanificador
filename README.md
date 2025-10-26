# 🖥️ Simulador de Planificación de Procesos

Un simulador completo de algoritmos de planificación de procesos desarrollado en Java, que permite analizar y comparar el rendimiento de diferentes políticas de scheduling en sistemas operativos.

## 🚀 Características Principales

### ⚡ Algoritmos Implementados
- **FCFS** (First-Come, First-Served) - Planificación por orden de llegada
- **RR** (Round Robin) - Planificación por turnos con quantum 5
- **SPN** (Shortest Process Next) - Prioriza procesos más cortos
- **SRT** (Shortest Remaining Time) - SPN con preempción
- **HRRN** (Highest Response Ratio Next) - Balance entre espera y tamaño
- **Feedback** - Colas multinivel con prioridades dinámicas

### 📊 Métricas en Tiempo Real
- **Throughput** - Procesos completados por unidad de tiempo
- **Utilización de CPU** - Porcentaje de uso del procesador
- **Tiempo de Respuesta** - Hasta primera ejecución

## 🛠️ Tecnologías Utilizadas

- **Java** - Lenguaje principal de desarrollo
- **JFreeChart** - Visualización de gráficas y métricas
- **Estructuras de Datos Personalizadas** - Colas y listas enlazadas
- **Programación Multihilo** - Simulación de E/S asíncronas



### 🔄 Cambio Dinámico de Schedulers
```java
cpu.cambiarScheduler(new RR(5));

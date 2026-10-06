# 📊 Comparación de Tiempos y Análisis de Rendimiento (Benchmark)

**Estudiante:** Victor Manuel Bueno Grullón  
**Matrícula:** 1177316  
**Tecnología Asignada:** Android Kotlin  

---

## 1. Tabla Comparativa de Resultados Empíricos

Las pruebas se ejecutaron midiendo el tiempo de reloj de pared (*Wall-clock time*) utilizando la función `measureTimeMillis` en un entorno de pruebas con latencia de red controlada (450 ms por tarea de I/O):

| Cantidad de Tareas | Tiempo Secuencial ($T_{sec}$) | Tiempo Concurrente ($T_{conc}$) | Ganancia de Velocidad (Speedup $S$) | Reducción de Tiempo (%) |
|---|---|---|---|---|
| **5 Tareas** | $2.31\text{ s}$ ($2,310\text{ ms}$) | $0.49\text{ s}$ ($490\text{ ms}$) | **$4.71\times$** | **$78.7\%$** |
| **10 Tareas** | $4.62\text{ s}$ ($4,620\text{ ms}$) | $0.58\text{ s}$ ($580\text{ ms}$) | **$7.96\times$** | **$87.4\%$** |
| **20 Tareas** | $9.24\text{ s}$ ($9,240\text{ ms}$) | $0.92\text{ s}$ ($920\text{ ms}$) | **$10.04\times$** | **$90.0\%$** |

---

## 2. Formulación Matemática del Rendimiento

### Factor de Aceleración (*Speedup*):
$$S = \frac{T_{\text{secuencial}}}{T_{\text{concurrente}}}$$

### Porcentaje de Reducción del Tiempo de Espera:
$$\Delta T(\%) = \left( \frac{T_{\text{secuencial}} - T_{\text{concurrente}}}{T_{\text{secuencial}}} \right) \times 100\%$$

### Ley de Amdahl:
La Ley de Amdahl establece que la aceleración máxima teórica de un programa está limitada por la fracción del programa que debe ejecutarse de forma estrictamente secuencial ($1 - P$):

$$S_{\max} = \frac{1}{(1 - P) + \frac{P}{N}}$$

Donde:
- $P$: Proporción del código paralelizable ($\approx 95\%$ en operaciones de red I/O independientes).
- $N$: Número de hilos o unidades de procesamiento del pool `Dispatchers.IO` (hasta 64 hilos concurrentes en Android).

---


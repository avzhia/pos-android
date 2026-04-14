# POS Naturista — App Android

App nativa Android para el sistema POS TiendaNaturistaMX.
Apunta a: **https://pos.minube.icu**

## Compilar con GitHub Actions (sin Android Studio)

1. Sube este proyecto a un repositorio de GitHub
2. Ve a **Actions** → **Build APK** → **Run workflow**
3. Espera ~3 minutos
4. Descarga el APK desde **Artifacts → pos-naturista-debug**
5. Instala en el celular habilitando "Fuentes desconocidas"

## Funcionalidades

- ✅ Login propio (tienda, cajero, PIN, fondo inicial)
- ✅ Panel Ventas con grid de productos y categorías
- ✅ Escáner de código de barras (cámara nativa)
- ✅ Ticket con ajuste de cantidades
- ✅ Cobro con 3 formas de pago y cálculo de cambio
- ✅ Panel Inventario con stock en tiempo real
- ✅ Panel Clientes con historial de compras
- ✅ Sesión persistente (no pide login en cada apertura)
- ✅ Botón Salir con confirmación

## Estructura del proyecto

```
app/src/main/
├── java/com/tiendanaturista/pos/
│   ├── data/           — Modelos, API, Sesión
│   └── ui/             — Activities, Fragments, Adapters
├── res/
│   ├── layout/         — Layouts XML
│   ├── values/         — Colores, strings, temas
│   └── drawable/       — Fondos y formas
└── AndroidManifest.xml
```

## Cambiar URL del servidor

Edita `app/src/main/java/com/tiendanaturista/pos/data/ApiService.kt`:
```kotlin
private const val BASE_URL = "https://pos.minube.icu/"
```

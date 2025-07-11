package com.alura.demo.principal;

import com.alura.demo.model.Datos;
import com.alura.demo.model.DatosLibros;
import com.alura.demo.service.ConsumoAPI;
import com.alura.demo.service.ConvierteDatos;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Optional;
import java.util.Scanner;

/**
 * Clase Principal: controla el flujo de la aplicación de consola.
 * - Consulta datos iniciales de libros.
 * - Muestra el top 10 por descargas.
 * - Permite búsquedas por título.
 */
public class Principal {
    // URL base de la API de Gutendex (con barra final para evitar problemas de redirección)
    private static final String URL_BASE = "https://gutendex.com/books/";

    // Cliente para realizar peticiones HTTP
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    // Objeto para convertir JSON a objetos Java
    private final ConvierteDatos conversor = new ConvierteDatos();
    // Scanner para leer input del usuario desde la consola
    private final Scanner teclado = new Scanner(System.in);

    /**
     * Muestra el menú principal con opciones:
     * 1) Listar top 10 libros más descargados
     * 2) Buscar un libro por título
     */
    public void muestraElMenu() {
        // 1. CONSULTA INICIAL a la API sin parámetros
        String json = consumoAPI.obtenerDatos(URL_BASE);
        System.out.println("JSON bruto recibido inicial: \n" + json);

        // 2. CONVERTIR JSON a objeto Datos
        Datos datos = conversor.obtenerDatos(json, Datos.class);
        System.out.println("Datos parseados: " + datos);

        // 3. TOP 10 LIBROS MÁS DESCARGADOS
        System.out.println("\nTop 10 libros más descargados:");
        datos.resultados().stream()
                .sorted(Comparator.comparing(DatosLibros::numeroDeDescargas).reversed()) // ordenar descendentemente
                .limit(10)  // tomar solo los primeros 10
                .map(libro -> libro.titulo().toUpperCase()) // convertir título a mayúsculas
                .forEach(System.out::println);

        // 4. BÚSQUEDA DE LIBRO POR NOMBRE
        System.out.println("\nIngrese el nombre del libro que desea buscar:");
        String tituloLibro = teclado.nextLine().trim(); // leer y limpiar espacios

        try {
            // Codificar término para usar en URL (sustituye espacios y caracteres especiales)
            String termino = URLEncoder.encode(tituloLibro, StandardCharsets.UTF_8);
            String urlBusqueda = URL_BASE + "?search=" + termino;  // URL final de búsqueda

            System.out.println(">> Llamando a la URL de búsqueda: " + urlBusqueda);
            String jsonBusqueda = consumoAPI.obtenerDatos(urlBusqueda);  // llamada HTTP

            if (jsonBusqueda == null || jsonBusqueda.isBlank()) {
                System.err.println("La respuesta de la API está vacía. Puede deberse a un problema de redirección o timeouts.");
                return;
            }
            System.out.println("<< ¡Recibí respuesta de la API! >>");

            // Parsear respuesta de búsqueda
            Datos datosBusqueda = conversor.obtenerDatos(jsonBusqueda, Datos.class);

            // Mostrar títulos obtenidos para control
            System.out.println("\nResultados obtenidos (títulos):");
            datosBusqueda.resultados().stream()
                    .map(DatosLibros::titulo)
                    .forEach(t -> System.out.println("  - " + t));

            // Filtrar coincidencia exacta o parcial en lista de resultados
            Optional<DatosLibros> libroBuscado = datosBusqueda.resultados().stream()
                    .filter(l -> l.titulo().equalsIgnoreCase(tituloLibro)
                            || l.titulo().toLowerCase().contains(tituloLibro.toLowerCase()))
                    .findFirst();

            // Mostrar resultado de búsqueda
            if (libroBuscado.isPresent()) {
                System.out.println("\nLibro encontrado:");
                System.out.println(libroBuscado.get());
            } else {
                System.out.println("\nLibro no encontrado. Revisa la ortografía o los resultados listados arriba.");
            }

            //Trabajando con estdísticas:

        } catch (Exception e) {
            // Capturar posibles errores de URL o parseo
            System.err.println("Error al buscar el libro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

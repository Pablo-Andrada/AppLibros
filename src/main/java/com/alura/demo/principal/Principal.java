package com.alura.demo.principal;

import com.alura.demo.model.Datos;
import com.alura.demo.model.DatosLibros;
import com.alura.demo.service.ConsumoAPI;
import com.alura.demo.service.ConvierteDatos;

import java.util.Comparator;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/" ;
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();


    public void muestraElMenu() {
        var json = consumoAPI.obtenerDatos(URL_BASE);
        System.out.println(json);
        var datos = conversor.obtenerDatos(json, Datos.class);
        System.out.println(datos);

        //Top 10 libros más descargados
        System.out.println("Top 10 libros más descargados: ");
        datos.resultados().stream()
                .sorted(Comparator.comparing(DatosLibros::numeroDeDescargas).reversed())
                .limit(10)
                .map(l->l.titulo().toUpperCase())
                .forEach(System.out::println);

    }
}

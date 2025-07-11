package com.alura.demo.service;

public interface IConvierteDatos {
    <T> T obtenerDatos (String json, Class<T> clase);
}

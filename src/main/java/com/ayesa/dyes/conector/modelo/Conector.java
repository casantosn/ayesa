package com.ayesa.dyes.conector.modelo;

/**
 * Conector asignado a una señal. Para conectores lógicos {@code borna} es {@code null}.
 */
public record Conector(String nombre, String borna, TipoConector tipo) {

    public static Conector fisico(String nombre, String borna) {
        return new Conector(nombre, borna, TipoConector.FISICO);
    }

    public static Conector logico(String nombre) {
        return new Conector(nombre, null, TipoConector.LOGICO);
    }
}

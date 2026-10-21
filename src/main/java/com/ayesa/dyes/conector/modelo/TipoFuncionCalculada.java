package com.ayesa.dyes.conector.modelo;

/** Fila de la tabla {@code tipofuncioncalculada}. Hoy sólo existen tipo = 'PLC' o tipo = 'Intrínseca'. */
public record TipoFuncionCalculada(Long id, String tipo) {
}

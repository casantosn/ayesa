package com.ayesa.dyes.conector.modelo;

import java.util.List;

/** Fila de la tabla {@code funcioncalculada}; {@code tipoFuncionCalculada} es la FK {@code idtipofuncioncalculada}. */
public record FuncionCalculada(Long id,
                               String nombre,
                               TipoFuncionCalculada tipoFuncionCalculada,
                               List<EntradaFuncionCalculada> entradas) {

    public FuncionCalculada {
        entradas = entradas == null ? List.of() : List.copyOf(entradas);
    }
}

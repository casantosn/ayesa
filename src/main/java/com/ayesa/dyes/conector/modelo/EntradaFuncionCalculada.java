package com.ayesa.dyes.conector.modelo;

/** Entrada de una función calculada. {@code orden} indica su posición (la menor es la primera entrada). */
public record EntradaFuncionCalculada(Long id, int orden, Long idSenalInventario) {
}

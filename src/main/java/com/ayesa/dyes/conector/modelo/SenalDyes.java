package com.ayesa.dyes.conector.modelo;

/** Señal DYES; {@code tipoSenalDyes} es la FK {@code idtiposenaldyes}. */
public record SenalDyes(Long id, String nombre, TipoSenalDyes tipoSenalDyes) {
}

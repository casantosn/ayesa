package com.ayesa.dyes.conector.servicio;

import com.ayesa.dyes.conector.modelo.Conector;
import com.ayesa.dyes.conector.modelo.EntradaFuncionCalculada;

import java.util.Optional;

/**
 * Acceso al inventario para obtener el conector físico y la borna de una entrada.
 * Implementarlo con el repositorio/DAO del inventario.
 */
@FunctionalInterface
public interface InventarioConectores {

    Optional<Conector> buscarConectorFisico(EntradaFuncionCalculada entrada);
}

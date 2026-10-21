package com.ayesa.dyes.conector.servicio;

import com.ayesa.dyes.conector.modelo.Conector;
import com.ayesa.dyes.conector.modelo.EntradaFuncionCalculada;
import com.ayesa.dyes.conector.modelo.FuncionCalculada;
import com.ayesa.dyes.conector.modelo.GrupoTipoSenalDyes;
import com.ayesa.dyes.conector.modelo.SenalDyes;
import com.ayesa.dyes.conector.modelo.SenalDyesSc;
import com.ayesa.dyes.conector.modelo.TipoFuncionCalculada;
import com.ayesa.dyes.conector.modelo.TipoSenalDyes;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Determina el conector de las señales calculadas (salidas de funciones calculadas).
 *
 * <ul>
 *   <li>Función 'Intrínseca': conector físico y borna de la primera entrada de la función en el inventario.</li>
 *   <li>Función 'PLC': conector lógico según el grupo de tipo de señal DYES:
 *     <ul>
 *       <li>'SD' o 'SA': {@code SDLOG NN}, con un contador compartido por SD y SA.</li>
 *       <li>Cualquier otro grupo: {@code <grupo>CAL NN} (EDCAL 01, EACAL 02, ...), con un único
 *           contador para todos esos grupos.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * Los contadores viven en la instancia: usar una instancia por proceso de generación para que
 * los nombres no se repitan dentro de él.
 */
public class ConectorSenalCalculadaResolver {

    static final String TIPO_INTRINSECA = "INTRINSECA";
    static final String TIPO_PLC = "PLC";
    static final Set<String> GRUPOS_SDLOG = Set.of("SD", "SA");

    private final InventarioConectores inventario;
    private final AtomicInteger contadorSdLog;
    private final AtomicInteger contadorCal;

    public ConectorSenalCalculadaResolver(InventarioConectores inventario) {
        this(inventario, 0, 0);
    }

    /**
     * Permite arrancar los contadores desde el último valor ya usado (p. ej. el máximo existente en BD),
     * para no repetir nombres generados en ejecuciones anteriores.
     */
    public ConectorSenalCalculadaResolver(InventarioConectores inventario, int ultimoSdLog, int ultimoCal) {
        this.inventario = Objects.requireNonNull(inventario, "inventario");
        this.contadorSdLog = new AtomicInteger(ultimoSdLog);
        this.contadorCal = new AtomicInteger(ultimoCal);
    }

    public Conector resolver(SenalDyesSc senalCalculada) {
        Objects.requireNonNull(senalCalculada, "senalCalculada");
        FuncionCalculada funcion = Objects.requireNonNull(senalCalculada.funcionCalculada(),
                () -> "La señal calculada " + senalCalculada.id() + " no tiene función calculada");

        String tipo = normalizar(Optional.ofNullable(funcion.tipoFuncionCalculada())
                .map(TipoFuncionCalculada::tipo)
                .orElseThrow(() -> new IllegalStateException(
                        "La función calculada " + funcion.id() + " no tiene tipo")));

        return switch (tipo) {
            case TIPO_INTRINSECA -> conectorFisico(funcion);
            case TIPO_PLC -> conectorLogico(senalCalculada);
            default -> throw new IllegalStateException(
                    "Tipo de función calculada no soportado: " + funcion.tipoFuncionCalculada().tipo());
        };
    }

    private Conector conectorFisico(FuncionCalculada funcion) {
        EntradaFuncionCalculada primeraEntrada = funcion.entradas().stream()
                .min(Comparator.comparingInt(EntradaFuncionCalculada::orden))
                .orElseThrow(() -> new IllegalStateException(
                        "La función calculada intrínseca " + funcion.id() + " no tiene entradas"));
        return inventario.buscarConectorFisico(primeraEntrada)
                .orElseThrow(() -> new IllegalStateException(
                        "La primera entrada " + primeraEntrada.id() + " de la función calculada "
                                + funcion.id() + " no tiene conector en el inventario"));
    }

    private Conector conectorLogico(SenalDyesSc senalCalculada) {
        String grupo = grupoTipoSenal(senalCalculada);
        if (GRUPOS_SDLOG.contains(grupo)) {
            return Conector.logico(formatear("SDLOG", contadorSdLog.incrementAndGet()));
        }
        return Conector.logico(formatear(grupo + "CAL", contadorCal.incrementAndGet()));
    }

    private static String grupoTipoSenal(SenalDyesSc senalCalculada) {
        return Optional.ofNullable(senalCalculada.senalDyes())
                .map(SenalDyes::tipoSenalDyes)
                .map(TipoSenalDyes::grupoTipoSenalDyes)
                .map(GrupoTipoSenalDyes::grupo)
                .map(g -> g.trim().toUpperCase(Locale.ROOT))
                .filter(g -> !g.isEmpty())
                .orElseThrow(() -> new IllegalStateException(
                        "La señal calculada " + senalCalculada.id() + " no tiene grupo de tipo de señal DYES"));
    }

    private static String formatear(String prefijo, int numero) {
        return String.format("%s %02d", prefijo, numero);
    }

    /** Mayúsculas y sin tildes, para aceptar tanto 'Intrínseca' como 'Intrinseca'. */
    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }
}

package com.ayesa.dyes.conector.servicio;

import com.ayesa.dyes.conector.modelo.Conector;
import com.ayesa.dyes.conector.modelo.EntradaFuncionCalculada;
import com.ayesa.dyes.conector.modelo.FuncionCalculada;
import com.ayesa.dyes.conector.modelo.GrupoTipoSenalDyes;
import com.ayesa.dyes.conector.modelo.SenalDyes;
import com.ayesa.dyes.conector.modelo.SenalDyesSc;
import com.ayesa.dyes.conector.modelo.TipoConector;
import com.ayesa.dyes.conector.modelo.TipoFuncionCalculada;
import com.ayesa.dyes.conector.modelo.TipoSenalDyes;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConectorSenalCalculadaResolverTest {

    private static final TipoFuncionCalculada PLC = new TipoFuncionCalculada(1L, "PLC");
    private static final TipoFuncionCalculada INTRINSECA = new TipoFuncionCalculada(2L, "Intrínseca");

    private final Map<Long, Conector> inventario = Map.of(
            100L, Conector.fisico("X1", "3"),
            200L, Conector.fisico("X2", "7"));

    private final ConectorSenalCalculadaResolver resolver = new ConectorSenalCalculadaResolver(
            entrada -> Optional.ofNullable(inventario.get(entrada.idSenalInventario())));

    @Test
    void intrinsecaUsaConectorYBornaDeLaPrimeraEntrada() {
        FuncionCalculada funcion = new FuncionCalculada(1L, "f", INTRINSECA, List.of(
                new EntradaFuncionCalculada(11L, 2, 100L),
                new EntradaFuncionCalculada(10L, 1, 200L)));

        Conector conector = resolver.resolver(senal("ED", funcion));

        assertEquals(Conector.fisico("X2", "7"), conector);
    }

    @Test
    void intrinsecaSinTildeTambienSeReconoce() {
        FuncionCalculada funcion = new FuncionCalculada(1L, "f", new TipoFuncionCalculada(2L, "Intrinseca"),
                List.of(new EntradaFuncionCalculada(10L, 1, 100L)));

        assertEquals(Conector.fisico("X1", "3"), resolver.resolver(senal("SD", funcion)));
    }

    @Test
    void plcSdYSaCompartenContadorSdlog() {
        Conector c1 = resolver.resolver(senal("SD", plc()));
        Conector c2 = resolver.resolver(senal("SA", plc()));
        Conector c3 = resolver.resolver(senal("SD", plc()));

        assertEquals("SDLOG 01", c1.nombre());
        assertEquals("SDLOG 02", c2.nombre());
        assertEquals("SDLOG 03", c3.nombre());
        assertEquals(TipoConector.LOGICO, c1.tipo());
        assertNull(c1.borna());
    }

    @Test
    void plcOtrosGruposCompartenContadorCal() {
        assertEquals("EDCAL 01", resolver.resolver(senal("ED", plc())).nombre());
        assertEquals("EACAL 02", resolver.resolver(senal("EA", plc())).nombre());
        assertEquals("EDCAL 03", resolver.resolver(senal("ED", plc())).nombre());
    }

    @Test
    void contadoresSdlogYCalSonIndependientes() {
        assertEquals("EDCAL 01", resolver.resolver(senal("ED", plc())).nombre());
        assertEquals("SDLOG 01", resolver.resolver(senal("SD", plc())).nombre());
        assertEquals("EACAL 02", resolver.resolver(senal("EA", plc())).nombre());
        assertEquals("SDLOG 02", resolver.resolver(senal("SA", plc())).nombre());
    }

    @Test
    void contadoresPuedenArrancarDesdeUltimoValorUsado() {
        ConectorSenalCalculadaResolver r = new ConectorSenalCalculadaResolver(e -> Optional.empty(), 5, 9);

        assertEquals("SDLOG 06", r.resolver(senal("SD", plc())).nombre());
        assertEquals("EDCAL 10", r.resolver(senal("ED", plc())).nombre());
    }

    @Test
    void intrinsecaSinEntradasFalla() {
        FuncionCalculada funcion = new FuncionCalculada(1L, "f", INTRINSECA, List.of());

        assertThrows(IllegalStateException.class, () -> resolver.resolver(senal("ED", funcion)));
    }

    @Test
    void intrinsecaSinConectorEnInventarioFalla() {
        FuncionCalculada funcion = new FuncionCalculada(1L, "f", INTRINSECA,
                List.of(new EntradaFuncionCalculada(10L, 1, 999L)));

        assertThrows(IllegalStateException.class, () -> resolver.resolver(senal("ED", funcion)));
    }

    @Test
    void tipoDesconocidoFalla() {
        FuncionCalculada funcion = new FuncionCalculada(1L, "f", new TipoFuncionCalculada(3L, "Otro"), List.of());

        assertThrows(IllegalStateException.class, () -> resolver.resolver(senal("ED", funcion)));
    }

    private static FuncionCalculada plc() {
        return new FuncionCalculada(1L, "f", PLC, List.of());
    }

    private static SenalDyesSc senal(String grupo, FuncionCalculada funcion) {
        TipoSenalDyes tipo = new TipoSenalDyes(1L, new GrupoTipoSenalDyes(1L, grupo));
        return new SenalDyesSc(1L, new SenalDyes(1L, "s", tipo), funcion);
    }
}

package com.app.preguntas.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "app-estadistica")
public interface EstadisticaFeignClient {

	@PostMapping("/resultados/crear/")
	public Boolean crearResultados(@RequestParam("idProyecto") Integer idProyecto,
			@RequestParam("formulario") Integer formulario, @RequestParam("numeroPregunta") Integer numeroPregunta,
			@RequestParam("pregunta") String pregunta, @RequestParam("tipoConsulta") Integer tipoConsulta,
			@RequestParam("opciones") List<String> opciones,
			@RequestParam("mensajeImpacto") List<String> mensajeImpacto);

}

package com.app.preguntas.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "app-respuestas")
public interface RespuestasFeignClient {

	@DeleteMapping("/respuestas/eliminar/proyecto/formulario/{idProyecto}")
	public Boolean eliminarRespuestasProyectoFormulario(@PathVariable("idProyecto") Integer idProyecto,
			@RequestParam("formulario") Integer formulario);

	@DeleteMapping("/respuestas/eliminar/proyecto/pregunta/{idProyecto}")
	public Boolean eliminarRespuestasProyectoFormularioPregunta(@PathVariable("idProyecto") Integer idProyecto,
			@RequestParam("formulario") Integer formulario, @PathVariable("numeroPregunta") Integer numeroPregunta);

}

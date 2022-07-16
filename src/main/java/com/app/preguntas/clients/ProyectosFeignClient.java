package com.app.preguntas.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "app-proyectos")
public interface ProyectosFeignClient {

	@GetMapping("/proyectos/exists-codigo-proyecto/")
	public Boolean existCodigoProyecto(@RequestParam("codigoProyecto") Integer codigoProyecto);

	@GetMapping("/proyectos/obtener-lista-codigo-proyecto/")
	public List<Integer> obtenerListaCodigo();
}

package com.app.preguntas.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.app.preguntas.clients.EstadisticaFeignClient;
import com.app.preguntas.clients.ProyectosFeignClient;
import com.app.preguntas.models.Preguntas;
import com.app.preguntas.repository.PreguntasRepository;
import com.app.preguntas.services.IPreguntasServices;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class PreguntaController {

	@SuppressWarnings("rawtypes")
	@Autowired
	private CircuitBreakerFactory cbFactory;

	@Autowired
	IPreguntasServices pServices;

	@Autowired
	PreguntasRepository pRepository;

	@Autowired
	ProyectosFeignClient prClient;

	@Autowired
	EstadisticaFeignClient eClient;

//  ****************************	PREGUNTAS	***********************************  //

	// CREAR PREGUNTA
	@PostMapping("/preguntas/pregunta/")
	@ResponseStatus(code = HttpStatus.CREATED)
	public Boolean crearPreguntas(@RequestBody @Validated Preguntas pregunta) {
		if (pregunta.getTipoConsulta() != 5 && (pregunta.getOpciones().size() == 0))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Llene las opciones");
		if (pregunta.getTipoConsulta() == 6 && pregunta.getOpciones().size() != 2)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "2 opciones para la pregunta tipo Kano");
		if (pregunta.getPriorizacion() == null)
			pregunta.setPriorizacion("Priorizacion");
		if (pregunta.getImpacto() == null) {
			if (pregunta.getTipoConsulta() == 4 || pregunta.getTipoConsulta() == 1)
				pregunta.setImpacto(Arrays.asList("Excelente impacto", "Alto impacto", "Medio Impacto", "Bajo Impacto",
						"Pesimo Impacto"));
			if (pregunta.getTipoConsulta() == 6)
				pregunta.setImpacto(
						Arrays.asList("Atractiva", "Gusta", "Basica", "Indeferente", "No Gusta", "Cuestionable"));
			else
				pregunta.setImpacto(new ArrayList<String>());
		} else if ((pregunta.getTipoConsulta() == 1 || pregunta.getTipoConsulta() == 4)
				&& pregunta.getImpacto().size() != 5)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "5 impactos para la pregunta tipo 1 y 4");
		else if (pregunta.getTipoConsulta() == 6 && pregunta.getImpacto().size() != 6)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "6 impactos para la pregunta tipo kano");
		if (pregunta.getFormulario() == null)
			pregunta.setFormulario(1);
		if (cbFactory.create("preguntas").run(() -> prClient.existCodigoProyecto(pregunta.getIdProyecto()),
				e -> encontrarProyecto(pregunta.getIdProyecto(), e))) {
			if (!pServices.existsIdNumeroFormulario(pregunta.getIdProyecto(), pregunta.getNumeroPregunta(),
					pregunta.getFormulario())) {

				if (cbFactory.create("preguntas")
						.run(() -> eClient.crearResultados(pregunta.getIdProyecto(), pregunta.getFormulario(),
								pregunta.getNumeroPregunta(), pregunta.getPregunta(), pregunta.getTipoConsulta(),
								pregunta.getOpciones(), pregunta.getImpacto()), e -> errorConexionEstadistica(e))) {
					log.info("Creacion Correcta Formulario -> Estadistica");
					pServices.crearPregunta(pregunta);
					return true;
				}
			}
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pregunta numero " + pregunta.getNumeroPregunta()
					+ ", Ya existe en el formulario " + pregunta.getFormulario());
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	// EDITAR PREGUNTA
	@PutMapping("/preguntas/editar/")
	@ResponseStatus(code = HttpStatus.OK)
	public Boolean editarPreguntas(@RequestBody @Validated Preguntas pregunta) throws Exception {
		if (cbFactory.create("preguntas").run(() -> prClient.existCodigoProyecto(pregunta.getIdProyecto()),
				e -> encontrarProyecto(pregunta.getIdProyecto(), e))) {
			if (pregunta.getFormulario() == null)
				pregunta.setFormulario(1);
			if (pServices.existsIdNumeroFormulario(pregunta.getIdProyecto(), pregunta.getNumeroPregunta(),
					pregunta.getFormulario())) {
				pServices.editarPregunta(pregunta);
				return true;
			}
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pregunta no existe");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El proyecto no existe");
	}

	// VER TODAS PREGUNTAS
	@GetMapping("/preguntas/todas/{idProyecto}/{formulario}/")
	@ResponseStatus(code = HttpStatus.OK)
	public List<Preguntas> verTodasPreguntas(@PathVariable(value = "idProyecto") Integer idProyecto,
			@PathVariable(value = "formulario", required = false) Integer formulario) {
		if (cbFactory.create("preguntas").run(() -> prClient.existCodigoProyecto(idProyecto),
				e -> encontrarProyecto(idProyecto, e))) {
			if (formulario == null)
				formulario = 1;
			if (pServices.existIdFormulario(idProyecto, formulario)) {
				return pServices.verTodas(idProyecto, formulario);
			}
			throw new ResponseStatusException(HttpStatus.LENGTH_REQUIRED, "El Proyecto no tiene preguntas");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	// VER UNA PREGUNTA
	@GetMapping("/preguntas/una/{idProyecto}/{numeroPregunta}/{formulario}/")
	@ResponseStatus(code = HttpStatus.OK)
	public Preguntas verUnaPreguntas(@PathVariable(value = "idProyecto") Integer idProyecto,
			@PathVariable(value = "numeroPregunta") Integer numeroPregunta,
			@PathVariable(value = "formulario", required = false) Integer formulario) {
		if (cbFactory.create("preguntas").run(() -> prClient.existCodigoProyecto(idProyecto),
				e -> encontrarProyecto(idProyecto, e))) {
			if (formulario == null)
				formulario = 1;
			if (pServices.existsIdNumeroFormulario(idProyecto, numeroPregunta, formulario)) {
				return pServices.verUna(idProyecto, numeroPregunta, formulario);
			}
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregunta no existe");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	// VER CANTIDAD PREGUNTAS
	@GetMapping("/preguntas/cantidad/{idProyecto}/{formulario}/")
	@ResponseStatus(code = HttpStatus.OK)
	public Integer verCantidad(@PathVariable(value = "idProyecto") Integer idProyecto,
			@PathVariable(value = "formulario", required = false) Integer formulario) {
		if (cbFactory.create("preguntas").run(() -> prClient.existCodigoProyecto(idProyecto),
				e -> encontrarProyecto(idProyecto, e))) {
			if (formulario == null)
				formulario = 1;
			if (pServices.existIdFormulario(idProyecto, formulario)) {
				return pServices.verTodas(idProyecto, formulario).size();
			}
			throw new ResponseStatusException(HttpStatus.LENGTH_REQUIRED, "El Proyecto no tiene preguntas");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	// MICROSERVICIO RESPUESTAS -> EXISTE PREGUNTA
	@GetMapping("/preguntas/existe-pregunta/")
	public Boolean existePregunta(@RequestParam("idProyecto") Integer idProyecto,
			@RequestParam("numeroPregunta") Integer numeroPregunta, @RequestParam("formulario") Integer formulario)
			throws IOException {
		try {
			return pServices.existsIdNumeroFormulario(idProyecto, numeroPregunta, formulario);
		} catch (Exception e) {
			throw new IOException("error obtener preguntas: " + e.getMessage());
		}
	}

	// ELIMINAR TODAS PREGUNTAS -> FORMULARIO
	@DeleteMapping("/preguntas/todas/{idProyecto}/{formulario}/")
	@ResponseStatus(code = HttpStatus.OK)
	public Boolean eliminarTodasPreguntas(@PathVariable(value = "idProyecto") Integer idProyecto,
			@PathVariable(value = "formulario", required = false) Integer formulario) {
		if (cbFactory.create("preguntas").run(() -> prClient.existCodigoProyecto(idProyecto),
				e -> encontrarProyecto(idProyecto, e))) {
			if (formulario == null)
				formulario = 1;
			if (pServices.existIdFormulario(idProyecto, formulario)) {
				pServices.deleteIdFormulario(idProyecto, formulario);
				return true;
			}
			throw new ResponseStatusException(HttpStatus.LENGTH_REQUIRED, "El Proyecto no tiene preguntas");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	// ELIMINAR UNA PREGUNTAS -> FORMULARIO
	@DeleteMapping("/preguntas/una/{idProyecto}/{numeroPregunta}/{formulario}/")
	@ResponseStatus(code = HttpStatus.OK)
	public Boolean eliminarUnaPregunta(@PathVariable(value = "idProyecto") Integer idProyecto,
			@PathVariable(value = "numeroPregunta") Integer numeroPregunta,
			@PathVariable(value = "formulario", required = false) Integer formulario) {
		if (cbFactory.create("preguntas").run(() -> prClient.existCodigoProyecto(idProyecto),
				e -> encontrarProyecto(idProyecto, e))) {
			if (formulario == null)
				formulario = 1;
			if (pServices.existsIdNumeroFormulario(idProyecto, numeroPregunta, formulario)) {
				pServices.deleteIdNumeroFormulario(idProyecto, numeroPregunta, formulario);
				return true;
			}
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregunta no existe");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	// MICROSERVICIO PROYECTOS -> ELIMINAR PROYECTO
	@DeleteMapping("/preguntas/eliminar-proyecto/{idProyecto}/")
	public Boolean eliminarProyecto(@PathVariable(value = "idProyecto") Integer idProyecto) throws IOException {
		try {
			if (cbFactory.create("preguntas").run(() -> prClient.existCodigoProyecto(idProyecto),
					e -> encontrarProyecto(idProyecto, e))) {
				if (pServices.existId(idProyecto)) {
					pRepository.deleteByIdProyecto(idProyecto);
					return true;
				}
				throw new ResponseStatusException(HttpStatus.LENGTH_REQUIRED, "El Proyecto no tiene preguntas");
			}
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
		} catch (Exception e) {
			throw new IOException("error crear proyecto en preguntas: " + e.getMessage());
		}
	}

//  ****************************	FUNCIONES TOLERANCIA A FALLOS	***********************************  //

	private Boolean errorConexionEstadistica(Throwable e) {
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Servicio de estadisticas no esta disponible");
	}

	private Boolean encontrarProyecto(Integer codigoProyecto, Throwable e) {
		log.error(e.getMessage());
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Servicio de proyectos no esta disponible");
	}

}

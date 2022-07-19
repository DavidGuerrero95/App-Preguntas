package com.app.preguntas.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.app.preguntas.clients.EstadisticaFeignClient;
import com.app.preguntas.clients.RespuestasFeignClient;
import com.app.preguntas.models.Preguntas;
import com.app.preguntas.repository.PreguntasRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PreguntasServices implements IPreguntasServices {

	@SuppressWarnings("rawtypes")
	@Autowired
	private CircuitBreakerFactory cbFactory;

	@Autowired
	PreguntasRepository pRepository;

	@Autowired
	RespuestasFeignClient rClient;

	@Autowired
	EstadisticaFeignClient eClient;

	@Override
	public void crearPregunta(Preguntas p) {
		if (p.getPriorizacion() == null)
			p.setPriorizacion("priorizacion");
		if (p.getFormulario() == null)
			p.setFormulario(1);
		if (p.getTipoConsulta() == 5) {
			p.setPriorizacion("");
			p.setOpciones(new ArrayList<String>());
		}
		pRepository.save(p);
	}

	@Override
	public void editarPregunta(Preguntas pregunta) throws ResponseStatusException {
		Preguntas p = pRepository.findByIdProyectoAndNumeroPreguntaAndFormulario(pregunta.getIdProyecto(),
				pregunta.getNumeroPregunta(), pregunta.getFormulario());
		if (pregunta.getPregunta() != null)
			p.setPregunta(pregunta.getPregunta());
		if (pregunta.getInformacion() != null)
			p.setInformacion(pregunta.getInformacion());
		if (pregunta.getPriorizacion() != null)
			p.setPriorizacion(pregunta.getPriorizacion());
		if (pregunta.getObligatorio() != null)
			p.setObligatorio(pregunta.getObligatorio());
		if (pregunta.getTipoConsulta() != null && !pregunta.getTipoConsulta().equals(p.getTipoConsulta())) {
			if (pregunta.getTipoConsulta() == 5) {
				p.setOpciones(new ArrayList<String>());
				p.setTipoConsulta(pregunta.getTipoConsulta());
				if (cbFactory
						.create("preguntas").run(
								() -> rClient.eliminarRespuestasProyectoFormularioPregunta(pregunta.getIdProyecto(),
										pregunta.getFormulario(), pregunta.getNumeroPregunta()),
								e -> errorConexion(e))) {
					log.info("Eliminacion Correcta -> Respuestas");
				}
			}
			if ((p.getTipoConsulta() == 1 || p.getTipoConsulta() == 4)
					&& (pregunta.getTipoConsulta() == 1 || pregunta.getTipoConsulta() == 4)) {
				p.setTipoConsulta(pregunta.getTipoConsulta());
				if (cbFactory
						.create("preguntas").run(
								() -> rClient.eliminarRespuestasProyectoFormularioPregunta(pregunta.getIdProyecto(),
										pregunta.getFormulario(), pregunta.getNumeroPregunta()),
								e -> errorConexion(e))) {
					log.info("Eliminacion Correcta -> Respuestas");
				}
			} else {
				if ((pregunta.getOpciones() == null || pregunta.getOpciones().size() == 0))
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Se debe modificar las opciones");
				if (pregunta.getOpciones().size() != 2 && p.getTipoConsulta() == 6)
					throw new ResponseStatusException(HttpStatus.CONFLICT,
							"El tamaño de las opciones tipo Kano debe ser 2");
				if ((pregunta.getTipoConsulta() == 1 || pregunta.getTipoConsulta() == 4)
						&& pregunta.getImpacto() == null && pregunta.getImpacto().size() != 5)
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "5 impactos para la pregunta tipo 1 y 4");
				if (pregunta.getTipoConsulta() == 6 && pregunta.getImpacto() != null
						&& pregunta.getImpacto().size() != 6)
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "6 impactos para la pregunta tipo kano");
				p.setTipoConsulta(pregunta.getTipoConsulta());
				if (cbFactory
						.create("preguntas").run(
								() -> rClient.eliminarRespuestasProyectoFormularioPregunta(pregunta.getIdProyecto(),
										pregunta.getFormulario(), pregunta.getNumeroPregunta()),
								e -> errorConexion(e))) {
					log.info("Eliminacion Correcta -> Respuestas");
				}
			}
		}
		if (pregunta.getOpciones() != null) {
			if (pregunta.getOpciones().size() == 0 && p.getTipoConsulta() != 5)
				throw new ResponseStatusException(HttpStatus.CONFLICT, "El tamaño de las opciones debe ser mayor");
			if (pregunta.getOpciones().size() != 2 && p.getTipoConsulta() == 6)
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"El tamaño de las opciones tipo Kano debe ser 2");
			if (p.getTipoConsulta() != 5)
				p.setOpciones(pregunta.getOpciones());
		}
		if (pregunta.getImpacto() != null) {
			if ((pregunta.getTipoConsulta() == 1 || pregunta.getTipoConsulta() == 4)
					&& pregunta.getImpacto().size() != 5)
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "5 impactos para la pregunta tipo 1 y 4");
			if (pregunta.getTipoConsulta() == 6 && pregunta.getImpacto().size() != 6)
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "6 impactos para la pregunta tipo kano");
			p.setImpacto(pregunta.getImpacto());
		}
		pRepository.save(p);
	}

	@Override
	public List<Preguntas> verTodas(Integer idProyecto, Integer formulario) throws ResponseStatusException {
		List<Preguntas> preguntas = pRepository.findByIdProyectoAndFormulario(idProyecto, formulario);
		if (!preguntas.isEmpty())
			return preguntas;
		throw new ResponseStatusException(HttpStatus.LENGTH_REQUIRED, "El Proyecto no tiene preguntas");
	}

	@Override
	public Preguntas verUna(Integer idProyecto, Integer numeroPregunta, Integer formulario) {
		return pRepository.findByIdProyectoAndNumeroPreguntaAndFormulario(idProyecto, numeroPregunta, formulario);
	}

	@Override
	public boolean existsIdNumeroFormulario(Integer idProyecto, Integer numeroPregunta, Integer formulario) {
		return pRepository.existsByIdProyectoAndNumeroPreguntaAndFormulario(idProyecto, numeroPregunta, formulario);
	}

	@Override
	public boolean existIdFormulario(Integer idProyecto, Integer formulario) {
		return pRepository.existsByIdProyectoAndFormulario(idProyecto, formulario);
	}

	@Override
	public void deleteIdFormulario(Integer idProyecto, Integer formulario) {
		pRepository.deleteByIdProyectoAndFormulario(idProyecto, formulario);
		if (cbFactory.create("preguntas").run(
				() -> rClient.eliminarRespuestasProyectoFormulario(idProyecto, formulario), e -> errorConexion(e))) {
			log.info("Eliminacion Correcta -> Respuestas");
		}

		if (cbFactory.create("preguntas").run(() -> eClient.elimininarFormulario(idProyecto, formulario),
				e -> errorConexion(e))) {
			log.info("Eliminacion Correcta -> Estadisticas");
		}
	}

	@Override
	public void deleteIdNumeroFormulario(Integer idProyecto, Integer numeroPregunta, Integer formulario) {
		pRepository.deleteByIdProyectoAndNumeroPreguntaAndFormulario(idProyecto, numeroPregunta, formulario);
		if (cbFactory.create("preguntas").run(
				() -> rClient.eliminarRespuestasProyectoFormularioPregunta(idProyecto, formulario, numeroPregunta),
				e -> errorConexion(e))) {
			log.info("Eliminacion Correcta -> Respuestas");
		}
		if (cbFactory.create("preguntas").run(
				() -> eClient.elimininarFormularioPregunta(idProyecto, formulario, numeroPregunta),
				e -> errorConexion(e))) {
			log.info("Eliminacion Correcta -> Estadisticas");
		}
	}

	@Override
	public boolean existId(Integer idProyecto) {
		return pRepository.existsByIdProyecto(idProyecto);
	}

	private Boolean errorConexion(Throwable e) {
		log.info(e.getMessage());
		return false;
	}

}

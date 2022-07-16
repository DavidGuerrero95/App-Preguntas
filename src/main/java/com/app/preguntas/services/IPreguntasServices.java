package com.app.preguntas.services;

import java.util.List;

import com.app.preguntas.models.Preguntas;

public interface IPreguntasServices {

	public void crearPregunta(Preguntas p);

	public void editarPregunta(Preguntas pregunta) throws Exception;

	public List<Preguntas> verTodas(Integer idProyecto, Integer formulario);

	public Preguntas verUna(Integer idProyecto, Integer numeroPregunta, Integer formulario);

	public boolean existsIdNumeroFormulario(Integer idProyecto, Integer numeroPregunta, Integer formulario);

	public boolean existIdFormulario(Integer idProyecto, Integer formulario);

	public void deleteIdFormulario(Integer idProyecto, Integer formulario);

	public void deleteIdNumeroFormulario(Integer idProyecto, Integer numeroPregunta, Integer formulario);

	public boolean existId(Integer idProyecto);

}

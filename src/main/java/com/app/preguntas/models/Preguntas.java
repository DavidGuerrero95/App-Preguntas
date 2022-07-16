package com.app.preguntas.models;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "preguntas")
@Data
@NoArgsConstructor
public class Preguntas {

	@Id
	@JsonIgnore
	private String id;

	@NotNull(message = "id proyecto cannot be null")
	@Indexed(unique = false)
	private Integer idProyecto;

	@NotNull(message = "numero de pregunta be null")
	@Indexed(unique = false)
	private Integer numeroPregunta;

	@NotNull(message = "Tipo consulta cannot be null")
	@Indexed(unique = false)
	@Max(6)
	@Min(1)
	private Integer tipoConsulta;

	@NotNull(message = "Pregunta cannot be null")
	@Indexed(unique = false)
	private String pregunta;

	@NotNull(message = "Infomacion cannot be null")
	@Indexed(unique = false)
	private String informacion;

	@NotEmpty(message = "Opciones cannot be empty")
	private List<String> opciones;

	@NotNull(message = "Obligatorio cannot be null")
	@Indexed(unique = false)
	private Boolean obligatorio;

	@Indexed(unique = false)
	private String priorizacion;

	@Indexed(unique = false)
	private Integer formulario;

	@Indexed(unique = false)
	private List<String> impacto;

}

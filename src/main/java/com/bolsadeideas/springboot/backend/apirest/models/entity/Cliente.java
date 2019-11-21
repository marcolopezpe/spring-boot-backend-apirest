package com.bolsadeideas.springboot.backend.apirest.models.entity;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "clientes")
public class Cliente implements Serializable {

    private static final long serialVersionUID = -4393808295556442182L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "No puede estar vacio")
    @Size(min = 4, max = 12, message = "El tamaño tiene que estar entre 4 y 12 caracteres")
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @NotEmpty(message = "No puede estar vacio")
    @Column(name = "apellido")
    private String apellido;

    @NotEmpty(message = "No puede estar vacio")
    @Email(message = "No es una direccion de correo valida")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "create_at")
    @Temporal(TemporalType.DATE)
    private Date createAt;

    @PrePersist
    public void prePersist() {
        createAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public Cliente setId(Long id) {
        this.id = id;
        return this;
    }

    public String getNombre() {
        return nombre;
    }

    public Cliente setNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public String getApellido() {
        return apellido;
    }

    public Cliente setApellido(String apellido) {
        this.apellido = apellido;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Cliente setEmail(String email) {
        this.email = email;
        return this;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public Cliente setCreateAt(Date createAt) {
        this.createAt = createAt;
        return this;
    }
}

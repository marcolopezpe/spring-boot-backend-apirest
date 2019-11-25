package com.bolsadeideas.springboot.backend.apirest.controllers;

import com.bolsadeideas.springboot.backend.apirest.models.entity.Cliente;
import com.bolsadeideas.springboot.backend.apirest.models.services.IClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class ClienteRestController {

    private final Logger log = LoggerFactory.getLogger(ClienteRestController.class);

    private IClienteService clienteService;

    @Autowired
    public ClienteRestController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("clientes")
    public List<Cliente> index() {
        return clienteService.findAll();
    }

    @GetMapping("clientes/page/{page}")
    public Page<Cliente> index(@PathVariable("page") Integer page) {
        PageRequest pageable = PageRequest.of(page, 4);
        return clienteService.findAll(pageable);
    }

    @GetMapping("clientes/{id}")
    public ResponseEntity<?> show(@PathVariable("id") Long id) {
        Cliente cliente;
        Map<String, Object> response = new HashMap<>();

        try {
            cliente = clienteService.findById(id);
        } catch (DataAccessException e) {
            response.put("mensaje", "Error al consultar en la Base de Datos!");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (cliente == null) {
            response.put("mensaje", "El Cliente ID ".concat(id.toString().concat(" no existe en la Base de Datos!")));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(cliente, HttpStatus.OK);
    }

    @PostMapping("clientes")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@Valid @RequestBody Cliente cliente, BindingResult result) {
        Cliente newCliente;
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
                    .collect(Collectors.toList());
            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            newCliente = clienteService.save(cliente);
        } catch (DataAccessException e) {
            response.put("mensaje", "Error al realizar el insert en la Base de Datos");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("mensaje", "El cliente ha sido creado con exito!");
        response.put("cliente", newCliente);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("clientes/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> update(@Valid @RequestBody Cliente cliente, BindingResult result, @PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();

        Cliente clienteActual = clienteService.findById(id);
        Cliente clienteUpdated;

        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
                    .collect(Collectors.toList());
            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (clienteActual == null) {
            response.put("mensaje", "Error no se pudo editar: El Cliente ID ".concat(id.toString().concat(" no existe en la Base de Datos!")));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        try {
            clienteActual.setApellido(cliente.getApellido());
            clienteActual.setNombre(cliente.getNombre());
            clienteActual.setEmail(cliente.getEmail());
            clienteActual.setCreateAt(cliente.getCreateAt());

            clienteUpdated = clienteService.save(clienteActual);
        } catch (DataAccessException e) {
            response.put("mensaje", "Error al actualizar en la Base de Datos");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("mensaje", "El cliente ha sido actualizado con exito!");
        response.put("cliente", clienteUpdated);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("clientes/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Cliente cliente = clienteService.findById(id);
            String nombreFotoAnterior = cliente.getFoto();

            if (nombreFotoAnterior != null && nombreFotoAnterior.length() > 0) {
                Path rutaFotoAnterior = Paths.get("uploads").resolve(nombreFotoAnterior).toAbsolutePath();
                File archivoFotoAnterior = rutaFotoAnterior.toFile();
                if (archivoFotoAnterior.exists() && archivoFotoAnterior.canRead()) {
                    archivoFotoAnterior.delete();
                }
            }

            clienteService.delete(id);
        } catch (DataAccessException e) {
            response.put("mensaje", "Error al eliminar en la Base de Datos");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("mensaje", "El cliente ha sido eliminado con exito!");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("clientes/upload")
    public ResponseEntity<?> upload(@RequestParam("archivo") MultipartFile archivo, @RequestParam("id") Long id) {
        Map<String, Object> response = new HashMap<>();

        Cliente cliente = clienteService.findById(id);
        if (!archivo.isEmpty()) {
            String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename().replace(" ", "_");
            Path rutaArchivo = Paths.get("uploads").resolve(nombreArchivo).toAbsolutePath();

            log.info(rutaArchivo.toString());

            try {
                Files.copy(archivo.getInputStream(), rutaArchivo);
            } catch (IOException e) {
                response.put("mensaje", "Error al subir la imagen del cliente " + nombreArchivo);
                response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getCause().getMessage()));
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String nombreFotoAnterior = cliente.getFoto();
            if (nombreFotoAnterior != null && nombreFotoAnterior.length() > 0) {
                Path rutaFotoAnterior = Paths.get("uploads").resolve(nombreFotoAnterior).toAbsolutePath();
                File archivoFotoAnterior = rutaFotoAnterior.toFile();
                if (archivoFotoAnterior.exists() && archivoFotoAnterior.canRead()) {
                    archivoFotoAnterior.delete();
                }
            }

            cliente.setFoto(nombreArchivo);

            clienteService.save(cliente);

            response.put("cliente", cliente);
            response.put("mensaje", "Has subido correctamente la imagen: " + nombreArchivo);
        }

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("uploads/img/{nombreFoto:.+}")
    public ResponseEntity<Resource> verFoto(@PathVariable("nombreFoto") String nombreFoto) {
        Path rutaArchivo = Paths.get("uploads").resolve(nombreFoto).toAbsolutePath();

        log.info(rutaArchivo.toString());

        Resource recurso = null;

        try {
            recurso = new UrlResource(rutaArchivo.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (!recurso.exists() && !recurso.isReadable()) {
            throw new RuntimeException("No se pudo cargar la imagen: " + nombreFoto);
        }

        HttpHeaders cabecera = new HttpHeaders();
        cabecera.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"");

        return new ResponseEntity<>(recurso, cabecera, HttpStatus.OK);
    }
}

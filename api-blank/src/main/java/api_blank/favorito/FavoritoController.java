package api_blank.favorito;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "CRUD de productos favoritos del usuario")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los favoritos", description = "Devuelve la lista de todos los favoritos guardados")
    public ResponseEntity<List<FavoritoResponseDTO>> listar() {
        List<FavoritoResponseDTO> favoritos = favoritoService.obtenerTodos();
        return ResponseEntity.ok(favoritos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un favorito por ID", description = "Devuelve un favorito específico")
    public ResponseEntity<FavoritoResponseDTO> obtenerPorId(
            @Parameter(description = "ID del favorito", example = "1")
            @PathVariable Long id) {
        FavoritoResponseDTO favorito = favoritoService.obtenerPorId(id);
        return ResponseEntity.ok(favorito);
    }

    @PostMapping
    @Operation(summary = "Crear un favorito", description = "Agrega un nuevo producto a favoritos")
    public ResponseEntity<FavoritoResponseDTO> crear(@Valid @RequestBody FavoritoDTO favoritoDTO) {
        FavoritoResponseDTO creado = favoritoService.crear(favoritoDTO);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un favorito", description = "Actualiza la información de un favorito existente")
    public ResponseEntity<FavoritoResponseDTO> actualizar(
            @Parameter(description = "ID del favorito", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody FavoritoDTO favoritoDTO) {
        FavoritoResponseDTO actualizado = favoritoService.actualizar(id, favoritoDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un favorito", description = "Elimina un favorito por su ID")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del favorito", example = "1")
            @PathVariable Long id) {
        favoritoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

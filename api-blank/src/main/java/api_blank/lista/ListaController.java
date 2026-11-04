package api_blank.lista;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import api_blank.favorito.FavoritoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/listas")
@Tag(name = "Listas", description = "Listas que organizan los favoritos del usuario")
public class ListaController {

    private final ListaService listaService;

    public ListaController(ListaService listaService) {
        this.listaService = listaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las listas", description = "Devuelve todas las listas de favoritos existentes")
    public ResponseEntity<List<ListaResponseDTO>> listar() {
        return ResponseEntity.ok(listaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una lista por ID", description = "Devuelve una lista específica; 404 si no existe")
    public ResponseEntity<ListaResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la lista", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(listaService.obtenerPorId(id));
    }

    @GetMapping("/{id}/favoritos")
    @Operation(summary = "Favoritos de una lista", description = "Devuelve los favoritos que pertenecen a la lista; 404 si la lista no existe")
    public ResponseEntity<List<FavoritoResponseDTO>> favoritosDeLista(
            @Parameter(description = "ID de la lista", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(listaService.obtenerFavoritos(id));
    }

    @PostMapping
    @Operation(summary = "Crear una lista", description = "Crea una nueva lista de favoritos y devuelve 201 Created con su ubicación")
    public ResponseEntity<ListaResponseDTO> crear(@Valid @RequestBody ListaDTO listaDTO) {
        ListaResponseDTO creada = listaService.crear(listaDTO);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una lista", description = "Elimina una lista vacía (204); 409 Conflict si todavía tiene favoritos; 404 si no existe")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la lista", example = "1")
            @PathVariable Long id) {
        listaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{origenId}/mover-favoritos")
    @Operation(summary = "Mover favoritos entre listas", description = "Reasigna todos los favoritos de la lista origen a la lista destino y elimina la origen. Todo dentro de una única transacción: o se aplican ambas escrituras o ninguna")
    public ResponseEntity<MoverFavoritosResponseDTO> moverFavoritos(
            @Parameter(description = "ID de la lista origen (se elimina)", example = "1")
            @PathVariable Long origenId,
            @Valid @RequestBody MoverFavoritosDTO moverDTO) {
        return ResponseEntity.ok(listaService.moverFavoritos(origenId, moverDTO));
    }
}

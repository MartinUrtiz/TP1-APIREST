package api_blank.favorito;

import java.time.LocalDateTime;

import api_blank.lista.ListaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad JPA que mapea la tabla {@code favoritos} (migración V1).
 *
 * Una entidad JPA no puede ser un record: Hibernate necesita un constructor
 * vacío para instanciarla al leer del ResultSet y estado mutable para
 * gestionar el ciclo de vida del objeto (dirty checking al hacer flush).
 *
 * Esta clase es parte de la infraestructura: el dominio sigue siendo el
 * record/clase {@link Favorito}, que no se toca por fuera de lo que el
 * práctico pide sumar (listaId).
 */
@Entity
@Table(name = "favoritos")
public class FavoritoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "nota")
    private String nota;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

    /**
     * Relación N:1 con lista (columna lista_id, migraciones V3/V4).
     *
     * @ManyToOne es EAGER por defecto en JPA y así se mantiene: con
     * spring.jpa.open-in-view=false la sesión ya está cerrada cuando el
     * adapter convierte a dominio, y una asociación perezosa explotaría con
     * LazyInitializationException. Es UNA referencia (un join), no una
     * colección: el lado inverso (favoritos de una lista) se resuelve con la
     * consulta derivada findByListaId, sin @OneToMany bidireccional.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "lista_id", nullable = false)
    private ListaEntity lista;

    public FavoritoEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public ListaEntity getLista() {
        return lista;
    }

    public void setLista(ListaEntity lista) {
        this.lista = lista;
    }
}

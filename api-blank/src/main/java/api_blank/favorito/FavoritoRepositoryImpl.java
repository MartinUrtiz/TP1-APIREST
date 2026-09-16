package api_blank.favorito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

@Repository
public class FavoritoRepositoryImpl implements FavoritoRepository {

    private final Map<Long, Favorito> almacen = new ConcurrentHashMap<>();
    private final AtomicLong contador = new AtomicLong(1);

    @Override
    public List<Favorito> findAll() {
        return new ArrayList<>(almacen.values());
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public Favorito save(Favorito favorito) {
        if (favorito.getId() == null) {
            favorito.setId(contador.getAndIncrement());
        }
        almacen.put(favorito.getId(), favorito);
        return favorito;
    }

    @Override
    public void deleteById(Long id) {
        almacen.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return almacen.containsKey(id);
    }
}

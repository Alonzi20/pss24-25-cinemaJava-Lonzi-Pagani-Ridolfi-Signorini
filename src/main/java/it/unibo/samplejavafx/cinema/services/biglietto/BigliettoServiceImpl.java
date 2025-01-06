package it.unibo.samplejavafx.cinema.services.biglietto;

import it.unibo.samplejavafx.cinema.application.models.Biglietto;
import it.unibo.samplejavafx.cinema.repositories.BigliettoRepository;
import it.unibo.samplejavafx.cinema.services.exceptions.BigliettoNotFoundException;
import it.unibo.samplejavafx.cinema.services.posto.PostoService;
import it.unibo.samplejavafx.cinema.services.proiezione.ProiezioneService;
import it.unibo.samplejavafx.cinema.services.sala.SalaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BigliettoServiceImpl implements BigliettoService {

  private final BigliettoRepository bigliettoRepository;
  private final PostoService postoService;
  private final ProiezioneService proiezioneService;
  private final SalaService salaService;

  @Override
  public Biglietto findBigliettoById(Long id) {
    return bigliettoRepository
        .findById(id)
        .orElseThrow(() -> new BigliettoNotFoundException(String.valueOf(id)));
  }

  @Override
  public List<Biglietto> findAllBiglietti() {
    return bigliettoRepository.findAll();
  }

  @Override
  public List<Biglietto> findAllBigliettiByClienteId(long idCliente) {
    return bigliettoRepository.findAllByClienteId(idCliente);
  }

  // Commento per Luca:
  // non chiedo salaId perché lo recupero dalla Proiezione
  // Per Sala e Posto non serve il controllo che sia null
  // perché è fatto già dentro al metodo di recupero ById.
  // Li recupero per avere le info
  @Override
  public Biglietto compra(long idProiezione, long idPosto, boolean ridotto) {
    var proiezione = proiezioneService.findProiezioneById(idProiezione);

    var idPostoPrenotato = proiezioneService.prenota(idPosto, idProiezione, proiezione.getSalaId());
    if (idPostoPrenotato != null) {
      Biglietto biglietto = new Biglietto();
      biglietto.setProiezioneId(idProiezione);
      biglietto.setRidotto(ridotto);
      biglietto.setPrezzo(ridotto ? Biglietto.PREZZO_RIDOTTO : Biglietto.PREZZO_INTERO);

      // INFO POSTO
      var posto = postoService.findPostoById(idPosto);
      biglietto.setNumero(posto.getNumero());
      biglietto.setFila(posto.getFila());

      // INFO SALA
      var sala = salaService.findSalaById(proiezione.getSalaId());
      biglietto.setSala(sala.getNumero());

      return bigliettoRepository.save(biglietto);
    } else {
      throw new RuntimeException("Biglietto non comprato, la proiezione non è prenotabile");
    }
  }
}

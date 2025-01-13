package it.unibo.samplejavafx.cinema.application.models;

import jakarta.persistence.*;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import lombok.*;

@Entity
@Data
@Builder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Proiezione {
  @Id
  @EqualsAndHashCode.Include
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  Long filmId;
  Long salaId;
  Date data;
  Time orario;

  @OneToMany(mappedBy = "proiezione", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Posto> postiPrenotati;
}

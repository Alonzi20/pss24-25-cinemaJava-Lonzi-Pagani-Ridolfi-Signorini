package it.unibo.samplejavafx.cinema.services;

import it.unibo.samplejavafx.cinema.application.models.Film;
import it.unibo.samplejavafx.cinema.repositories.FilmRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

@Slf4j
@Data
public class MovieProjections {
  private static final String API_KEY = "2ad42fcfac14ac8869896349fa9c4b6f";
  private List<Film> weeklyMovies;
  private final FilmRepository filmRepository;

  public MovieProjections(FilmRepository filmRepository) {
    this.filmRepository = filmRepository;  // Aggiunta questa riga
    this.weeklyMovies = new ArrayList<>();
    fetchWeeklyMovies();
}

  private void fetchWeeklyMovies() {
    try {
        // Recupera i film dal database e prendi solo i primi 10 più recenti
        weeklyMovies = filmRepository.findAll().stream()
            .sorted((m1, m2) -> {
                LocalDate date1 = LocalDate.parse(m1.getReleaseDate());
                LocalDate date2 = LocalDate.parse(m2.getReleaseDate());
                return date2.compareTo(date1);
            })
            .limit(10)
            .collect(Collectors.toList());
            
    } catch (Exception e) {
        log.error("Errore durante il recupero dei film dal database", e);
        weeklyMovies = new ArrayList<>(); // Inizializza a lista vuota in caso di errore
    }
}

  // Recupero dettagli specifici di un film
  private Film getMovieDetails(int movieId) {
    OkHttpClient client = new OkHttpClient();

    // Chiamate API per dettagli e cast
    Request detailsRequest =
        new Request.Builder()
            .url(
                "https://api.themoviedb.org/3/movie/"
                    + movieId
                    + "?api_key="
                    + API_KEY
                    + "&language=it-IT")
            .get()
            .addHeader("accept", "application/json")
            .build();

    Request creditsRequest =
        new Request.Builder()
            .url("https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + API_KEY)
            .get()
            .addHeader("accept", "application/json")
            .build();

    try {
      Response detailsResponse = client.newCall(detailsRequest).execute();
      Response creditsResponse = client.newCall(creditsRequest).execute();

      if (detailsResponse.isSuccessful()
          && creditsResponse.isSuccessful()
          && detailsResponse.body() != null
          && creditsResponse.body() != null) {

        JSONObject movieDetails = new JSONObject(detailsResponse.body().string());
        JSONObject credits = new JSONObject(creditsResponse.body().string());

        // Estrai generi
        JSONArray genresArray = movieDetails.getJSONArray("genres");
        List<String> genres = new ArrayList<>();
        for (int i = 0; i < genresArray.length(); i++) {
          genres.add(genresArray.getJSONObject(i).getString("name"));
        }

        // Estrai cast principale (primi 5)
        JSONArray castArray = credits.getJSONArray("cast");
        List<String> mainCast = new ArrayList<>();
        int castLimit = Math.min(castArray.length(), 5);
        for (int i = 0; i < castLimit; i++) {
          mainCast.add(castArray.getJSONObject(i).getString("name"));
        }

        JSONArray crewArray = credits.getJSONArray("crew");
        String director = "Non disponibile";
        for (int i = 0; i < crewArray.length(); i++) {
          JSONObject crewMember = crewArray.getJSONObject(i);
          if ("Director".equals(crewMember.getString("job"))) {
            director = crewMember.getString("name");
            break;
          }
        }

        return Film.of(
                null,
            movieDetails.getString("title"),
            movieDetails.getString("overview"),
            movieDetails.getString("release_date"),
            movieDetails.getString("poster_path"),
            String.join(",", genres),
            movieDetails.getInt("runtime"),
            String.join(",", mainCast),
            director,
            movieDetails.getBoolean("adult"));
      }
    } catch (IOException e) {
      log.error("Errore durante il recupero dei dettagli del film", e);
      e.printStackTrace();
    }
    return null;
  }

  // recupero film settimanali
  public List<Film> getWeeklyMovies() {
    return new ArrayList<>(weeklyMovies);
  }

  // metodo di ricerca film (da cambiare)
  public List<Film> searchMovieByName(String name) {
    List<Film> foundMovies = new ArrayList<>();

    for (Film film : weeklyMovies) {
      if (film.getTitle().toLowerCase().contains(name.toLowerCase())) {
        foundMovies.add(film);
      }
    }

    return foundMovies;
  }
}

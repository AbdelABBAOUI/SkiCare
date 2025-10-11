package com.hackthon.skicare;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class CapteurSimulator {

  // Injecte le template pour envoyer des messages aux clients STOMP (WebSocket)
  private final SimpMessagingTemplate messagingTemplate;
  private final Random random = new Random();


  public CapteurSimulator(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Tâche planifiée pour simuler l'envoi de nouvelles données toutes les 2 secondes.
   */
  @Scheduled(fixedRate = 2000)
  public void sendDataUpdate() {
    // 1. Génération et Simulation des données Biomètriques
    BiometricsData data = generateSimulatedData();

    // 2. Envoi des données vers le topic '/topic/biometrics'
    // Spring/Jackson sérialisera automatiquement l'objet BiometricsData en JSON.
    messagingTemplate.convertAndSend("/topic/biometrics", data);
  }

  /**
   * Génère un objet BiometricsData avec des valeurs simulées.
   */
  private BiometricsData generateSimulatedData() {
    BiometricsData data = new BiometricsData();

    // --- Physiologique ---
    data.setFrequenceCardiaque(random.nextInt(51) + 60); // 60-110 bpm
    data.setFrequenceRespiratoire(random.nextInt(11) + 12); // 12-22 breaths/min
    // Simule 36.5 à 38.5 C, arrondi à une décimale
    data.setTemperatureCutanee(round(random.nextDouble() * 2 + 36.5, 1));
    data.setSaturationOxygene(random.nextInt(6) + 95); // 95-100 %
    data.setVariabiliteFrequenceCardiaque(random.nextInt(60) + 40); // 40-100 ms

    // --- Mouvement et Localisation ---
    data.setVitesse(round(random.nextDouble() * 10 + 1, 2)); // 1.0-11.0 km/h (simulation de marche/ski léger)

    // Simule une faible probabilité de chute ou d'immobilité (moins de 5%)
    data.setChute(random.nextDouble() < 0.005);
    data.setImmobilité(random.nextDouble() < 0.02);

    // Simplification: coordonnées statiques ou très légèrement variables
    double latitude = 45.72 + random.nextDouble() * 0.01;
    double longitude = 3.14 + random.nextDouble() * 0.01;
    data.setGps(String.format("%.4f,%.4f", latitude, longitude));

    // --- Environnementales ---
    // Simule 0 à 15 C pour la température ambiante (ski en montagne)
    data.setTemperatureAmb(round(random.nextDouble() * 15, 1));
    data.setPression(random.nextInt(50) + 950); // 950-1000 hPa (selon l'altitude)
    data.setAltitude(random.nextInt(1500) + 500); // 500-2000 mètres

    int scoreEWS = data.calculerScorePartielEWS();
    data.setScoreEwsPartiel(scoreEWS);

    // --- Horodatage ---
    data.setTimestamp(LocalDateTime.now()); // L'horodatage courant

    return data;
  }



  /**
   * Fonction utilitaire pour arrondir un double.
   */
  private double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();
    long factor = (long) Math.pow(10, places);
    value = value * factor;
    long tmp = Math.round(value);
    return (double) tmp / factor;
  }
}
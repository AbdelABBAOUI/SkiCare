package com.hackthon.skicare.simulation;

import com.hackthon.skicare.MovementDetector;
import com.hackthon.skicare.model.BiometricsData;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;


@Service
public class CapteurSimulator {

  private final SimpMessagingTemplate messagingTemplate;
  private final Random random = new Random();
  private final MovementDetector movementDetector = new MovementDetector();

  private long lastSensorTime = System.currentTimeMillis();
  private int forcedScenarioDuration = 0;
  private static final int CHUTE_FORCED_CYCLES = 100;


  public CapteurSimulator(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }


  @Scheduled(fixedRate = 1000)
  public void sendDataUpdate() {
    long currentTime = System.currentTimeMillis();

    BiometricsData data = generateSimulatedData(currentTime);
    messagingTemplate.convertAndSend("/topic/biometrics", data);

    if (data.isChute() || data.isImmobilite()) {
      System.err.printf("[ALERTE] 🚨 Chute: %b, Immobilité: %b, EWS: %d, State: %s\n",
          data.isChute(), data.isImmobilite(), data.getScoreEwsPartiel(), movementDetector.getCurrentState());
    } else {
      System.out.printf("[INFO] Data sent. EWS: %d, FC: %d, TempCut: %.1f\n",
          data.getScoreEwsPartiel(), data.getFrequenceCardiaque(), data.getTemperatureCutanee());
    }

    lastSensorTime = currentTime;
  }

  private BiometricsData generateSimulatedData(long currentTime) {
    BiometricsData data = new BiometricsData();

    double ax, ay, az;
    double currentVitesse;

    // Déclencher aléatoirement un scénario critique (0.5% de chance)
    if (forcedScenarioDuration == 0 && random.nextDouble() < 0.005) {
      System.out.println("\n>>> Démarrage d'un scénario critique forcé... <<<");
      forcedScenarioDuration = CHUTE_FORCED_CYCLES;
    }

    if (forcedScenarioDuration > 0) {
      // SCÉNARIO CRITIQUE FORCÉ
      forcedScenarioDuration--;

      // Les données de mouvement sont forcées en fonction de l'état actuel
      if (movementDetector.getCurrentState() == MovementDetector.FallState.NORMAL) {
        ax = 0.1; // FREEFALL
        ay = 0.1;
        az = 0.1;
      } else if (movementDetector.getCurrentState() == MovementDetector.FallState.FREEFALL) {
        ax = 35.0; // IMPACT
        ay = 35.0;
        az = 35.0;
      } else if (movementDetector.getCurrentState() == MovementDetector.FallState.IMPACT ||
          movementDetector.getCurrentState() == MovementDetector.FallState.POST_FALL_REST) {
        ax = 0.3; // POST_FALL_REST
        ay = 0.3;
        az = 0.3;
      } else {
        ax = 9.81; ay = 0.5; az = 0.5;
      }
      currentVitesse = 0.0;
      data.setFrequenceCardiaque(45 + random.nextInt(10)); // FC basse
    } else {
      // SCÉNARIO NORMAL
      ax = 9.81 + random.nextDouble() * 0.5;
      ay = random.nextDouble() * 1.0;
      az = random.nextDouble() * 1.0;
      currentVitesse = round(random.nextDouble() * 10 + 1, 2);

      data.setFrequenceCardiaque(random.nextInt(51) + 60); // FC normale
    }

    // --- 2. UTILISATION DES FONCTIONS DE DÉTECTION ---

    boolean fallDetected = movementDetector.detectSequentialFall(ax, ay, az, currentTime);
    boolean immobilityDetected = movementDetector.detectImmobility(ax, ay, az, currentTime);

    data.setChute(fallDetected);
    data.setImmobilite(immobilityDetected);
    data.setVitesse(currentVitesse);

    // --- 3. Physiologique (Aléatoire) ---
    data.setFrequenceRespiratoire(random.nextInt(11) + 12);
    data.setTemperatureCutanee(round(random.nextDouble() * 2 + 36.5, 1));
    data.setSaturationOxygene(random.nextInt(6) + 95);
    data.setVariabiliteFrequenceCardiaque(random.nextInt(60) + 40);

    // --- 4. Localisation et Environnementales ---
    double latitude = 45.72 + random.nextDouble() * 0.01;
    double longitude = 3.14 + random.nextDouble() * 0.01;
    data.setGps(String.format("%.4f,%.4f", latitude, longitude));

    data.setTemperatureAmb(round(random.nextDouble() * 15 + 10, 1));
    data.setPression(random.nextInt(50) + 950);
    data.setAltitude(random.nextInt(1500) + 500);

    // --- 5. Calcul EWS et Horodatage ---
    int scoreEWS = data.calculerScorePartielEWS();
    data.setScoreEwsPartiel(scoreEWS);
    data.setTimestamp(LocalDateTime.now());

    return data;
  }


  private double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();
    long factor = (long) Math.pow(10, places);
    value = value * factor;
    long tmp = Math.round(value);
    return (double) tmp / factor;
  }
}
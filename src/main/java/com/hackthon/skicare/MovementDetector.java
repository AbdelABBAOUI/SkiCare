package com.hackthon.skicare;


public class MovementDetector {

  private static final double GRAVITY = 9.81;


  private static final double THRESHOLD_FREEFALL_LOW = 0.2 * GRAVITY;
  private static final double THRESHOLD_IMPACT_HIGH = 3.0 * GRAVITY;
  private static final double THRESHOLD_POST_FALL_REST = 0.5 * GRAVITY;

  // Durées (en millisecondes)
  private static final long DURATION_FREEFALL_MAX_MS = 300;
  private static final long DURATION_POST_FALL_REST_MS = 5000;

  // Seuils pour la Détection d'Immobilité Prolongée
  private static final double THRESHOLD_INACTIVITY = 0.5 * GRAVITY;
  private static final long DURATION_INACTIVITY_ALERT_MS = 120000; // 2 minutes
  private static final double THRESHOLD_VERTICALITY_ANGLE_DEGREES = 45.0;

  // --- 2. Variables d'état (pour la logique séquentielle et temporelle) ---

  // États pour la machine à états de détection de chute
  public enum FallState {
    NORMAL,
    FREEFALL,
    IMPACT,
    POST_FALL_REST
  }

  public FallState currentState = FallState.NORMAL;
  private long fallStateChangeTime = 0; // Temps du dernier changement d'état de chute

  private long inactivityStartTime = 0; // Temps où l'immobilité a commencé
  private boolean isImmobilityAlertSent = false;

  public FallState getCurrentState() {
    return currentState;
  }

  // --- 3. Fonctions Utilitaires (Calculs) ---

  /**
   * Calcule la norme (magnitude) du vecteur accélération.
   */
  private double calculateMagnitude(double ax, double ay, double az) {
    return Math.sqrt(ax * ax + ay * ay + az * az);
  }

  /**
   * Calcule l'angle d'inclinaison de l'appareil par rapport à la verticale (axe Z).
   */
  private double calculateTiltAngle(double ax, double ay, double az) {
    double magnitude = calculateMagnitude(ax, ay, az);
    if (magnitude == 0) return 0;

    // Câblé pour l'axe Z, suppose que l'axe Z est la verticale au repos
    double cosTheta = az / magnitude;

    // Clamping pour Math.acos (protection contre les erreurs de flottant)
    if (cosTheta > 1.0) cosTheta = 1.0;
    if (cosTheta < -1.0) cosTheta = -1.0;

    return Math.toDegrees(Math.acos(cosTheta));
  }

  // --- 4. Logique de Détection d'Immobilité ---

  /**
   * Vérifie et alerte si l'utilisateur est immobile et couché/incliné pendant trop longtemps.
   * @param ax, ay, az : Accélérations actuelles.
   * @param currentTimeMs : Le temps actuel en millisecondes.
   * @return true si une alerte d'immobilité est envoyée.
   */
  public boolean detectImmobility(double ax, double ay, double az, long currentTimeMs) {
    double magnitude = calculateMagnitude(ax, ay, az);
    double tiltAngle = calculateTiltAngle(ax, ay, az);

    // Conditions : Faible activité ET position non verticale
    boolean isInactive = magnitude < THRESHOLD_INACTIVITY;
    boolean isNotVertical = tiltAngle > THRESHOLD_VERTICALITY_ANGLE_DEGREES;

    if (isInactive && isNotVertical) {
      if (inactivityStartTime == 0) {
        inactivityStartTime = currentTimeMs;
      }

      long duration = currentTimeMs - inactivityStartTime;

      if (duration >= DURATION_INACTIVITY_ALERT_MS && !isImmobilityAlertSent) {
        System.out.println("ALERTE Immobilité : Utilisateur immobile et incliné depuis " +
            (DURATION_INACTIVITY_ALERT_MS / 1000) + "s.");
        isImmobilityAlertSent = true;
        return true;
      }
    } else {
      // Réinitialiser si l'utilisateur est actif ou en position verticale
      inactivityStartTime = 0;
      isImmobilityAlertSent = false;
    }
    return false;
  }

  // --- 5. Logique de Détection de Chute Séquentielle ---

  /**
   * Détecte une chute en suivant la séquence (chute libre -> impact -> repos).
   * @param ax, ay, az : Accélérations actuelles.
   * @param currentTimeMs : Le temps actuel en millisecondes.
   * @return true si une chute est détectée et confirmée.
   */
  public boolean detectSequentialFall(double ax, double ay, double az, long currentTimeMs) {
    double magnitude = calculateMagnitude(ax, ay, az);

    switch (currentState) {

      case NORMAL:
        // Passage à FREEFALL si l'accélération chute brusquement
        if (magnitude < THRESHOLD_FREEFALL_LOW) {
          currentState = FallState.FREEFALL;
          fallStateChangeTime = currentTimeMs;
        }
        break;

      case FREEFALL:
        // Timeout si la phase FREEFALL est trop longue
        if ((currentTimeMs - fallStateChangeTime) > DURATION_FREEFALL_MAX_MS) {
          currentState = FallState.NORMAL;
        }
        // Passage à IMPACT si un pic d'accélération (choc) est détecté
        else if (magnitude > THRESHOLD_IMPACT_HIGH) {
          currentState = FallState.IMPACT;
          fallStateChangeTime = currentTimeMs;
        }
        break;

      case IMPACT:
        // Passage à POST_FALL_REST si l'activité redevient faible immédiatement
        if (magnitude < THRESHOLD_POST_FALL_REST) {
          currentState = FallState.POST_FALL_REST;
          fallStateChangeTime = currentTimeMs;
        } else {
          // Si l'activité est toujours élevée, ce n'était pas une chute stable
          currentState = FallState.NORMAL;
        }
        break;

      case POST_FALL_REST:
        // L'utilisateur reste-t-il au repos après l'impact ?
        if (magnitude < THRESHOLD_POST_FALL_REST) {
          long duration = currentTimeMs - fallStateChangeTime;

          if (duration >= DURATION_POST_FALL_REST_MS) {
            System.out.println("ALERTE CHUTE SÉQUENTIELLE: Chute confirmée !");
            currentState = FallState.NORMAL; // Réinitialiser
            return true;
          }
        } else {
          // Mouvement détecté (l'utilisateur se relève, annulation de la chute)
          currentState = FallState.NORMAL;
        }
        break;
    }

    return false;
  }


}

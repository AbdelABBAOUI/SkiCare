package com.hackthon.skicare.model;

import java.time.LocalDateTime;

/**
 * Représente un ensemble de données biométriques et environnementales
 * collectées à un instant précis.
 */
public class BiometricsData {

  // --- Données Physiologiques (Biométriques) ---
  private int frequenceCardiaque;
  private int frequenceRespiratoire;
  private double temperatureCutanee; // Correction du nom de variable
  private int saturationOxygene;
  private int variabiliteFrequenceCardiaque; // Correction de l'accent

  // --- Données de Mouvement et de Localisation ---
  private double vitesse; // Double pour plus de précision
  public boolean chute; // Type boolean pour une détection Oui/Non
  public boolean immobilite; // Type boolean
  private String gps; // Stockage de la localisation GPS (ex: "lat,lon")

  // --- Données Environnementales ---
  private double temperatureAmb; // temperature ambiante
  private int pression;
  private int altitude;

  private int scoreEwsPartiel;

  // --- Horodatage ---
  private LocalDateTime timestamp;


  public BiometricsData() {
    this.timestamp = LocalDateTime.now();
  }

  public BiometricsData(int frequenceCardiaque, double temperatureCutanee, int frequenceRespiratoire, int saturationOxygene, double temperatureAmb) {
    this.frequenceCardiaque = frequenceCardiaque;
    this.temperatureCutanee = temperatureCutanee;
    this.frequenceRespiratoire = frequenceRespiratoire;
    this.saturationOxygene = saturationOxygene;
    this.temperatureAmb = temperatureAmb;
    this.timestamp = LocalDateTime.now();
  }


  public int getFrequenceCardiaque() {
    return frequenceCardiaque;
  }

  public void setFrequenceCardiaque(int frequenceCardiaque) {
    this.frequenceCardiaque = frequenceCardiaque;
  }

  public int getFrequenceRespiratoire() {
    return frequenceRespiratoire;
  }

  public void setFrequenceRespiratoire(int frequenceRespiratoire) {
    this.frequenceRespiratoire = frequenceRespiratoire;
  }

  public double getTemperatureCutanee() {
    return temperatureCutanee;
  }

  public void setTemperatureCutanee(double temperatureCutanee) {
    this.temperatureCutanee = temperatureCutanee;
  }

  public int getSaturationOxygene() {
    return saturationOxygene;
  }

  public void setSaturationOxygene(int saturationOxygene) {
    this.saturationOxygene = saturationOxygene;
  }

  public int getVariabiliteFrequenceCardiaque() {
    return variabiliteFrequenceCardiaque;
  }

  public void setVariabiliteFrequenceCardiaque(int variabiliteFrequenceCardiaque) {
    this.variabiliteFrequenceCardiaque = variabiliteFrequenceCardiaque;
  }

  public double getVitesse() {
    return vitesse;
  }

  public void setVitesse(double vitesse) {
    this.vitesse = vitesse;
  }

  public boolean isChute() { return chute; }

  public void setChute(boolean chute) {
    this.chute = chute;
  }

  public boolean isImmobilite() { return immobilite; }

  public void setImmobilite(boolean immobilite) {
    this.immobilite = immobilite;
  }

  public String getGps() {
    return gps;
  }

  public void setGps(String gps) {
    this.gps = gps;
  }

  public double getTemperatureAmb() {
    return temperatureAmb;
  }

  public void setTemperatureAmb(double temperatureAmb) {
    this.temperatureAmb = temperatureAmb;
  }

  public int getPression() {
    return pression;
  }

  public void setPression(int pression) {
    this.pression = pression;
  }

  public int getAltitude() {
    return altitude;
  }

  public void setAltitude(int altitude) {
    this.altitude = altitude;
  }

  public int getScoreEwsPartiel() {
    return scoreEwsPartiel;
  }

  public void setScoreEwsPartiel(int scoreEwsPartiel) {
    this.scoreEwsPartiel = scoreEwsPartiel;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }



  public int calculerScorePartielEWS() {
    int scoreTotal = 0;

    // 1. Score de Fréquence Cardiaque
    if (frequenceCardiaque <= 40 || frequenceCardiaque >= 131) {
      scoreTotal += 3;
    } else if ((frequenceCardiaque <= 50) ||
        (frequenceCardiaque >= 91 && frequenceCardiaque <= 110)) {
      scoreTotal += 1;
    } else if (frequenceCardiaque >= 111 ) {
      scoreTotal += 2;
    }
    // else: 51-90 est score 0

    // 2. Score de Fréquence Respiratoire
    if (frequenceRespiratoire <= 8 || frequenceRespiratoire >= 25) {
      scoreTotal += 3;
    } else if (frequenceRespiratoire == 9 || frequenceRespiratoire == 21 || frequenceRespiratoire == 24) {
      scoreTotal += 2;
    } else if (frequenceRespiratoire >= 15 && frequenceRespiratoire <= 20) {
      scoreTotal += 1;
    }
    // else: 10-14 est score 0

    // 3. Score de Température Cutanée
    if (temperatureCutanee <= 35.0) {
      scoreTotal += 3;
    } else if (temperatureCutanee >= 39.1) {
      scoreTotal += 2;
    } else if ((temperatureCutanee >= 35.1 && temperatureCutanee <= 36.0) ||
        (temperatureCutanee >= 38.1 && temperatureCutanee <= 39.0)) {
      scoreTotal += 1;
    }
    // else: 36.1-38.0 est score 0

    // 4. Score de Saturation en Oxygène
    if (saturationOxygene <= 91) {
      scoreTotal += 3;
    } else if (saturationOxygene <= 93) {
      scoreTotal += 2;
    } else if (saturationOxygene <= 95) {
      scoreTotal += 1;
    }
    // else: >= 96 est score 0

    return scoreTotal;
  }
}
# Filmeverwaltung

## Kurzbeschreibung
\- Kleines Java/Maven-Projekt zur Verwaltung von Filmdaten. Enthält Clients für TMDB/OMDB und eine Übersetzungsintegration (MyMemory). Ziel ist eine einfache lokale Anwendung zur Pflege und Anzeige von Film-Metadaten.

## Inhalt dieses README
\- Projektüberblick  
\- Voraussetzungen  
\- Build \& Run  
\- Konfiguration (lokal vs. Umgebungsvariablen)  
\- Run\-Konfigurationen in IntelliJ  
\- Fehlerbehebung / Troubleshooting  
\- Entwicklung \& Tests  
\- Contribution \& Lizenz

## Voraussetzungen
\- Java 17\+  
\- Maven 3.6\+  
\- IntelliJ IDEA (empfohlen) oder eine andere Java-fähige IDE

## Projektaufbau
\- Quellcode: `src/main/java`  
\- Ressourcen / Konfiguration: `src/main/resources`  
\- Lokale Template-Konfiguration: `src/main/resources/config/config.json` (sollte nicht committet werden)

## Build
\- Projekt bauen:
  \- `mvn clean package`  
\- Erzeugte Artefakte landen in `target/`

## Run
\- Ausgeführte JAR:
  \- `java -jar target/<artifact>-<version>.jar`  
\- In IntelliJ:
  \- Run\-Konfiguration anlegen und als "Share" markieren (Speichert in `\.idea/runConfigurations/`)

## Konfiguration
Empfohlen: sensible Daten nicht ins Repo committen.

Möglichkeit A — Environment\-Variablen (empfohlen):
\- `TMDB_API_KEY`  
\- `OMDB_API_KEY`  
\- `MYMEMORY_EMAIL` (oder API\-Key falls vorhanden)

Möglichkeit B — Lokale Datei (nur als Template, niemals committen):
\- Template speichern als `src/main/resources/config/config.json` (diese Datei ist in `\.gitignore` gelistet)

Beispiel-Template:
```json
{
  "tmdbApiKey": "PUT_YOUR_KEY_HERE",
  "omdbApiKey": "PUT_YOUR_KEY_HERE",
  "translation": {
    "email": "your@example.com"
  }
}

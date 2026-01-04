# Filmeverwaltung

## Überblick
Kleines Java/Maven\-Projekt zur Verwaltung und Anzeige von Filmmetadaten. Externe APIs (TMDB, OMDB) werden genutzt, Beschreibungen können mit MyMemory übersetzt werden.

\- Importiert Film\-Metadaten (TMDB, OMDB)  
\- Ergänzt Poster und zusätzliche Details  
\- Übersetzt Beschreibungen via MyMemory mit Fallback bei Fehlern  
\- Konfiguration über Environment\-Variablen oder lokales Template (`src/main/resources/config/config.json`) — dieses Template nicht committen

## Kurz: Build \& Run
\- Build: `mvn clean package`  
\- Run JAR: `java -jar target/<artifact>-<version>.jar`

## Hinweise
\- Geteilte IntelliJ Run\-Konfigurationen können in `/.idea/runConfigurations/` liegen — keine sensiblen Daten dort speichern.  
\- API\-Keys sollten über Environment\-Variablen gesetzt werden (`TMDB_API_KEY`, `OMDB_API_KEY`).

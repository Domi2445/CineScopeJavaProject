-- Migration Script: Füge TEASER-Spalte zur films-Tabelle hinzu
-- Datum: 2026-01-15

-- Füge TEASER-Spalte hinzu (falls noch nicht vorhanden)
ALTER TABLE films ADD (
    TEASER VARCHAR2(500)
);

-- Optional: Kommentar zur Spalte hinzufügen
COMMENT ON COLUMN films.TEASER IS 'Kurze Zusammenfassung/Tagline des Films aus TMDB API';
